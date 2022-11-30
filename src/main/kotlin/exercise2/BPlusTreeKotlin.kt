package exercise2

import de.hpi.dbs2.ChosenImplementation
import de.hpi.dbs2.exercise2.*
import java.text.FieldPosition
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

    private fun insertIntoLeaf(key: Int, value: ValueReference, leaf: LeafNode){
        val newKeys = IntArray(leaf.n)
        val newReferences = Array<ValueReference?>(leaf.order) { null }
        var position = 0;
        while (leaf.keys[position] < key && leaf.keys[position] != null){
            position++
        }
        for (i in 0 until position){
            newKeys[i] = leaf.keys[i]
            newReferences[i] = leaf.references[i]
        }
        newKeys[position] = key
        newReferences[position] = value
        for(i in position until leaf.n - 1){
            newKeys[i + 1] = leaf.keys[i]
            newReferences[i + 1] = leaf.references[i]
        }
        for(i in 0 until leaf.n){
            leaf.keys[i] = newKeys[i]
            leaf.references[i] = newReferences[i]
        }
    }

    private fun betterInsertIntoLeaf(key: Int, value: ValueReference, leaf: LeafNode){
        var keys = leaf.keys.toMutableList()
        var values = leaf.references.toMutableList()
        var i = 0
        while(keys[i] < key && keys[i] != null){i++}
        keys.add(i, key)
        values.add(i, value)
        keys.removeLast()
        values.removeLast()
        for(i in 0 until leaf.n){
            leaf.keys[i] = keys[i]
            leaf.references[i] = values[i]
        }
    }

    override fun insert(key: Int, value: ValueReference): ValueReference? {

        val path = findLeafPath(key)
        var leaf = path.pop()
        var returnValue: ValueReference? = null
        // key already exists
        if (leaf.getOrNull(key) != null) {
            for (i in 0 until leaf.nodeSize) {
                if (leaf.keys[i] == key) {
                    returnValue = (leaf as LeafNode).references[i]
                    leaf.references[i] = value
                }
            }
        }
        else {
            // node is not full
            if (!leaf.isFull) {
                betterInsertIntoLeaf(key, value, leaf as LeafNode)
            }
            // node already full -> Split the LeafNode in two!
            else{
                // move half of the keys and references to a new LeafNode
                val divider = ceil((leaf.n + 1) / 2.0).toInt()
                val rightEntries = mutableListOf<Entry>()
                for (i in divider until leaf.n){
                    rightEntries.add(Entry(leaf.keys[i], (leaf as LeafNode).references[i]))
                    leaf.keys[i] = null
                    leaf.references[i] = null
                }
                // create new LeafNode
                var rightNode = BPlusTreeNode.buildTree(leaf.order, arrayOf(rightEntries.toTypedArray()))
                // TODO: integrate the new LeafNode into the tree
            }
        }

        return returnValue
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
