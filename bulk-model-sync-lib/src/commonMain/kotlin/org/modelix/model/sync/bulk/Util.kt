package org.modelix.model.sync.bulk

import mu.KLogger
import org.modelix.model.api.BuiltinLanguages
import org.modelix.model.data.ModelData
import org.modelix.model.data.NodeData
import kotlin.math.max

/**
 * Checks if a module is included in the sync.
 *
 * Modules can be included via name ([includedModules]) or prefix ([includedPrefixes])
 * Exclusion works analogously.
 *
 * Returns true iff the module is included and not excluded.
 */
fun isModuleIncluded(
    moduleName: String,
    includedModules: Collection<String>,
    includedPrefixes: Collection<String>,
    excludedModules: Collection<String> = emptySet(),
    excludedPrefixes: Collection<String> = emptySet(),
): Boolean {
    val includedDirectly = includedModules.contains(moduleName)
    val includedByPrefix = includedPrefixes.any { prefix -> moduleName.startsWith(prefix) }
    val included = includedDirectly || includedByPrefix
    if (!included) return false

    val excludedDirectly = excludedModules.contains(moduleName)
    val excludedByPrefix = excludedPrefixes.any { prefix -> moduleName.startsWith(prefix) }
    val excluded = excludedDirectly || excludedByPrefix

    return !excluded
}

fun mergeModelData(models: Collection<ModelData>): ModelData {
    return ModelData(root = NodeData(children = models.map { it.root }))
}

@Deprecated("use collection parameter for better performance")
fun mergeModelData(vararg models: ModelData): ModelData {
    return ModelData(root = NodeData(children = models.map { it.root }))
}

internal fun logImportSize(nodeData: NodeData, logger: KLogger) {
    logger.debug { measureImportSize(nodeData).toString() }
}

private data class ImportSizeMetrics(
    var numModules: Int = 0,
    var numModels: Int = 0,
    val concepts: MutableSet<String> = mutableSetOf(),
    var numProperties: Int = 0,
    var numReferences: Int = 0,
) {
    override fun toString(): String {
        return """
            [Bulk Model Sync Import Size]
            number of modules: $numModules
            number of models: $numModels
            number of concepts: ${concepts.size}
            number of properties: $numProperties
            number of references: $numReferences
        """.trimIndent()
    }
}

private fun measureImportSize(data: NodeData, metrics: ImportSizeMetrics = ImportSizeMetrics()): ImportSizeMetrics {
    data.concept?.let { metrics.concepts.add(it) }

    when (data.concept) {
        BuiltinLanguages.MPSRepositoryConcepts.Module.getUID() -> metrics.numModules++
        BuiltinLanguages.MPSRepositoryConcepts.Model.getUID() -> metrics.numModels++
    }

    metrics.numProperties += data.properties.size
    metrics.numReferences += data.references.size

    data.children.forEach { measureImportSize(it, metrics) }
    return metrics
}

expect fun isTty(): Boolean

class ProgressReporter(
    private val total: ULong,
    private val logger: KLogger,
    private val print: (line: String) -> Unit = ::println,
    private val isTty: () -> Boolean = ::isTty,
) {

    // Determine how often to log. For a small number of total nodes, ensure that we log at all. Otherwise,
    // update progress ever 1% of the total to avoid spamming the output with log lines.
    private val loggingStepSize = max(1UL, (total.toDouble() / 100).toULong())

    /**
     * Increments the progress. This function is assumed to be called for every element that's processed.
     */
    fun step(current: ULong) {
        if (isTty()) {
            // print instead of log, so that the progress line can be overwritten by the carriage return
            print("\r($current / $total) Synchronizing nodes...                    ")
        } else {
            // Report on desired increments or first and last element to ensure users get important start and end
            // information
            if ((current % loggingStepSize) == 0UL || current == total || current == 1UL) {
                logger.info { "($current / $total) Synchronizing nodes..." }
            }
        }
    }
}
