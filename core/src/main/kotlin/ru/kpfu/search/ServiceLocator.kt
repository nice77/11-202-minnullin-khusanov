package ru.kpfu.search

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

object ServiceLocator {

    val httpClient: HttpClient by lazy {
        HttpClient(CIO)
    }

}