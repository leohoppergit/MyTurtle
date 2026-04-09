package de.leohopper.myturtle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import de.leohopper.myturtle.ui.MyTurtleApp
import de.leohopper.myturtle.ui.MyTurtleViewModel
import de.leohopper.myturtle.ui.theme.MyTurtleTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MyTurtleViewModel by viewModels {
        MyTurtleViewModel.Factory(
            repository = (application as MyTurtleApplication).repository,
            appSettings = (application as MyTurtleApplication).appSettings,
            backupManager = (application as MyTurtleApplication).backupManager,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyTurtleTheme {
                MyTurtleApp(viewModel = viewModel)
            }
        }
    }
}
