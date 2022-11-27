package dayTen

import arrow.fx.coroutines.*
import cc.ekblad.konbini.*
import kotlinx.coroutines.*
import kotlin.io.path.Path
import flows.*

enum class Bracket {
    PARENS,
    SQUARE,
    CURLY,
    ANGLE
}

sealed class RoseTree<out T : Any>
data class RoseNode<out T : Any>(val x: T, val xs: List<RoseTree<T>>) : RoseTree<T>()

fun makeBracketParser(begin: Char, end: Char, bracket: Bracket): Parser<RoseTree<Bracket>> =
    parser {
        oneOf(parser { char(begin) })
        val internal = roseTreeParser()
        oneOf(parser { char(end) })
        RoseNode(bracket, internal)
    }

val parensParser: Parser<RoseTree<Bracket>> =
    makeBracketParser('(', ')', Bracket.PARENS)

val squareParser: Parser<RoseTree<Bracket>> =
    makeBracketParser('[', ']', Bracket.SQUARE)

val curlyParser: Parser<RoseTree<Bracket>> =
    makeBracketParser('{', '}', Bracket.CURLY)

val angleParser: Parser<RoseTree<Bracket>> =
    makeBracketParser('<', '>', Bracket.ANGLE)

// NOTE: This parser is not actually useful for the problem, however
// it is an interesting parser.
val roseTreeParser: Parser<List<RoseTree<Bracket>>> =
    many(oneOf(parensParser, squareParser, curlyParser, angleParser))

@FlowPreview
suspend fun dayTen2021() = coroutineScope {
    // Again, not that useful...
    val path = Path("inputFiles/dayTen2021.test.txt")
    lines(path)
        .parMapUnordered { roseTreeParser.parse(it) }
        .collect { println(it) }

    // This one is correct, which is neat!
    println(roseTreeParser.parse("{<()><{}>{}}<()(([]))>"))
}