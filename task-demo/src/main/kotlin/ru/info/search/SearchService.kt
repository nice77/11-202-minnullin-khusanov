package ru.info.search

import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import java.io.File

@Service
class SearchService {

    private lateinit var searchController: SearchController

    @PostConstruct
    fun init() {
        searchController = SearchController(
            invertedIndexFile = File("output/inverted-index.txt"),
            lemmasFile = File("output/lemmas.txt"),
            tfidfDir = File("output/metrics/lemmas"),
            outputDir = null, // не сохраняем в файлы из веба
            topN = 10
        )
        searchController.initialize()
        println("Поисковый движок инициализирован")
    }

    fun search(query: String): List<WebSearchResult> {
        if (query.isBlank()) return emptyList()

        val results = searchController.search(query)

        return results.mapIndexed { i, result ->
            WebSearchResult(
                rank = i + 1,
                docId = result.docId,
                score = result.score,
                matchedLemmas = result.matchedLemmas
            )
        }
    }
}

data class WebSearchResult(
    val rank: Int,
    val docId: Int,
    val score: Double,
    val matchedLemmas: List<String>
)
