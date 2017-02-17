package vladyagl

import java.util.*

open class Quantifier(name: String, val variable: Variable, val expression: Expression) : Expression(name, null, expression) {

    override fun getFreeVariables(): Set<String> {
        return expression.getFreeVariables() - variable.varName
    }

    override fun isFreeToSubstitute(other: Expression, varName: String): Boolean {
        return !(varName != variable.varName &&
                expression.getFreeVariables().contains(varName) &&
                other.getFreeVariables().contains(variable.varName))
    }

    override fun substitute(other: Expression, varName: String): Expression {
        if (varName != variable.varName) {
            //We loose our class type (such as Universal or Existential) but it still better then commented version
            return Quantifier(name, variable, expression.substitute(other, varName))
            //return = this.javaClass.getConstructor(Variable::class.java, Expression::class.java)
            //        .newInstance(variable, expression.substitute(other, varName))
        } else {
            return this
        }
    }

    override fun nodeEquals(other: Expression, variableMap: HashMap<String, String>): Boolean {
        return if (other is Quantifier) {
            val saveMatch = variableMap[variable.varName]
            variableMap[variable.varName] = other.variable.varName
            val result = expression.treeEquals(other.expression, variableMap)
            if (saveMatch == null) {
                variableMap.remove(variable.varName)
            } else {
                variableMap[variable.varName] = saveMatch
            }
            result
        } else {
            false
        }
    }

    override fun nodeToString(level: Int): String {
        return "$name{$variable}"
    }
}