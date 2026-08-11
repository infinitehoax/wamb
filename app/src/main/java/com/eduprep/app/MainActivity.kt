package com.eduprep.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import com.eduprep.app.presentation.quiz.ActiveTheoryScreen
import com.eduprep.app.presentation.quiz.BookmarksScreen
import com.eduprep.app.presentation.quiz.QuizViewModel
import com.eduprep.app.presentation.quiz.ResultsScreen
import com.eduprep.app.presentation.quiz.TheoryResultsScreen
import com.eduprep.app.presentation.quiz.TheoryViewModel
import com.eduprep.app.ui.theme.EduPrepTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // MUST be called before super.onCreate!
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var currentThemeSetting by remember { mutableStateOf(com.eduprep.app.data.local.ThemePreferences.getTheme(context)) }
            val isDarkTheme = when (currentThemeSetting) {
                "Light" -> false
                "Dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            EduPrepTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMainScreen(
                        currentTheme = currentThemeSetting,
                        onThemeChanged = { currentThemeSetting = it }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMainScreen(
    currentTheme: String,
    onThemeChanged: (String) -> Unit
) {
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
                    actions = {
                        if (currentRoute == Screen.Profile.route) {
                            IconButton(onClick = { navController.navigate("settings") }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
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
            startDestination = Screen.Splash.route, // CHANGED THIS
            modifier = Modifier.padding(if (showGlobalBars) innerPadding else PaddingValues(0.dp)),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
        ) {
            // ADD THIS NEW COMPOSABLE BLOCK:
            composable(Screen.Splash.route) {
                com.eduprep.app.presentation.AnimatedSplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.PracticeHome.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true } // Destroy splash so user can't hit back button to it
                        }
                    }
                )
            }

            composable(Screen.PracticeHome.route) {
                PracticeScreen(
                    onStartQuiz = { subject, year, topic, mode, limit, duration, shuffleQ, shuffleOpt ->
                        navController.navigate("quiz_flow/$subject/$year/$topic/$mode/$limit/$duration/$shuffleQ/$shuffleOpt")
                    },
                    onStartTheory = { questionId ->
                        navController.navigate("theory_flow/$questionId")
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

            // Settings Screen
            composable("settings") {
                com.eduprep.app.presentation.SettingsScreen(
                    onBack = { navController.popBackStack() },
                    currentTheme = currentTheme,
                    onThemeChanged = onThemeChanged
                )
            }

            // Quiz Flow nested navigation to share QuizViewModel
            navigation(
                startDestination = "active_quiz",
                route = "quiz_flow/{subject}/{year}/{topic}/{mode}/{limit}/{duration}/{shuffleQuestions}/{shuffleOptions}"
            ) {
                composable(route = "active_quiz") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("quiz_flow/{subject}/{year}/{topic}/{mode}/{limit}/{duration}/{shuffleQuestions}/{shuffleOptions}")
                    }
                    val quizViewModel: QuizViewModel = hiltViewModel(parentEntry)
                    ActiveQuizScreen(
                        onBack = { navController.popBackStack() },
                        onSubmitSuccess = { _, _, _ ->
                            navController.navigate("results")
                        },
                        viewModel = quizViewModel
                    )
                }

                composable(route = "results") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("quiz_flow/{subject}/{year}/{topic}/{mode}/{limit}/{duration}/{shuffleQuestions}/{shuffleOptions}")
                    }
                    val quizViewModel: QuizViewModel = hiltViewModel(parentEntry)
                    ResultsScreen(
                        viewModel = quizViewModel,
                        onRetake = {
                            // Pop results to start quiz again
                            navController.popBackStack("active_quiz", inclusive = false)
                        },
                        onHome = {
                            // Pop entire quiz flow to go back to main practice dashboard
                            navController.popBackStack(Screen.PracticeHome.route, inclusive = false)
                        }
                    )
                }
            }

            // Theory Flow nested navigation to share TheoryViewModel
            navigation(
                startDestination = Screen.ActiveTheory.route,
                route = "theory_flow/{questionId}"
            ) {
                composable(
                    route = Screen.ActiveTheory.route,
                    arguments = listOf(navArgument("questionId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("theory_flow/{questionId}")
                    }
                    val theoryViewModel: TheoryViewModel = hiltViewModel(parentEntry)
                    ActiveTheoryScreen(
                        onBack = { navController.popBackStack() },
                        onSubmitSuccess = { questionId ->
                            navController.navigate("active_theory/$questionId/results") {
                                popUpTo("active_theory/$questionId") { inclusive = true }
                            }
                        },
                        viewModel = theoryViewModel
                    )
                }

                composable(
                    route = "active_theory/{questionId}/results",
                    arguments = listOf(navArgument("questionId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("theory_flow/{questionId}")
                    }
                    val theoryViewModel: TheoryViewModel = hiltViewModel(parentEntry)
                    TheoryResultsScreen(
                        viewModel = theoryViewModel,
                        onBack = {
                            navController.popBackStack(Screen.PracticeHome.route, inclusive = false)
                        }
                    )
                }
            }
        }
    }
}
