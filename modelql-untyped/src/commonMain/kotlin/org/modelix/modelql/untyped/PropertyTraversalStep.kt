/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modelix.modelql.untyped

import com.badoo.reaktive.observable.flatMapSingle
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.modelix.model.api.INode
import org.modelix.model.api.IPropertyReference
import org.modelix.model.api.async.asAsyncNode
import org.modelix.modelql.core.IFluxStep
import org.modelix.modelql.core.IMonoStep
import org.modelix.modelql.core.IStep
import org.modelix.modelql.core.IStepOutput
import org.modelix.modelql.core.IStreamInstantiationContext
import org.modelix.modelql.core.IdReassignments
import org.modelix.modelql.core.MonoTransformingStep
import org.modelix.modelql.core.QueryDeserializationContext
import org.modelix.modelql.core.QueryGraphDescriptorBuilder
import org.modelix.modelql.core.SerializationContext
import org.modelix.modelql.core.StepDescriptor
import org.modelix.modelql.core.StepStream
import org.modelix.modelql.core.asStepStream
import org.modelix.modelql.core.stepOutputSerializer
import org.modelix.streams.orNull

class PropertyTraversalStep(val property: IPropertyReference) : MonoTransformingStep<INode, String?>(), IMonoStep<String?> {
    override fun createStream(input: StepStream<INode>, context: IStreamInstantiationContext): StepStream<String?> {
        return input.flatMapSingle {
            it.value.asAsyncNode().getPropertyValue(property).orNull()
        }.asStepStream(this)
    }

    override fun canBeEmpty(): Boolean = getProducer().canBeEmpty()

    override fun canBeMultiple(): Boolean = getProducer().canBeMultiple()

    override fun getOutputSerializer(serializationContext: SerializationContext): KSerializer<out IStepOutput<String?>> {
        return serializationContext.serializer<String?>().stepOutputSerializer(this)
    }

    override fun createDescriptor(context: QueryGraphDescriptorBuilder) = PropertyStepDescriptor(property.getIdOrName(), property)

    @Serializable
    @SerialName("untyped.property")
    data class PropertyStepDescriptor(val role: String, val property: IPropertyReference? = null) : StepDescriptor() {
        override fun createStep(context: QueryDeserializationContext): IStep {
            return PropertyTraversalStep(property ?: IPropertyReference.fromUnclassifiedString(role))
        }

        override fun doNormalize(idReassignments: IdReassignments): StepDescriptor = PropertyStepDescriptor(role, property)
    }

    override fun toString(): String {
        return "${getProducers().single()}\n.property(\"$property\")"
    }
}

fun IMonoStep<INode>.property(role: IPropertyReference) = PropertyTraversalStep(role).connectAndDowncast(this)
fun IFluxStep<INode>.property(role: IPropertyReference) = PropertyTraversalStep(role).connectAndDowncast(this)

@Deprecated("provide an IPropertyReference")
fun IMonoStep<INode>.property(role: String) = property(IPropertyReference.fromUnclassifiedString(role))

@Deprecated("provide an IPropertyReference")
fun IFluxStep<INode>.property(role: String) = property(IPropertyReference.fromUnclassifiedString(role))
