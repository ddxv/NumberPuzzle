package com.thirdgate.numberpuzzle.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.launch

class ConfigurationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context: Context = this
        val glanceWidgetId: GlanceId

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        fun finishActivity(result: Int) {
            val resultValue =
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(result, resultValue)
            finish()
        }

        // Try block because getGlanceIdBy throws IllegalArgumentException if no GlanceId is found for this appWidgetId.
        try {
            val glanceAppWidgetManager = GlanceAppWidgetManager(context)
            glanceWidgetId = glanceAppWidgetManager.getGlanceIdBy(appWidgetId)

            val glanceAppWidget = MyWidget()
            setContent {
                CompositionLocalProvider(LocalContext provides context) {
                    ConfigurationScreen(
                        glanceWidgetId = glanceWidgetId,
                        glanceAppWidget,
                        ::finishActivity
                    )
                }
            }

            //glanceAppWidget.update(context, glanceWidgetId)
        } catch (e: IllegalArgumentException) {
            Log.d("WidgetConfig", "No GlanceId found for this appWidgetId.")
            setContent {
                CompositionLocalProvider(LocalContext provides context) {
                    ErrorScreen()
                }
            }
        }
    }
}

@Composable
fun ConfigurationScreen(
    glanceWidgetId: GlanceId,
    glanceApp: MyWidget,
    finishActivity: (Int) -> Unit
) {
    val context = LocalContext.current

    var widgetInfo by remember { mutableStateOf(WidgetInfo()) }
    var isLoaded by remember { mutableStateOf(false) }  // <-- New loading state

    LaunchedEffect(Unit) {
        try {
            widgetInfo = glanceApp.getAppWidgetState<WidgetInfo>(context, glanceWidgetId)
            isLoaded = true  // <-- Update the state once data is loaded
        } catch (e: Exception) {
            Log.w("WidgetConfiguration", "crashed!")
            widgetInfo = WidgetInfo()
        }
    }

    // Based on the loading state, decide to display the UI or a loading spinner.
    if (isLoaded) {
        ConfigurationUI(widgetInfo, glanceApp, glanceWidgetId, finishActivity)
    } else {
        // Show a loading spinner or some placeholder here
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // Example loading spinner from Compose
        }
    }
}

@Composable
fun ConfigurationUI(
    widgetInfo: WidgetInfo,
    glanceApp: MyWidget,
    glanceWidgetId: GlanceId,
    finishActivity: (Int) -> Unit
) {
    val context = LocalContext.current

    var rowChoice by remember { mutableStateOf(widgetInfo.rows) }
    //var columnChoice by remember { mutableStateOf(widgetInfo.columns) }

    LazyColumn {
        items(1) {
            Text("Widget Settings:")
            RowsGroup(
                selectedRowSize = rowChoice,
                onSelectedChanged = { selected -> rowChoice = selected }
            )

            Row {
                FinishButton(
                    context = context,
                    glanceApp = glanceApp,
                    glanceWidgetId = glanceWidgetId,
                    finishActivity = finishActivity,
                    rows = rowChoice,
                    columns = rowChoice
                )
            }
        }
    }
}


@Composable
fun RowsGroup(
    selectedRowSize: Int,
    onSelectedChanged: (Int) -> Unit = {}
) {

    val sizeOptions = listOf(3,4,5
    )

    Column(modifier = Modifier.padding(8.dp)) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize().background(MaterialTheme.colorScheme.background),

        ) {
            Column(Modifier.padding(8.dp)) {
                Row() {
                    Text(
                        "Select The Size of board",
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp
                    )
                }
                Row {
                    sizeOptions.forEach { size ->
                        Button(
                            onClick = {
                                onSelectedChanged(size)
                            },
                            colors = ButtonDefaults.buttonColors(
                                //backgroundColor = if (selectedRowSize == size) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (selectedRowSize == size) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp),
                            content = {Text(size.toString())}
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun FinishButton(
    context: Context,
    glanceApp: GlanceAppWidget,
    glanceWidgetId: GlanceId,
    finishActivity: (Int) -> Unit,
    rows: Int,
    columns: Int,
) {
    val scope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = {
            Log.i(
                "WidgetConfig",
                "$glanceWidgetId: Finish button clicked, set size $rows, $columns"
            )
            scope.launch {
                updateAppWidgetState(context = context,
                    glanceId = glanceWidgetId,
                    definition = MyWidgetStateDefinition(),
                    updateState = { widgetInfo ->
                        WidgetInfo(
                            rows = rows,
                            columns = columns
                        )
                    }
                )
                Log.i("WidgetConfig", "$glanceWidgetId: updateAppWidgetState done")
                glanceApp.update(context, glanceWidgetId)
                Log.i("WidgetConfig", "$glanceWidgetId update done")
                finishActivity(Activity.RESULT_OK)
            }
        }) {
            Text("Finish")
        }
    }
}

@Composable
fun ErrorScreen() {
    Text("Hi: We failed!")
}

