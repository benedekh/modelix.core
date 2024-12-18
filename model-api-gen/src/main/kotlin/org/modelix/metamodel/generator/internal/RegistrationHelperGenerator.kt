package org.modelix.metamodel.generator.internal

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.modelix.metamodel.GeneratedLanguage
import org.modelix.metamodel.generator.MetaModelGenerator
import org.modelix.metamodel.generator.NameConfig
import org.modelix.metamodel.generator.ProcessedLanguageSet
import org.modelix.metamodel.generator.runBuild
import java.nio.file.Path

internal class RegistrationHelperGenerator(
    private val classFqName: String,
    private val languages: ProcessedLanguageSet,
    override val outputDir: Path,
    override val nameConfig: NameConfig,
) : NameConfigBasedGenerator(nameConfig), FileGenerator {

    override fun generateFileSpec(): FileSpec {
        require(classFqName.contains(".")) { "The name of the registrationHelper does not contain a dot. Use a fully qualified name." }
        val typeName = ClassName(classFqName.substringBeforeLast("."), classFqName.substringAfterLast("."))

        val languagesProperty = PropertySpec.builder(
            name = "languages",
            type = List::class.parameterizedBy(GeneratedLanguage::class),
        ).runBuild {
            initializer(
                buildString {
                    append("listOf(")
                    append(
                        languages.getLanguages()
                            .map { it.generatedClassName() }
                            .joinToString(", ") { it.canonicalName },
                    )
                    append(")")
                },
            )
        }

        val registerAllFun = FunSpec.builder("registerAll").runBuild {
            addStatement("""languages.forEach { it.register() }""")
        }

        val registrationHelperClass = TypeSpec.objectBuilder(typeName).runBuild {
            addProperty(languagesProperty)
            addFunction(registerAllFun)
        }

        return FileSpec.builder(typeName.packageName, typeName.simpleName).runBuild {
            addFileComment(MetaModelGenerator.HEADER_COMMENT)
            addType(registrationHelperClass)
        }
    }
}
