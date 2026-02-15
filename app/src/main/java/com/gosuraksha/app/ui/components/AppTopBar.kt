package com.gosuraksha.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.gosuraksha.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onCyberSosClick: () -> Unit
) {


// 🔹 Rotating image list
    val items = listOf(
        R.drawable.logo,
        R.drawable.go_safe,
        R.drawable.go_secure
    )

    var currentIndex by remember { mutableStateOf(0) }

    // 🔹 Auto rotate every 2.5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            currentIndex = (currentIndex + 1) % items.size
        }
    }

    TopAppBar(
        modifier = Modifier.shadow(4.dp),

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),

        title = {
            Box(
                modifier = Modifier.height(40.dp),
                contentAlignment = Alignment.CenterStart
            ) {

                AnimatedContent(
                    targetState = items[currentIndex],
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() togetherWith
                                slideOutVertically { -it } + fadeOut()
                    },
                    label = "imageSwitcher"
                ) { imageRes ->

                    Icon(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .height(26.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        },

        actions = {

            AssistChip(
                onClick = onCyberSosClick,
                label = { Text("CyberSOS") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    labelColor = MaterialTheme.colorScheme.onError,
                    leadingIconContentColor = MaterialTheme.colorScheme.onError
                )
            )

            Spacer(modifier = Modifier.width(8.dp))
        }
    )
}
