import dayOne.dayOne
import kotlinx.coroutines.*

@ExperimentalCoroutinesApi
@FlowPreview
fun main() {
    runBlocking {
        dayOne()
    }
}