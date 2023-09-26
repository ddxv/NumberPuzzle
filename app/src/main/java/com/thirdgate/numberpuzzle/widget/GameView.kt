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
import androidx.glance.Button
import androidx.glance.ButtonColors
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.thirdgate.numberpuzzle.deepCopy
import com.thirdgate.numberpuzzle.initialBoard
import com.thirdgate.numberpuzzle.onCellClick
import com.thirdgate.numberpuzzle.sumInversions
import com.thirdgate.numberpuzzle.ui.theme.colorSets


@Composable
fun PuzzleGameGlance(glanceId: GlanceId, context:Context, numWins:Int, numGames:Int, rows:Int, columns:Int) {

    // Mutable state to keep track of the current index
    var colorIndex by remember { mutableStateOf(0) }

    // Fetch the current set of colors
    val colors = colorSets[colorIndex]

    val startColor = colors.first
    val endColor = colors.second

    var isGameOver = false

    var resetCounter by remember { mutableStateOf(0) }

    val board = remember { mutableStateOf(initialBoard(rows = rows, columns = columns)) }

    // Update the index for the next recomposition
    LaunchedEffect(resetCounter) {
        colorIndex = (colorIndex + 1) % colorSets.size
    }

    GlanceTheme {
        GameTitleText(numWins, endColor)
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
                                        .background(myBoxColor)
                                        .clickable {
                                            onCellClick(board, rowIndex, colIndex)
                                            val updatedBoard =
                                                board.value.deepCopy()
                                            board.value =
                                                updatedBoard

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
                                            style = TextStyle(
                                                fontSize = 40.sp,
                                                color = ColorProvider(Color.White)
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = GlanceModifier.height(32.dp))
                val checkInversions = sumInversions(board.value)
                if ((checkInversions <= 1 && board.value[rows-1][columns-1].number == -1) || isGameOver) {
                    if (!isGameOver) {
                        isGameOver = true
                        Log.i("Game", "Inversions=$checkInversions resetting game")
                        var games: Int = numGames + 1
                        var wins: Int = numWins + 1
                        updateWidgetInfo(
                            context = context,
                            glanceWidgetId = glanceId,
                            wins = wins,
                            games = games
                        )
                    }
                    GameOverView(endColor) {
                        resetCounter++
                        board.value = initialBoard(rows = rows, columns = columns)
                        actionRunCallback<RefreshAction>()
                    }
                } else {
                    //GamesWonText(numWins = numWins, games = numGames)
                }
            }

        }
    }
}



@Composable
fun GameTitleText(wins:Int, myColor:Color) {
    val displayText =if (wins > 0)  "Puzzles Completed: $wins" else "Sort starting from 1"
    Row(horizontalAlignment = Alignment.CenterHorizontally, modifier = GlanceModifier.fillMaxWidth()) {
        Text(
            text = displayText,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color= ColorProvider(myColor)
            )
        )
    }
}

@Composable
fun GamesWonText(numWins: Int, games:Int) {
    val displayText = "Puzzles completed: $numWins"
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

@Composable
fun GameOverView(myColor:Color, onReset: () -> Unit) {
    Spacer(modifier = GlanceModifier.height(20.dp))
    Button(
        text = "New Puzzle",
        onClick = onReset,
        modifier = GlanceModifier.background(color = myColor),
    )
}

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

