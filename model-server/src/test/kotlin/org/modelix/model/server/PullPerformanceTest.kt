package org.modelix.model.server

import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.coroutineScope
import org.modelix.model.api.IChildLink
import org.modelix.model.api.IConceptReference
import org.modelix.model.api.INode
import org.modelix.model.api.getRootNode
import org.modelix.model.client2.ModelClientV2
import org.modelix.model.client2.runWriteOnBranch
import org.modelix.model.lazy.RepositoryId
import org.modelix.model.server.handlers.IdsApiImpl
import org.modelix.model.server.handlers.KeyValueLikeModelServer
import org.modelix.model.server.handlers.ModelReplicationServer
import org.modelix.model.server.handlers.RepositoriesManager
import org.modelix.model.server.store.InMemoryStoreClient
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class PullPerformanceTest {
    private fun runTest(block: suspend ApplicationTestBuilder.(storeClientWithStatistics: StoreClientWithStatistics) -> Unit) = testApplication {
        val storeClientWithStatistics = StoreClientWithStatistics(InMemoryStoreClient())
        val repositoriesManager = RepositoriesManager(storeClientWithStatistics)
        application {
            installDefaultServerPlugins()
            ModelReplicationServer(repositoriesManager).init(this)
            KeyValueLikeModelServer(repositoriesManager).init(this)
            IdsApiImpl(repositoriesManager).init(this)
        }

        coroutineScope {
            block(storeClientWithStatistics)
        }
    }

    /**
     * Tests the performance of the `GET /v2/repositories/{repository}/branches/{branch}` endpoint.
     * Many small request to an IgniteStoreClient lead to a poor performance. This test ensure that bulk requests are
     * used for loading a model.
     */
    @Test
    fun `bulk requests are used`() = runTest { storeClientWithStatistics ->
        val client = createClient {
            expectSuccess = true
        }
        val rand = Random(1056343)
        val url = "http://localhost/v2"
        val preparingModelClient = ModelClientV2.builder().url(url).client(client).build().also { it.init() }
        val repositoryId = RepositoryId("repo1")
        preparingModelClient.initRepository(repositoryId)
        preparingModelClient.runWriteOnBranch(repositoryId.getBranchReference()) { branch ->
            val rootNode = branch.getRootNode()
            repeat(10_000) {
                val randomNode = rootNode.getRandomNode(rand)
                randomNode.addNewChild(IChildLink.fromName("roleA"), -1, null as IConceptReference?)
            }
        }

        val requestingModelClient = ModelClientV2.builder().url(url).client(client).build().also { it.init() }
        val totalRequestsBeforePull = storeClientWithStatistics.getTotalRequests()
        requestingModelClient.pull(repositoryId.getBranchReference(), null)
        val totalRequestsAfterPull = storeClientWithStatistics.getTotalRequests()
        val actualRequestCount = totalRequestsAfterPull - totalRequestsBeforePull

        // The request count when not using a bulk query is a couple ten thousand.
        // Using a bulk query reduces the number of separate requests to the underling store to much fewer.
        assertTrue(actualRequestCount < 30, "Too many request: $actualRequestCount")
    }
}

private fun INode.getRandomNode(rand: Random): INode {
    val node = this
    val children = node.allChildren.toList()
    val index = rand.nextInt(children.size + 1)
    return if (index < children.size) {
        children.get(index).getRandomNode(rand)
    } else {
        node
    }
}
