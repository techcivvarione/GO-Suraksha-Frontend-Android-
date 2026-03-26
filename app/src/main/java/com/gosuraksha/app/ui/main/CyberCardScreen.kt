package com.gosuraksha.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.domain.model.Feature
import com.gosuraksha.app.domain.model.hasFeature

private fun screenBg(isDark: Boolean) = if (isDark) Color(0xFF080E08) else Color(0xFFF5F7F5)
fun cardBg(isDark: Boolean)   = if (isDark) Color(0xFF0F1A10) else Color(0xFFFFFFFF)
fun panelBg(isDark: Boolean)  = if (isDark) Color(0xFF111E13) else Color(0xFFF0F6F1)
fun chipBg(isDark: Boolean)   = if (isDark) Color(0xFF162819) else Color(0xFFE8F5EC)
fun onSurf(isDark: Boolean)   = if (isDark) Color(0xFFD4EDD9) else Color(0xFF0D1F14)
fun subText(isDark: Boolean)  = if (isDark) Color(0xFF6BAA80) else Color(0xFF5A8A6A)
fun divider(isDark: Boolean)  = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000)

val Green400 = Color(0xFF2EC472)

@Composable
fun CyberCardScreen(
    onBack: () -> Unit,
    onNavigateToScan: () -> Unit,
    onUpgradePlan: () -> Unit
) {
    val isDark = ColorTokens.LocalAppDarkMode.current
    val app    = LocalContext.current.applicationContext as android.app.Application
    val viewModel: CyberCardViewModel = viewModel(
        factory = CyberCardViewModelFactory(app)
    )
    val card               by viewModel.card.collectAsStateWithLifecycle()
    val loading            by viewModel.loading.collectAsStateWithLifecycle()
    val error              by viewModel.error.collectAsStateWithLifecycle()
    val user               by SessionManager.user.collectAsStateWithLifecycle()
    val improvementMessage by viewModel.improvementMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show improvement toast when score goes up after a silent refresh
    LaunchedEffect(improvementMessage) {
        if (improvementMessage != null) {
            snackbarHostState.showSnackbar(improvementMessage!!)
            viewModel.clearImprovementMessage()
        }
    }

    LaunchedEffect(Unit) { viewModel.loadCard() }

    Scaffold(
        containerColor = screenBg(isDark),
        topBar         = { CyberCardHeader(isDark = isDark, onBack = onBack) },
        snackbarHost   = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData     = data,
                    containerColor   = Color(0xFF1A2E1A),
                    contentColor     = Green400,
                    actionColor      = Green400
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBg(isDark))
                .padding(innerPadding)
        ) {
            when {
                loading -> LoadingCyberCardState()

                !hasFeature(user, Feature.CYBER_CARD) ->
                    FreeCyberCardState(isDark = isDark, onUpgradePlan = onUpgradePlan)

                card == null -> CalculatingCyberCardState(
                    isDark  = isDark,
                    onRetry = { viewModel.loadCard() }
                )

                else -> when (card?.card_status?.uppercase()) {
                    "PENDING" -> PendingCyberCardState(
                        isDark           = isDark,
                        onNavigateToScan = onNavigateToScan
                    )
                    "LOCKED" -> LockedCyberCardState(
                        isDark        = isDark,
                        card          = card!!,
                        onUpgradePlan = onUpgradePlan
                    )
                    "ACTIVE" -> ActiveCardScrollContent(
                        isDark           = isDark,
                        card             = card!!,
                        userName         = user?.name.orEmpty(),
                        onNavigateToScan = onNavigateToScan,
                        onRefreshCard    = { viewModel.refreshCard() }
                    )
                    else -> ErrorCyberCardState(
                        isDark  = isDark,
                        message = error ?: "Unable to load Cyber Card",
                        onRetry = { viewModel.loadCard() }
                    )
                }
            }
        }
    }
}

// ── ACTIVE state — spring entry + bg scale + LazyColumn ──────────────────────

@Composable
private fun ActiveCardScrollContent(
    isDark: Boolean,
    card: CyberCardResponse,
    userName: String,
    onNavigateToScan: () -> Unit,
    onRefreshCard: () -> Unit
) {
    val insights = card.insights ?: emptyList()
    val actions  = card.actions.toCyberActions()

    // ── Background scale: starts slightly zoomed in, relaxes as card slides up
    var bgReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { bgReady = true }

    val bgScale by animateFloatAsState(
        targetValue   = if (bgReady) 1f else 1.05f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label         = "cyber_bg_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Scaled background layer — gives subtle "depth unlock" feel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { scaleX = bgScale; scaleY = bgScale }
                .background(screenBg(isDark))
        )

        // Card slides in with spring — feels like unlocking a secure identity card
        var cardVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { cardVisible = true }

        AnimatedVisibility(
            visible = cardVisible,
            enter   = fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMediumLow
                )
            ) { it / 2 }
        ) {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ActiveCyberCardContent(
                        isDark   = isDark,
                        card     = card,
                        userName = userName
                    )
                }

                item {
                    CyberHistorySection(
                        isDark        = isDark,
                        insights      = insights,
                        actions       = actions,
                        onActionClick = { action ->
                            // Navigate to the appropriate screen
                            when (action.id) {
                                "scan_now",
                                "scan_email",
                                "scan_password",
                                "resume_scanning" -> onNavigateToScan()
                                // verify_phone and add_trusted_contact are navigation targets
                                // that don't exist yet — fall through to scan as default
                                else              -> { /* caller handles via deep link */ }
                            }
                            // Always trigger a silent refresh so score reflects the change
                            onRefreshCard()
                        }
                    )
                }

                // Disclaimer
                item {
                    DisclaimerText(isDark = isDark)
                }
            }
        }
    }
}

@Composable
private fun DisclaimerText(isDark: Boolean) {
    Text(
        text     = "This is GO Suraksha's internal safety score. It is not a government-issued rating.",
        color    = subText(isDark).copy(alpha = 0.55f),
        fontSize = 10.sp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}
