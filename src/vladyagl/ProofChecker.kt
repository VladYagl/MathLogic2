package vladyagl

import java.util.*

fun HashMap<String, Int>.add(expression: Expression) {
    this.put(expression.toString(), this.size)
}

class ProofChecker {
    private val axiomSchemas = arrayOf(
            ExpressionParser.parse("%A->%B->%A"),
            ExpressionParser.parse("(%A->%B)->(%A->%B->%C)->(%A->%C)"),
            ExpressionParser.parse("%A->%B->%A&%B"),
            ExpressionParser.parse("%A&%B->%A"),
            ExpressionParser.parse("%A&%B->%B"),
            ExpressionParser.parse("%A->%A|%B"),
            ExpressionParser.parse("%B->%A|%B"),
            ExpressionParser.parse("(%A->%C)->(%B->%C)->(%A|%B->%C)"),
            ExpressionParser.parse("(%A->%B)->(%A->!%B)->!%A"),
            ExpressionParser.parse("!!%A->%A"))

    private val axioms = arrayOf(
            ExpressionParser.parse("a=b->a'=b'"),
            ExpressionParser.parse("a=b->a=c->b=c"),
            ExpressionParser.parse("a'=b'->a=b"),
            ExpressionParser.parse("!a'=0"),
            ExpressionParser.parse("a+b'=(a+b)'"),
            ExpressionParser.parse("a+0=a"),
            ExpressionParser.parse("a*0=0"),
            ExpressionParser.parse("a*b'=a*b+a"))

    private val suppositions: HashMap<String, Int> = HashMap()
    private val proofed: HashMap<String, Int> = HashMap()
    private val proofedList: ArrayList<Expression> = ArrayList()

    fun checkAxiomSchema(axiom: Expression,
                         expression: Expression,
                         replaceableMap: HashMap<String, Expression> = HashMap()): Boolean {
        if (axiom is Replaceable) {
            if (replaceableMap.containsKey(axiom.varName)) {
                return replaceableMap[axiom.varName] == expression
            } else {
                replaceableMap[axiom.varName] = expression
                return true
            }
        } else if (axiom.nodeEquals(expression)) {
            return axiom.args.mapIndexed { i, axiomArg ->
                checkAxiomSchema(axiomArg, expression.args[i], replaceableMap)
            }.reduce(Boolean::and)
        } else {
            return false
        }
    }

    fun checkQuantifierAxiom(expression: Expression): Boolean {
        if (expression !is Implication) return false

        fun check(expression: Expression, quant: Quantifier): Boolean {
            val phi = quant.expression
            val theta = (expression - phi ?: return true) as? Term ?: return false
            val varName = quant.variable.varName
            return if (phi.isFreeToSubstitute(theta, varName)) {
                phi.substitute(theta, varName).toString() == expression.toString()
            } else throw SubstituteException(varName, theta, phi)
        }

        val left = expression.left
        val right = expression.right
        return (right is Existential && check(left, right)) || (left is Universal && check(right, left))
    }

    fun checkInductionAxiom(expression: Expression): Boolean {
        if (expression !is Implication) return false
        try {
            val psi = expression.right
            val conjunction = expression.left as Conjunction
            val base = conjunction.left
            val stepQuant = conjunction.right as Universal
            (base - psi) as Zero
            val variable = (psi - base) as Variable
            val step = stepQuant.expression as Implication
            return if (psi.substitute(Zero(), variable.varName).toString() != base.toString()) false
            else !(psi.toString() != step.left.toString() ||
                    psi.substitute(Stroke(variable), variable.varName).toString() != step.right.toString())
        } catch (e: ClassCastException) {
            return false
        }
    }

    fun isAxiom(expression: Expression): Boolean {
        return axiomSchemas.map {
            checkAxiomSchema(it, expression)
        }.reduce(Boolean::or) || axioms.map {
            it == expression
        }.reduce(Boolean::or) ||
                checkQuantifierAxiom(expression) ||
                checkInductionAxiom(expression)
    }

    fun checkQuantifierRule(expression: Expression, freeVariables: Set<String>?): Boolean {
        if (expression !is Implication) return false

        fun check(expression: Expression, quant: Quantifier, impl: Expression): Boolean =
                proofed.containsKey(impl.toString()) &&
                        if (freeVariables?.contains(quant.variable.varName) ?: false)
                            throw QuantifierRuleVariableException(quant.variable.varName)
                        else if (expression.getFreeVariables().contains(quant.variable.varName))
                            throw FreeVariableException(quant.variable.varName, impl)
                        else
                            true


        val left = expression.left
        val right = expression.right
        return (right is Universal && check(left, right, Implication(left, right.expression))) ||
                (left is Existential && check(right, left, Implication(left.expression, right)))
    }

    fun checkModusPonens(expression: Expression): Expression? {
        if (proofedList.isEmpty()) return null
        return proofedList.find {
            if (it !is Implication)
                false
            else
                proofed.contains(it.left.toString()) && it.right.toString() == expression.toString()
        }
    }

    fun processForAllRule(proofLine: Expression, alpha: Expression, printExpression: (Expression) -> Unit) {
        val suppositions: ArrayList<Expression> = ArrayList()
        val proof: ArrayList<Expression> = ArrayList()
        val phi = proofLine.args[0]
        val universal = proofLine.args[1] as Universal
        val conjunction = Conjunction(alpha, phi)
        val psi = universal.expression

        suppositions.add(Implication(alpha, Implication(phi, psi)))             // alpha -> phi -> psi
        suppositions.add(conjunction)                                           // alpha & phi

        proof.add(conjunction)                                                  // alpha & phi
        proof.add(axiomSchemas[3].substitute(alpha, "A").substitute(phi, "B"))  // alpha & phi -> alpha
        proof.add(axiomSchemas[4].substitute(alpha, "A").substitute(phi, "B"))  // alpha & phi -> phi
        proof.add(alpha)                                                        // alpha
        proof.add(phi)                                                          // phi
        proof.add(Implication(alpha, Implication(phi, psi)))                    // alpha -> phi -> psi
        proof.add(Implication(phi, psi))                                        // phi -> psi
        proof.add(psi)                                                          // psi

        ProofChecker().check(suppositions, proof, psi, printExpression)         // alpha & phi -> psi
        suppositions.clear()
        proof.clear()

        val forAllRule = Implication(conjunction, universal)                    // alpha & phi -> @x psi
        val axiom = axiomSchemas[2].substitute(alpha, "A").substitute(phi, "B") // alpha -> phi -> alpha & phi
        printExpression(forAllRule)

        suppositions.add(forAllRule)
        suppositions.add(alpha)
        suppositions.add(phi)

        proof.add(alpha)                                                        // alpha
        proof.add(phi)                                                          // phi
        proof.add(axiom)                                                        // alpha -> phi -> alpha & phi
        proof.add(axiom.args[1])                                                // phi -> alpha & phi
        proof.add(axiom.args[1].args[1])                                        // alpha & phi
        proof.add(forAllRule)                                                   // alpha & phi -> @x psi
        proof.add(universal)                                                    // @x psi

        val newProof = ArrayList<Expression>()
        ProofChecker().check(suppositions, proof, universal) { newProof.add(it) }
        ProofChecker().check(suppositions.dropLast(1), newProof, proofLine, printExpression)
    }

    fun processExistRule(proofLine: Expression, alpha: Expression, printExpression: (Expression) -> Unit) {
        val suppositions: ArrayList<Expression> = ArrayList()
        val proof: ArrayList<Expression> = ArrayList()
        val phi = proofLine.args[1]
        val existential = proofLine.args[0] as Existential
        val psi = existential.expression
        val implication = Implication(alpha, Implication(psi, phi))             // alpha -> psi -> phi

        suppositions.add(implication)
        suppositions.add(psi)
        suppositions.add(alpha)

        proof.add(alpha)                                                        // alpha
        proof.add(psi)                                                          // psi
        proof.add(implication)                                                  // alpha -> psi -> phi
        proof.add(implication.right)                                            // psi -> phi
        proof.add(phi)                                                          // phi

        val newProof = ArrayList<Expression>()
        ProofChecker().check(suppositions, proof, phi) { newProof.add(it) }     // alpha -> phi
        ProofChecker().check(suppositions.dropLast(1), newProof, Implication(alpha, phi), printExpression)   // psi -> alpha -> phi
        suppositions.clear()
        proof.clear()
        newProof.clear()

        val existRule = Implication(existential, Implication(alpha, phi))       // ?x psi -> alpha -> phi
        printExpression(existRule)                                              // ?x psi -> alpha -> phi

        suppositions.add(existRule)                                             // ?x psi -> alpha -> phi
        suppositions.add(alpha)                                                 // alpha
        suppositions.add(existential)                                           // ?x psi

        proof.add(alpha)                                                        // alpha
        proof.add(existential)                                                  // ?x psi
        proof.add(existRule)                                                    // ?x psi -> alpha -> phi
        proof.add(existRule.right)                                              // alpha -> phi
        proof.add(phi)                                                          // phi

        ProofChecker().check(suppositions, proof, phi) { newProof.add(it) }
        ProofChecker().check(suppositions.dropLast(1), newProof, proofLine, printExpression)
    }

    fun processLine(proofLine: Expression, suppositions: Map<String, Int>, alpha: Expression?, println: (Expression) -> Unit): Pair<Boolean, String?> {
        try {
            val modusPonens = checkModusPonens(proofLine)
            val result = if (isAxiom(proofLine)) {
                println(proofLine)
                alpha?.let {
                    val axiom = axiomSchemas[0].substitute(proofLine, "A").substitute(alpha, "B")
                    println(axiom)          // proofLine -> alpha -> proofLine
                    println(axiom.args[1])  // alpha -> proofLine
                }
                true
            } else if (suppositions.contains(proofLine.toString())) {
                alpha?.let {
                    if (alpha.toString() == proofLine.toString()) {
                        val axiomAAA = axiomSchemas[0].substitute(alpha, "A").substitute(alpha, "B")
                        val axiomAAAA = axiomSchemas[0].substitute(alpha, "A").substitute(Implication(alpha, alpha), "B")
                        val axiom = axiomSchemas[1].substitute(alpha, "A").substitute(Implication(alpha, alpha), "B").substitute(alpha, "C")
                        println(axiomAAA)               // alpha -> alpha -> alpha
                        println(axiomAAAA)              // alpha -> (alpha -> alpha) -> alpha
                        println(axiom)                  // (alpha -> (alpha -> alpha)) -> (alpha -> (alpha -> alpha) -> alpha) -> (alpha -> alpha)
                        println(axiom.args[1])          // (alpha -> (alpha -> alpha) -> alpha) -> (alpha -> alpha)"
                        println(axiom.args[1].args[1])  // (alpha -> alpha)
                    } else {
                        val axiom = axiomSchemas[0].substitute(proofLine, "A").substitute(alpha, "B")
                        println(proofLine)
                        println(axiom)          // proofLine -> alpha -> proofLine
                        println(axiom.args[1])  // alpha -> proofLine
                    }
                } ?:
                        println(proofLine)
                true
            } else if (modusPonens != null) {
                alpha?.let {
                    val axiom = axiomSchemas[1].substitute(alpha, "A").substitute(modusPonens.args[0], "B").substitute(proofLine, "C")
                    println(axiom)                  // (alpha -> modusPonens) -> (alpha -> modusPonens -> proofLine) -> (alpha -> proofLine)
                    println(axiom.args[1])          // (alpha -> modusPonens -> proofLine) -> (alpha -> proofLine)
                    println(axiom.args[1].args[1])  // (alpha -> proofLine)
                } ?:
                        println(proofLine)
                true
            } else if (checkQuantifierRule(proofLine, alpha?.getFreeVariables())) {
                alpha?.let {
                    if (proofLine.args[1] is Universal) {
                        processForAllRule(proofLine, alpha, println)
                    } else {
                        processExistRule(proofLine, alpha, println)
                    }
                } ?:
                        println(proofLine)
                true
            } else false
            return Pair(result, null)
        } catch (e: QuantifierRuleVariableException) {
            return Pair(false, "используется правило с квантором по переменной <${e.varName}>, свободно входящей в допущение. ${alpha!!}")
        } catch (e: FreeVariableException) {
            return Pair(false, "переменная <${e.varName}> входит свободно в формулу ${e.expression}.")
        } catch (e: SubstituteException) {
            return Pair(false, "терм <${e.term}> не свободен для подстановки в формулу <${e.expression}> вместо переменной <${e.varName}>.")
        } catch (e: SubstituteError) {
            e.printStackTrace(System.out)
            return Pair(false, "Ошибка подстановки. ${e.varName}  -->  ${e.expression}")
        } catch (e: Exception) {
            e.printStackTrace(System.out)
            return Pair(false, "Неизвестная ошибка")
        }
    }

    fun check(suppositionList: List<Expression>, proof: List<Expression>, expression: Expression, printExpression: (Expression) -> Unit): Boolean {
        suppositions.clear()
        proofed.clear()
        proofedList.clear()
        suppositionList.forEach { suppositions.add(it) }

        val alpha = suppositionList.lastOrNull()

        run loop@ {
            proof.forEachIndexed { i, proofLine ->
                val (result, errorMessage) = processLine(proofLine, suppositions, alpha, printExpression)
                if (result) {
                    proofed.put(proofLine.toString(), i + 1)
                    proofedList.add(proofLine)
                } else {
                    println("Вывод некорректен начиная с формулы номер ${i + 1} ${if (errorMessage != null) ": " + errorMessage else ""}")
                    return@loop
                }
            }
            if (proof.isNotEmpty() && proof.last() != expression) {
                println("Последняя строчка не совпадает с доказуемым.")
            }
            return true
        }
        return false
    }
}