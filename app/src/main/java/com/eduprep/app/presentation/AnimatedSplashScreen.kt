package com.eduprep.app.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.eduprep.app.R

@Composable
fun AnimatedSplashScreen(onSplashFinished: () -> Unit) {
    // 5-Second Timer
    LaunchedEffect(key1 = true) {
        delay(5000L)
        onSplashFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_anim")

    // 1. Bouncing Animation (Moves up and down)
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -40f, // Bounces up 40 pixels
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // 2. Subtle Rotation Animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotate"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)), // Dark gray background as requested
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        translationY = offsetY
                        rotationZ = rotation
                    }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "EduPrep Offline",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}
