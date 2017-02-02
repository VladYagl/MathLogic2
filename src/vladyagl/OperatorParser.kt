package vladyagl

class OperatorParser<S : Expression>(val operators: List<OperatorCreator<S>>, val parseToken: (String) -> S) {

    operator fun Int.plus(other: Boolean) : Int {
        return if (other) this.plus(1) else plus(0)
    }

    private fun parse(text: String, level: Int): S {
        if (level >= operators.size) {
            return parseToken(text)
        }
        val type = operators[level]
        var position = -1
        var balance = 0
        for (i in 0..text.length - 1) {
            val c = text[i]
            if (c == '(') balance++
            if (c == ')') balance--
            if (balance == 0 && c == type.symbol[0]) {
                position = i
                if (!type.leftPriority) {
                    break
                }
            }
        }
        if (position == -1) {
            return parse(text, level + 1)
        } else {
            try {
                val first : S = parse(text.substring(0, position), level + !type.leftPriority)
                val second : S = parse(text.substring(position + operators[level].symbol.length), level + type.leftPriority)
                return operators[level].create(first, second)
            } catch (e: Exception) {
                throw ParseException("Error while parsing: " + text, e)
            }
        }
    }

    fun parse(text: String) : S {
        return parse(text, 0)
    }
}