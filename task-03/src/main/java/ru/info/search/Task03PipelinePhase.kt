package ru.info.search

import org.jsoup.Jsoup
import ru.kpfu.search.PipelinePhase
import java.io.File

class Task03PipelinePhase(): PipelinePhase {

    private val indexFile = "output/index.txt"
    private val contentsFile = "output/выкачка.txt"
    private val lemmasFile = "output/lemmas.txt"

    private val invertedIndexFile = "output/inverted-index.txt"

    override fun invoke() {
        val invertedIndex = createInvertedIndex(documents = getAllDocuments()).also {
            val output = it.entries.joinToString(separator = "\n") { (string, set) ->
                "$string ${set.sorted().joinToString(" ")}"
            }
            File(invertedIndexFile).writeText(output)
        }
    }

    private fun createInvertedIndex(documents: List<Document>): Map<String, MutableSet<Int>> {
        val invertedIndex = mutableMapOf<String, MutableSet<Int>>()
        val tokenToLemma = mutableMapOf<String, String>()
        File(lemmasFile).readLines().forEach { line ->
            val parts = line.split(" ")
            if (parts.isNotEmpty()) {
                val lemma = parts[0]
                parts.forEach { tokenToLemma[it] = lemma }
            }
        }
        documents.forEach { document ->
            document.text
                .split(REGEX)
                .toSet()
                .map { it.lowercase().trim() }
                .filter { word ->
                    word.length > MIN_LENGTH && word.all {
                        'a'.code <= it.code && it.code <= 'я'.code
                    }
                }.forEach { token ->
                    val lemma = tokenToLemma[token] ?: return@forEach
                    invertedIndex.getOrPut(lemma) { mutableSetOf() }.add(document.id)
                }
        }
        return invertedIndex
    }

    private fun getAllDocuments(): List<Document> {
        val urlIndex = File(indexFile)
            .readLines()
            .associate { line ->
                val (id, url) = line.split("\t")
                id.toInt() to url
            }.also { println("Загружено ссылок: ${it.size}") }

        val rawHtml = File(contentsFile).readText(Charsets.UTF_8)
        val htmlPages = rawHtml
            .split(Regex("(<!-- \\d\\.\\d+ -->)", RegexOption.IGNORE_CASE))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .also { println("Всего страниц - ${it.size}") }

        return htmlPages.mapIndexed { index, html ->
            val url = urlIndex[index] ?: "unknown"
            val text = Jsoup.parse(html).text()
            Document(index, url, text)
        }.also { println("Создано документов: ${it.size}") }
    }

    private companion object {
        private const val MIN_LENGTH = 3
        private val REGEX = Regex("[^a-zA-Zа-яёА-ЯЁ]+")
    }
}
