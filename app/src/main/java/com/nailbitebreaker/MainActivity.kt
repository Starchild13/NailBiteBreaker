package com.nailbitebreaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nailbitebreaker.ui.BreathingExercise
import com.nailbitebreaker.ui.CoachScreen
import com.nailbitebreaker.ui.HomeScreen
import com.nailbitebreaker.ui.ProgressScreen
import com.nailbitebreaker.ui.ReactionGame
import com.nailbitebreaker.ui.PatternMemoryGame
import com.nailbitebreaker.ui.SocialChatScreen
import com.nailbitebreaker.ui.TipJarScreen
import com.nailbitebreaker.ui.Screen
import com.nailbitebreaker.ui.theme.NailBiteBreakerTheme
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            NailBiteBreakerTheme {
                NailBiteBreakerApp()
            }
        }
    }
}



@Composable
private fun NailBiteBreakerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // NavigationSuiteScaffold automatically adapts between Bottom Navigation (Phone)
    // and Navigation Rail (Tablet / XR / Wide windows).
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            NavTab.entries.forEach { tab ->
                item(
                    icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) },
                    selected = currentRoute == tab.route,
                    onClick = {
                        if (currentRoute != tab.route) {
                            navController.navigate(tab.route) {
                                popUpTo(Screen.Main.route) { inclusive = true }
                            }
                        }
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.safeDrawingPadding()) {
                AppNavHost(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
private fun AppNavHost(navController: NavController) {
    val goHome = {
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Main.route) { inclusive = true }
        }
    }

    NavHost(
        navController = navController as androidx.navigation.NavHostController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Coach.route) {
            CoachScreen(
                navController = navController,
                onBack = goHome
            )
        }
        composable(Screen.Breathe.route) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = goHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Box Breathing",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Follow the circle and breathe with it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                )
                Spacer(modifier = Modifier.height(32.dp))
                BreathingExercise(modifier = Modifier.fillMaxWidth())
            }
        }
        composable(Screen.ReactionGame.route) {
            ReactionGame(onBack = goHome)
        }
        composable(Screen.PatternGame.route) {
            PatternMemoryGame(onBack = goHome)
        }
        composable(Screen.Social.route) {
            SocialChatScreen(onBack = goHome)
        }
        composable(Screen.Progress.route) {
            ProgressScreen(onBack = goHome)
        }
        composable(Screen.Paywall.route) {
            TipJarScreen { navController.popBackStack() }
        }
    }
}

private enum class NavTab(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    HOME(Screen.Main.route, Icons.Filled.Home, "Home"),
    COACH(Screen.Coach.route, Icons.Filled.Psychology, "Coach"),
    @Suppress("DEPRECATION")
    PROGRESS(Screen.Progress.route, Icons.Filled.ShowChart, "Progress")
}
