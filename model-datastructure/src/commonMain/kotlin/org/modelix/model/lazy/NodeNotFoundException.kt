package org.modelix.model.lazy

import org.modelix.model.persistent.SerializationUtil

class NodeNotFoundException(nodeId: Long) :
    RuntimeException("Node doesn't exist: ${SerializationUtil.longToHex(nodeId)} ($nodeId)")
