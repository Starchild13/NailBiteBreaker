package com.nailbitebreaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.nailbitebreaker.ui.BREATHE_ROUTE
import com.nailbitebreaker.ui.COACH_ROUTE
import com.nailbitebreaker.ui.BreathingExercise
import com.nailbitebreaker.ui.CoachScreen
import com.nailbitebreaker.ui.HOME_ROUTE
import com.nailbitebreaker.ui.HomeScreen
import com.nailbitebreaker.ui.PROGRESS_ROUTE
import com.nailbitebreaker.ui.ProgressScreen
import com.nailbitebreaker.ui.REACTION_GAME_ROUTE
import com.nailbitebreaker.ui.ReactionGame
import com.nailbitebreaker.ui.PATTERN_GAME_ROUTE
import com.nailbitebreaker.ui.PatternMemoryGame
import com.nailbitebreaker.ui.SOCIAL_CHAT_ROUTE
import com.nailbitebreaker.ui.SocialChatScreen
import com.nailbitebreaker.ui.theme.NailBiteBreakerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NailBiteBreakerTheme {
                // Launch the app directly. 
                // Wrapping in Subspace at the root causes blank screens on non-XR devices.
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
                                popUpTo(HOME_ROUTE) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavHost(navController = navController)
        }
    }
}

@Composable
private fun AppNavHost(navController: NavController) {
    val goHome = {
        navController.navigate(HOME_ROUTE) {
            popUpTo(HOME_ROUTE) { inclusive = true }
        }
    }

    NavHost(
        navController = navController as androidx.navigation.NavHostController,
        startDestination = HOME_ROUTE
    ) {
        composable(HOME_ROUTE) {
            HomeScreen(navController = navController)
        }
        composable(COACH_ROUTE) {
            CoachScreen(
                navController = navController,
                onBack = goHome
            )
        }
        composable(BREATHE_ROUTE) {
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
        composable(REACTION_GAME_ROUTE) {
            ReactionGame(onBack = goHome)
        }
        composable(PATTERN_GAME_ROUTE) {
            PatternMemoryGame(onBack = goHome)
        }
        composable(SOCIAL_CHAT_ROUTE) {
            SocialChatScreen(onBack = goHome)
        }
        composable(PROGRESS_ROUTE) {
            ProgressScreen(onBack = goHome)
        }
    }
}

private enum class NavTab(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    HOME(HOME_ROUTE, Icons.Filled.Home, "Home"),
    COACH(COACH_ROUTE, Icons.Filled.Psychology, "Coach"),
    PROGRESS(PROGRESS_ROUTE, Icons.Filled.ShowChart, "Progress")
}
