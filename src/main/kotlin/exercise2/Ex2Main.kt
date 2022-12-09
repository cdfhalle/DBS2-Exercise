package exercise2

import de.hpi.dbs2.exercise2.AbstractBPlusTree
import de.hpi.dbs2.exercise2.BPlusTreeNode
import de.hpi.dbs2.exercise2.*

fun main() {
    val order = 4

    val root = BPlusTreeNode.buildTree(order,
        arrayOf(
            entryArrayOf(
                2 to ref(0),
                3 to ref(1),
                5 to ref(2)
            ), entryArrayOf(
                7 to ref(3),
                11 to ref(4)
            )
        ), arrayOf(
            entryArrayOf(
                13 to ref(5),
                17 to ref(6),
                19 to ref(7),
            ), entryArrayOf(
                23 to ref(8),
                29 to ref(9)
            ), entryArrayOf(
                31 to ref(10),
                37 to ref(11),
                41 to ref(12)
            ), entryArrayOf(
                43 to ref(13),
                47 to ref(14)
            )
        )
    )

    val tree: AbstractBPlusTree = BPlusTreeKotlin(root)
    println(tree)

    // check what happens when we insert a key that already exists
    // println(tree.insert(2, ref(15)))
    // println(tree)

    // check what happens when we insert a key that does not exist
    println(tree.insert(8, ref(11)))
    println(tree.insert(9, ref(12)))

    println(tree.insert(10, ref(13)))

    println(tree.insert(12, ref(14)))
    println(tree)
    println(tree.insert(6, ref(15)))
    println(tree)




    /*
     * playground
     * ~ feel free to experiment with the tree and tree nodes here
     */
    val rootT = BPlusTreeNode.buildTree(4,
        arrayOf(
            entryArrayOf(
                1 to ref(6),
                2 to ref(1),
                3 to ref(4)
            ), entryArrayOf(
                4 to ref(3),
                7 to ref(2)
            ),
            entryArrayOf(
                8 to ref(5),
                9 to ref(7)
            )
        )
    )

//    val treeT: AbstractBPlusTree = BPlusTreeKotlin(rootT)
//    println(treeT)
//    treeT.insert(2, ref(1))
//    treeT.insert(7, ref(2))
//    treeT.insert(4, ref(3))
//    println(treeT)
//    treeT.insert(3, ref(4))
//    println(treeT)
//    treeT.insert(8, ref(5))
//    treeT.insert(1, ref(6))
//    treeT.insert(9, ref(7))

}
