package exercise2

import de.hpi.dbs2.exercise2.*

fun main() {
/*
    val order = 4

    val root = BPlusTreeNode.buildTree(order,
        arrayOf(
            entryArrayOf(
                2 to ref(2),
                3 to ref(3),
                5 to ref(5)
            ), entryArrayOf(
                7 to ref(7),
                11 to ref(11)
            )
        ), arrayOf(
            entryArrayOf(
                13 to ref(13),
                17 to ref(17),
                19 to ref(19),
            ), entryArrayOf(
                23 to ref(23),
                29 to ref(29)
            ), entryArrayOf(
                31 to ref(31),
                37 to ref(37),
                41 to ref(41)
            ), entryArrayOf(
                43 to ref(43),
                47 to ref(47)
            )
        )
    )

    val tree: AbstractBPlusTree = BPlusTreeKotlin(root)
    println(tree)

    // check what happens when we insert a key that already exists
    println(tree.insert(2, ref(15)))
    println(tree)

    // check what happens when we insert a key that does not exist
    tree.insert(8, ref(8))
    println(tree)
    tree.insert(9, ref(9))
    println(tree)
    tree.insert(10, ref(10))
    println(tree)
    tree.insert(12, ref(12))
    println(tree)
    tree.insert(6, ref(6))
    println(tree)
*/


    /*
     * playground
     * ~ feel free to experiment with the tree and tree nodes here
     */
/*    val rootT = BPlusTreeNode.buildTree(4,
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
    )*/

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


    //val rootT = BPlusTreeNode.buildTree(3, )

/*    val treeT: AbstractBPlusTree = BPlusTreeKotlin(InitialRootNode(3))
    println(treeT)
    treeT.insert(1, ref(1))
    println(treeT)
    treeT.insert(2, ref(2))
    println(treeT)
    treeT.insert(3, ref(3))
    println(treeT)*/

    val rootT = BPlusTreeNode.buildTree(4,
        arrayOf(
            entryArrayOf(
                2 to ref(1),
                3 to ref(4)
            ), entryArrayOf(
                4 to ref(3),
                7 to ref(2)
            )
        )
    )

    val treeT: AbstractBPlusTree = BPlusTreeKotlin(rootT)
    println(treeT)
    println(treeT.isValid)


}
