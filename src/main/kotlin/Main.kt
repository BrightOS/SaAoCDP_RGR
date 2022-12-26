
import javax.swing.tree.TreeNode


var lastOperatedDepth = 0

internal class BTreeNode(
    var minimumDegree: Int, // Минимальное количество ключей
    var isLeaf: Boolean // Лист или нет
) {
    var keys: IntArray // Массив ключей
    var children: Array<BTreeNode?> // Массив потомков
    var currentSize: Int // Количество ключей в узле

    init {
        keys = IntArray(2 * minimumDegree) // Узел может иметь максимум 2t - 1 ключей
        children = arrayOfNulls(2 * minimumDegree + 1)
        currentSize = 0
    }

    // Индекс первого элемента, который равен или больше, чем ключ
    fun findKey(key: Int): Int {
        var index = 0
        while (index < currentSize && keys[index] < key)
            ++index
        return index
    }

    fun remove(key: Int) {
        val index = findKey(key)
        if (index < currentSize && keys[index] == key) {
            if (isLeaf)
                removeFromLeaf(index)
            else
                removeFromNonLeaf(index)
        } else {
            if (isLeaf) { // Если узел является листом, то нужный узел не находится в дереве B
                println("The key $key is does not exist in the tree")
                return
            }

            // В противном случае ключ, подлежащий удалению, существует в поддереве с узлом в качестве корня

            // Этот флаг указывает, существует ли ключ в поддереве, корень которого является последним дочерним элементом узла.
            // Когда index равен currentSize, сравнивается весь узел, и флаг имеет значение true
            val flag = index == currentSize
            if (children[index]!!.currentSize < minimumDegree) // Если потомок не полный, просто добавляем в него элемент
                fill(index)


            // Если последний дочерний узел был объединен, он должен был быть объединен с предыдущим дочерним узлом, поэтому мы выполняем рекурсию по дочернему узлу (index - 1).
            // В противном случае мы возвращаемся к дочернему узлу (index), который теперь имеет по крайней мере ключи минимальной степени
            if (flag && index > currentSize)
                children[index - 1]?.remove(key)
            else
                children[index]?.remove(key)
        }
    }

    fun removeFromLeaf(index: Int) {
        for (i in index + 1 until currentSize)
            keys[i - 1] = keys[i]
        currentSize--
    }

    fun removeFromNonLeaf(index: Int) {
        val key = keys[index]

        // Если поддерево перед ключом (children[index]) содержит не менее t ключей
        // Затем найдите предшественник 'pred' ключа в поддереве children[index] в качестве корня
        // Замените ключ на 'pred', рекурсивно удалите pred в children[index]
        if (children[index]!!.currentSize >= minimumDegree) {
            val pred = getPredecessor(index)
            keys[index] = pred
            children[index]?.remove(pred)
        } else if (children[index + 1]!!.currentSize >= minimumDegree) {
            val succ = getSubsequent(index)
            keys[index] = succ
            children[index + 1]?.remove(succ)
        } else {
            // Если количество ключей children[index] и children[index+1] меньше minimumDegree,
            // То ключ и children[index+1] объединяются в children[index]
            // И теперь children[index] содержит 2t-1 ключей.
            // Освобождаем children[index+1], рекурсивно удаляем этот ключ из children[index]
            merge(index)
            children[index]?.remove(key)
        }
    }

    fun getPredecessor(index: Int): Int { // Узел-предшественник — это узел, который всегда находит самый правый узел из левого поддерева.

        // Двигаемся к крайнему правому узлу, пока не дойдем до конечного узла.
        var cur = children[index]!!
        while (!cur.isLeaf)
            cur = cur.children[cur.currentSize]!!
        return cur.keys[cur.currentSize - 1]
    }

    fun getSubsequent(index: Int): Int { // Последующие узлы находятся от правого поддерева до левого.

        // Продолжаем перемещать крайний левый узел от children[index+1] до тех пор, пока он не достигнет конечного узла.
        var cur = children[index + 1]!!
        while (!cur.isLeaf) cur = cur.children[0]!!
        return cur.keys[0]
    }

    // Заполнение children[index] ключами меньше, чем minimumDegree
    fun fill(index: Int) {

        // Если предыдущий дочерний узел имеет несколько ключей MinimumDegree-1, заимствуем его
        if (index != 0 && children[index - 1]!!.currentSize >= minimumDegree)
            borrowFromPrev(index)
        else if (index != currentSize && children[index + 1]!!.currentSize >= minimumDegree)
            borrowFromNext(index)
        else {
            if (index != currentSize)
                merge(index)
            else
                merge(index - 1)
        }
    }

    // Берём ключ из children[index-1] и вставляем его в children[index]
    fun borrowFromPrev(index: Int) {
        val child = children[index]!!
        val neighbor = children[index - 1]!!

        for (i in child.currentSize - 1 downTo 0)
            child.keys[i + 1] = child.keys[i]
        if (!child.isLeaf)
            for (i in child.currentSize downTo 0)
                child.children[i + 1] = child.children[i]

        // Заменяем первый ключ текущего узла на ключ на позиции [index-1]
        child.keys[0] = keys[index - 1]
        if (!child.isLeaf)
            child.children[0] = neighbor.children[neighbor.currentSize]

        keys[index - 1] = neighbor.keys[neighbor.currentSize - 1]
        child.currentSize += 1
        neighbor.currentSize -= 1
    }

    // То же самое, но из следующего
    fun borrowFromNext(index: Int) {
        val child = children[index]!!
        val sibling = children[index + 1]!!

        child.keys[child.currentSize] = keys[index]

        if (!child.isLeaf)
            child.children[child.currentSize + 1] = sibling.children[0]

        keys[index] = sibling.keys[0]

        for (i in 1 until sibling.currentSize)
            sibling.keys[i - 1] = sibling.keys[i]

        if (!sibling.isLeaf)
            for (i in 1..sibling.currentSize)
                sibling.children[i - 1] = sibling.children[i]

        child.currentSize += 1
        sibling.currentSize -= 1
    }

    // Сливаем children[index+1] с children[index]
    fun merge(index: Int) {
        val child = children[index]!!
        val sibling = children[index + 1]!!

        // Вставляем последний ключ текущего узла в позицию MinimumDegree-1 дочернего узла.
        child.keys[minimumDegree - 1] = keys[index]

        // Копируем ключи из children[index+1] в children[index]
        for (i in 0 until sibling.currentSize)
            child.keys[i + minimumDegree] = sibling.keys[i]

        // Копируем детей children[index+1] в children[index]
        if (!child.isLeaf)
            for (i in 0..sibling.currentSize)
                child.children[i + minimumDegree] = sibling.children[i]

        // Перемещаем остаток ключей без пробела
        for (i in index + 1 until currentSize)
            keys[i - 1] = keys[i]

        // Двигаем оставшиеся узлы
        for (i in index + 2..currentSize)
            children[i - 1] = children[i]
        child.currentSize += sibling.currentSize + 1
        currentSize--
    }

    fun insertNotFull(key: Int) {
        var i = currentSize - 1
        if (isLeaf) {
            // Ищем место, куда следует вставить новый ключ.
            while (i >= 0 && keys[i] > key) {
                keys[i + 1] = keys[i] // Двигаем ключи
                i--
            }
            keys[i + 1] = key
            currentSize = currentSize + 1
        } else {
            // Ищем, куда вставить элемент
            while (i >= 0 && keys[i] > key) i--
            if (children[i + 1]!!.currentSize == 2 * minimumDegree - 1) { // Если узел переполнен
                splitChild(i + 1, children[i + 1]!!)
                // После разделения ключ в середине дочернего узла перемещается вверх, и дочерний узел разделяется на два
                if (keys[i + 1] < key) i++
            }
            children[i + 1]?.insertNotFull(key)
        }
    }

    fun splitChild(index: Int, srcNode: BTreeNode) {

        val tempNode = BTreeNode(srcNode.minimumDegree, srcNode.isLeaf)
        tempNode.currentSize = minimumDegree - 1

        // Перемещаем свойства во временную ноду
        for (j in 0 until minimumDegree - 1)
            tempNode.keys[j] = srcNode.keys[j + minimumDegree]

        if (!srcNode.isLeaf)
            for (j in 0 until minimumDegree)
                tempNode.children[j] = srcNode.children[j + minimumDegree]
        srcNode.currentSize = minimumDegree - 1

        // Вставляем временную ноду
        for (j in currentSize downTo index + 1)
            children[j + 1] = children[j]
        children[index + 1] = tempNode

        // Перемещаем ключ из src ноды в текущую ноду
        for (j in currentSize - 1 downTo index)
            keys[j + 1] = keys[j]
        keys[index] = srcNode.keys[minimumDegree - 1]
        currentSize++
    }

//    fun print(buffer: StringBuilder, prefix: String, childrenPrefix: String) {
//        buffer.append(prefix)
//        buffer.append("[")
//        repeat(currentSize) {
//            buffer.append("${keys[it]} | ")
//        }
//        buffer.deleteRange(buffer.length - 3, buffer.length)
//        buffer.append("]")
//        buffer.append('\n')
//        val it: Iterator<BTreeNode?> = children.toMutableList().apply {
//            removeIf { it == null }
//        }.iterator()
//        while (it.hasNext()) {
//            val next = it.next()
//            if (it.hasNext()) {
//                next?.print(buffer, "$childrenPrefix├── ", "$childrenPrefix│   ")
//            } else {
//                next?.print(buffer, "$childrenPrefix└── ", "$childrenPrefix    ")
//            }
//        }
//    }

    fun traverse(sb: ArrayList<String>, depth: Int) {
        var i = 0

//        sb.append("\n")
//        repeat(depth) {
//            sb.append("\t")
//        }

        while (sb.size <= depth) {
            sb.add("")
        }

        sb[depth] += "["
        repeat(currentSize) {
            sb[depth] += "${keys[it]} | "
            sb.forEachIndexed { index, s ->
                if (index != depth)
                    sb[index] += " ".repeat("${keys[it]} | ".length)
            }
        }
        sb[depth] = sb[depth].removeRange(sb[depth].length - 3, sb[depth].length)
        sb[depth] += "] "


        while (i < currentSize) {
            if (!isLeaf)
                children[i]?.traverse(sb, depth + 1)
            i++
        }

        if (!isLeaf)
            children[i]?.traverse(sb, depth + 1)
    }

    fun search(key: Int): BTreeNode? {
        var i = 0
        while (i < currentSize && key > keys[i]) i++
        if (keys[i] == key) return this
        return if (isLeaf) null else children[i]?.search(key)
    }
}

internal class BTree(var minimumDegree: Int) {
    var root: BTreeNode? = null

    fun traverse() {
//        val buffer = java.lang.StringBuilder(100)
//        root?.print(buffer, "", "")
//        println(buffer.toString())
        lastOperatedDepth = 0
        println("--------------------")
        val sb = arrayListOf<String>()
        root?.traverse(sb, 0)
        sb.forEach { println(it) }
        println("--------------------")
    }

    fun search(key: Int) = if (root == null)
        null
    else
        root!!.search(key)

    fun insert(key: Int) {
        if (root == null) {
            root = BTreeNode(minimumDegree, true).apply {
                keys[0] = key
                currentSize = 1
            }
        } else {
            // Если корневой узел заполнен, увеличиваем высоту дерева
            if (root!!.currentSize == 2 * minimumDegree - 1) {
                val s = BTreeNode(minimumDegree, false)
                // Старый корневой узел становится потомком нового корневого узла.
                s.children[0] = root
                // Разделяем старую рут ноду
                s.splitChild(0, root!!)
                // Новая рут нода имеет 2 дочерних узла. Переносим старый рут туда
                var i = 0
                if (s.keys[0] < key) i++
                s.children[i]?.insertNotFull(key)
                root = s
            } else root!!.insertNotFull(key)
        }
        traverse()
    }

    fun remove(key: Int) {
        if (root == null) {
            println("The tree is empty")
            return
        }
        root!!.remove(key)
        // Аккуратно удаляем ноду
        if (root!!.currentSize == 0)
            root = if (root!!.isLeaf) null else root!!.children[0]
    }
}

fun main() {
    val t = MyBTree(2)
    t.insert(1)
    t.insert(3)
    t.insert(7)
    t.insert(10)
    t.insert(11)
    t.insert(13)
    t.insert(14)
    t.insert(15)
    t.insert(18)
    t.insert(16)
    t.insert(33)
    t.insert(34)
    t.insert(29)
    t.insert(19)
    t.insert(24)
    t.insert(25)
    t.insert(32)
    t.insert(26)
    t.insert(21)
    t.insert(4)
    t.insert(5)
    t.insert(20)
    t.insert(22)
    t.insert(2)
    t.insert(31)
    t.insert(17)
    t.insert(12)
    t.insert(6)
    t.insert(30)

    println("Traversal of tree constructed is")
    t.traverse()

    listOf(1, 6, 16, 25, 10, 17, 26, 101).forEach {
        println("Number $it ${if (t.search(it) != null) "found" else "not found"}")
    }

    listOf(6, 13, 7, 4, 2, 16, 24).forEach {
        t.remove(it)
        println("Traversal of tree after removing $it")
        t.traverse()
    }
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