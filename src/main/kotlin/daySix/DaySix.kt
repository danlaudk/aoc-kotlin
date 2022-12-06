package daySix

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.useLines

// .indices is a nice method to describe something like `0 until s.length`.
// This didn't handle multiple lines of input the way I wanted.
//fun searchMarkers(s: String, markerLength: Int = 4): Int =
//    s.indices.zip(s.windowed(markerLength, 1))
//        .first { (_, x) -> x.toSet().size == markerLength }
//        .first + markerLength

// Produces `.windowed(4, 1)` for each line in the file as a `Flow`.
// The windows contain the index of the first character and the `CharSequence`.
// Note: windows don't span multiple lines, new lines start a new series of windows.
fun produceSubsequences(path: Path, markerLength: Int = 4) = flow {
    path.useLines { lines ->
        lines.forEachIndexed { outer, line ->
            line.indices.forEach { inner ->
                val endIdx = inner + markerLength
                if (endIdx <= line.length) {
                    emit(outer to (inner to line.subSequence(inner, endIdx)))
                }
            }
        }
    }
}.flowOn(Dispatchers.IO)

data class Result(val line: Int, val position: Int)

fun searchMarkersSet(p: Pair<Int, Pair<Int, CharSequence>>): Result? = run {
    val set = mutableSetOf<Char>()
    // `Set<T>.add` returns a `Boolean` describing if it was added to the `Set<T>`.
    // `Collection<T>.all` requires every entry to be true **and** short-circuits.
    if (p.second.second.all { set.add(it) }) {
        Result(p.first, p.second.first)
    } else {
        null
    }
}

suspend fun runSubsequence(path: Path, markerLength: Int) =
    produceSubsequences(path, markerLength)
        .first { searchMarkersSet(it) != null }

fun toResult(p: Pair<Int, Pair<Int, CharSequence>>) =
    Result(p.first, p.second.first)

fun displayResult(r: Result, markerLength: Int) {
    println("On line ${r.line}, found unique sequence at ${r.position + markerLength}")
}

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun daySix() {
    val path = Path("inputFiles/daySix.txt")

    // `Flow` version with short-circuits and multi-line behavior
    val partOneResultLazier = toResult(runSubsequence(path, 4))
    displayResult(partOneResultLazier, 4)

    val partTwoResultLazier = toResult(runSubsequence(path, 14))
    displayResult(partTwoResultLazier, 14)
}