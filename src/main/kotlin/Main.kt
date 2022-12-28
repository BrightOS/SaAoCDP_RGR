fun main() {
    val t = BTree(2)

    listOf(
        1, 3, 7, 10, 11, 13,
        14, 15, 18, 16, 19,
        24, 25, 26, 21, 4, 5,
        20, 22, 2, 17, 12, 6
    ).forEach {
        t.add(it)
    }

    println("Полученное дерево:")
    println(t)

    listOf(1, 6, 16, 25, 10, 17, 26, 101).forEach {
        println("Число $it ${if (t.contains(it)) "найдено" else "не найдено"}")
    }
    println()

    listOf(6, 13, 7, 4, 2, 3, 24, 16, 19).forEach {
        t.remove(it)
        println("Дерево после удаления числа $it:")
        println(t)
    }

//    listOf(1, 3, 4, 5, 6).forEach {
//        t.add(it)
//    }
//
//    println(t.toString())
//    t.remove(1)
//    println(t.toString())



//
//    t.remove(13)
//    println("Traversal of tree after removing 13")
//    t.traverse()
//
//    t.remove(7)
//    println("Traversal of tree after removing 7")
//    t.traverse()
//
//    t.remove(4)
//    println("Traversal of tree after removing 4")
//    t.traverse()
//
//    t.remove(2)
//    println("Traversal of tree after removing 2")
//    t.traverse()
//
//    t.remove(16)
//    println("Traversal of tree after removing 16")
//    t.traverse()
}