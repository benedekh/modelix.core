@file:OptIn(DelicateCoroutinesApi::class)

package org.modelix.model.client2

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlinx.datetime.toJSDate
import org.modelix.model.withAutoTransactions
import kotlin.js.Promise

internal class ReplicatedModelJSImpl(private val model: ReplicatedModel) : ReplicatedModelJS {
    override fun dispose() {
        model.dispose()
    }

    override fun getBranch(): BranchJS {
        return BranchJSImpl(model.getBranch().withAutoTransactions())
    }

    override fun getCurrentVersionInformation(): Promise<VersionInformationJS> {
        return GlobalScope.promise {
            val currentVersion = model.getCurrentVersion()
            val currentVersionAuthor = currentVersion.author
            val currentVersionTime = currentVersion.getTimestamp()?.toJSDate()
            return@promise VersionInformationJS(currentVersionAuthor, currentVersionTime)
        }
    }
}
