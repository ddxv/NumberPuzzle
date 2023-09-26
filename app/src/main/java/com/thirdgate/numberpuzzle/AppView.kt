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

    var resetCounter by remember { mutableStateOf(0) }

    val rows = 4
    val columns = 4
    val board = remember { mutableStateOf(initialBoard(rows = rows, columns = columns)) }

    // Update the index for the next recomposition
    LaunchedEffect(resetCounter) {
        colorIndex = (colorIndex + 1) % colorSets.size
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
            board.value.forEachIndexed { rowIndex, row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    row.forEachIndexed { colIndex, cell ->
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
                                    onCellClick(board, rowIndex, colIndex)
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
            resetCounter++
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
