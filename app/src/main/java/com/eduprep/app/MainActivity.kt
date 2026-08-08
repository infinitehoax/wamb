package com.eduprep.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.eduprep.app.presentation.AITutorScreen
import com.eduprep.app.presentation.ClassroomScreen
import com.eduprep.app.presentation.PracticeScreen
import com.eduprep.app.presentation.ProfileScreen
import com.eduprep.app.presentation.Screen
import com.eduprep.app.presentation.quiz.ActiveQuizScreen
import com.eduprep.app.presentation.quiz.BookmarksScreen
import com.eduprep.app.presentation.quiz.QuizViewModel
import com.eduprep.app.presentation.quiz.ResultsScreen
import com.eduprep.app.ui.theme.EduPrepTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EduPrepTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        Screen.PracticeHome,
        Screen.Classroom,
        Screen.AITutor,
        Screen.Profile
    )

    // Only show the global Top Bar and Bottom Navigation Bar on top-level tabs
    val showGlobalBars = currentRoute in listOf(
        Screen.PracticeHome.route,
        Screen.Classroom.route,
        Screen.AITutor.route,
        Screen.Profile.route
    )

    Scaffold(
        topBar = {
            if (showGlobalBars) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) {
                                Screen.PracticeHome.route -> "Practice Hub"
                                Screen.Classroom.route -> "Classroom"
                                Screen.AITutor.route -> "AI Tutor & Examiner"
                                Screen.Profile.route -> "Profile & Analytics"
                                else -> "EduPrep Offline"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (showGlobalBars) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    navigationItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Box(modifier = Modifier.size(24.dp)) {
                                    Text(
                                        text = when (item) {
                                            is Screen.PracticeHome -> "📝"
                                            is Screen.Classroom -> "🏫"
                                            is Screen.AITutor -> "🤖"
                                            is Screen.Profile -> "👤"
                                            else -> ""
                                        },
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.PracticeHome.route,
            modifier = Modifier.padding(if (showGlobalBars) innerPadding else PaddingValues(0.dp)),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
        ) {
            composable(Screen.PracticeHome.route) {
                PracticeScreen(
                    onStartQuiz = { subject, year, topic, mode ->
                        navController.navigate("quiz_flow/$subject/$year/$topic/$mode")
                    }
                )
            }
            composable(Screen.Classroom.route) {
                ClassroomScreen()
            }
            composable(Screen.AITutor.route) {
                AITutorScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToBookmarks = {
                        navController.navigate(Screen.Bookmarks.route)
                    }
                )
            }

            // Bookmarks Screen
            composable(Screen.Bookmarks.route) {
                BookmarksScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Quiz Flow nested navigation to share QuizViewModel
            navigation(
                startDestination = Screen.ActiveQuiz.route,
                route = "quiz_flow/{subject}/{year}/{topic}/{mode}"
            ) {
                composable(route = Screen.ActiveQuiz.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("quiz_flow/{subject}/{year}/{topic}/{mode}")
                    }
                    val quizViewModel: QuizViewModel = hiltViewModel(parentEntry)
                    ActiveQuizScreen(
                        onBack = { navController.popBackStack() },
                        onSubmitSuccess = { _, _, _ ->
                            navController.navigate(Screen.Results.route)
                        },
                        viewModel = quizViewModel
                    )
                }

                composable(route = Screen.Results.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("quiz_flow/{subject}/{year}/{topic}/{mode}")
                    }
                    val quizViewModel: QuizViewModel = hiltViewModel(parentEntry)
                    ResultsScreen(
                        viewModel = quizViewModel,
                        onRetake = {
                            // Pop results to start quiz again
                            navController.popBackStack(Screen.ActiveQuiz.route, inclusive = false)
                        },
                        onHome = {
                            // Pop entire quiz flow to go back to main practice dashboard
                            navController.popBackStack(Screen.PracticeHome.route, inclusive = false)
                        }
                    )
                }
            }
        }
    }
}
