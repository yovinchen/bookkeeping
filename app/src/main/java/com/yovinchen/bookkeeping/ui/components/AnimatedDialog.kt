package com.yovinchen.bookkeeping.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun AnimatedDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(visible) {
        if (visible) {
            showDialog = true
            delay(50)
            showContent = true
        } else {
            showContent = false
            delay(300)
            showDialog = false
        }
    }
    
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // 背景遮罩动画
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutSlowInEasing
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 150,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                    )
                }
                
                // 对话框内容动画
                AnimatedVisibility(
                    visible = showContent,
                    enter = scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ),
                    exit = scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    content()
                }
            }
        }
    }
}