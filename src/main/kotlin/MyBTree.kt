class MyBTree(val power: Int) {
    val minimumKeys = power
    val minimumChildren = power + 1
    val maximumKeys = power * 2
    val maximumChildren = maximumKeys + 1

    lateinit var root: MyBTreeNode

    fun print() {
        println("--------------------")
        val sb = arrayListOf<String>()
        root.print(sb, 0)
        sb.forEach { println(it) }
        println("--------------------")
        println()
    }

    fun search(key: Int) = if (this::root.isInitialized.not())
        null
    else
        root.search(key)

    fun insert(key: Int) {
        if (this::root.isInitialized.not()) {
            root = MyBTreeNode(power, this, true).apply {
                keys.add(key)
            }
        } else {
            root.insertNotFull(key)

            if (root.keys.size > maximumKeys)
                root = MyBTreeNode(power, this, false).apply {
                    children.add(root.apply { parentNode = this })
                    splitChild(0, root)
                }
        }
    }

    fun remove(key: Int): Boolean {
        val searchResult = search(key)

        if (searchResult == null)
            return false

        root.remove(key)

        if (root.keys.size == 0)
            if (root.isLeaf)
                this.javaClass.getDeclaredField("root").apply {
                    isAccessible = true
                    set("root", null)
                }
            else {
                root.children[0].parentNode = null
                root = root.children.first()
            }

        return true
    }
}