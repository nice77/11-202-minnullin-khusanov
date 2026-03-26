package ru.info.search

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
class WebSearchController(private val searchService: SearchService) {

    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    fun index(): String = page("Поисковая система") {
        searchForm("")
    }

    @GetMapping("/search", produces = [MediaType.TEXT_HTML_VALUE])
    fun search(@RequestParam q: String): String {
        val results = searchService.search(q)

        return page("Результаты: $q") {
            searchForm(q)

            h2 { +"Результаты по запросу: \"$q\"" }

            if (results.isEmpty()) {
                p("empty") { +"Ничего не найдено." }
            } else {
                p { +"Найдено результатов: ${results.size}" }

                results.forEach { result ->
                    div("result") {
                        div("result-header") {
                            span("rank") { +"#${result.rank}" }
                            span("doc-id") { +"Документ ${result.docId}" }
                        }
                        div("score") {
                            +"Сходство: ${"%.6f".format(result.score)}"
                        }
                        div("lemmas") {
                            +"Совпавшие леммы: ${result.matchedLemmas.joinToString(", ")}"
                        }
                    }
                }
            }
        }
    }

    private fun page(title: String, content: BODY.() -> Unit): String {
        return createHTML().html {
            head {
                title { +title }
                meta(charset = "UTF-8")
                style { +CSS }
            }
            body {
                div("container") {
                    h1 { +"Поисковая система" }
                    this@body.content()
                }
            }
        }
    }

    private fun FlowContent.searchForm(query: String) {
        form(action = "/search", method = FormMethod.get, classes = "search-form") {
            input(type = InputType.text, name = "q") {
                value = query
                placeholder = "Введите запрос..."
            }
            button(type = ButtonType.submit) { +"Найти" }
        }
    }

    companion object {
        private val CSS = """
            body {
                font-family: Arial, sans-serif;
                background: #f5f5f5;
                margin: 0;
                padding: 0;
            }
            .container {
                max-width: 800px;
                margin: 40px auto;
                padding: 0 20px;
            }
            h1 {
                color: #333;
            }
            .search-form {
                display: flex;
                gap: 10px;
                margin: 20px 0;
            }
            .search-form input[type=text] {
                flex: 1;
                padding: 12px;
                font-size: 16px;
                border: 1px solid #ddd;
                border-radius: 4px;
            }
            .search-form button {
                padding: 12px 24px;
                font-size: 16px;
                background: #4285f4;
                color: white;
                border: none;
                border-radius: 4px;
                cursor: pointer;
            }
            .search-form button:hover {
                background: #3367d6;
            }
            .result {
                background: white;
                padding: 16px;
                margin: 12px 0;
                border-radius: 4px;
                box-shadow: 0 1px 3px rgba(0,0,0,0.1);
            }
            .result-header {
                display: flex;
                gap: 12px;
                align-items: center;
            }
            .rank {
                font-weight: bold;
                color: #4285f4;
                font-size: 18px;
            }
            .doc-id {
                font-size: 18px;
                color: #1a0dab;
            }
            .score {
                color: #666;
                font-size: 14px;
                margin-top: 4px;
            }
            .lemmas {
                color: #006621;
                font-size: 14px;
                margin-top: 4px;
            }
            .empty {
                color: #666;
                font-style: italic;
            }
        """.trimIndent()
    }
}
