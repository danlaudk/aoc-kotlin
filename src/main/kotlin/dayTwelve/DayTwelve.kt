package dayTwelve

import arrow.fx.coroutines.parMap
import arrow.optics.optics
import cc.ekblad.konbini.*
import flows.lines
import grid.Grid
import grid.Point
import kotlinx.coroutines.flow.toList
import kotlin.io.path.Path

const val debug = true

val parseChars: Parser<List<Char>> = parser {
    many1(parser { char() })
}

fun searchPaths(matrix: Grid<Char>, startingPoint: Point, paths: List<List<Point>>) = run {
    val up = matrix.up(startingPoint)
    val down = matrix.down(startingPoint)
    val left = matrix.left(startingPoint)
    val right = matrix.right(startingPoint)
    // TODO: recursively search paths
}

suspend fun dayTwelve() {
    val path = Path("inputFiles/dayTwelve.test.txt")
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
    val startingPoint = matrix.first { it == 'S' }
    if (debug) {
        matrix.display()
        println("The starting position is $startingPoint")
    }
    startingPoint?.let { p ->
        val paths = searchPaths(matrix, p, listOf(listOf(p)))
        println(paths)
    }
}