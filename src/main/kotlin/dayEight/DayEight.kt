package dayEight

import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapUnordered
import cc.ekblad.konbini.ParserResult
import cc.ekblad.konbini.parse
import flows.lines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import parsers.parseMany1Digits
import kotlin.io.path.Path

fun splitRow(idx: Int, row: List<Int>) =
    Triple(row[idx], row.take(idx).reversed(), row.takeLast(row.size - idx - 1))

fun splitCol(rowIdx: Int, colIdx: Int, inputMatrix: List<List<Int>>) = run {
    val value = inputMatrix[rowIdx][colIdx]
    val above = mutableListOf<Int>()
    for (i in 0 until rowIdx) {
        above.add(inputMatrix[i][colIdx])
    }
    val below = mutableListOf<Int>()
    for (i in rowIdx + 1 until inputMatrix[0].size) {
        below.add(inputMatrix[i][colIdx])
    }
    Triple(value, above.reversed(), below)
}

fun fullStencil(inputMatrix: List<List<Int>>) = flow {
    inputMatrix.forEachIndexed { rowIdx, row ->
        row.indices.forEach { colIdx ->
            val rowStencil = splitRow(colIdx, row)
            val colStencil = splitCol(rowIdx, colIdx, inputMatrix)
            emit(rowStencil to colStencil)
        }
    }
}.flowOn(Dispatchers.IO)

typealias RowColStencil = Triple<Int, List<Int>, List<Int>>

fun visibile(rowColStencil: RowColStencil): Boolean =
    rowColStencil.second.all { rowColStencil.first > it }
            || rowColStencil.third.all { rowColStencil.first > it }

fun determineVisibility(p: Pair<RowColStencil, RowColStencil>): Boolean =
    visibile(p.first) || visibile(p.second)

fun scenicScore(tree: Int, view: List<Int>): Int =
    if (view.isEmpty()) {
        0
    } else {
        when (val result = view.indexOfFirst { tree <= it }) {
            -1 -> view.size
            else -> result + 1
        }
    }

fun scenicScores(rowColStencil: RowColStencil): Int = run {
    val first = scenicScore(rowColStencil.first, rowColStencil.second)
    val second = scenicScore(rowColStencil.first, rowColStencil.third)
    first * second
}

typealias Stencils = Pair<RowColStencil, RowColStencil>

fun computeScenicScore(p: Stencils): Int =
    scenicScores(p.first) * scenicScores(p.second)

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayEight() {
    val path = Path("inputFiles/dayEight.txt")
    val inputMatrix =
        lines(path)
            .parMap { line ->
                when (val r = parseMany1Digits.parse(line)) {
                    is ParserResult.Ok -> r.result
                    is ParserResult.Error -> throw Error("Unable to parse line $line")
                }
            }
            .fold(mutableListOf<List<Int>>()) { acc, xs ->
                acc.add(xs)
                acc
            }

    val partOneResult = fullStencil(inputMatrix)
        .parMapUnordered { determineVisibility(it) }
        .count { it }
    println("Part 1: $partOneResult")

    val partTwoResult = fullStencil(inputMatrix)
        .parMapUnordered { computeScenicScore(it) }
        .fold(0) { acc, result ->
            if (result > acc) {
                result
            } else {
                acc
            }
        }
    println("Part 2: $partTwoResult")
}