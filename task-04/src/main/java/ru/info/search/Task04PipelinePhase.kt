package ru.info.search

import ru.kpfu.search.PipelinePhase
import java.io.File

class Task04PipelinePhase: PipelinePhase {
    override fun invoke() {
        val controller = TfIdfController(
            documentsFile = File("output/выкачка.txt"),
            lemmasFile = File("output/lemmas.txt"),
            indexFile = File("output/inverted-index.txt"),
            tokensOutputDir = File("output/metrics/tokens"),
            lemmasOutputDir = File("output/metrics/lemmas")
        )

        controller.run()
    }
}
