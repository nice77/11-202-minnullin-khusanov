package ru.info.search

import ru.kpfu.search.PipelinePhase

private val pipeline = listOf(
    Task01PipelinePhase(),
    Task02PipelinePhase(),
    Task03PipelinePhase(),
)

fun main(args: Array<String>) {
    pipeline.forEach(PipelinePhase::invoke)
}