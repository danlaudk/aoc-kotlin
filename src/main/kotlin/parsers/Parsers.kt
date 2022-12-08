package parsers

import cc.ekblad.konbini.*

val digitParser = parser {
    val digit = char()
    if (digit in "0123456789") {
        digit.toString().toInt()
    } else {
        fail("$digit isn't 0..9")
    }
}

val parseMany1Digits = parser {
    many1(digitParser)
}