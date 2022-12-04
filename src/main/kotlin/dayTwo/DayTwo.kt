package dayTwo

import arrow.fx.coroutines.parMapUnordered
import cc.ekblad.konbini.*
import flows.lines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.fold
import kotlin.io.path.Path

enum class RockPaperScissors {
    ROCK,
    PAPER,
    SCISSORS,
}

enum class WinDrawLoss {
    WIN,
    DRAW,
    LOSS,
}

val rockParser = parser {
    oneOf(parser { char('A') }, parser { char('X') })
    RockPaperScissors.ROCK
}

val paperParser = parser {
    oneOf(parser { char('B') }, parser { char('Y') })
    RockPaperScissors.PAPER
}

val scissorsParser = parser {
    oneOf(parser { char('C') }, parser { char('Z') })
    RockPaperScissors.SCISSORS
}

val rockPaperScissorsParser = oneOf(rockParser, paperParser, scissorsParser)

val parseGame = parser {
    val first = rockPaperScissorsParser()
    whitespace()
    val second = rockPaperScissorsParser()
    Pair(first, second)
}

val lossParser = parser {
    char('X')
    WinDrawLoss.LOSS
}

val drawParser = parser {
    char('Y')
    WinDrawLoss.DRAW
}

val winParser = parser {
    char('Z')
    WinDrawLoss.WIN
}

val parseGamePartTwo = parser {
    val first = rockPaperScissorsParser()
    whitespace()
    val second = oneOf(winParser, lossParser, drawParser)
    first to second
}

fun scorePlay(rockPaperScissors: RockPaperScissors) = when (rockPaperScissors) {
    RockPaperScissors.ROCK -> 1
    RockPaperScissors.PAPER -> 2
    RockPaperScissors.SCISSORS -> 3
}

fun scoreResult(winDrawLoss: WinDrawLoss) = when (winDrawLoss) {
    WinDrawLoss.WIN -> 6
    WinDrawLoss.LOSS -> 0
    WinDrawLoss.DRAW -> 3
}

fun determineResult(p: Pair<RockPaperScissors, RockPaperScissors>): WinDrawLoss = when (p) {
    RockPaperScissors.ROCK to RockPaperScissors.ROCK -> WinDrawLoss.DRAW
    RockPaperScissors.PAPER to RockPaperScissors.PAPER -> WinDrawLoss.DRAW
    RockPaperScissors.SCISSORS to RockPaperScissors.SCISSORS -> WinDrawLoss.DRAW

    RockPaperScissors.ROCK to RockPaperScissors.PAPER -> WinDrawLoss.WIN
    RockPaperScissors.ROCK to RockPaperScissors.SCISSORS -> WinDrawLoss.LOSS

    RockPaperScissors.PAPER to RockPaperScissors.ROCK -> WinDrawLoss.LOSS
    RockPaperScissors.PAPER to RockPaperScissors.SCISSORS -> WinDrawLoss.WIN

    RockPaperScissors.SCISSORS to RockPaperScissors.ROCK -> WinDrawLoss.WIN
    RockPaperScissors.SCISSORS to RockPaperScissors.PAPER -> WinDrawLoss.LOSS

    else -> error("impossible")
}

fun determinePlay(p: Pair<RockPaperScissors, WinDrawLoss>) = when (p) {
    RockPaperScissors.ROCK to WinDrawLoss.WIN -> RockPaperScissors.PAPER
    RockPaperScissors.ROCK to WinDrawLoss.LOSS -> RockPaperScissors.SCISSORS
    RockPaperScissors.ROCK to WinDrawLoss.DRAW -> RockPaperScissors.ROCK

    RockPaperScissors.PAPER to WinDrawLoss.WIN -> RockPaperScissors.SCISSORS
    RockPaperScissors.PAPER to WinDrawLoss.LOSS -> RockPaperScissors.ROCK
    RockPaperScissors.PAPER to WinDrawLoss.DRAW -> RockPaperScissors.PAPER

    RockPaperScissors.SCISSORS to WinDrawLoss.WIN -> RockPaperScissors.ROCK
    RockPaperScissors.SCISSORS to WinDrawLoss.LOSS -> RockPaperScissors.PAPER
    RockPaperScissors.SCISSORS to WinDrawLoss.DRAW -> RockPaperScissors.SCISSORS

    else -> error("impossible")
}

fun scoreGame(p: Pair<RockPaperScissors, RockPaperScissors>) =
    scorePlay(p.second) + scoreResult(determineResult(p))

fun scorePartTwo(p: Pair<RockPaperScissors, WinDrawLoss>) = run {
    val game = p.first to determinePlay(p)
    scoreGame(game)
}

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayTwo2022() = coroutineScope {
    val path = Path("inputFiles/dayTwo2022.txt")
    val totalScorePartOne = lines(path)
        .parMapUnordered { parseGame.parse(it) }
        .parMapUnordered { r ->
            when (r) {
                is ParserResult.Ok -> scoreGame(r.result)
                is ParserResult.Error -> throw Error("Unable to parse game $r")
            }
        }
        .fold(0) { acc, x -> acc + x }
    println("The part one score is: $totalScorePartOne")

    val totalScorePartTwo = lines(path)
        .parMapUnordered { parseGamePartTwo.parse(it) }
        .parMapUnordered { r ->
            when (r) {
                is ParserResult.Ok -> scorePartTwo(r.result)
                is ParserResult.Error -> throw Error("Unable to parse game $r")
            }
        }
        .fold(0) { acc, x -> acc + x }
    println("The part two score is: $totalScorePartTwo")
}