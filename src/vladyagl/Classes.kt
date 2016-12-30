package vladyagl

//BASE

class ParseException(message: String = "WTF", cause: Throwable? = null) : Exception(message, cause)

open class Predicate(name: String, vararg termArgs: Term) : Expression(name) {
    override val args = termArgs
}

open class Term(name: String, vararg termArgs: Term) : Predicate(name, *termArgs)

class Variable(name: String) : Term("__Variable[$name]__")

//FACTORIES

class ExpressionFactory(
        override val name: String,
        override val size: Int,
        override val symbol: String,
        override val leftPriority: Boolean = true) : OperatorFactory<Expression> {
    override fun create(vararg args: Expression): Expression {
        return Expression(name, *args)
    }
}

class TermFactory(
        override val name: String,
        override val size: Int,
        override val symbol: String,
        override val leftPriority: Boolean = true) : OperatorFactory<Term> {
    override fun create(vararg args: Expression): Term {
        return Term(name, *args.map { it as Term }.toTypedArray())
    }
}

// LOGIC

class Replaceable(name: String, val oldName: String? = null, val newName: String? = null) : Expression("__Replaceable[$name${if (!oldName.isNullOrEmpty()) "($oldName := $newName)" else ""}]__")

class Conjunction(left: Expression, right: Expression) : Expression("__Conjunction__", left, right)

class Disjunction(left: Expression, right: Expression) : Expression("__Disjunction__", left, right)

class Implication(left: Expression, right: Expression) : Expression("__Implication__", left, right)

class Negation(a: Expression) : Expression("__Negation__", a)

// QUANTIFIERS

class Universal(variable: Variable, expression: Expression) : Quantifier("__Universal__", variable, expression)

class Existential(variable: Variable, expression: Expression) : Quantifier("__Existential__", variable, expression)

// PREDICATES

class Equality(left: Term, right: Term) : Predicate("__Equality__", left, right)

// TERM

class Add(left: Term, right: Term) : Term("__Sum__", left, right)

class Multiply(left: Term, right: Term) : Term("__Multiply__", left, right)

class Stroke(term: Term) : Term("__Stroke__", term)

class Zero : Term("__Zero__")