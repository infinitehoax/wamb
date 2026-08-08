package com.eduprep.app.presentation

sealed class Screen(val route: String, val title: String) {
    object PracticeHome : Screen("practice", "Practice")
    object Classroom : Screen("classroom", "Classroom")
    object AITutor : Screen("aitutor", "AI Tutor")
    object Profile : Screen("profile", "Profile")
}
