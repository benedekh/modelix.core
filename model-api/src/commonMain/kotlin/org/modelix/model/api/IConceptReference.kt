package org.modelix.model.api

import org.modelix.model.area.IArea

/**
 * Reference to an [IConcept].
 */
@Deprecated("use ConceptReference")
interface IConceptReference {
    companion object {
        private var deserializers: Map<Any, ((String) -> IConceptReference?)> = LinkedHashMap()

        @Deprecated("use ConceptReference()")
        fun deserialize(serialized: String?): ConceptReference? {
            if (serialized == null) return null
            val refs = deserializers.values.mapNotNull { deserialize(serialized) }
            return when (refs.size) {
                0 -> ConceptReference(serialized)
                1 -> refs.first()
                else -> throw RuntimeException("Multiple deserializers applicable to $serialized")
            }
        }

        @Deprecated("use ILanguageRepository.register")
        fun registerDeserializer(key: Any, deserializer: ((String) -> IConceptReference?)) {
            deserializers = deserializers + (key to deserializer)
        }

        @Deprecated("use ILanguageRepository.unregister")
        fun unregisterSerializer(key: Any) {
            deserializers = deserializers - key
        }
    }

    /**
     * Returns the unique id of this concept reference.
     *
     * @return uid of this concept reference
     */
    fun getUID(): String

    @Deprecated("use ILanguageRepository.resolveConcept")
    fun resolve(area: IArea?): IConcept?

    @Deprecated("use getUID()")
    fun serialize(): String
}

/**
 * Resolves the receiver concept reference within all registered language repositories.
 *
 * @receiver concept reference to be resolved
 * @return resolved concept
 * @throws RuntimeException if the concept could not be found
 *          or multiple concepts were found for this concept reference
 *
 * @see ILanguageRepository.resolveConcept
 */
fun IConceptReference.resolve(): IConcept = ILanguageRepository.resolveConcept(this)

/**
 * Tries to resolve the receiver concept reference within all registered language repositories.
 *
 * @receiver concept reference to be resolved
 * @return resolved concept or null, if the concept could not be found
 * @throws RuntimeException if multiple concepts were found for this concept reference
 *
 * @see ILanguageRepository.tryResolveConcept
 */
fun IConceptReference.tryResolve(): IConcept? = ILanguageRepository.tryResolveConcept(this)
