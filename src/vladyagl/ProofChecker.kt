package vladyagl

import java.util.*

class ProofChecker(suppositionList: ArrayList<Expression>, val proof: ArrayList<Expression>) {
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
            val theta = expression - phi ?: return true
            val varName = quant.variable.varName
            return if (phi.isFreeToSubstitute(theta, varName)) {
                phi.substitute(theta, varName).toString() == expression.toString()
            } else false
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

    fun checkQuantifierRule(expression: Expression): Boolean {
        if (expression !is Implication) return false

        fun check(expression: Expression, quant: Quantifier, impl: Expression): Boolean =
                !expression.getFreeVariables().contains(quant.variable.varName) &&
                        proofed.containsKey(impl.toString())

        val left = expression.left
        val right = expression.right
        return (right is Universal && check(left, right, Implication(left, right.expression))) ||
                (left is Existential && check(right, left, Implication(left.expression, right)))
    }

    fun checkModusPonens(expression: Expression): Boolean {
        if (proofedList.isEmpty()) return false
        return proofedList.map {
            if (it !is Implication)
                false
            else
                proofed.contains(it.left.toString()) && it.right.toString() == expression.toString()
        }.reduce(Boolean::or)
    }

    init {
        suppositionList.forEachIndexed { i, expression -> suppositions.put(expression.toString(), i) }

        println("Start Proofing")
        //println(axiomSchemas.joinToString(separator = "\n\n"))

        run loop@ {
            proof.forEachIndexed { i, proofLine ->
                if (isAxiom(proofLine) ||
                        suppositions.contains(proofLine.toString()) ||
                        checkQuantifierRule(proofLine) ||
                        checkModusPonens(proofLine)) {
                    proofed.put(proofLine.toString(), i)
                    proofedList.add(proofLine)
                } else {
                    println("OLOLO YOU ARE NOT CORRECT AT: {$i}")
                    return@loop
                }
                if (i % 100 == 0) {
                    println("HO HO HO I'VE CHECKED $i LINES")
                }
            }
        }
        println("WTF YOU ARE RIGHT")
    }
}