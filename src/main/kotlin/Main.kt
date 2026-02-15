package ru.info.search

import io.ktor.util.pipeline.Pipeline
import ru.kpfu.search.PipelinePhase

private val pipeline = listOf<PipelinePhase>(
    Task01PipelinePhase()
)

fun main(args: Array<String>) {
    pipeline.forEach(PipelinePhase::invoke)
}