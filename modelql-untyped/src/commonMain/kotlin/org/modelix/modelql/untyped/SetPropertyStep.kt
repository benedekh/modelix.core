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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.modelix.model.api.INode
import org.modelix.model.api.IProperty
import org.modelix.model.api.IPropertyReference
import org.modelix.modelql.core.IMonoStep
import org.modelix.modelql.core.IStep
import org.modelix.modelql.core.IStepOutput
import org.modelix.modelql.core.QueryDeserializationContext
import org.modelix.modelql.core.QueryGraphDescriptorBuilder
import org.modelix.modelql.core.SerializationContext
import org.modelix.modelql.core.StepDescriptor
import org.modelix.modelql.core.TransformingStepWithParameter
import org.modelix.modelql.core.asMono
import org.modelix.modelql.core.connect

class SetPropertyStep(val property: IPropertyReference) :
    TransformingStepWithParameter<INode, String?, Any?, INode>() {
    override fun getOutputSerializer(serializationContext: SerializationContext): KSerializer<out IStepOutput<INode>> {
        return getInputProducer().getOutputSerializer(serializationContext)
    }

    override fun transformElement(input: IStepOutput<INode>, parameter: IStepOutput<String?>?): IStepOutput<INode> {
        input.value.setPropertyValue(property.toLegacy(), parameter?.value)
        return input
    }

    override fun createDescriptor(context: QueryGraphDescriptorBuilder): StepDescriptor {
        return Descriptor(property.getIdOrName(), property)
    }

    override fun requiresWriteAccess(): Boolean {
        return true
    }

    override fun toString(): String {
        return "${getProducer()}.setProperty($property, ${getParameterProducer()})"
    }

    @Serializable
    @SerialName("untyped.setProperty")
    class Descriptor(val role: String, val property: IPropertyReference? = null) : StepDescriptor() {
        override fun createStep(context: QueryDeserializationContext): IStep {
            return SetPropertyStep(property ?: IPropertyReference.fromUnclassifiedString(role))
        }
    }
}

@Deprecated("provide an IPropertyReference")
fun IMonoStep<INode>.setProperty(role: String, value: String?): IMonoStep<INode> {
    return setProperty(role, value.asMono())
}

@Deprecated("provide an IPropertyReference")
fun IMonoStep<INode>.setProperty(role: String, value: IMonoStep<String?>): IMonoStep<INode> {
    return this.setProperty(IPropertyReference.fromUnclassifiedString(role), value)
}

fun IMonoStep<INode>.setProperty(role: IPropertyReference, value: IMonoStep<String?>): IMonoStep<INode> {
    return SetPropertyStep(role).also {
        connect(it)
        value.connect(it)
    }
}

fun IMonoStep<INode>.setProperty(role: IProperty, value: String?): IMonoStep<INode> {
    return setProperty(role, value.asMono())
}

fun IMonoStep<INode>.setProperty(role: IPropertyReference, value: String?): IMonoStep<INode> {
    return setProperty(role, value.asMono())
}

fun IMonoStep<INode>.setProperty(role: IProperty, value: IMonoStep<String?>): IMonoStep<INode> {
    return setProperty(role.toReference(), value)
}
