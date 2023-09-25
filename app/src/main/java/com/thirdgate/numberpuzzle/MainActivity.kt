package com.thirdgate.numberpuzzle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.thirdgate.numberpuzzle.ui.theme.NumberPuzzleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NumberPuzzleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold( topBar = {
        TopAppBar(
            title = {
                Text(text = "Number Puzzle Widget")
            },
            Modifier.background(color = MaterialTheme.colorScheme.primary),
            //contentColor = MaterialTheme.colors.onPrimary,
            actions = {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Settings")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                        text = { Text(text = "About App") },
                        onClick = {      // Create and launch the Intent here
                            val intent = Intent(context, AboutActivity::class.java)
                            context.startActivity(intent)
                            showMenu = false  // close the menu after launching the activity}
                        }
                    )
                }
            }
        )
    } ) {  padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)){
            Text(
                text = "Hi, this is a simple puzzle game. It comes with a widget, so feel free to try that out too!", modifier=Modifier.padding(20.dp)
            )





            NumberGame()

        }
    }

}


