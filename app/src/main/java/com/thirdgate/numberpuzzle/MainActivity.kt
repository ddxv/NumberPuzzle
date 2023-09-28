package com.thirdgate.numberpuzzle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.integration.IntegrationHelper
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener
import com.thirdgate.numberpuzzle.ui.theme.NumberPuzzleTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        //val mIronSourceBannerLayout = IronSource.createBanner(this, ISBannerSize.BANNER)

        super.onCreate(savedInstanceState)
        //mIronSourceBannerLayout.setLevelPlayBannerListener(mLevelPlayBannerListener);

        //Init Banner
        val appKey= "XX"
        IronSource.init(this, appKey, IronSource.AD_UNIT.BANNER);

        IntegrationHelper.validateIntegration(this);

        setContent {
            NumberPuzzleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(this)
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(activity:Activity) {
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

            Text("Banner?")
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context: Context ->
                    val banner = IronSource.createBanner(activity, ISBannerSize.BANNER)

                    banner.levelPlayBannerListener = object : LevelPlayBannerListener {
                        // Invoked each time a banner was loaded. Either on refresh, or manual load.
                        //  AdInfo parameter includes information about the loaded ad
                        override fun onAdLoaded(adInfo: AdInfo) {
                            Log.i("Ads","Loaded")
                        }

                        // Invoked when the banner loading process has failed.
                        //  This callback will be sent both for manual load and refreshed banner failures.
                        override fun onAdLoadFailed(error: IronSourceError) {
                            Log.i("Ads", "AdLoadFailed")
                        }

                        // Invoked when end user clicks on the banner ad
                        override fun onAdClicked(adInfo: AdInfo) {}

                        // Notifies the presentation of a full screen content following user click
                        override fun onAdScreenPresented(adInfo: AdInfo) {}

                        // Notifies the presented screen has been dismissed
                        override fun onAdScreenDismissed(adInfo: AdInfo) {}

                        //Invoked when the user left the app
                        override fun onAdLeftApplication(adInfo: AdInfo) {}
                    }
                    IronSource.loadBanner(banner)
                    banner
                },
//                        update = { banner ->
//                        }
            )




            Row(modifier = Modifier.align(Alignment.CenterHorizontally)){
                PinWidgetButton()
            }

            Text(
                text = "Hi, this is a simple puzzle game. It comes with a widget, so feel free to try that out too!", modifier=Modifier.padding(20.dp)
            )

            NumberGame()
        }
    }

}







