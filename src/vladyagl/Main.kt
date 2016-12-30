package vladyagl

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*

private val suppositions = ArrayList<Expression>()
private val proof = ArrayList<Expression>()
private val lines = ArrayList<String>()

fun main(args: Array<String>) {
    val file = File("res\\input.txt")
    var header = ""
    try {
        BufferedReader(FileReader(file)).use { reader ->
            header = reader.readLine()
            val headers = header.split(",|\\|-".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            (0..headers.size - 1 - 1).takeWhile { !headers[it].isEmpty() }
                    .mapTo(suppositions) { ExpressionParser.parse(headers[it]) }
            var line: String? = reader.readLine()
            while (line != null) {
                if (line.isEmpty()) {
                    continue
                }
                proof.add(ExpressionParser.parse(line))
                lines.add(line)
                line = reader.readLine()
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    val checker = ProofChecker(suppositions, proof)
    /*checker.annotate()
    println(header)
    for (i in lines.indices) {
        System.out.printf("(" + (i + 1) + ") " + lines[i] + " (" + checker.getAnnotation(i) + ")\n")
    }*/
}