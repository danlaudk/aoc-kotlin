package dayNine

import arrow.fx.coroutines.parMap
import arrow.optics.optics
import cc.ekblad.konbini.*
import flows.lines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.toList
import kotlin.io.path.Path
import kotlin.math.abs
import kotlin.math.max

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT,
}

data class Movement(val magnitude: Int, val direction: Direction)

val parseDirection = parser {
    val direction = char()
    whitespace()
    val magnitude = integer()
    when (direction) {
        'U' -> Movement(magnitude.toInt(), Direction.UP)
        'D' -> Movement(magnitude.toInt(), Direction.DOWN)
        'L' -> Movement(magnitude.toInt(), Direction.LEFT)
        'R' -> Movement(magnitude.toInt(), Direction.RIGHT)
        else -> fail("Unable to parse $direction as direction")
    }
}

@optics
data class Delta(val headDelta: Point, val tailFollows: Point) {
    companion object
}

val deltas: Map<Direction, Delta> = mapOf(
    Direction.RIGHT to Delta(Point(0, 1), Point(0, -1)),
    Direction.LEFT to Delta(Point(0, -1), Point(0, 1)),
    Direction.UP to Delta(Point(1, 0), Point(-1, 0)),
    Direction.DOWN to Delta(Point(-1, 0), Point(1, 0))
)

@optics
data class Point(val row: Int, val col: Int) {
    companion object
}

fun Point.isBottomRow() = this.row == 0
fun Point.isLeftCol() = this.col == 0

fun Point.add(p: Point) =
    Point(row + p.row, col + p.col)

fun Point.toPair() =
    row to col

fun chebyshevDistance(p1: Point, p2: Point) =
    max(abs(p2.row - p1.row), abs(p2.col - p1.col))

@optics
data class DayNineState(
    val head: Point, // current head position
    val tail: Point, // current tail position
    val seenTail: Set<Pair<Int, Int>> // all unique tail positions
) {
    companion object
}

fun move(dayNineState: DayNineState, direction: Direction): DayNineState = run {
    val delta = deltas.getOrElse(direction) { throw Error("Unable to find $direction in $deltas") }
    val newHead = dayNineState.head.add(delta.headDelta)
    val followingTail = newHead.add(delta.tailFollows)
    val isLeftCol = direction == Direction.LEFT && dayNineState.head.isLeftCol()
    val isBottomRow = direction == Direction.DOWN && dayNineState.head.isBottomRow()
    if (isLeftCol || isBottomRow) {
        dayNineState
    } else if (chebyshevDistance(newHead, dayNineState.tail) == 2) {
        DayNineState(newHead, followingTail, dayNineState.seenTail + setOf(followingTail.toPair()))
    } else {
        DayNineState(newHead, dayNineState.tail, dayNineState.seenTail)
    }
}

fun printSeenTail(seenTail: Set<Pair<Int, Int>>) = run {
    val maxRow = seenTail.maxOf { it.first }
    val maxCol = seenTail.maxOf { it.second }

    val inner = (0..maxCol).map { '.' }
    val initialGrid = (0..maxRow).map { inner.toMutableList() }

    seenTail.forEach { p ->
        initialGrid[p.first][p.second] = '#'
    }

    (maxRow downTo 0).forEach { rowIdx ->
        (0..maxCol).forEach { colIdx ->
            print(initialGrid[rowIdx][colIdx])
        }
        println()
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayNine() {
    val path = Path("inputFiles/dayNine.txt")
    val movements: List<Movement> =
        lines(path)
            .parMap { input ->
                when (val r = parseDirection.parse(input)) {
                    is ParserResult.Ok -> r.result
                    is ParserResult.Error -> throw Error("Unable to parse $input")
                }
            }
            .toList()

    val initialState = DayNineState(
        Point(0, 0),
        Point(0, 0),
        setOf(0 to 0)
    )

    val finalState =
        movements.fold(initialState) { acc, movement ->
            val indices = 0 until movement.magnitude
            indices.fold(acc) { innerAcc, _ -> move(innerAcc, movement.direction) }
        }

    println(finalState.seenTail) // works for the test input, but not the test input :shrug:
    println(finalState.seenTail.size)
    // println(printSeenTail(finalState.seenTail))
}