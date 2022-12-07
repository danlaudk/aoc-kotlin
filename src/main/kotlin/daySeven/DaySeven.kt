package daySeven

import arrow.fx.coroutines.parMap
import cc.ekblad.konbini.*
import flows.lines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlin.io.path.Path

sealed class CommandLineAction
object MoveToRoot : CommandLineAction()
data class ChangeDirectory(val value: String) : CommandLineAction()
object MoveUpDirectory : CommandLineAction()
object ListDirectory : CommandLineAction()
data class InsertDirectory(val directoryName: String) : CommandLineAction()
data class InsertFile(val fileName: String, val fileSize: Int) : CommandLineAction()

val parseFile = parser {
    val fileSize = integer()
    whitespace()
    val fileName = many1(char)
    InsertFile(fileName.toString(), fileSize.toInt())
}

val parseDirectory = parser {
    string("dir")
    whitespace()
    val directoryName = many1(char)
    InsertDirectory(directoryName.toString())
}

val parseListDirectory = parser {
    char('$')
    whitespace()
    string("ls")
    ListDirectory
}

val parseDirectoryMovement = parser {
    char('$')
    whitespace()
    string("cd")
    whitespace()
    when (val movement = many1(char)) {
        listOf('/') -> MoveToRoot
        listOf('.', '.') -> MoveUpDirectory
        else -> ChangeDirectory(movement.toString())
    }
}

val parseCommandLineOutput = parser {
    oneOf(parseFile, parseDirectory, parseListDirectory, parseDirectoryMovement)
}

sealed class RoseTree<out T : Any>

// terminal branch, i.e. `File`
data class RoseLeaf<out T : Any>(val leaf: T) : RoseTree<T>()

// non-terminal branch, i.e. `Directory`
data class RoseNode<out T : Any>(val leaf: T, val node: List<RoseTree<T>>) : RoseTree<T>()

data class RoseZipper<out T : Any>(
    val focus: RoseTree<T>,
    val depth: Int,
    val unfocused: RoseTree<T>?
)

sealed class FileOrDirectory
data class File(val name: String) : FileOrDirectory()
data class Directory(val name: String) : FileOrDirectory()

@FlowPreview
@ExperimentalCoroutinesApi
suspend fun daySeven() = coroutineScope {
    val path = Path("inputFiles/daySeven.test.txt")
    val commandLineOutput = lines(path)
        .parMap { parseCommandLineOutput.parse(it) }
        .toList()
    println(commandLineOutput)

    val roseZipper: RoseZipper<FileOrDirectory> =
        RoseZipper(
            RoseNode(Directory("/"), emptyList()), // initial empty root directory
            0, // no subdirectories
            null, // nothing above the root directory
        )

    // ChangeDirectory('hello')
    val roseZipperAfterChangeDirectory: RoseZipper<FileOrDirectory> =
        RoseZipper(
            RoseNode(Directory("hello"), emptyList()),
            1,
            RoseNode(Directory("/"), emptyList())
        )

    // MoveUpDirectory && ChangeDirectory("world")
    val roseZipperAfterActions: RoseZipper<FileOrDirectory> =
        RoseZipper(
            RoseNode(Directory("world"), emptyList()),
            1,
            RoseNode(Directory("/"), listOf(RoseNode(Directory("hello"), emptyList())))
        )

    commandLineOutput.fold(roseZipper) { acc, p ->
        when (p) {
            is ParserResult.Ok -> when (p.result) {
                MoveToRoot -> acc
                is ChangeDirectory -> acc
                MoveUpDirectory -> acc
                ListDirectory -> acc
                is InsertDirectory -> acc
                is InsertFile -> acc
            }

            is ParserResult.Error -> error("Something broke...")
        }
    }
}