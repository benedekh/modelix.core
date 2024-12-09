package org.modelix.model.mpsadapters

import org.modelix.model.api.BuiltinLanguages
import org.modelix.model.api.INode

class MPSAreaTest : MpsAdaptersTestBase("SimpleProject") {

    fun testResolveModuleInNonExistingProject() {
        val repositoryNode: INode = MPSRepositoryAsNode(mpsProject.repository)
        val area = repositoryNode.getArea()
        readAction {
            val nonExistingProject = MPSProjectReference("nonExistingProject")
            val module = repositoryNode.getChildren(BuiltinLanguages.MPSRepositoryConcepts.Repository.modules)
                .single { it.getPropertyValue(BuiltinLanguages.jetbrains_mps_lang_core.INamedConcept.name) == "Solution1" }
            val projectModuleReference = MPSProjectModuleReference((module.reference as MPSModuleReference).moduleReference, nonExistingProject)

            val resolutionResult = area.resolveNode(projectModuleReference)

            assertNull(resolutionResult)
        }
    }
}
