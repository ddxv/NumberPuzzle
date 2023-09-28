package com.thirdgate.numberpuzzle.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import com.thirdgate.numberpuzzle.Board

class MyWidget : GlanceAppWidget() {

    override val stateDefinition = MyWidgetStateDefinition()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.i("MyWidget", "provideGlance started")
        provideContent {
            Content(context)
        }
    }

    @Composable
    fun Content(context:Context) {
        Log.i(
            "MyWidget",
            "Content: start"
        )


        val widgetInfo = currentState<WidgetInfo>()

        if (widgetInfo.boardState == null) {
            Log.i(
                "Widget",
                "board is NULL!"
            )
        }
        else {
            Log.i(
                "Widget",
                "board ${
                    widgetInfo.boardState.grid.flatMap { it.toList() }
                        .joinToString(", ") { it.number.toString() }
                } MyWidget provideGlance loaded board"
            )
        }

        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(GlanceTheme.colors.background)
                    .cornerRadius(8.dp)
            ) {
                            PuzzleGameGlance(context, widgetInfo = widgetInfo)
                    }

            }
        }
    }

@Composable
fun updateWidgetInfo(context:Context, glanceWidgetId:GlanceId, wins:Int, games:Int, boardState: Board) {
    Log.i("Widget", "board ${boardState.grid.flatMap { it.toList() }.joinToString(", ") { it.number.toString() }} updateWidgetInfo launch save info!")

    LaunchedEffect(key1=boardState) {
        updateAppWidgetState(context = context,
            glanceId = glanceWidgetId,
            definition = MyWidgetStateDefinition(),
            updateState = { widgetInfo ->
                WidgetInfo(
                    games = games,
                    wins = wins,
                    rows = widgetInfo.rows,
                    columns = widgetInfo.columns,
                    boardState = boardState
                )
            }
        )
        Log.i("Widget", "updateAppWidgetState for  ${boardState.grid[0][0].number},${boardState.grid[0][1].number} saved to disk")
        //MyWidget().update(context, glanceWidgetId)
        Log.i("Widget", "updateAppWidgetState for  ${boardState.grid[0][0].number},${boardState.grid[0][1].number} called update()")
    }
}


class GameWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MyWidget()
}
