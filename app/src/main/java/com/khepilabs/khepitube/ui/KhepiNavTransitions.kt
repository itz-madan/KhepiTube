package com.khepilabs.khepitube.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

internal object KhepiNavTransitions {
    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
            slideInHorizontally(
                initialOffsetX = { (it * 0.06f).toInt() },
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
            )
    }
    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))
    }
    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing))
    }
    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing)) +
            slideOutHorizontally(
                targetOffsetX = { (it * 0.06f).toInt() },
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
            )
    }

    val predictivePopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.(Int) -> EnterTransition = {
        fadeIn(animationSpec = tween(PREDICTIVE_BACK_DURATION_MS, easing = LinearEasing))
    }
    val predictivePopExit: AnimatedContentTransitionScope<NavBackStackEntry>.(Int) -> ExitTransition = {
        fadeOut(animationSpec = tween(PREDICTIVE_BACK_DURATION_MS, easing = LinearEasing)) +
            slideOutHorizontally(
                targetOffsetX = { (it * 0.06f).toInt() },
                animationSpec = tween(PREDICTIVE_BACK_DURATION_MS, easing = LinearEasing),
            )
    }
}

private const val PREDICTIVE_BACK_DURATION_MS = 300
