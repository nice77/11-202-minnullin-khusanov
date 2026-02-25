package ru.info.search

import ru.kpfu.search.PipelinePhase

private val pipeline = listOf(
    Task01PipelinePhase(),
    Task02PipelinePhase(),
    Task03PipelinePhase(),
    Task04PipelinePhase()
)

fun main() {
    pipeline.forEach(PipelinePhase::invoke)
}
