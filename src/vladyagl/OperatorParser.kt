package vladyagl

class OperatorParser<out T: Expression>(val operators: List<OperatorFactory<T>>, val parseToken: (String) -> T) {
    private fun parse(text: String, level: Int): T {
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
                val first : T = parse(text.substring(0, position), level + 1)
                val second : T = parse(text.substring(position + operators[level].symbol.length), level)
                return operators[level].create(first, second)
            } catch (e: Exception) {
                throw ParseException("Error while parsing: " + text, e)
            }
        }
    }

    fun parse(text: String) : T {
        return parse(text, 0)
    }
}