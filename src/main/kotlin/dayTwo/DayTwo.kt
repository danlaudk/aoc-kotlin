package dayTwo

import arrow.fx.coroutines.parMapUnordered
import cc.ekblad.konbini.*
import com.sun.net.httpserver.Authenticator.Failure
import com.sun.net.httpserver.Authenticator.Success
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

val parseGame = parser {
    val first = oneOf(rockParser, paperParser, scissorsParser)
    whitespace()
    val second = oneOf(rockParser, paperParser, scissorsParser)
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
    val first = oneOf(rockParser, paperParser, scissorsParser)
    whitespace()
    val second = oneOf(winParser, lossParser, drawParser)
    Pair(first, second)
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
    Pair(RockPaperScissors.ROCK, RockPaperScissors.ROCK) -> WinDrawLoss.DRAW
    Pair(RockPaperScissors.PAPER, RockPaperScissors.PAPER) -> WinDrawLoss.DRAW
    Pair(RockPaperScissors.SCISSORS, RockPaperScissors.SCISSORS) -> WinDrawLoss.DRAW

    Pair(RockPaperScissors.ROCK, RockPaperScissors.PAPER) -> WinDrawLoss.WIN
    Pair(RockPaperScissors.ROCK, RockPaperScissors.SCISSORS) -> WinDrawLoss.LOSS

    Pair(RockPaperScissors.PAPER, RockPaperScissors.ROCK) -> WinDrawLoss.LOSS
    Pair(RockPaperScissors.PAPER, RockPaperScissors.SCISSORS) -> WinDrawLoss.WIN

    Pair(RockPaperScissors.SCISSORS, RockPaperScissors.ROCK) -> WinDrawLoss.WIN
    Pair(RockPaperScissors.SCISSORS, RockPaperScissors.PAPER) -> WinDrawLoss.LOSS

    else -> error("impossible")
}

fun determinePlay(p: Pair<RockPaperScissors, WinDrawLoss>) = when (p) {
    Pair(RockPaperScissors.ROCK, WinDrawLoss.WIN) -> RockPaperScissors.PAPER
    Pair(RockPaperScissors.ROCK, WinDrawLoss.LOSS) -> RockPaperScissors.SCISSORS
    Pair(RockPaperScissors.ROCK, WinDrawLoss.DRAW) -> RockPaperScissors.ROCK

    Pair(RockPaperScissors.PAPER, WinDrawLoss.WIN) -> RockPaperScissors.SCISSORS
    Pair(RockPaperScissors.PAPER, WinDrawLoss.LOSS) -> RockPaperScissors.ROCK
    Pair(RockPaperScissors.PAPER, WinDrawLoss.DRAW) -> RockPaperScissors.PAPER

    Pair(RockPaperScissors.SCISSORS, WinDrawLoss.WIN) -> RockPaperScissors.ROCK
    Pair(RockPaperScissors.SCISSORS, WinDrawLoss.LOSS) -> RockPaperScissors.PAPER
    Pair(RockPaperScissors.SCISSORS, WinDrawLoss.DRAW) -> RockPaperScissors.SCISSORS

    else -> error("impossible")
}

fun scoreGame(p: Pair<RockPaperScissors, RockPaperScissors>) =
    scorePlay(p.second) + scoreResult(determineResult(p))

fun scorePartTwo(p: Pair<RockPaperScissors, WinDrawLoss>) = run {
    val game = Pair(p.first, determinePlay(p))
    scoreGame(game)
}

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun dayTwo2022() = coroutineScope {
    val path = Path("inputFiles/dayTwo2022.txt")
    val totalScorePartOne = lines(path)
        .parMapUnordered { parseGame.parse(it) }
        .parMapUnordered { r -> when (r) {
            is ParserResult.Ok -> scoreGame(r.result)
            is ParserResult.Error -> throw Error("Unable to parse game $r")
        }}
        .fold(0) { acc, x -> acc + x }
    println("The part one score is: $totalScorePartOne")

    val totalScorePartTwo = lines(path)
        .parMapUnordered { parseGamePartTwo.parse(it) }
        .parMapUnordered { r -> when (r) {
            is ParserResult.Ok -> scorePartTwo(r.result)
            is ParserResult.Error -> throw Error("Unable to parse game $r")
        }}
        .fold(0) { acc, x -> acc + x }
    println("The part two score is: $totalScorePartTwo")
}