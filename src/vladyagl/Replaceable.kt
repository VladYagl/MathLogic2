package vladyagl

import java.util.*

open class Replaceable(val varName: String) : Term("__Replaceable[$varName]__") {
    override fun nodeEquals(other: Expression, variableMap: HashMap<String, String>): Boolean {
        throw NotImplementedError()
    }

    override fun getFreeVariables(): Set<String> {
        throw NotImplementedError()
    }

    override fun isFreeToSubstitute(other: Expression, varName: String): Boolean {
        return true
    }

    override fun substitute(other: Expression, varName: String): Expression {
        throw NotImplementedError()
    }
}