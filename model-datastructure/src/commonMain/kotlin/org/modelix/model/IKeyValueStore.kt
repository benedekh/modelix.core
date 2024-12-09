package org.modelix.model

import org.modelix.model.lazy.BulkQuery
import org.modelix.model.lazy.BulkQueryConfiguration
import org.modelix.model.lazy.IBulkQuery
import org.modelix.model.lazy.IDeserializingKeyValueStore

interface IKeyValueStore {
    fun newBulkQuery(deserializingCache: IDeserializingKeyValueStore, config: BulkQueryConfiguration): IBulkQuery = BulkQuery(deserializingCache, config)
    operator fun get(key: String): String?
    fun getIfCached(key: String): String?
    suspend fun getA(key: String): String? = get(key)
    fun put(key: String, value: String?)
    fun getAll(keys: Iterable<String>): Map<String, String?>
    fun putAll(entries: Map<String, String?>)
    fun prefetch(key: String)
    fun listen(key: String, listener: IKeyListener)
    fun removeListener(key: String, listener: IKeyListener)
    fun getPendingSize(): Int
}
