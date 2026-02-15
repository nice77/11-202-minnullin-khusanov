package ru.info.search

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import ru.kpfu.search.PipelinePhase
import ru.kpfu.search.ServiceLocator
import java.io.File

class Task01PipelinePhase: PipelinePhase {

    private val bookIds = listOf(94, 1199)
    private val httpClient = ServiceLocator.httpClient
    private val outputFileName = "output/выкачка.txt"
    private val indexOutputFileName = "output/index.txt"

    /**
     * Данный `invoke` выполняет последовательные запросы по двум книгам, собирая ссылки и результаты в разные файлы
     *
     * Работает не так быстро: мы в рамках одной и той же итерации и делаем запрос в сеть, и делаем запись в диск
     * - оттого и высокая длительность работы
     */
    override fun invoke() {
        runBlocking {
            val file = File(outputFileName).also { it.writeText("") }
            val indexFile = File(indexOutputFileName).also { it.writeText("") }
            var totalCounter = 0
            bookIds.forEach { bookId ->
                var currentPage = 1
                do {
                    val url = "https://ilibrary.ru/text/$bookId/p.$currentPage/index.html"
                    val response = httpClient.get(urlString = url)
                    if (response.status.isSuccess()) {
                        file.appendText("\n${response.body<String>()}")
                        indexFile.appendText("$totalCounter\thttps://ilibrary.ru/text/$bookId/p.$currentPage/index.html\n")
                        currentPage += 1
                        totalCounter += 1
                    }
                    if (totalCounter % 10 == 0) println("done $totalCounter iterations")
                } while (response.status.isSuccess())
            }
        }
    }
}
