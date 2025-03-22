import org.modelix.model.api.PBranch
import org.modelix.model.operations.IAppliedOperation
import org.modelix.model.operations.OTBranch
import kotlin.test.Test

class OTBranchTest : TreeTestBase() {
    @Test
    fun test_random() {
        val branch1 = OTBranch(PBranch(initialTree, idGenerator), idGenerator, storeCache)
        val branch2 = OTBranch(PBranch(initialTree, idGenerator), idGenerator, storeCache)
        val expectedTree = ExpectedTreeData()

        for (i in 0..999) {
            if (i % 100 == 0) storeCache.clearCache()

            applyRandomChange(branch1, expectedTree)

            val (ops, _) = branch1.operationsAndTree
            applyOps(branch2, ops)

            assertBranch(branch1, expectedTree)
            assertBranch(branch2, expectedTree)
        }
    }

    private fun applyOps(branch: OTBranch, ops: List<IAppliedOperation>) {
        branch.runWrite {
            ops.forEach {
                it.getOriginalOp().apply(branch.writeTransaction, storeCache.getLegacyObjectStore())
            }
        }
    }
}
