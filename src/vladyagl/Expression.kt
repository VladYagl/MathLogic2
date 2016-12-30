package vladyagl

import java.util.*

@Suppress("EqualsOrHashCode")
open class Expression(val name: String, vararg argsTemp: Expression) {
    val TAB = "   "

    open val args = argsTemp

    override fun equals(other: Any?): Boolean {
        return if (other is Expression) {
            nodeEquals(other) && Arrays.equals(args, other.args)
        } else {
            false
        }
    }

    open fun nodeEquals(other: Any?): Boolean {
        return other is Expression && name == other.name
    }

    override fun toString(): String {
        return toStringTree(0)
    }

    fun toStringTree(level: Int): String {
        val tabs = TAB.repeat(level)
        return if (args.isNotEmpty()) args.joinToString(
                separator = ",\n",
                prefix = tabs + nodeToString(level) + "(\n",
                postfix = "\n$tabs)") { it.toStringTree(level + 1) }
        else tabs + nodeToString(level)
    }

    open protected fun nodeToString(level: Int): String {
        return name
    }
}
