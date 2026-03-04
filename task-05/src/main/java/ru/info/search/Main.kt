package ru.info.search

import java.io.File

fun main() {
    val controller = SearchController(
        invertedIndexFile = File("output/inverted-index.txt"),
        lemmasFile = File("output/lemmas.txt"),
        tfidfDir = File("output/metrics/lemmas"),
        outputDir = File("output/search-results"),
        topN = 10
    )
    controller.runInteractive()
}
