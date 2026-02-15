package ru.info.search

import java.io.File

fun main() {
    val index = loadInvertedIndex("output/inverted-index.txt")
    val tokenToLemma = loadTokenToLemma("output/lemmas.txt")
    val allDocIds = index.values.flatten().toSet()

    val urlIndex = File("output/index.txt")
        .readLines()
        .associate { line ->
            val (id, url) = line.split("\t")
            id.toInt() to url
        }

    val parser = BooleanSearchParser(index, tokenToLemma, allDocIds)

    println("=== Булев поиск ===")
    println("Операторы: AND, OR, NOT")
    println("Введите -1 для выхода\n")

    while (true) {
        print("Запрос: ")
        val query = readlnOrNull()?.trim() ?: continue

        if (query == "-1") {
            println("Выход.")
            break
        }

        if (query.isBlank()) continue

        try {
            val resultIds = parser.search(query)

            if (resultIds.isEmpty()) {
                println("Ничего не найдено.\n")
            } else {
                println("Найдено документов: ${resultIds.size}")
                resultIds.sorted().forEach { id ->
                    val url = urlIndex[id] ?: "unknown"
                    println("  [$id] $url")
                }
                println()
            }
        } catch (e: Exception) {
            println("Ошибка: ${e.message}\n")
        }
    }
}