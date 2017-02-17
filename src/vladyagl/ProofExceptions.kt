package vladyagl

open class ProofException(message: String = "") : Exception(message)

class QuantifierRuleVariableException(val varName: String) : ProofException()

class FreeVariableException(val varName: String, val expressionNumber: Int) : ProofException()

class SubstituteException(val varName: String) : ProofException()