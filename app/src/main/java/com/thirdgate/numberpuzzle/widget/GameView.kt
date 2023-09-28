package com.thirdgate.numberpuzzle.widget

import android.content.Context
import android.util.Log

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalGlanceId
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.thirdgate.numberpuzzle.Board
import com.thirdgate.numberpuzzle.R
import com.thirdgate.numberpuzzle.onCellClick
import com.thirdgate.numberpuzzle.sumInversions
import com.thirdgate.numberpuzzle.ui.theme.colorSets


@Composable
fun PuzzleGameGlance(context:Context, widgetInfo:WidgetInfo) {

    // Mutable state to keep track of the current index
    var colorIndex by remember { mutableStateOf(0) }

    // Fetch the current set of colors
    val colors = colorSets[colorIndex]

    val startColor = colors.first
    val endColor = colors.second

    var isGameOver = false

    var resetCounter by remember { mutableStateOf(0) }


    // Update the index for the next recomposition
    LaunchedEffect(resetCounter) {
        colorIndex = (colorIndex + 1) % colorSets.size
    }
    val glanceId = LocalGlanceId.current

    val numGames = widgetInfo.games
    val numWins = widgetInfo.wins
    var rows = widgetInfo.rows
    var columns = widgetInfo.columns
    var boardState = widgetInfo.boardState


   val board: MutableState<Board>
    if (boardState == null) {
        board = remember{ mutableStateOf(Board(rows=rows, cols=columns)) }
        Log.i("Widget", "Start Size=$rows,$columns: ${board.value.grid[0][0].number},${board.value.grid[0][1].number} create new")
    }
    else{
        board = remember { mutableStateOf(boardState) }
        Log.i("Widget", "Start Size=$rows,$columns: ${board.value.grid[0][0].number},${board.value.grid[0][1].number} loaded existing")
    }

    Log.i("Widget", "board ${board.value.grid.flatMap { it.toList() }.joinToString(", ") { it.number.toString() }}")

    GlanceTheme {
        Row(modifier = GlanceModifier.fillMaxWidth()){
        //GameTitleText(numWins, endColor)
            val displayText =if (numWins > 0)  "Completed Puzzles: $numWins" else "Number Puzzle"
            Text(
                text = displayText,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color= ColorProvider(endColor)
                ),
                modifier = GlanceModifier.defaultWeight()
            )

        Image(
            provider = ImageProvider(R.drawable.round_refresh_24),
            modifier = GlanceModifier.clickable {
                board.value = Board(rows = rows, cols = columns)
                actionRunCallback<RefreshAction>()
            },
            contentDescription = "Refresh",
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground)

        )
        }
        Log.i("Widget", "board ${board.value.grid.flatMap { it.toList() }.joinToString(", ") { it.number.toString() }} LazyRow Before")
        LazyColumn(
            modifier = GlanceModifier.padding(2.dp).background(endColor),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Log.i("Widget", "board ${board.value.grid.flatMap { it.toList() }.joinToString(", ") { it.number.toString() }} LazyRow Inside, before start")
            itemsIndexed(board.value.grid) { rowIndex, row ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = GlanceModifier.background(Color.Gray).fillMaxWidth()
                    ) {
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            Log.i("Widget", "board ${board.value.grid.flatMap { it.toList() }.joinToString(", ") { it.number.toString() }} LazyRow Inside cell")
                            row.forEachIndexed { colIndex, cell ->
                                val positionRatio =
                                    if (cell.number == -1) 1f else (cell.number - 1).toFloat() / (rows * columns - 1)
                                //Log.i("Game", "colorpos=$positionRatio")
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
                                            Log.i("Widget", "board ${board.value.grid.flatMap { it.toList() }.joinToString(", ") { it.number.toString() }} deepCopy before")
                                            val updatedBoard = board.value.deepCopy()
                                            board.value = updatedBoard
                                            Log.i("Widget", "board ${board.value.grid.flatMap { it.toList() }.joinToString(", ") { it.number.toString() }} deepCopy after")

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
                                if (colIndex < board.value.grid[rowIndex].indices.last) {
                                    Spacer(
                                        modifier = GlanceModifier
                                            .width(2.dp)
                                            .background(GlanceTheme.colors.background)
                                    )
                                }
                            }
                        }
                        if (rowIndex < board.value.grid[rowIndex].indices.last) {
                            Spacer(
                                modifier = GlanceModifier
                                    .height(2.dp)
                                    .padding(top = 2.dp, bottom = 2.dp)
                                    .background(GlanceTheme.colors.background)
                            )
                        }
                    }
                }
            }
            item {
                val checkInversions = sumInversions(board.value.grid)
                if ((checkInversions <= 1 && board.value.grid[rows-1][columns-1].number == -1) || isGameOver) {
                    Spacer(modifier = GlanceModifier.height(32.dp))
                    if (!isGameOver) {
                        isGameOver = true
                        Log.i("Game", "Inversions=$checkInversions resetting game")
                        var games: Int = numGames + 1
                        var wins: Int = numWins + 1
                        updateWidgetInfo(
                            context = context,
                            glanceWidgetId = glanceId,
                            wins = wins,
                            games = games,
                            boardState=board.value
                        )
                    }
                    GameOverView(endColor) {
                        resetCounter++
                        board.value = Board(rows=rows, cols = columns)
                        actionRunCallback<RefreshAction>()
                    }
                } else {
                    updateWidgetInfo(
                        context = context,
                        glanceWidgetId = glanceId,
                        wins = widgetInfo.wins,
                        games = widgetInfo.games,
                        boardState=board.value
                    )
                }
            }

        }
    }
}



@Composable
fun GameTitleText(wins:Int, myColor:Color) {
    val displayText =if (wins > 0)  "Puzzles Completed: $wins" else "Sort starting from 1"
    Text(
            text = displayText,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color= ColorProvider(myColor)
            ),
            modifier = GlanceModifier
        )
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

class RefreshAction() : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Force the worker to refresh
        MyWidget().update(context, glanceId)
    }
}

