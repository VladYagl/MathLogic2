package vladyagl

open class Quantifier(name: String, val variable: Variable, val expression: Expression) : Expression(name, expression) {
    override fun nodeEquals(other: Any?): Boolean {
        return if (other is Quantifier) {
            super.nodeEquals(other) && variable == other.variable
        } else {
            false
        }
    }

    override fun nodeToString(level: Int): String {
        return "$name{$variable}"
    }
}