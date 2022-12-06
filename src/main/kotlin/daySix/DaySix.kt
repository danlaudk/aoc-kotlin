package daySix

import arrow.fx.coroutines.parMap
import flows.lines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.toList
import kotlin.io.path.Path

// .indices is a nice method to describe something like `0 until s.length`
fun searchMarkers(s: String, markerLength: Int = 4): Int =
    s.indices.zip(s.windowed(markerLength, 1))
        .first { (_, x) -> x.toSet().size == markerLength }
        .first + markerLength

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun daySix() {
    val path = Path("inputFiles/daySix.txt")
    val partOneResult = lines(path).parMap { searchMarkers(it, 4) }.toList()
    val partTwoResult = lines(path).parMap { searchMarkers(it, 14) }.toList()
    println("Part 1: $partOneResult")
    println("Part 2: $partTwoResult")
}