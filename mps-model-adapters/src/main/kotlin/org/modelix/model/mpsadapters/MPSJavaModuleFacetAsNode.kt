package org.modelix.model.mpsadapters

import jetbrains.mps.project.AbstractModule
import jetbrains.mps.project.facets.JavaModuleFacet
import jetbrains.mps.smodel.MPSModuleRepository
import org.modelix.model.api.BuiltinLanguages
import org.modelix.model.api.IChildLink
import org.modelix.model.api.IConcept
import org.modelix.model.api.INode
import org.modelix.model.api.INodeReference
import org.modelix.model.api.IProperty
import org.modelix.model.area.IArea

data class MPSJavaModuleFacetAsNode(val facet: JavaModuleFacet) : IDefaultNodeAdapter {

    override fun getArea(): IArea {
        return MPSArea(facet.module?.repository ?: MPSModuleRepository.getInstance())
    }

    override val reference: INodeReference
        get() {
            val module = checkNotNull(facet.module) { "Module of facet $facet not found" }
            return MPSJavaModuleFacetReference(module.moduleReference)
        }
    override val concept: IConcept
        get() = BuiltinLanguages.MPSRepositoryConcepts.JavaModuleFacet
    override val parent: INode?
        get() = facet.module?.let { MPSModuleAsNode(it) }

    override fun getContainmentLink(): IChildLink {
        return BuiltinLanguages.MPSRepositoryConcepts.Module.facets
    }

    override fun getPropertyValue(property: IProperty): String? {
        return if (property.conformsTo(BuiltinLanguages.MPSRepositoryConcepts.JavaModuleFacet.generated)) {
            // Should always be true
            // https://github.com/JetBrains/MPS/blob/2820965ff7b8836ed1d14adaf1bde29744c88147/core/project/source/jetbrains/mps/project/facets/JavaModuleFacetImpl.java
            true.toString()
        } else if (property.conformsTo(BuiltinLanguages.MPSRepositoryConcepts.JavaModuleFacet.path)) {
            getPath()
        } else {
            null
        }
    }

    private fun getPath(): String? {
        val originalPath = facet.classesGen?.path
        val module = facet.module
        val moduleRoot = if (module is AbstractModule) {
            module.descriptorFile?.parent?.path
        } else {
            null
        }

        return if (moduleRoot != null && originalPath?.startsWith(moduleRoot) == true) {
            "\${module}${originalPath.substring(moduleRoot.length)}"
        } else {
            originalPath
        }
    }
}
