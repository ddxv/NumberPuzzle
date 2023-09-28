package com.thirdgate.numberpuzzle

import android.util.Log
import androidx.compose.runtime.MutableState
import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
data class NumberBlock(var number: Int)

// Top-level variable
//lateinit var emptyBlockPosition : Pair<Int, Int>



fun sumInversions(board: Array<Array<NumberBlock>>): Int {
    val rows = board.size
    val cols = board[0].size

    var inversions: Int = 0
    for (i in 0 until rows) {
        for (j in 0 until cols) {
            for (k in i until rows) {
                for (l in (if (i == k) j + 1 else 0) until cols) {
                    //Log.i("Game", "countInversion: $i,$j tv=${board[i][j].number} > kl=$k,$l ${board[k][l].number}")
                    if (board[i][j].number > board[k][l].number && board[i][j].number != -1 && board[k][l].number != -1) {
                        inversions++
                    }
                }
            }
        }
    }
    Log.i("Game", "sumInversions:$inversions")
    return inversions
}


@Serializable
data class Board(val rows: Int, val cols: Int, val initialGrid: Array<Array<NumberBlock>>? = null) {
    var grid: Array<Array<NumberBlock>>
    var emptyBlockPosition: Pair<Int, Int>

    init {
        grid = initialGrid ?: initializeBoard()
        emptyBlockPosition = findEmptyBlockPosition()
    }

    private fun findEmptyBlockPosition(): Pair<Int, Int> {
        grid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, block ->
                if (block.number == -1) {
                    return Pair(rowIndex, colIndex)
                }
            }
        }
        throw IllegalStateException("No empty block found in the grid")
    }

    private fun initializeBoard(): Array<Array<NumberBlock>> {
        val board = Array(rows) { Array(cols) { NumberBlock(0) } }
        val numbers = MutableList(rows * cols) { it + 1 }.apply { shuffle() }

        val indexOfMax = numbers.indexOf(numbers.maxOrNull())

        // Swap the highest number with the last element
        numbers[indexOfMax] = numbers.last()
        numbers[numbers.size - 1] = rows * cols

        // Set one block to -1 (empty)
        numbers[numbers.size - 1] = -1

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val number = numbers[r * cols + c]
                board[r][c] = NumberBlock(number)
                if (number == -1) {
                    emptyBlockPosition = Pair(r, c)
                }
            }
        }

        if (!isSolvable(board, rows, cols, emptyRow=emptyBlockPosition.first)) {
            return initializeBoard()
        }

        return board
    }

    private fun isSolvable(
        board: Array<Array<NumberBlock>>,
        width: Int,
        height: Int,
        emptyRow: Int
    ): Boolean {
        return if (width % 2 == 1) { // odd
            Log.i("Game", "isSolvable width is odd, start")
            sumInversions(board) % 2 == 0
        } else { // even
            Log.i("Game", "isSolvable width is even, start")
            (sumInversions(board) + height - emptyRow+1) % 2 == 0
        }
    }
    fun deepCopy(): Board {
        val copiedGrid = grid.deepCopy()
        return Board(rows, cols, copiedGrid)
    }
}





fun Array<Array<NumberBlock>>.deepCopy(): Array<Array<NumberBlock>> {
    return Array(this.size) { this[it].clone() }
}


fun onCellClick(boardState: MutableState<Board>, clickRow: Int, clickCol: Int) {
    val board = boardState.value.grid
    val emptyBlockPosition = boardState.value.emptyBlockPosition

    if (isAdjacentToEmptyBlock(Pair(clickRow, clickCol), emptyBlockPosition)) {
        val clickedBlock = board[clickRow][clickCol]
        board[clickRow][clickCol] = NumberBlock(-1)
        Log.i(
            "Game",
            "click=$clickRow,$clickCol e:$emptyBlockPosition:${board[emptyBlockPosition.first][emptyBlockPosition.second]}"
        )
        board[emptyBlockPosition.first][emptyBlockPosition.second].number =
            clickedBlock.number
        // Update the empty block's position
        Log.i("Click", "Change emptyBlock pos ${boardState.value.emptyBlockPosition}")
        boardState.value.emptyBlockPosition = Pair(clickRow, clickCol)
        Log.i("Click", "Changed emptyBlock pos ${boardState.value.emptyBlockPosition}")
        Log.i(
            "Game",
            "click=$clickRow,$clickCol e:$emptyBlockPosition:${board[emptyBlockPosition.first][emptyBlockPosition.second]}"
        )
    } else if (clickRow == emptyBlockPosition.first) {
        if (emptyBlockPosition.second < clickCol) {
            for (i in emptyBlockPosition.second + 1 until clickCol + 1) {
                Log.i("Game", "left to right $i")
                val newNum = board[clickRow][i].number
                board[emptyBlockPosition.first][i - 1].number = newNum
            }
        } else {
            for (i in emptyBlockPosition.second downTo clickCol + 1) {
                Log.i("Game", "YES $i")
                val newNum = board[clickRow][i - 1].number
                board[emptyBlockPosition.first][i].number = newNum
            }
        }
        Log.i(
            "Game",
            "click=$clickRow,$clickCol e:$emptyBlockPosition:${board[emptyBlockPosition.first][emptyBlockPosition.second]}"
        )
        // Update the empty block's position
        board[clickRow][clickCol] = NumberBlock(-1)
        boardState.value.emptyBlockPosition = Pair(clickRow, clickCol)
    } else if (clickCol == emptyBlockPosition.second) {
        if (emptyBlockPosition.first < clickRow) {
            for (i in emptyBlockPosition.first + 1 until clickRow + 1) {
                Log.i("Game", "left to right $i")
                val newNum = board[i][clickCol].number
                board[i - 1][emptyBlockPosition.second].number = newNum
            }
        } else {
            for (i in emptyBlockPosition.first downTo clickRow + 1) {
                Log.i("Game", "YES $i")
                val newNum = board[i - 1][clickCol].number
                board[i][emptyBlockPosition.second].number = newNum
            }
        }
        Log.i(
            "Game",
            "click=$clickRow,$clickCol e:$emptyBlockPosition:${board[emptyBlockPosition.first][emptyBlockPosition.second]}"
        )
        // Update the empty block's position
        board[clickRow][clickCol] = NumberBlock(-1)
        Log.i("Click", "Change emptyBlock pos ${boardState.value.emptyBlockPosition}")
        boardState.value.emptyBlockPosition = Pair(clickRow, clickCol)
        Log.i("Click", "Changed emptyBlock pos ${boardState.value.emptyBlockPosition}")
    } else {
        Log.w("Game", "click $clickRow,$clickCol not adjacent to $emptyBlockPosition")
    }
}

fun isAdjacentToEmptyBlock(pos1: Pair<Int, Int>, pos2: Pair<Int, Int>): Boolean {
    return (abs(pos1.first - pos2.first) == 1 && pos1.second == pos2.second) ||
            (abs(pos1.second - pos2.second) == 1 && pos1.first == pos2.first)
}

