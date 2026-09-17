package com.halilozel.codeblockview

/** Lightweight display tokenizer, not a compiler. Ranges never modify the original code. */
object KotlinSyntax {
    enum class Kind { COMMENT, STRING, KEYWORD, NUMBER, ANNOTATION }
    data class Token(val start: Int, val end: Int, val kind: Kind)

    private val keywords = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if",
        "in", "interface", "is", "null", "object", "package", "return", "super", "this",
        "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while",
        "by", "catch", "constructor", "delegate", "dynamic", "field", "file", "finally",
        "get", "import", "init", "param", "property", "receiver", "set", "setparam",
        "where", "actual", "abstract", "annotation", "companion", "const", "crossinline",
        "data", "enum", "expect", "external", "final", "infix", "inline", "inner",
        "internal", "lateinit", "noinline", "open", "operator", "out", "override",
        "private", "protected", "public", "reified", "sealed", "suspend", "tailrec",
        "vararg", "value",
    )

    fun tokens(source: String): List<Token> = buildList {
        var i = 0
        while (i < source.length) {
            val start = i
            val kind: Kind?
            when {
                source.startsWith("//", i) -> {
                    i = source.indexOf('\n', i).let { if (it < 0) source.length else it }
                    kind = Kind.COMMENT
                }
                source.startsWith("/*", i) -> {
                    var depth = 1
                    i += 2
                    while (i < source.length && depth > 0) {
                        when {
                            source.startsWith("/*", i) -> { depth++; i += 2 }
                            source.startsWith("*/", i) -> { depth--; i += 2 }
                            else -> i++
                        }
                    }
                    kind = Kind.COMMENT
                }
                source.startsWith("\"\"\"", i) -> {
                    i = source.indexOf("\"\"\"", i + 3).let { if (it < 0) source.length else it + 3 }
                    kind = Kind.STRING
                }
                source[i] == '"' || source[i] == '\'' || source[i] == '`' -> {
                    val quote = source[i++]
                    while (i < source.length) {
                        if (source[i] == '\\' && quote != '`') i = (i + 2).coerceAtMost(source.length)
                        else if (source[i++] == quote) break
                    }
                    kind = if (quote == '`') null else Kind.STRING
                }
                source[i] == '@' -> {
                    i++
                    while (i < source.length && (source[i].isLetterOrDigit() || source[i] in "_.:")) i++
                    kind = Kind.ANNOTATION
                }
                source[i].isLetter() || source[i] == '_' -> {
                    i++
                    while (i < source.length && (source[i].isLetterOrDigit() || source[i] == '_')) i++
                    kind = if (source.substring(start, i) in keywords) Kind.KEYWORD else null
                }
                source[i].isDigit() -> {
                    i++
                    while (i < source.length && (source[i].isLetterOrDigit() || source[i] == '_')) i++
                    kind = Kind.NUMBER
                }
                else -> { i++; kind = null }
            }
            if (kind != null) add(Token(start, i, kind))
        }
    }
}
