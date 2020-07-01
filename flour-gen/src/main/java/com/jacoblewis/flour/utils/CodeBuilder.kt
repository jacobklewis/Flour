package com.jacoblewis.flour.utils

class CodeBuilder
private constructor(level: Int) {
    private val levelPre = (0 until level).joinToString("") { "\t" }
    private val builder = StringBuilder()

    fun addLine(code: String) {
        builder.append("${levelPre}$code\n")
    }

    fun build(): String {
        return builder.toString().replace(Regex("\n\\s*\n"),"\n")
    }

    companion object {
        fun create(level: Int = 0): CodeBuilder {
            return CodeBuilder(level)
        }
    }
}