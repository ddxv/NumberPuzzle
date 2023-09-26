package com.thirdgate.numberpuzzle.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalGlanceId
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

class MyWidget : GlanceAppWidget() {

    override val stateDefinition = GlanceButtonWidgetStateDefinition()
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
        val numGames = widgetInfo.games
        val numWins = widgetInfo.wins
        val glanceId = LocalGlanceId.current

        Log.i("MyWidget", "Content: numGames=$numGames: check numWins=$numWins")

        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(GlanceTheme.colors.background)
                    .cornerRadius(8.dp)
            ) {
                            Log.i("MyWidget", "Content: got imageProvider")
                            PuzzleGameGlance(glanceId, context, numWins, numGames)
                    }

            }
        }
    }

@Composable
fun updateWidgetInfo(context:Context, glanceWidgetId:GlanceId, wins:Int, games:Int) {
    LaunchedEffect(key1=Unit) {
        updateAppWidgetState(context = context,
            glanceId = glanceWidgetId,
            definition = GlanceButtonWidgetStateDefinition(),
            updateState = { widgetInfo ->
                WidgetInfo(
                    games = games,
                    wins = wins,
                )
            }
        )
        Log.i("WidgetConfig", "$glanceWidgetId: updateAppWidgetState done")
        MyWidget().update(context, glanceWidgetId)
        Log.i("WidgetConfig", "$glanceWidgetId update done")
    }
}


class GameWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MyWidget()
}
