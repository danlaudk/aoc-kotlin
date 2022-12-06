package daySix

import arrow.fx.coroutines.parMap
import flows.lines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.useLines

fun produceSets(path: Path, windowSize: Int = 4) = flow {
    path.useLines { lines ->
        val firstLine = lines.first()
        for (idx in firstLine.indices) {
            val endIdx = idx + windowSize
            if (endIdx <= firstLine.length) {
                emit(idx to firstLine.subSequence(idx, endIdx).toSet())
            }
        }
    }
}.flowOn(Dispatchers.IO)

// .indices is a nice method to describe something like `0 until s.length`
fun searchMarkers(s: String, markerLength: Int = 4): Int =
    s.indices.zip(s.windowed(markerLength, 1))
        .first { (_, x) -> x.toSet().size == markerLength }
        .first + markerLength

fun searchMarkersSet(p: Pair<Int, Set<Char>>, markerLength: Int = 4): Int? =
    if (p.second.size == markerLength) {
        p.first + markerLength
    } else {
        null
    }

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun daySix() {
    val path = Path("inputFiles/daySix.txt")
    val partOneResult = lines(path).parMap { searchMarkers(it, 4) }.toList()
    val partTwoResult = lines(path).parMap { searchMarkers(it, 14) }.toList()
    println("Part 1: $partOneResult")
    println("Part 2: $partTwoResult")

    val partOneMarkerLength = 4
    val partOneResultLazier =
        produceSets(path)
            .first { searchMarkersSet(it, partOneMarkerLength) != null }
            .first + partOneMarkerLength
    println("Lazier 1: $partOneResultLazier")

    val partTwoMarkerLength = 14
    val partTwoResultLazier =
        produceSets(path, partTwoMarkerLength)
            .first { searchMarkersSet(it, partTwoMarkerLength) != null }
            .first + partTwoMarkerLength
    println("Lazier 2: $partTwoResultLazier")
}