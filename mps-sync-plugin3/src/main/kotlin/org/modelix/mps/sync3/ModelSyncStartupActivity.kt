package org.modelix.mps.sync3

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class ModelSyncStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<ModelSyncService>() // just ensure it's initialized
    }
}
