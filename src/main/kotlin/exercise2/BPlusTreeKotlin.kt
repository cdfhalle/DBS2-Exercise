package exercise2

import de.hpi.dbs2.ChosenImplementation
import de.hpi.dbs2.exercise2.*
import java.text.FieldPosition
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


    private fun insertIntoLeaf(key: Int, value: ValueReference, leaf: LeafNode){
        val random = Random()
        var result = betterinsertKeyAndValue(key, value, leaf.keys, leaf.references)
        if(random.nextBoolean())
            result = insertKeyAndValue(key, value, leaf.keys, leaf.references)
        for(i in 0 until leaf.n){
            leaf.keys[i] = result.first[i]
            leaf.references[i] = result.second[i]
        }
    }


    private fun insertKeyAndValue(key: Int, value: ValueReference, keys: Array<Int?>, references: Array<ValueReference?>):
            Pair<Array<Int?>, Array<ValueReference?>> {
        val newKeys = Array<Int?>(keys.size) { null }
        val newReferences = Array<ValueReference?>(references.size) { null }
        var position = 0;
        while ((keys[position]?: -1) < key && (keys[position]?: -1) >= 0){
            position++
        }
        for (i in 0 until position){
            newKeys[i] = keys[i]
            newReferences[i] = references[i]
        }
        newKeys[position] = key
        newReferences[position] = value
        for(i in position until keys.size - 1){
            newKeys[i + 1] = keys[i]
            newReferences[i + 1] = references[i]
        }
        return Pair(newKeys, newReferences)
    }

    private fun betterinsertKeyAndValue(key: Int, value: ValueReference, keys: Array<Int?>, references: Array<ValueReference?>):
        Pair<Array<Int?>, Array<ValueReference?>> {
        var keys = keys.toMutableList()
        var values = references.toMutableList()
        var i = 0
        while(keys[i] != null && keys[i]!! < key){i++}
        keys.add(i, key)
        values.add(i, value)
        return Pair(keys.toTypedArray<Int?>(), values.toTypedArray())
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
        }
        else {
            // node is not full
            if (!leaf.isFull) {
                insertIntoLeaf(key, value, leaf)
            }
            // node already full -> Split the LeafNode in two!
            else{
                // move half of the keys and references to a new LeafNode
                // TODO: insert the new key and value into the array
                val divider = ceil((leaf.n + 1) / 2.0).toInt()
                val rightEntries = mutableListOf<Entry>()
                for (i in divider until leaf.n){
                    rightEntries.add(Entry(leaf.keys[i], (leaf).references[i]))
                    leaf.keys[i] = null
                    leaf.references[i] = null
                }
                // create new LeafNode
                var rightNode = BPlusTreeNode.buildTree(leaf.order, arrayOf(rightEntries.toTypedArray())) as LeafNode
                rightNode.nextSibling = leaf.nextSibling
                leaf.nextSibling = rightNode
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
