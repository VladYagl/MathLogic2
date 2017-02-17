package vladyagl

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*

private val suppositions = ArrayList<Expression>()
private val proof = ArrayList<Expression>()

fun checkCorrectBrackets(text: String): Boolean {
    var balance = 0
    text.forEach {
        if (it == '(') balance++
        if (it == ')') balance--
        if (balance < 0) return false
    }
    return balance == 0
}

fun main(args: Array<String>) {
    File(args.first()).walk().forEach { file ->
        if (file.isDirectory) return@forEach
        println("Test: $file")
        try {
            BufferedReader(FileReader(file)).use { reader ->
                val header = reader.readLine().split("|-").first()
                var last = 0
                (header + ',').mapIndexedNotNullTo(suppositions) { i, c ->
                    if (c == ',' && checkCorrectBrackets(header.substring(i))) {
                        val tmp = last
                        last = i + 1
                        ExpressionParser.parse(header.substring(tmp, i))
                    } else null
                }
                var line: String? = reader.readLine()
                while (line != null) {
                    if (line.isEmpty()) {
                        continue
                    }
                    proof.add(ExpressionParser.parse(line))
                    line = reader.readLine()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        ProofChecker.check(suppositions, proof)
        suppositions.clear()
        proof.clear()
    }
}