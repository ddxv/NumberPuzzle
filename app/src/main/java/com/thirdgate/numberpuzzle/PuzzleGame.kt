package com.thirdgate.numberpuzzle

import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import kotlin.math.abs




data class NumberBlock(var number: Int)

// Top-level variable
lateinit var emptyBlockPosition : Pair<Int, Int>


fun countInversions(board: Array<Array<NumberBlock>>, width: Int): Int {
    val rows = board.size
    val cols = board[0].size


    var inversions: Int = 0
    for (i in 0 until rows) {
        for (j in 0 until cols) {
            for (k in i until rows) {
                for (l in (if (i == k) j + 1 else 0) until cols) {
                    Log.i("Game", "countInversion: $i,$j tv=${board[i][j].number} > kl=$k,$l ${board[k][l].number}")
                    if (board[i][j].number > board[k][l].number) {
                        inversions++
                    }
                }
            }
        }
    }
    return inversions
}

fun sumInversions(board: Array<Array<NumberBlock>>, width: Int): Int {
    val inversions = countInversions(board, width)
    Log.i("Game", "sumInversions:$inversions")
    return inversions
}

fun isSolvable(board: Array<Array<NumberBlock>>, width: Int, height: Int, emptyRow: Int): Boolean {
    return if (width % 2 == 1) { // odd
        Log.i("Game", "isSolvable width is odd, start")
        sumInversions(board, width=width) % 2 == 0
    } else { // even
        Log.i("Game", "isSolvable width is even, start")
        (sumInversions(board, width) + height - emptyRow) % 2 == 0
    }
}


fun initialBoard(rows: Int, columns: Int): Array<Array<NumberBlock>> {
    val board = Array(rows) { Array(columns) { NumberBlock(0) } }
    val numbers = MutableList((rows * columns)) { it +1 }.apply{shuffle()}

    val indexOfMax = numbers.indexOf(numbers.maxOrNull())

    // Swap the highest number with the last element
    numbers[indexOfMax] = numbers.last()
    numbers[numbers.size - 1] = rows * columns
    Log.i("Game", "My numbers=$numbers")

    // Set one block to -1 (empty)
    numbers[numbers.size - 1] = -1


    for (r in 0 until rows) {
        for (c in 0 until columns) {
            val number = numbers[r * columns + c]
            board[r][c] = NumberBlock(number)
            if (number == -1) {
                emptyBlockPosition = Pair(r, c)
            }
        }
    }

    if (!isSolvable(board, rows, columns, emptyBlockPosition.first)) {
        Log.w("Game", "board not winnable try again")
       return initialBoard(rows, columns)
    }

    return board
}

fun Array<Array<NumberBlock>>.deepCopy(): Array<Array<NumberBlock>> {
    return Array(this.size) { this[it].clone() }
}




@Composable
fun NumberGame() {

    // Define the sets of colors
    val colorSets = listOf(
        Pair(Color(0xFFD449FF), Color(0xFF6CBAFF)),
        Pair(Color(0xFFF837A4), Color(0xFFD6EE6F)),
        Pair(Color(0xFFF7DE00), Color(0xFF0E9785)),
        Pair(Color(0xFF7647FF), Color(0xFFFF7A38)),
        Pair(Color(0xFF926119), Color(0xFF186188))
    )

    // Mutable state to keep track of the current index
    var colorIndex by remember { mutableStateOf(0) }




    // Fetch the current set of colors
    val colors = colorSets[colorIndex]

    val startColor = colors.first
    val endColor = colors.second

    val rows = 4
    val columns = 4
    val board = remember { mutableStateOf(initialBoard(rows = rows, columns = columns)) }

    // Update the index for the next recomposition
    LaunchedEffect(board.value) {
        colorIndex = (colorIndex + 1) % colorSets.size
    }

    fun isAdjacentToEmptyBlock(pos1: Pair<Int, Int>, pos2: Pair<Int, Int>): Boolean {
        return (abs(pos1.first - pos2.first) == 1 && pos1.second == pos2.second) ||
                (abs(pos1.second - pos2.second) == 1 && pos1.first == pos2.first)
    }

    fun onCellClick(clickRow: Int, clickCol: Int) {
        if (isAdjacentToEmptyBlock(Pair(clickRow, clickCol), emptyBlockPosition)) {
            val clickedBlock = board.value[clickRow][clickCol]

            board.value[clickRow][clickCol] = NumberBlock(-1)

            Log.i(
                "Game",
                "click=$clickRow,$clickCol e:$emptyBlockPosition:${board.value[emptyBlockPosition.first][emptyBlockPosition.second]}"
            )
            board.value[emptyBlockPosition.first][emptyBlockPosition.second].number =
                clickedBlock.number
            // Update the empty block's position
            emptyBlockPosition = Pair(clickRow, clickCol)
            Log.i(
                "Game",
                "click=$clickRow,$clickCol e:$emptyBlockPosition:${board.value[emptyBlockPosition.first][emptyBlockPosition.second]}"
            )
        } else {
            Log.w("Game", "click $clickRow,$clickCol not adjacent to $emptyBlockPosition")
        }

    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
            board.value.forEachIndexed { rowIndex, row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    row.forEachIndexed { colIndex, cell ->
//                        val positionRatio = (rowIndex * rows + colIndex).toFloat() / (rows * columns - 1)
//                        val interpolatedColor = lerp(startColor, endColor, positionRatio)

                        val positionRatio = if (cell.number == -1) 1f else (cell.number - 1).toFloat() / (rows * columns - 1)
                        val interpolatedColor = lerp(startColor, endColor, positionRatio)


                        val myBoxColor: Color
                        val myTextColor: Color
                        if (cell.number == -1) {
                            myBoxColor = endColor
                            myTextColor = MaterialTheme.colorScheme.onPrimary
                        } else {
                            myBoxColor = interpolatedColor
                            myTextColor = MaterialTheme.colorScheme.onPrimary
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(myBoxColor)
                                .border(width = 1.dp, color = MaterialTheme.colorScheme.background, shape = RectangleShape)
                                .clickable {
                                    onCellClick(rowIndex, colIndex)
                                    val updatedBoard = board.value.deepCopy()  // Create a deep copy
                                    board.value =
                                        updatedBoard  // Assign the updated board to the state, triggering recomposition

                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (cell.number != -1) {

                                Text(
                                    text = cell.number.toString(),
                                    style = TextStyle(fontSize = 40.sp),
                                    color = myTextColor
                                )
                            }
                            if (cell.number == -1) {
                                Text(
                                    text = "",
                                    style = TextStyle(fontSize = 18.sp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        ResetButton(startColor) {
            // This will reset the game state
            board.value = initialBoard(rows, columns)
        }
    }
}

@Composable
fun ResetButton(myColor:Color, onClick: () -> Unit) {
    androidx.compose.material3.Button(onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = myColor)
    ) { Text("Reset")
    }
}





