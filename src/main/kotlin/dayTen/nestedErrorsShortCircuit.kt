//import arrow.core.Either
//import arrow.core.Left
//import arrow.core.Right
//
//// Simulating some computations that can fail
//fun computeA(): Either<String, Int> = Left("Error in computeA")
//fun computeB(a: Int): Either<String, Int> = Left("Error in computeB")
//fun computeC(b: Int): Either<String, Int> = Left("Error in computeC")
//
//fun main() {
//    val result: Either<String, Int> = computeA()
//        .mapLeft { "Error in computeA: $it" }
//        .flatMap { a ->
//            computeB(a).mapLeft { "Error in computeB: $it" }
//                .flatMap { b ->
//                    computeC(b).mapLeft { "Error in computeC: $it" }
//                }
//        }
//
//    // Handling the result
//    when (result) {
//        is Right -> println("The final result is: ${result.value}")
//        is Left -> println("An error occurred: ${result.value}")
//    }
//}
