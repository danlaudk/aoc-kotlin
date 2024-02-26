
import dayTen.dayTen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun main() {
    dayTen()
    // also doesn't work complains no context receiver found for dayTen.dayTen() CLASS DayTen doesn't have context receiver
//    val dayTen = DayTen()
//    dayTen.dayTen()
}
