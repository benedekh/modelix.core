package org.modelix.modelql.untyped

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.modelix.model.api.INode
import org.modelix.model.api.INodeReference
import org.modelix.model.api.UnresolvableNodeReferenceException
import org.modelix.model.api.resolveInCurrentContext
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

class ResolveNodeStep() : MonoTransformingStep<INodeReference, INode>() {
    override fun createStream(input: StepStream<INodeReference>, context: IStreamInstantiationContext): StepStream<INode> {
        return input.map {
            it.value.resolveInCurrentContext() ?: throw UnresolvableNodeReferenceException(it.value)
        }.asStepStream(this)
    }

    override fun getOutputSerializer(serializationContext: SerializationContext): KSerializer<out IStepOutput<INode>> {
        return serializationContext.serializer<INode>().stepOutputSerializer(this)
    }

    override fun createDescriptor(context: QueryGraphDescriptorBuilder) = Descriptor()

    @Serializable
    @SerialName("untyped.resolveNode")
    class Descriptor() : StepDescriptor() {
        override fun createStep(context: QueryDeserializationContext): IStep {
            return ResolveNodeStep()
        }

        override fun doNormalize(idReassignments: IdReassignments): StepDescriptor = Descriptor()
    }

    override fun toString(): String {
        return "${getProducers().single()}\n.resolve()"
    }
}

fun IMonoStep<INodeReference>.resolve() = ResolveNodeStep().connectAndDowncast(this)
fun IFluxStep<INodeReference>.resolve() = ResolveNodeStep().connectAndDowncast(this)
