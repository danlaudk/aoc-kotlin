
import arrow.core.raise.Raise
import arrow.core.raise.fold
import dayTen.ParseError
import dayTen.dayTen
import kotlinx.coroutines.runBlocking

suspend fun main() {
    fold({ dayTen() }, { println(it) }, { println(it) })
    // also doesn't work complains no context receiver found for dayTen.dayTen() CLASS DayTen doesn't have context receiver
//    val dayTen = DayTen()
//    dayTen.dayTen()
}

//fun makeExplicit() {
//    val raiseContext = object : Raise<ParseError> {}
//
//    runBlocking {
////        assert(this is CoroutineScope)
//
//        with(raiseContext) {
//            assert(this is Raise<ParseError>)
//
//            dayTen()
//        }
//    }
//}
