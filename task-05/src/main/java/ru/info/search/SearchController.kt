package ru.info.search// ru.info.search.SearchController.kt
import java.io.File

class SearchController(
    invertedIndexFile: File,
    lemmasFile: File?,
    tfidfDir: File?,
    private val outputDir: File?,
    private val topN: Int = 10
) {
    private val loader = DataLoader(invertedIndexFile, lemmasFile, tfidfDir)
    private val presenter = SearchResultPresenter(outputDir)
    private val similarity = SimilarityCalculator()

    private lateinit var data: IndexData
    private lateinit var vectorBuilder: VectorBuilder
    private lateinit var documentVectors: Map<Int, DoubleArray>

    fun initialize() {
        data = loader.load()
        vectorBuilder = VectorBuilder(data)
        documentVectors = vectorBuilder.buildAllDocumentVectors()
    }

    fun search(query: String): List<SearchResult> {
        val tokens = query.lowercase().split(Regex("[^а-яёa-z]+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return emptyList()

        val qr = vectorBuilder.buildQueryVector(tokens)
        if (qr.vector.all { it == 0.0 }) {
            presenter.display(query, qr.resolvedLemmas, emptyList())
            return emptyList()
        }

        val results = similarity.rank(qr.vector, documentVectors, qr.resolvedLemmas, data.invertedIndex, topN)
        presenter.display(query, qr.resolvedLemmas, results)
        presenter.save(query, qr.resolvedLemmas, results)
        return results
    }

    fun runInteractive() {
        initialize()
        println("Search ready. Type query or 'exit'.")
        println()

        while (true) {
            print("> ")
            val input = readlnOrNull()?.trim() ?: break
            if (input.equals("exit", true)) break
            if (input.isBlank()) continue
            search(input)
        }
    }
}
