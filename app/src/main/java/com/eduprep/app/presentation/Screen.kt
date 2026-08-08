package com.eduprep.app.presentation

sealed class Screen(val route: String, val title: String) {
    object PracticeHome : Screen("practice", "Practice")
    object Classroom : Screen("classroom", "Classroom")
    object AITutor : Screen("aitutor", "AI Tutor")
    object Profile : Screen("profile", "Profile")

    // Sub-screens for quiz flows
    object ActiveQuiz : Screen("active_quiz", "Active Quiz")
    object Results : Screen("results", "Results")
    object Bookmarks : Screen("bookmarks", "My Bookmarks")
}
