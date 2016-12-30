package vladyagl

import java.util.*

class ProofChecker(suppositionList: ArrayList<Expression>, val proof: ArrayList<Expression>) {
    private val axioms = arrayOf(
            ExpressionParser.parse("%A->%B->%A"),
            ExpressionParser.parse("(%A->%B)->(%A->%B->%C)->(%A->%C)"),
            ExpressionParser.parse("%A->%B->%A&%B"),
            ExpressionParser.parse("%A&%B->%A"),
            ExpressionParser.parse("%A&%B->%B"),
            ExpressionParser.parse("%A->%A|%B"),
            ExpressionParser.parse("%B->%A|%B"),
            ExpressionParser.parse("(%A->%C)->(%B->%C)->(%A|%B->%C)"),
            ExpressionParser.parse("(%A->%B)->(%A->!%B)->!%A"),
            ExpressionParser.parse("!!%A->%A"),

            ExpressionParser.parse("@x(%A)->(%A[x:=t])"),
            ExpressionParser.parse("(%A[x:=t])->?x(%A)"),

            ExpressionParser.parse("a=b->a'=b'"),
            ExpressionParser.parse("a=b->a=c->b=c"),
            ExpressionParser.parse("a'=b'->a=b"),
            ExpressionParser.parse("!a'=0"),
            ExpressionParser.parse("a+b'=(a+b)'"),
            ExpressionParser.parse("a+0=a"),
            ExpressionParser.parse("a*0=0"),
            ExpressionParser.parse("a*b'=a*b+a"),
            ExpressionParser.parse("(%A[x:=0])&@x(A->%A[x:=0])->(%A)"))

    private val suppositions : HashMap<String, Int> = HashMap()

    init {
        suppositionList.forEachIndexed { i, expression -> suppositions.put(expression.toString(), i) }

        println("Start Proofing")
        println(axioms.joinToString(separator = "\n\n"))
    }
}