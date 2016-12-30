package vladyagl

object ExpressionParser {
    private val logicOperators = listOf(
            ExpressionFactory("__Implication__", size = 2, symbol = "->", leftPriority = false),
            ExpressionFactory("__Disjunction__", size = 2, symbol = "|"),
            ExpressionFactory("__Conjunction__", size = 2, symbol = "&")
    )

    private val terms = listOf(
            TermFactory("__Sum__", size = 2, symbol = "+"),
            TermFactory("__Multiply__", size = 2, symbol = "*")
    )

    private fun parseTermUnary(text: String): Term {
        val token = text.trim()
        when {
            token.takeLast(1) == "'" -> return Stroke(termParser.parse(token.dropLast(1)))
            token[0] == '(' -> return termParser.parse(token.substring(1, token.length - 1))
            token.takeLast(1) == ")" -> {
                val name = token.substringBefore('(')
                val arguments = token.dropLast(1).substringAfter('(').split(",")
                return Term(name, *arguments.map { termParser.parse(it) }.toTypedArray())
            }
            token == "0" -> return Zero()
            else -> return Variable(token)
        }
    }

    private fun parsePredicate(text: String): Predicate {
        if (text.contains('=')) {
            return Equality(termParser.parse(text.substringBefore('=')), termParser.parse(text.substringAfter('=')))
        } else {
            val name = text.substringBefore('(')
            val arguments = text.substringAfter('(').dropLast(1).split(",")
            return Predicate(name, *arguments.map { termParser.parse(it) }.toTypedArray())
        }
    }

    private fun parseLogicUnary(text: String): Expression {
        val token = text.trim()
        when (token[0]) {
            '%' -> if (token.contains("["))
                return Replaceable(
                        token.drop(1).substringBefore("["),
                        token.drop(1).substringAfter("[").substringBefore(":="),
                        token.drop(1).substringAfter(":=").substringBefore("]"))
            else
                return Replaceable(token.drop(1))
            '!' -> return Negation(parseLogicUnary(token.drop(1)))
            '(' -> return logicParser.parse(token.substring(1, token.length - 1))
            '?', '@' -> {
                val position = token.drop(1).indexOfFirst { !it.isLowerCase() && !it.isDigit() }
                val variable = Variable(token.drop(1).take(position))
                val args = parseLogicUnary(token.drop(position + 1))
                return if (token[0] == '?') Existential(variable, args) else Universal(variable, args)
            }
            else -> return parsePredicate(token)
        }
    }

    private val logicParser = OperatorParser(logicOperators, { parseLogicUnary(it) })
    private val termParser = OperatorParser(terms, { parseTermUnary(it) })

    internal fun parse(text: String): Expression {
        //text = text.replace(" |\t|\r".toRegex(), "")
        return logicParser.parse(text)
    }
}
