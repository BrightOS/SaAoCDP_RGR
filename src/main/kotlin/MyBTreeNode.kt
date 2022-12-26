import kotlin.math.min

class MyBTreeNode(val power: Int, var isLeaf: Boolean = false, var parentNode: MyBTreeNode? = null) {
    val minimumKeys = power
    val minimumChildren = power + 1
    val maximumKeys = power * 2
    val maximumChildren = maximumKeys + 1

    var keys = arrayListOf<Int>()
    var children = arrayListOf<MyBTreeNode>()


    fun search(key: Int): MyBTreeNode? {
        var i = 0
        while (i < keys.size && key > keys[i]) i++
        if (i < keys.size && keys[i] == key)
            return this
        return if (isLeaf)
            null
        else
            children[i].search(key)
    }


    fun insertNotFull(key: Int) {
        val i = keys.indexOfLast { it < key } + 1
        if (isLeaf)
            keys.add(i, key)
        else {
            children[i].insertNotFull(key)
            if (children[i].keys.size > maximumKeys)
                splitChild(i, children[i])
        }
    }

    fun splitChild(index: Int, srcNode: MyBTreeNode) {

        val tempNode = MyBTreeNode(srcNode.power, srcNode.isLeaf)

        repeat(minimumKeys) {
            tempNode.keys.add(srcNode.keys.removeFirst())
            println("add key ${tempNode.keys.last()}")
        }

        if (!srcNode.isLeaf)
            repeat(minimumChildren) {
                tempNode.children.add(srcNode.children.removeFirst())
                println("add child ${tempNode.children.last()}")
            }

        children.add(index, tempNode)
        keys.add(index, srcNode.keys.removeFirst())
    }


    fun remove(key: Int) {
        val index = keys.indexOfLast { it < key } + 1
        println("remove($key key $index index)")
        if (index < keys.size && keys[index] == key) {
            if (isLeaf)
                removeFromLeaf(index)
            else
                removeFromNonLeaf(index)
        } else {
            if (isLeaf) {
                println("Key $key doesn't exist")
                return
            }

            if (children[index].keys.size < minimumKeys)
                fill(index)

            val flag = index == keys.size
            println("${children[index].keys.size} $index ${minimumKeys}")
            if (flag && index > keys.size)
                children[index - 1].remove(key)
            else
                children[index].remove(key)

            if (children[index].keys.size < minimumKeys)
                fill(index)
        }
    }

    private fun removeFromLeaf(index: Int) {
        println("removeFromLeaf($index index)")
        keys.removeAt(index)
    }

    private fun removeFromNonLeaf(index: Int) {
        println("removeFromNonLeaf($index index)")
        val value = keys[index]

        if (children[index].keys.size > maximumKeys / 2) {
            val predecessor = getPredecessor(index)
            keys[index] = predecessor
            children[index].remove(predecessor)
        } else if (children[index + 1].keys.size > maximumKeys / 2) {
            val subsequent = getSubsequent(index)
            keys[index] = subsequent
            children[index + 1].remove(subsequent)
        } else {
            // Если суммарно количество ключей ребёнка по index и index+1
            // не превышает maximumKeys, то в таком случае мы эти две страницы
            // объединяем в одну новую.
            merge(index)
            children[index].remove(value)
        }
    }

    private fun fill(index: Int) {
        println("fill($index index)")
        if (index != 0 && children[index - 1].keys.size >= minimumChildren)
            borrowFromPrev(index)
        else if (index != keys.size && children[index + 1].keys.size >= minimumChildren)
            borrowFromNext(index)
        else if (index != keys.size)
            merge(index)
        else
            merge(index - 1)
    }

    private fun borrowFromPrev(index: Int) {
        println("borrowFromPrev($index index)")
        val first = children[index]
        val second = children[index - 1]

        first.keys.add(0, keys[index - 1])
        if (!first.isLeaf)
            first.children.add(0, second.children.last())

        keys[index - 1] = second.keys.removeLast()
        if (!second.isLeaf)
            second.children.removeLast()
    }

    private fun borrowFromNext(index: Int) {
        println("borrowFromNext($index index)")
        val first = children[index]
        val second = children[index + 1]

        first.keys.add(keys[index])
        if (!first.isLeaf)
            first.children.add(second.children.first())

        keys[index] = second.keys.first()

        second.keys.removeFirst()
        if (!second.isLeaf)
            second.children.removeFirst()
    }

    private fun getPredecessor(index: Int): Int {
        println("getPredecessor($index index)")
        var current = children[index]
        while (current.isLeaf.not())
            current = current.children.last()
        return current.keys.last()
    }

    private fun getSubsequent(index: Int): Int {
        println("getSubsequent($index index)")
        var current = children[index + 1]
        while (current.isLeaf.not())
            current = current.children.first()
        return current.keys.first()
    }

    fun merge(index: Int) {
        println("merge($index index)")
        val first = children[index]
        val second = children[index + 1]

        keys.removeAt(index)

        repeat(second.keys.size) {
            first.keys.add(second.keys[it])
        }

        if (!first.isLeaf)
            repeat(second.children.size) {
                first.children.add(second.children[it])
            }

        children.removeAt(index + 1)
    }


    fun traverse(resultString: ArrayList<String>, depth: Int) {
        while (resultString.size <= depth)
            resultString.add("")

        resultString[depth] += "["
        keys.forEach {
            resultString[depth] += "$it | "
            resultString.forEachIndexed { index, s ->
                if (index != depth)
                    resultString[index] += " ".repeat("$it | ".length)
            }
        }
        resultString[depth] = resultString[depth].dropLast(3)
        resultString[depth] += "] "


        children.forEach {
            if (!isLeaf)
                it.traverse(resultString, depth + 1)
        }
    }
}