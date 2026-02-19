package ru.info.search

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import ru.kpfu.search.PipelinePhase
import ru.kpfu.search.ServiceLocator
import java.io.File

class Task02PipelinePhase : PipelinePhase {

    private val contentsFile = "output/выкачка.txt"
    private val indexFile = "output/index.txt"
    private val httpClient = ServiceLocator.httpClient

    private val tokensOutput = "output/tokens.txt"
    private val lemmasOutput = "output/lemmas.txt"

    override fun invoke() {
        runBlocking {
            val tokensList = fetchTokensList()
            val joinedTokensList = tokensList.joinToString(separator = "\n")
            with (File(tokensOutput)) {
                writeText(joinedTokensList)
            }

            val lemmatizeResult = httpClient.post("http://localhost:5000/lemmatize") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("tokens" to tokensList))
            }.body<Map<String, List<String>>>()
            val joinedLemmatizeResult = lemmatizeResult
                .entries
                .sortedBy { it.key }
                .joinToString(separator = "\n") { (lemma, tokenList) ->
                    "$lemma ${tokenList.joinToString(" ")}"
                }
            with (File(lemmasOutput)) {
                writeText(joinedLemmatizeResult)
            }
        }
    }

    private fun fetchTokensList(): List<String> {
        val content = File(contentsFile).readText(Charsets.UTF_8)
        val htmlDocument = Jsoup.parse(content)
        return htmlDocument.text() // достаём текст
            .split(REGEX) // разделяем все слова в список по всему, что не является буквами
            .toSet() // приводим список во множество уникальных значений
            .map { it.lowercase().trim() } // приводим всё множество в нижний регистр и избавляемся от лишних пробелов
            .filter { word ->
                // фильтруем слова по тому, какие они короткие и по тому, содержат ли они отличные от букв символы
                word.length > MIN_LENGTH && word.all { letter ->
                    'a'.code <= letter.code && letter.code <= 'я'.code
                }
            }
    }

    private companion object {
        private const val MIN_LENGTH = 3
        private val REGEX = Regex("[^a-zA-Zа-яёА-ЯЁ]+")
    }
}

fun main() {
    Task02PipelinePhase().invoke()
}