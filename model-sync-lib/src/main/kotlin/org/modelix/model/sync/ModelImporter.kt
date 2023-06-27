package org.modelix.model.sync

import org.modelix.model.api.ConceptReference
import org.modelix.model.api.INode
import org.modelix.model.api.INodeReference
import org.modelix.model.data.ModelData
import org.modelix.model.data.NodeData
import java.io.File

class ModelImporter(private val root: INode) {

    private lateinit var originalIdToRef: MutableMap<String, INodeReference>
    private lateinit var originalIdToSpec: MutableMap<String, NodeData>
    private lateinit var originalIdToParentSpec: MutableMap<String, NodeData>
    private lateinit var existingNodeIds: MutableSet<String>

    fun import(jsonFile: File) {
        require(jsonFile.exists())
        require(jsonFile.extension == "json")

        val data = ModelData.fromJson(jsonFile.readText())
        import(data)
    }
    
    fun import(data: ModelData) {
        existingNodeIds = mutableSetOf()
        collectExistingNodeIds(root)

        originalIdToSpec = mutableMapOf()
        buildSpecIndex(data.root)

        originalIdToParentSpec = mutableMapOf()
        buildParentSpecIndex(data.root)

        syncNode(root, data.root)

        originalIdToRef = mutableMapOf()
        buildRefIndex(root)

        syncAllReferences(root, data.root)
        syncAllChildOrders(root, data.root)
    }

    private fun collectExistingNodeIds(root: INode) {
        root.originalId()?.let { existingNodeIds.add(it) }
        root.allChildren.forEach { collectExistingNodeIds(it) }
    }

    private fun buildParentSpecIndex(nodeData: NodeData) {
        for (child in nodeData.children) {
            child.originalId()?.let {  originalIdToParentSpec[it] = nodeData }
            buildParentSpecIndex(child)
        }
    }

    private fun buildRefIndex(node: INode) {
        node.originalId()?.let { originalIdToRef[it] = node.reference }
        node.allChildren.forEach { buildRefIndex(it) }
    }

    private fun buildSpecIndex(nodeData: NodeData) {
        nodeData.originalId()?.let { originalIdToSpec[it] = nodeData }
        nodeData.children.forEach { buildSpecIndex(it) }
    }

    private fun syncNode(node: INode, nodeData: NodeData) {
        syncProperties(node, nodeData)
        syncChildNodes(node, nodeData)
    }

    private fun syncProperties(node: INode, nodeData: NodeData) {
        nodeData.properties.forEach { node.setPropertyValue(it.key, it.value) }
        val toBeRemoved = node.getPropertyRoles().toSet()
            .subtract(nodeData.properties.keys)
            .filter { it != NodeData.idPropertyKey }
        toBeRemoved.forEach { node.setPropertyValue(it, null) }
    }

    private fun syncAllReferences(root: INode, rootData: NodeData) {
        syncReferences(root, rootData)
        for ((node, data) in root.allChildren.zip(rootData.children)) {
            syncAllReferences(node, data)
        }
    }

    private fun syncReferences(node: INode, nodeData: NodeData) {
        nodeData.references.forEach { node.setReferenceTarget(it.key, originalIdToRef[it.value]) }
        val toBeRemoved = node.getReferenceRoles().toSet().subtract(nodeData.references.keys)
        toBeRemoved.forEach { node.setPropertyValue(it, null) }
    }

    private fun syncChildNodes(node: INode, nodeData: NodeData) {
        val specifiedNodes = nodeData.children.toSet()
        val existingIds = node.allChildren.map { it.originalId() }.toSet()
        val missingNodes = specifiedNodes.filter { !existingIds.contains(it.originalId()) }

        val toBeMovedHere = missingNodes.filter { existingNodeIds.contains(it.originalId()) }.toSet()
        val toBeAdded = missingNodes.subtract(toBeMovedHere)

        toBeAdded.forEach {
            val index = nodeData.children.indexOf(it)
            val createdNode = node.addNewChild(it.role, index, it.concept?.let { s -> ConceptReference(s) })
            createdNode.setPropertyValue(NodeData.idPropertyKey, it.originalId())
        }

        toBeMovedHere.forEach {
            val target = originalIdToRef[it.originalId()]?.resolveNode(node.getArea())
            if (target != null) {
                node.moveChild(it.role, -1, target)
            }
        }

        val toBeRemoved = node.allChildren.filter { !originalIdToSpec.contains(it.originalId()) }
        toBeRemoved.forEach { node.removeChild(it) }

        node.allChildren.forEach {
            val childData = originalIdToSpec[it.originalId()]
            if (childData != null) {
                syncNode(it, childData)
            }
        }
    }

    private fun syncAllChildOrders(root: INode, rootData: NodeData) {
        syncChildOrder(root, rootData)
        for ((node, data) in root.allChildren.zip(rootData.children)) {
            syncAllChildOrders(node, data)
        }
    }

    private fun syncChildOrder(node: INode, nodeData: NodeData) {
        val existingChildren = node.allChildren.toList()
        val specifiedChildren = nodeData.children
        require(existingChildren.size == specifiedChildren.size)

        for ((actualNode, specifiedNode) in existingChildren zip specifiedChildren) {
            val actualId = actualNode.originalId()
            if (actualId != specifiedNode.originalId() ) {
                val targetIndex = specifiedChildren.indexOfFirst { actualId == it.originalId() }
                node.moveChild(actualNode.roleInParent, targetIndex, actualNode)
            }
        }
    }

    private fun INode.originalId(): String? {
        return this.getPropertyValue(NodeData.idPropertyKey)
    }

    private fun NodeData.originalId(): String? {
        return properties[NodeData.idPropertyKey] ?: id
    }

}