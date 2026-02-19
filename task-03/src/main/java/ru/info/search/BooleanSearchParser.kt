package ru.info.search

import kotlin.text.iterator

internal class BooleanSearchParser(
    private val index: Map<String, Set<Int>>,
    private val tokenToLemma: Map<String, String>,
    private val allDocIds: Set<Int>
) {
    private var tokens: List<String> = emptyList()
    private var position: Int = 0

    // Текущий токен
    private fun current(): String? = tokens.getOrNull(position)

    // Перейти к следующему токену
    private fun advance() { position++ }

    // Проверить и пропустить ожидаемый токен
    private fun expect(expected: String) {
        if (current().equals(expected, ignoreCase = true)) {
            advance()
        } else {
            throw IllegalArgumentException("Ожидался '$expected', получен '${current()}'")
        }
    }

    // Главный метод
    fun search(query: String): Set<Int> {
        tokens = tokenize(query)
        position = 0

        if (tokens.isEmpty()) return emptySet()

        return parseOr()
    }

    // Токенизация запроса
    private fun tokenize(query: String): List<String> {
        val result = mutableListOf<String>()
        var current = ""

        for (char in query) {
            when {
                char == '(' || char == ')' -> {
                    if (current.isNotBlank()) {
                        result.add(current.trim())
                        current = ""
                    }
                    result.add(char.toString())
                }
                char.isWhitespace() -> {
                    if (current.isNotBlank()) {
                        result.add(current.trim())
                        current = ""
                    }
                }
                else -> {
                    current += char
                }
            }
        }

        if (current.isNotBlank()) {
            result.add(current.trim())
        }

        return result
    }

    // Уровень OR (самый низкий приоритет)
    private fun parseOr(): Set<Int> {
        var result = parseAnd()

        while (current()?.uppercase() == "OR") {
            advance() // пропускаем OR
            val right = parseAnd()
            result = result union right
        }

        return result
    }

    // Уровень AND (средний приоритет)
    private fun parseAnd(): Set<Int> {
        var result = parseNot()

        while (current()?.uppercase() == "AND") {
            advance() // пропускаем AND
            val right = parseNot()
            result = result intersect right
        }

        return result
    }

    // Уровень NOT (высокий приоритет)
    private fun parseNot(): Set<Int> {
        return if (current()?.uppercase() == "NOT") {
            advance() // пропускаем NOT
            val operand = parseNot() // NOT может быть цепочкой: NOT NOT кошка
            allDocIds - operand
        } else {
            parseAtom()
        }
    }

    // Атом: скобки или слово
    private fun parseAtom(): Set<Int> {
        return when (current()) {
            "(" -> {
                advance() // пропускаем (
                val result = parseOr() // внутри скобок — всё заново
                expect(")") // ожидаем )
                result
            }
            null -> {
                throw IllegalArgumentException("Неожиданный конец запроса")
            }
            else -> {
                val word = current()!!.lowercase()
                advance()
                getDocuments(word)
            }
        }
    }

    private fun getDocuments(word: String): Set<Int> {
        val lemma = tokenToLemma[word] ?: word
        return index[lemma] ?: emptySet()
    }
}