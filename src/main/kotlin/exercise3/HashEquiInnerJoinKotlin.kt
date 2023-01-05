package exercise3

import de.hpi.dbs2.ChosenImplementation
import de.hpi.dbs2.dbms.Block
import de.hpi.dbs2.dbms.BlockManager
import de.hpi.dbs2.dbms.Operation
import de.hpi.dbs2.dbms.Relation
import de.hpi.dbs2.exercise3.InnerJoinOperation
import de.hpi.dbs2.exercise3.JoinAttributePair
import kotlin.math.absoluteValue
import kotlin.math.ceil

@ChosenImplementation(true)

//class TupleAppender : AutoCloseable, Consumer<Tuple> {
//
//    val blockOutput: BlockOutput
//
//    constructor(blockOutput: BlockOutput) {
//        this.blockOutput = blockOutput
//    }
//
//    val outputBlock = getBlockManager().allocate(true)
//
//    override fun accept(tuple: Tuple) {
//        if (outputBlock.isFull()) {
//            blockOutput.move(outputBlock)
//            outputBlock = getBlockManager().allocate(true)
//        }
//        outputBlock.append(tuple)
//    }
//
//    override fun close() {
//        if (!outputBlock.isEmpty()) {
//            blockOutput.move(outputBlock)
//        } else {
//            getBlockManager().release(outputBlock, false)
//        }
//    }
//}

class HashEquiInnerJoinKotlin(
    blockManager: BlockManager,
    leftColumnIndex: Int,
    rightColumnIndex: Int,
) : InnerJoinOperation(
    blockManager,
    JoinAttributePair.EquiJoinAttributePair(
        leftColumnIndex,
        rightColumnIndex
    )
) {
    override fun estimatedIOCost(
        leftInputRelation: Relation,
        rightInputRelation: Relation
    ): Int {
        return 3 * (leftInputRelation.estimatedBlockCount() + rightInputRelation.estimatedBlockCount())
    }

    private fun hashRelation(relation: Relation, columnIndex: Int, bucketCount : Int): Array<MutableList<Block>>{
        val buckets = Array<MutableList<Block>>(bucketCount) { mutableListOf<Block>() }
        val bucketBlocksInMemory = Array<Block>(bucketCount) { blockManager.allocate(true) }
        val rIterator = relation.iterator()
        // iterate over all blocks of the relation
        while (rIterator.hasNext()){
            var block = rIterator.next()
            block = blockManager.load(block)
            val tupleIterator = block.iterator()
            // iterate over all tuples of the loaded block
            while (tupleIterator.hasNext()){
                val tuple = tupleIterator.next()
                val bucketIndex = (tuple[columnIndex].hashCode()).absoluteValue % bucketCount
                // if the hashed bucket is full safe it to disc and allocate a new block
                if (bucketBlocksInMemory[bucketIndex].isFull()){
                    buckets[bucketIndex].add(blockManager.release(bucketBlocksInMemory[bucketIndex], true)!!)
                    bucketBlocksInMemory[bucketIndex] = blockManager.allocate(true)
                }else {
                    bucketBlocksInMemory[bucketIndex].append(tuple)
                }
            }
            // release each input block from memory after it has been processed
            blockManager.release(block, saveToDisk = false)
        }
        // save all remaining blocks to disc
        for (i in 0 until bucketCount){
            buckets[i].add(blockManager.release(bucketBlocksInMemory[i], true)!!)
        }
        return buckets
    }

    override fun join(
        leftInputRelation: Relation,
        rightInputRelation: Relation,
        outputRelation: Relation
    ) {


        val bucketCount: Int = blockManager.freeBlocks - 1
        val leftBucketSize = ceil(leftInputRelation.estimatedBlockCount().toDouble()/ bucketCount.toDouble()).toInt()
        val rightBucketSize = ceil(rightInputRelation.estimatedBlockCount().toDouble() / bucketCount.toDouble()).toInt()
        if (minOf(leftBucketSize, rightBucketSize) >= blockManager.freeBlocks -1){
            throw Operation.RelationSizeExceedsCapacityException()
        }


        var rs = rightInputRelation
        var rb = leftInputRelation
        var sColumnIndex = joinAttributePair.rightColumnIndex
        var bColumnIndex = joinAttributePair.leftColumnIndex
        if (leftBucketSize <= rightBucketSize){
            rs = leftInputRelation
            rb = rightInputRelation
            sColumnIndex = joinAttributePair.leftColumnIndex
            bColumnIndex = joinAttributePair.rightColumnIndex
        }

        val sBuckets = hashRelation(rs, sColumnIndex, bucketCount)
        var blockCounts = 0
        for (i in sBuckets.indices){
            blockCounts += sBuckets[i].size
        }
        println("sBuckets: $blockCounts")
        val bBuckets = hashRelation(rb, bColumnIndex, bucketCount)
        for (i in bBuckets.indices){
            blockCounts += bBuckets[i].size
        }
        println("bBuckets: $blockCounts")
        println("s blocks: ${rs.estimatedBlockCount()}")
        println("b blocks: ${rb.estimatedBlockCount()}")
        val colDef = buildOutputColumns(rs, rb)
        var outputBlock = blockManager.allocate(true)
        val blockOutput = outputRelation.getBlockOutput()

        for (i in 0 until bucketCount){
            // load complete bucket of small relation
            for (sBlock in sBuckets[i]){
                blockManager.load(sBlock)
            }
            // for every block in the corresponding bucket from b
            for (bBlock in bBuckets[i]){
                blockManager.load(bBlock)
                //check for every tuple if there is a join partner in the bucket from s
                for (bTuple in bBlock){
                    for (sBlock in sBuckets[i]){
                        for(sTuple in sBlock){
                            if (joinAttributePair.matches(sTuple, bTuple)) {
                                val joinedTuple = constructJoinedTuple(sTuple, bTuple, colDef)
                                if (outputBlock.isFull()){
                                    blockOutput.move(outputBlock)
                                    outputBlock = blockManager.allocate(true)
                                }else{
                                    outputBlock.append(joinedTuple)
                                }
                            }
                        }
                    }
                }
                blockManager.release(bBlock, saveToDisk = false)
            }
            for (sBlock in sBuckets[i]){
                blockManager.release(sBlock, saveToDisk = false)
            }
        }
        blockOutput.move(outputBlock)

    }
}
