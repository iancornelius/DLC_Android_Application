package ees.dlc.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import ees.dlc.application.NavigationBar
import ees.dlc.application.screens.HomeScreen
import ees.dlc.application.screens.StaffProfilesScreen
import ees.dlc.application.ui.theme.DLCTheme
import ees.dlc.application.Screen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ees.dlc.application.screens.TimetableScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DLCTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { NavigationBar(navController) }) { innerPadding ->
        val graph =
            navController.createGraph(startDestination = Screen.Home.rout) {
                composable(route = Screen.Home.rout) {
                    HomeScreen()
                }
                composable(route = Screen.Timetable.rout) {
                    TimetableScreen()
                }
                composable(route = Screen.StaffProfiles.rout) {
                    StaffProfilesScreen()
                }
            }
        NavHost(
            navController = navController,
            graph = graph,
            modifier = Modifier.padding(innerPadding)
        )

    }
}