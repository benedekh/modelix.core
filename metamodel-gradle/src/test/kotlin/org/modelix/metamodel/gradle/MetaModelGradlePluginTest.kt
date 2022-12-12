/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package org.modelix.metamodel.gradle

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * A simple unit test for the 'org.modelix.metamodel.gradle.greeting' plugin.
 */
class MetaModelGradlePluginTest {
    @Test fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("org.modelix.metamodel.gradle")

        // Verify the result
        assertNotNull(project.tasks.findByName("generateMetaModelSources"))
    }
}