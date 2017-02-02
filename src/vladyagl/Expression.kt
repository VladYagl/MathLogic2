package vladyagl

import java.util.*

@Suppress("EqualsOrHashCode")
open class Expression(val name: String, vararg argsTemp: Expression) {
    val TAB = "   "

    //Returns first not equal node
    operator fun minus(other: Expression) : Expression? {
        return if (name == other.name) {
            args.mapIndexed { i, expression ->
                expression - other.args[i]
            }.firstOrNull{ it != null }
        } else {
            return this
        }
    }

    open val args = argsTemp

    open fun getFreeVariables(): Set<String> {
        return if (args.isEmpty()) HashSet()
        else args.map(Expression::getFreeVariables).reduce(Set<String>::union)
    }

    open fun isFreeToSubstitute(other: Expression, varName: String): Boolean {
        return args.isEmpty() || args.map { it.isFreeToSubstitute(other, varName) }.reduce(Boolean::and)
    }

    //Guaranties same toString but not same types
    open fun substitute(other: Expression, varName: String) : Expression {
        return Expression(name, *args.map{ it.substitute(other, varName)}.toTypedArray())
    }

    fun treeEquals(other: Expression, variableMap: HashMap<String, String> = HashMap()): Boolean {
        return nodeEquals(other, variableMap) && (args.isEmpty() || args.mapIndexed { i, expression ->
            expression.treeEquals(other.args[i])
        }.reduce(Boolean::and))
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Expression) {
            treeEquals(other)
        } else {
            false
        }
    }

    open fun nodeEquals(other: Expression, variableMap: HashMap<String, String> = HashMap()): Boolean {
        return other is Expression && name == other.name
    }

    override fun toString(): String {
        return toStringTree(0)
    }

    private fun toStringTree(level: Int): String {
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