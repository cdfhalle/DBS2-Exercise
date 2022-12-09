package exercise2

import de.hpi.dbs2.ChosenImplementation
import de.hpi.dbs2.exercise2.*
import java.util.Random
import java.util.Stack
import kotlin.math.ceil

@ChosenImplementation(true)
class BPlusTreeKotlin : AbstractBPlusTree {
    constructor(order: Int) : super(order)
    constructor(rootNode: BPlusTreeNode<*>) : super(rootNode)

    private fun findLeafPath(key: Int): Stack<BPlusTreeNode<*>>{
        var currentNode : BPlusTreeNode<*> = rootNode
        val path = Stack<BPlusTreeNode<*>>()
        path.push(currentNode)

        while(currentNode.height != 0){
            currentNode = (currentNode as InnerNode).selectChild(key)
            path.push(currentNode)
        }
        return path
    }
    private inline fun <reified T> insertKeyAndReference(
        key: Int,
        reference: T,
        keys: Array<Int?>,
        references: Array<T?>
    ): Pair<Array<Int?>, Array<T?>> {
        val newKeys = Array<Int?>(keys.size +1) { null }
        val newReferences = Array<T?>(references.size +1) { null }
        var position = 0;
        val offset = if (reference is ValueReference) 0 else 1
        while (position < keys.size && keys[position] != null && keys[position]!! < key) {
            position++
        }
        keys.copyInto(newKeys, 0, 0, position)
        references.copyInto(newReferences, 0, 0, position + offset)
        newKeys[position] = key
        newReferences[position + offset] = reference
        keys.copyInto(newKeys, position + 1, position, keys.size)
        references.copyInto(newReferences, position + 1 + offset, position + offset, references.size)
        return Pair(newKeys, newReferences)
    }
    private fun LeafNode.insertKeyAndReference(key: Int, reference: ValueReference) {
        val result = insertKeyAndReference(key, reference, this.keys, this.references)
        for (i in 0 until this.keys.size){
            this.keys[i] = result.first[i]
        }
        for (i in 0 until this.references.size) {
            this.references[i] = result.second[i]
        }
    }
    private fun InnerNode.insertKeyAndReference(key: Int, reference: BPlusTreeNode<*>) {
        val result = insertKeyAndReference(key, reference, this.keys, this.references)
        for (i in 0 until this.keys.size){
            this.keys[i] = result.first[i]
        }
        for (i in 0 until this.references.size) {
            this.references[i] = result.second[i]
        }
    }
    private fun LeafNode.insertAndSplit(key: Int, reference: ValueReference): LeafNode {
        // insert key and reference
        val (newKeys, newReferences) = insertKeyAndReference<ValueReference>(key, reference, this.keys, this.references)
        // move half of the keys and references to a new LeafNode
        val divider = ceil((this.n + 1) / 2.0).toInt()
        newKeys.copyInto(this.keys, 0, 0, divider)
        newReferences.copyInto(this.references, 0, 0, divider)
        this.keys.fill(null, divider, keys.size)
        this.references.fill(null, divider, references.size)
        val rightNode = LeafNode(this.order)
        newKeys.copyInto(rightNode.keys, 0, divider, newKeys.size)
        newReferences.copyInto(rightNode.references, 0, divider, newReferences.size)
        rightNode.nextSibling = this.nextSibling
        this.nextSibling = rightNode
        return rightNode
    }
    private fun InnerNode.insertAndSplit(key: Int, reference: BPlusTreeNode<*>): InnerNode {
        // insert key and reference
        val (newKeys, newReferences) = insertKeyAndReference<BPlusTreeNode<*>>(key, reference, this.keys, this.references)
        // move half of the keys and references to a new LeafNode
        val divider = ceil((this.n + 1) / 2.0).toInt()
        newKeys.copyInto(this.keys, 0, 0, divider)
        newReferences.copyInto(this.references, 0, 0, divider)
        this.keys.fill(null, divider, keys.size)
        this.references.fill(null, divider, references.size)
        val rightNode = InnerNode(this.order)
        newKeys.copyInto(rightNode.keys, 0, divider, newKeys.size)
        newReferences.copyInto(rightNode.references, 0, divider, newReferences.size)
        return rightNode
    }
    private fun createNewRootNode(leftNode: BPlusTreeNode<*>, rightNode: BPlusTreeNode<*>): InnerNode {
        val newRootNode = InnerNode(this.order)
        if (leftNode is LeafNode) {
            newRootNode.keys[0] = rightNode.keys[0]
        } else {
            newRootNode.keys[0] = leftNode.keys.last()
            leftNode.keys[leftNode.keys.lastIndex] = null
        }
        newRootNode.references[0] = leftNode
        newRootNode.references[1] = rightNode
        return newRootNode
    }
    override fun insert(key: Int, value: ValueReference): ValueReference? {

        val path = findLeafPath(key)
        var leaf = path.pop() as LeafNode
        var returnValue: ValueReference? = null
        // key already exists
        if (leaf.getOrNull(key) != null) {
            for (i in 0 until leaf.nodeSize) {
                if (leaf.keys[i] == key) {
                    returnValue = leaf.references[i]
                    leaf.references[i] = value
                }
            }
        } else {
            var currentNode: BPlusTreeNode<*> = leaf
            var finished: Boolean = false
            var rightChild: BPlusTreeNode<*>
            var key = key
            var reference: BPlusTreeNode<*> = leaf
            while (!finished) {
                if (!currentNode.isFull) {
                    when (currentNode) {
                        is LeafNode -> currentNode.insertKeyAndReference(key, value)
                        is InnerNode -> currentNode.insertKeyAndReference(key, reference)
                    }
                    finished = true
                } else {
                    when (currentNode) {
                        rootNode -> {
                            val newNode = when (currentNode) {
                                is LeafNode -> currentNode.insertAndSplit(key, value)
                                is InnerNode -> currentNode.insertAndSplit(key, reference)
                            }
                            this.rootNode = createNewRootNode(currentNode, newNode)
                            finished = true
                        }

                        is LeafNode -> {
                            rightChild = currentNode.insertAndSplit(key, value)
                            key = rightChild.keys[0]
                            reference = rightChild
                            currentNode = path.pop()
                        }

                        is InnerNode -> {
                            rightChild = currentNode.insertAndSplit(key, reference)
                            key = currentNode.keys.last { it != null }!!
                            currentNode.keys[currentNode.keys.indexOf(key)] = null
                            reference = rightChild
                            currentNode = path.pop()
                        }
                    }
                }
            }
        }

        return returnValue
    }


    private fun insertIntoLeaf(key: Int, value: ValueReference, leaf: LeafNode) {
        val random = Random()
        var result = BadinsertKeyAndReference(key, value, leaf.keys, leaf.references)
        if (random.nextBoolean())
            result = insertKeyAndReference(key, value, leaf.keys, leaf.references)
        for (i in 0 until leaf.n) {
            leaf.keys[i] = result.first[i]
            leaf.references[i] = result.second[i]
        }
    }
    private inline fun <reified T> BadinsertKeyAndReference(
        key: Int,
        reference: T,
        keys: Array<Int?>,
        references: Array<T?>
    ): Pair<Array<Int?>, Array<T?>> {
        var keyList = keys.toMutableList()
        var referenceList = references.toMutableList()
        var i = 0
        while (keyList[i] != null && keyList[i]!! < key) {
            i++
        }
        keyList.add(i, key)
        referenceList.add(i+1, reference)
        return Pair(keyList.toTypedArray<Int?>(), referenceList.toTypedArray<T?>())
    }
    private fun insertEntryIntoLeaf(key: Int, value: ValueReference, leaf: LeafNode, parent: InnerNode) {
        // check if node is root node
        // ...
        // node is not full
        if (!leaf.isFull) {
            var result = insertKeyAndReference(key, value, leaf.keys, leaf.references)
            for (i in 0 until leaf.n) {
                leaf.keys[i] = result.first[i]
                leaf.references[i] = result.second[i]
            }
        }
        // node already full -> Split the LeafNode in two!
        else {
            // move half of the keys and references to a new LeafNode
            val (newKeys, newReferences) = insertKeyAndReference(key, value, leaf.keys, leaf.references)
            val divider = ceil((leaf.n + 1) / 2.0).toInt()
            for (i in 0 until divider) {
                leaf.keys[i] = newKeys[i]
                leaf.references[i] = newReferences[i]
            }
            var rightNode = LeafNode(order)
            for (i in divider until leaf.n) {
                rightNode.keys[i] = newKeys[i]
                rightNode.references[i] = newReferences[i]
                leaf.keys[i] = null
                leaf.references[i] = null
            }
            rightNode.nextSibling = leaf.nextSibling
            leaf.nextSibling = rightNode
            // insertEntryIntoInnerNode(leaf.getSmallestKey(), rightNode, parent)
        }
    }

        // Find LeafNode in which the key has to be inserted.
        //   It is a good idea to track the "path" to the LeafNode in a Stack or something alike.
        // Does the key already exist? Overwrite!
        //   leafNode.references[pos] = value;
        //   But remember return the old value!
        // New key - Is there still space?
        //   leafNode.keys[pos] = key;
        //   leafNode.references[pos] = value;
        //   Don't forget to update the parent keys and so on...
        // Otherwise
        //   Split the LeafNode in two!
        //   Is parent node root?
        //     update rootNode = ... // will have only one key
        //   Was node instanceof LeafNode?
        //     update parentNode.keys[?] = ...
        //   Don't forget to update the parent keys and so on...

        // Check out the exercise slides for a flow chart of this logic.
        // If you feel stuck, try to draw what you want to do and
        // check out Ex2Main for playing around with the tree by e.g. printing or debugging it.
        // Also check out all the methods on BPlusTreeNode and how they are implemented or
        // the tests in BPlusTreeNodeTests and BPlusTreeTests!
}
