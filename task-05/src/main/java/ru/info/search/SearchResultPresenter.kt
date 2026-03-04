package ru.info.search// ru.info.search.SearchResultPresenter.kt
import java.io.File

class SearchResultPresenter(private val outputDir: File?) {

    init { outputDir?.mkdirs() }

    fun display(query: String, lemmas: List<String>, results: List<SearchResult>) {
        println()
        println("Query: \"$query\"")
        println("Lemmas: $lemmas")
        println()

        if (results.isEmpty()) {
            println("Nothing found.")
        } else {
            println("%-5s  %-10s  %-12s  %s".format("Rank", "Document", "Score", "Matched lemmas"))
            println("-".repeat(70))
            results.forEachIndexed { i, r ->
                println("%-5d  doc_%-6d  %-12s  %s".format(
                    i + 1, r.docId, "%.6f".format(r.score), r.matchedLemmas.joinToString(", ")
                ))
            }
        }
        println()
    }

    fun save(query: String, lemmas: List<String>, results: List<SearchResult>) {
        if (outputDir == null) return
        val name = query.replace(Regex("[^a-zа-яё0-9]", RegexOption.IGNORE_CASE), "_").take(50)
        File(outputDir, "search_$name.txt").printWriter().use { w ->
            w.println("query: $query")
            w.println("lemmas: ${lemmas.joinToString(" ")}")
            results.forEachIndexed { i, r ->
                w.println("${i + 1} doc_${r.docId} ${"%.6f".format(r.score)} [${r.matchedLemmas.joinToString(", ")}]")
            }
        }
    }
}
