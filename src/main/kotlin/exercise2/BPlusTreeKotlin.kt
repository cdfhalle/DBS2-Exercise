package exercise2

import de.hpi.dbs2.ChosenImplementation
import de.hpi.dbs2.exercise2.*
import java.util.Stack
import kotlin.math.ceil

@ChosenImplementation(true)
class BPlusTreeKotlin : AbstractBPlusTree {
    constructor(order: Int) : super(order)
    constructor(rootNode: BPlusTreeNode<*>) : super(rootNode)

    private fun findLeafPath(key: Int): Stack<BPlusTreeNode<*>> {
        var currentNode: BPlusTreeNode<*> = rootNode
        val path = Stack<BPlusTreeNode<*>>()
        path.push(currentNode)

        while (currentNode.height != 0) {
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
        val newKeys = Array<Int?>(keys.size + 1) { null }
        val newReferences = Array<T?>(references.size + 1) { null }
        var position = 0
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
        for (i in 0 until this.keys.size) {
            this.keys[i] = result.first[i]
        }
        for (i in 0 until this.references.size) {
            this.references[i] = result.second[i]
        }
    }


    private fun InnerNode.insertKeyAndReference(key: Int, reference: BPlusTreeNode<*>) {
        val result = insertKeyAndReference(key, reference, this.keys, this.references)
        for (i in 0 until this.keys.size) {
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
        val (newKeys, newReferences) = insertKeyAndReference<BPlusTreeNode<*>>(
            key,
            reference,
            this.keys,
            this.references
        )
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
            val keyPosition = ceil((leftNode.n + 1) / 2.0).toInt() - 1
            newRootNode.keys[0] = leftNode.keys[keyPosition]
            leftNode.keys[keyPosition] = null
        }
        newRootNode.references[0] = leftNode
        newRootNode.references[1] = rightNode
        return newRootNode
    }

    override fun insert(key: Int, value: ValueReference): ValueReference? {

        val path = findLeafPath(key)
        val leaf = path.pop() as LeafNode
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
            var finished = false
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
                        is InitialRootNode -> {
                            val leftNode = LeafNode(order)
                            currentNode.keys.copyInto(leftNode.keys)
                            leftNode.references = currentNode.references
                            val rightNode = leftNode.insertAndSplit(key, value)
                            this.rootNode = createNewRootNode(leftNode, rightNode)
                            leftNode.nextSibling = rightNode
                            finished = true
                        }

                        rootNode -> {
                            currentNode as InnerNode
                            val newNode = currentNode.insertAndSplit(key, reference)

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
}