package vladyagl

import java.io.*
import java.util.*


fun checkCorrectBrackets(text: String): Boolean {
    var balance = 0
    text.forEach {
        if (it == '(') balance++
        if (it == ')') balance--
        if (balance < 0) return false
    }
    return balance == 0
}

fun String.parse(): Expression? {
    if (this.isBlank()) return null
    return ExpressionParser.parse(this)
}

fun processFile(file: File, writer: Writer) {
    val suppositions = ArrayList<Expression>()
    val proof = ArrayList<Expression>()
    var expression: Expression? = null
    try {
        BufferedReader(FileReader(file)).use { reader ->
            val headerLine = reader.readLine().split("|-")
            val header = headerLine.first()
            expression = headerLine.last().parse()
            var last = 0
            (header + ',').mapIndexedNotNullTo(suppositions) { i, c ->
                if (c == ',' && checkCorrectBrackets(header.substring(i))) {
                    val tmp = last
                    last = i + 1
                    header.substring(tmp, i).parse()
                } else null
            }
            var line: String? = reader.readLine()
            while (line != null) {
                if (!line.isEmpty() && line.trim().first() != '#') {
                    proof.add(line.parse()!!)
                }
                line = reader.readLine()
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return
    }

    val alpha = suppositions.lastOrNull()
    writer.appendln(suppositions.dropLast(1).map(Expression::toString).joinToString(separator = ",")
            + "|-(" + (alpha?.let { alpha.toString() + ")->(" } ?: "") + expression.toString() + ")")

    if (ProofChecker().check(suppositions, proof, expression!!) { writer.appendln(it.toString()) }) {
        println("Вывод корректен")
    }
}

fun main(args: Array<String>) {
    var file = File(args.first())
    file.walk().forEach { file ->
        if (file.isDirectory) return@forEach
        if (file.extension != "in") return@forEach
        println("\n   # Test: $file")
        BufferedWriter(FileWriter(File(file.parent, file.nameWithoutExtension + ".out"))).use { writer ->
            processFile(file, writer)
        }
    }

    println("_____________________________________________________________________________")
    println("_____________________________________________________________________________")

    if (file.isFile) file = File(file.parent, file.nameWithoutExtension + ".out")
    file.walk().forEach { file ->
        if (file.isDirectory) return@forEach
        if (file.extension != "out") return@forEach
        println("\n   # Check answer: $file")
        BufferedWriter(StringWriter()).use { writer ->
            processFile(file, writer)
        }
    }
}