package dayTen

import arrow.core.Either
import arrow.fx.coroutines.parMap
import arrow.optics.optics
import cc.ekblad.konbini.*
import flows.lines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.fold
import kotlin.io.path.Path
import arrow.core.raise.Raise
import kotlinx.coroutines.flow.map

const val debug = false

sealed class Operations
data object Noop : Operations()
data class AddX(val value: Int) : Operations()


sealed class LogicalError
data class ParseError(val message: String) : LogicalError()
data object InvalidInputError : LogicalError()


val noopParser = parser {
    string("noop")
    Noop
}

val addXParser = parser {
    string("addx")
    whitespace()
    val value = integer()
    AddX(value.toInt())
}

val operationsParser: Parser<Operations> = parser {
    oneOf(noopParser, addXParser)
}

fun noop(executionState: ExecutionState): ExecutionState {
    val result = tick(executionState)
    return pixel(result)
}

fun addX(executionState: ExecutionState, addX: AddX): ExecutionState {
    val firstTick = pixel(tick(executionState))
    val secondTick = pixel(tick(firstTick))

    return ExecutionState.value.modify(secondTick) { it + addX.value }
}

@optics
data class ExecutionState(
    val ticks: Int = 0,
    val value: Int = 1,
    val signal: List<Int> = emptyList(),
    val crtBuffer: List<Char> = emptyList(),
    val screen: List<List<Char>> = emptyList()
) {
    companion object
}

/**
fun ExecutionState.signalStrength(): Int? =
if (ticks == 20 || (ticks > 20 && (ticks - 20) % 40 == 0)) {
// println("Cycle is: $ticks Register is: $value Result: ${ticks * value}")
ticks * value
} else {
null
}
 */

fun tick(executionState: ExecutionState): ExecutionState =
    ExecutionState.ticks.modify(executionState) { it + 1 }

/** val result = ExecutionState.ticks.modify(executionState) { it + 1 }
// // Determine if we should append signal strength
//    result.signalStrength()?.let { strength ->
//        ExecutionState.signal.modify(result) { it + listOf(strength) }
//    } ?: result
**/

fun pixel(executionState: ExecutionState): ExecutionState {
    // If our current horizontal position overlaps with the register value:
    // write out '#', else '.'
    val horizontalPosition = (executionState.ticks - 1) % 40
    val sprite = listOf(executionState.value - 1, executionState.value, executionState.value + 1)
    val withBufferUpdate = pixelTheSprite(horizontalPosition, sprite, executionState)

    if (debug) {
        println("before: $executionState after: $withBufferUpdate")
    }

    // If we have a finished the line/crtBuffer, write to screen. else nothing
    return writeLineToScreenBuffer(withBufferUpdate)
//    if (debug) {
//        println("$sprite $horizontalPosition $withScreenUpdate")
//    }
//    withScreenUpdate
}

// If our current horizontal position overlaps with the register value, pixelthesprite
fun pixelTheSprite(horizontalPosition: Int, sprite: List<Int>, executionState: ExecutionState): ExecutionState {
    return if (horizontalPosition in sprite) {
        ExecutionState.crtBuffer.modify(executionState) { it + listOf('@') } // wrap in function
    } else {
        ExecutionState.crtBuffer.modify(executionState) { it + listOf(' ') }
    }
}

fun writeLineToScreenBuffer(executionState: ExecutionState): ExecutionState {
    return if (executionState.crtBuffer.size == 40) {
        val withScreenWrite = ExecutionState.screen.modify(executionState) { it.appendList(executionState.crtBuffer) }
        ExecutionState.crtBuffer.modify(withScreenWrite) { emptyList() }
    } else {
        executionState
    }
}

fun <T> List<List<T>>.appendList(l: List<T>): List<List<T>> = this + listOf(l)

fun displayScreen(input: List<List<Char>>) {
    val columns = 40
    val rows = 6
    (0 until rows).forEach { row ->
        (0 until columns).forEach { column ->
            print(input[row][column])
        }
        println()
    }
}

fun parseCustomError(input: String): Either<ParseError, Operations> {
    return when (val r = operationsParser.parse(input)) {
        is ParserResult.Ok -> Either.Right(r.result)
        is ParserResult.Error -> Either.Left(ParseError("Unable to parse. $r "))
//                is ParserResult.Error -> throw Error("Unable to parse $l as operation")
    }
}

context (Raise<ParseError>)
fun parseCustomErrorWithRaise(input: String): Operations = parseCustomError(input).bind()

suspend fun dayTenExplicit() {
    val path = Path("inputFiles/dayTen.txt")
    lines(path)
        .map { parseCustomError(it) }
        .fold(ExecutionState()) { acc, op: Either<ParseError, Operations> ->
            when(op) {
                is Either.Right -> op.value
            }


            println("op is $op")
            when (op) {
                is Either.Left -> {
                    is Noop -> noop(acc)
                    is AddX -> addX(acc, op)
                }
            }
        }
        .also { println(displayScreen(it.screen)) }
}


context (Raise<ParseError>)
suspend fun dayTen() {
    val path = Path("inputFiles/dayTen.txt")
    lines(path)
        .map {
            parseCustomErrorWithRaise(it) // with map and parMap, does this shortcircuti up to dayTen?
        }
        .fold(ExecutionState()) { acc, op: Either<ParseError, Operations> ->
            println("op is $op")
            when (op) {
                is Either.Left -> {
                    is Noop -> noop(acc)
                    is AddX -> addX(acc, op)
            }
        }
        //                now we  don't need this as we handle the error at the parse stage of the code
        //                is Left<ParseError> -> {
        //                    println("Error: $eitherOp")
        //                    acc
        }
        .also { r ->
//            val totalSignal = r.signal.fold(0) { acc: Int, x: Int -> acc + x }
//            println("Total signal is $totalSignal")
            println(displayScreen(r.screen))
        }
}
