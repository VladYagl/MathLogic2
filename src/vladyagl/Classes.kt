package vladyagl

//BASE

class ParseException(message: String = "WTF", cause: Throwable? = null) : Exception(message, cause)

open class Predicate(name: String, vararg termArgs: Term) : Expression(name) {
    override val args = termArgs
}

open class Term(name: String, vararg termArgs: Term) : Predicate(name, *termArgs)

// LOGIC

class Conjunction(val left: Expression, val right: Expression) : Expression("__Conjunction__", "&" ,left, right)

class Disjunction(val left: Expression, val right: Expression) : Expression("__Disjunction__", "|", left, right)

class Implication(val left: Expression, val right: Expression) : Expression("__Implication__", "->", left, right)

class Negation(val expression: Expression) : Expression("__Negation__", "-", expression)

// QUANTIFIERS

class Universal(variable: Variable, expression: Expression) : Quantifier("__Universal__", variable, expression)

class Existential(variable: Variable, expression: Expression) : Quantifier("__Existential__", variable, expression)

// PREDICATES

class Equality(val left: Term, val right: Term) : Predicate("__Equality__", left, right)

// TERM

class Addition(val left: Term, val right: Term) : Term("__Sum__", left, right)

class Multiplication(val left: Term, val right: Term) : Term("__Multiply__", left, right)

class Stroke(val term: Term) : Term("__Stroke__", term)

class Zero : Term("__Zero__")