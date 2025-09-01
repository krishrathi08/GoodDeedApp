package eu.tutorials.gooddeedproject.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.tutorials.gooddeedproject.R
import eu.tutorials.gooddeedproject.ui.theme.AppBackgroundColor
import eu.tutorials.gooddeedproject.ui.theme.GoodDeedProjectTheme
import eu.tutorials.gooddeedproject.ui.theme.LightGrayBackground
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(onAnimationFinished: () -> Unit) {

    // Yeh animation ko hamesha chalaata rahega (infinite)
    val infiniteTransition = rememberInfiniteTransition(label = "infinite_transition")

    // Logo ke size ko chhota-bada (pulse effect) karne ke liye
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f, // Logo 1.1x tak bada hoga
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing), // 600ms mein bada hoga
            repeatMode = RepeatMode.Reverse // Fir 600ms mein vaapis chhota hoga
        ), label = "logo_scale"
    )

    // Yeh effect 2.5 second ke baad automatically home screen pe le jaayega
    LaunchedEffect(Unit) {
        delay(2500) // 2.5 seconds ka delay
        onAnimationFinished()
    }

    Surface(
        color = LightGrayBackground, // Aapke dark theme wala background
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // Logo ko center mein rakhega
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .width(180.dp)
                    .scale(scale) // Yahan scale animation apply ho raha hai
            )
        }
    }
}

@Preview
@Composable
fun LoadingScreenPreview() {
    GoodDeedProjectTheme {
        LoadingScreen(onAnimationFinished = {})
    }
}