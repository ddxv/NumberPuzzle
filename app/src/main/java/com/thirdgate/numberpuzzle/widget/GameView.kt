package com.thirdgate.numberpuzzle.widget

import android.content.Context
import android.util.Log

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.thirdgate.numberpuzzle.deepCopy
import com.thirdgate.numberpuzzle.initialBoard
import com.thirdgate.numberpuzzle.onCellClick
import com.thirdgate.numberpuzzle.ui.theme.colorSets


@Composable
fun PuzzleGameGlance(glanceId: GlanceId, context:Context, numWins:Int, numGames:Int) {

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


//    val boardSize = 3
//    var currentPlayer = remember { mutableStateOf(Player.X) }
//    var board = remember { mutableStateOf(Array(boardSize) { Array(boardSize) { Player.NONE } }) }
//    var winner:MutableState<Player> = remember { mutableStateOf(Player.NONE) }
//    val lineEdgePadding = 20.dp

    // TODO: Use?
    val widgetSize = LocalSize.current
    val boxSize = widgetSize.height.value

    // Update the index for the next recomposition
    LaunchedEffect(resetCounter) {
        colorIndex = (colorIndex + 1) % colorSets.size
    }

    Log.i("Widget", "WidgetGameStart boxSize=$boxSize")

    GlanceTheme {
        GameStatusText(numWins)
        Text("Number puzzle widget game")
        LazyColumn(
            modifier = GlanceModifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            itemsIndexed(board.value) { rowIndex, row ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = GlanceModifier.background(Color.Gray).fillMaxWidth()
                    ) {
                            Row(modifier = GlanceModifier.fillMaxWidth()) {
                                row.forEachIndexed { colIndex, cell ->
                                    val positionRatio =
                                        if (cell.number == -1) 1f else (cell.number - 1).toFloat() / (rows * columns - 1)
                                    val interpolatedColor =
                                        lerp(startColor, endColor, positionRatio)

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
                                        modifier = GlanceModifier
                                            .defaultWeight()
                                            //.aspectRatio(1f)
                                            .background(myBoxColor)
//                                            .border(
//                                                width = 1.dp,
//                                                color = MaterialTheme.colorScheme.background,
//                                                shape = RectangleShape
//                                            )
                                            .clickable {
                                                onCellClick(board, rowIndex, colIndex)
                                                val updatedBoard =
                                                    board.value.deepCopy()  // Create a deep copy
                                                board.value =
                                                    updatedBoard  // Assign the updated board to the state, triggering recomposition

                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (cell.number != -1) {
                                            Text(
                                                text = cell.number.toString(),
                                                style = TextStyle(
                                                    fontSize = 40.sp,
                                                    color = ColorProvider(myTextColor)
                                                ),
                                            )
                                        }
                                        if (cell.number == -1) {
                                            Text(
                                                text = "",
                                                style = TextStyle(fontSize = 40.sp, color= ColorProvider(Color.White)),
                                            )
                                        }
                                    }
                                //}
                            }
                        }
                    }
//                    ResetButton(startColor) {
//                        // This will reset the game state
//                        board.value = initialBoard(rows, columns)
//                        resetCounter++
//                    }
                }
            }

        }
        Text("Bottom of Widget!")
        GamesWonText(numWins = numWins, games = numGames)
        //Spacer(modifier = GlanceModifier.height(32.dp))
    }
}


//@Composable
//fun ResetButton(myColor: Color, onClick: () -> Unit) {
//    Button(
//        onClick = onClick,
//        colors = ButtonDefaults.buttonColors(containerColor = myColor)
//    ) {
//        Text("Reset")
//    }
//}

@Composable
fun GameStatusText(wins:Int) {
    val displayText =if (wins > 0)  "Puzzle's Completed: $wins" else "Sort starting from 1"
    Row(horizontalAlignment = Alignment.CenterHorizontally, modifier = GlanceModifier.fillMaxWidth()) {
        Text(
            text = displayText,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color=GlanceTheme.colors.onBackground
            )
        )
    }
}

@Composable
fun GamesWonText(numWins: Int, games:Int) {
    val displayText = if (games > 0) {
       "Games: $games, Won: $numWins, Draw: ${games - (numWins)}"
    }
    else {
        ""
    }
    Row(horizontalAlignment = Alignment.CenterHorizontally, modifier = GlanceModifier.fillMaxWidth()) {
        Text(
            text = displayText,
            style = TextStyle(
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color=GlanceTheme.colors.onBackground
            )
        )
    }
}

//@Composable
//fun GameOverView(onReset: () -> Unit) {
//    Spacer(modifier = GlanceModifier.height(20.dp))
//    Button(
//        text = "Reset",
//        onClick = onReset
//    )
//}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Force the worker to refresh
        MyWidget().update(context, glanceId)
    }
}

