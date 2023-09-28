package com.thirdgate.numberpuzzle

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.thirdgate.numberpuzzle.ui.theme.colorSets
import com.thirdgate.numberpuzzle.widget.GameWidgetReceiver
import com.thirdgate.numberpuzzle.widget.MyWidget

@Composable
fun NumberGame() {

    // Mutable state to keep track of the current index
    var colorIndex by remember { mutableStateOf(0) }

    // Fetch the current set of colors
    val colors = colorSets[colorIndex]

    val startColor = colors.first
    val endColor = colors.second

    var resetCounter by remember { mutableStateOf(0) }

    val rows = 4
    val columns = 4
    val board = remember { mutableStateOf(Board(rows = rows, cols = columns)) }

    // Update the index for the next recomposition
    LaunchedEffect(resetCounter) {
        colorIndex = (colorIndex + 1) % colorSets.size
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier
            .background(Color.Gray)
            .fillMaxWidth()) {
            board.value.grid.forEachIndexed { rowIndex, row ->
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
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.background,
                                    shape = RectangleShape
                                )
                                .clickable {
                                    onCellClick(board, rowIndex, colIndex)
                                    Log.i("Copy", "eb1=${board.value.emptyBlockPosition}")
                                    val updatedBoard = board.value.deepCopy()
                                    Log.i("Copy", "eb2=${board.value.emptyBlockPosition}")
                                    board.value = updatedBoard

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
            board.value = Board(rows, columns)
            resetCounter++
        }
    }
}

@Composable
fun ResetButton(myColor:Color, onClick: () -> Unit) {
    Button(onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = myColor)
    ) { Text("Reset")
    }
}

@Composable
fun PinWidgetButton() {
    val context = LocalContext.current

    val appWidgetManager = AppWidgetManager.getInstance(context)
    val myProvider = ComponentName(context, GameWidgetReceiver::class.java)

    Button(onClick = {
        if (appWidgetManager.isRequestPinAppWidgetSupported()) {
            // Create the PendingIntent object only if your app needs to be notified
            // when the user chooses to pin the widget. Note that if the pinning
            // operation fails, your app isn't notified. This callback receives the ID
            // of the newly pinned widget (EXTRA_APPWIDGET_ID).
//            val successCallback = PendingIntent.getBroadcast(
//                context,
//                0,
//                Intent(),
//                PendingIntent.FLAG_UPDATE_CURRENT
//            )

            appWidgetManager.requestPinAppWidget(myProvider, null, null)
        }
    }) {
        Text(text = "Add Widget to Home Screen")
    }
}


