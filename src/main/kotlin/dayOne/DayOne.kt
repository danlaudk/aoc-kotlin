package dayOne

import java.io.File

suspend fun dayOne() {
    File("inputFiles/dayOne.test.txt").forEachLine {
        line -> println(line)
    }
}