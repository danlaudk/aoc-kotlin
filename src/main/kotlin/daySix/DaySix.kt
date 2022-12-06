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

// .indices is a nice method to describe something like `0 until s.length`
fun searchMarkers(s: String, markerLength: Int = 4): Int =
    s.indices.zip(s.windowed(markerLength, 1))
        .first { (_, x) -> x.toSet().size == markerLength }
        .first + markerLength

fun produceSubsequences(path: Path, markerLength: Int = 4) = flow {
    path.useLines { lines ->
        val firstLine = lines.first()
        firstLine.indices.forEach { idx ->
            val endIdx = idx + markerLength
            if (endIdx <= firstLine.length) {
                emit(idx to firstLine.subSequence(idx, endIdx))
            }
        }
    }
}.flowOn(Dispatchers.IO)

fun searchMarkersSet(p: Pair<Int, CharSequence>): Int? = run {
    val set = mutableSetOf<Char>()
    // `Set<T>.add` returns a `Boolean` describing if it was added to the `Set<T>`.
    // `Collection<T>.all` requires every entry to be true **and** short-circuits.
    if (p.second.all { set.add(it) }) {
        p.first
    } else {
        null
    }
}

suspend fun runSubsequence(path: Path, markerLength: Int) =
    produceSubsequences(path, markerLength)
        .first { searchMarkersSet(it) != null }
        .first + markerLength

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun daySix() {
    val path = Path("inputFiles/daySix.txt")

    // Original, non-`Flow` version. It uses `parMap` but no real parallel work
    val partOneResult = lines(path).parMap { searchMarkers(it, 4) }.toList()
    println("Part 1: $partOneResult")
    val partTwoResult = lines(path).parMap { searchMarkers(it, 14) }.toList()
    println("Part 2: $partTwoResult")

    // `Flow` version with short-circuits
    val partOneResultLazier = runSubsequence(path, 4)
    println("Lazier 1: $partOneResultLazier")
    val partTwoResultLazier = runSubsequence(path, 14)
    println("Lazier 2: $partTwoResultLazier")
}