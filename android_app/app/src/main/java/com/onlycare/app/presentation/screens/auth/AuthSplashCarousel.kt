package com.onlycare.app.presentation.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay

@Composable
fun AuthSplashCarousel(
    drawableIds: List<Int>,
    modifier: Modifier = Modifier,
    displayDurationMillis: Long = 2500L,
    transitionDurationMillis: Int = 450,
    contentScale: ContentScale = ContentScale.Fit
) {
    if (drawableIds.isEmpty()) return

    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(drawableIds.size, displayDurationMillis) {
        index = 0
        while (true) {
            delay(displayDurationMillis)
            index = (index + 1) % drawableIds.size
        }
    }

    val current = drawableIds[index.coerceIn(0, drawableIds.lastIndex)]

    AnimatedContent(
        targetState = current,
        modifier = modifier,
        label = "AuthSplashCarousel",
        transitionSpec = {
            (fadeIn(animationSpec = tween(transitionDurationMillis)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(transitionDurationMillis)
                )).togetherWith(
                fadeOut(animationSpec = tween(transitionDurationMillis)) +
                    scaleOut(
                        targetScale = 1.02f,
                        animationSpec = tween(transitionDurationMillis)
                    )
            ).using(SizeTransform(clip = false))
        }
    ) { drawableId ->
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale
        )
    }
}


