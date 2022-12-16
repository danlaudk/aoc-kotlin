package dayTwelve

import arrow.fx.coroutines.parMap
import cc.ekblad.konbini.*
import flows.lines
import grid.Grid
import grid.Point
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.toList
import kotlin.io.path.Path

const val debug = true

val parseChars: Parser<List<Char>> = parser {
    many1(parser { char() })
}

fun isAscendingHeight(currentHeight: Char, nextOption: Pair<Char, Point>?): Boolean =
    nextOption?.let { p ->
        (currentHeight == 'S' && p.first == 'a')
                || currentHeight.plus(1) >= p.first
    } ?: false

fun isEnd(currentHeight: Char, nextOption: Pair<Char, Point>?): Char? =
    nextOption?.let { p ->
        if (currentHeight == 'z' && p.first == 'E') {
            'E'
        } else {
            null
        }
    }

fun searchPaths(matrix: Grid<Char>, startingPoint: Point, maxIterations: Long = 100): MutableMap<Long, List<Point>> {
    val result = mutableMapOf(0.toLong() to listOf(startingPoint))
    val visited = mutableSetOf<Point>()

    for (currentDepth in 0 until maxIterations) {
        val nextDepth = currentDepth + 1
        val pathsToSearch = result[currentDepth].orEmpty()
        for (path in pathsToSearch) {
            val searchHeight = matrix[path] ?: 'Y' // TODO: what is better here?
            val newPaths = result[nextDepth]?.toMutableSet() ?: mutableSetOf()

            val up = matrix.up(path)
            val upIsAscending = isAscendingHeight(searchHeight, up)
            if (upIsAscending) {
                up?.let { p ->
                    if (!visited.contains(p.second)) {
                        visited.add(p.second)
                        newPaths.add(p.second)
                    }
                }
            }

            val down = matrix.down(path)
            val downIsAscending = isAscendingHeight(searchHeight, down)
            if (downIsAscending) {
                down?.let { p ->
                    if (!visited.contains(p.second)) {
                        visited.add(p.second)
                        newPaths.add(p.second)
                    }
                }
            }

            val left = matrix.left(path)
            val leftIsAscending = isAscendingHeight(searchHeight, left)
            if (leftIsAscending) {
                left?.let { p ->
                    if (!visited.contains(p.second)) {
                        visited.add(p.second)
                        newPaths.add(p.second)
                    }
                }
            }

            val right = matrix.right(path)
            val rightIsAscending = isAscendingHeight(searchHeight, right)
            if (rightIsAscending) {
                right?.let { p ->
                    if (!visited.contains(p.second)) {
                        visited.add(p.second)
                        newPaths.add(p.second)
                    }
                }
            }

            result[nextDepth] = newPaths.toMutableList()

            val ends = listOfNotNull(
                isEnd(searchHeight, up),
                isEnd(searchHeight, down),
                isEnd(searchHeight, left),
                isEnd(searchHeight, right)
            )

            if (ends.any { it == 'E' }) {
                return result
            }
        }
    }
    println("Max iterations reached...")
    return result
}

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayTwelve() {
    val path = Path("inputFiles/dayTwelve.txt")
    val rawInput = lines(path)
        .parMap { line ->
            when (val r = parseChars.parse(line)) {
                is ParserResult.Ok -> r.result
                is ParserResult.Error -> throw Error("Unable to parse $line")
            }
        }
        .toList()
    val rows = rawInput.size
    val cols = rawInput[0].size
    if (debug) {
        println("Matrix has $rows rows and $cols columns")
    }

    val matrix = Grid(rows, cols, rawInput.flatten())
    // TODO: Something is wrong here...
    val startingPoint = matrix.first { it == 'S' }
    if (debug) {
        matrix.display()
        println("The starting position is $startingPoint")
    }
    startingPoint?.let { p ->
        val paths = searchPaths(matrix, p, 1000)
        println(paths)
    }
}