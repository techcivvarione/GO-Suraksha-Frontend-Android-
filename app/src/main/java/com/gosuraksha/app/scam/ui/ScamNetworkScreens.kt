package com.gosuraksha.app.scam.ui

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.gosuraksha.app.call.CallScreeningHelper
import com.gosuraksha.app.design.components.AppButton
import com.gosuraksha.app.design.components.AppOutlinedButton
import com.gosuraksha.app.design.components.AppTextField
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens
import com.gosuraksha.app.scam.ScamNetworkUiState
import com.gosuraksha.app.scam.ScamNetworkViewModel
import com.gosuraksha.app.scam.ScamNetworkViewModelFactory
import com.gosuraksha.app.scam.model.ReportScamRequest
import com.gosuraksha.app.scam.model.ScamActivityPoint
import com.gosuraksha.app.scam.model.ScamCategory

@Composable
fun rememberScamNetworkViewModel(): ScamNetworkViewModel {
    val app = LocalContext.current.applicationContext as Application
    return viewModel(factory = ScamNetworkViewModelFactory(app))
}

@Composable
fun ScamAlertHubScreen(
    onReportScamClick: () -> Unit,
    onCheckNumberClick: () -> Unit,
    onTrendingClick: () -> Unit,
    onAlertClick: (String) -> Unit,
    viewModel: ScamNetworkViewModel = rememberScamNetworkViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCallProtectionRationale by rememberSaveable { mutableStateOf(false) }
    var showOverlayRationale by rememberSaveable { mutableStateOf(false) }
    val callPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result[Manifest.permission.READ_CALL_LOG] == true &&
            result[Manifest.permission.READ_PHONE_STATE] == true
        if (allGranted) {
            if (!Settings.canDrawOverlays(context)) {
                showOverlayRationale = true
            } else if (activity != null && !CallScreeningHelper.isCallScreeningEnabled(context)) {
                runCatching {
                    CallScreeningHelper.requestCallScreeningRole(activity)
                }.onFailure {
                    CallScreeningHelper.requestDefaultDialer(activity)
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.loadTrendingScams()
        val missingCallProtectionPermissions = listOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE
        ).filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
        when {
            missingCallProtectionPermissions.isNotEmpty() -> showCallProtectionRationale = true
            !Settings.canDrawOverlays(context) -> showOverlayRationale = true
            activity != null && !CallScreeningHelper.isCallScreeningEnabled(context) -> {
                runCatching {
                    CallScreeningHelper.requestCallScreeningRole(activity)
                }.onFailure {
                    CallScreeningHelper.requestDefaultDialer(activity)
                }
            }
        }
    }
    if (showCallProtectionRationale) {
        AlertDialog(
            onDismissRequest = { showCallProtectionRationale = false },
            title = { Text("Enable call protection") },
            text = {
                Text("Go Suraksha needs phone state and call log access to detect suspicious incoming calls and warn you in time.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCallProtectionRationale = false
                        callPermissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_CALL_LOG,
                                Manifest.permission.READ_PHONE_STATE
                            )
                        )
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCallProtectionRationale = false }) {
                    Text("Not now")
                }
            }
        )
    }
    if (showOverlayRationale) {
        AlertDialog(
            onDismissRequest = { showOverlayRationale = false },
            title = { Text("Enable scam warning overlay") },
            text = {
                Text("Allow overlay access so scam call alerts can appear over incoming-call screens when risk is high.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOverlayRationale = false
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                ) {
                    Text("Open settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayRationale = false }) {
                    Text("Not now")
                }
            }
        )
    }
    ScamAlertHubContent(uiState, onReportScamClick, onCheckNumberClick, onTrendingClick, onAlertClick)
}

@Composable
fun ScamAlertHubContent(
    uiState: ScamNetworkUiState,
    onReportScamClick: () -> Unit,
    onCheckNumberClick: () -> Unit,
    onTrendingClick: () -> Unit,
    onAlertClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(ColorTokens.background()),
        contentPadding = PaddingValues(SpacingTokens.screenPaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)
    ) {
        item {
            RegionalThreatBanner(
                title = "High scam activity reported in your area today.",
                subtitle = "Stay alert for courier, OTP, and payment request scams."
            )
        }
        item {
            QuickActionsRow(
                actions = listOf(
                    QuickActionUi("Report Scam", Icons.Rounded.Report, ColorTokens.error(), onReportScamClick),
                    QuickActionUi("Check Number", Icons.Rounded.Numbers, ColorTokens.info(), onCheckNumberClick),
                    QuickActionUi("Trending Scams", Icons.AutoMirrored.Rounded.TrendingUp, ColorTokens.accent(), onTrendingClick)
                )
            )
        }
        item { SectionHeader("Live scam activity", "Community reports and campaign spikes from the last 24 hours.") }
        items(uiState.trendingScams.take(5)) { campaign ->
            ScamAlertCard(campaign = campaign, onClick = { onAlertClick(campaign.id) })
        }
        if (uiState.loadingTrending) {
            item { CenterLoadingCard() }
        }
    }
}

@Composable
fun ReportScamScreen(
    onBackToHub: () -> Unit,
    viewModel: ScamNetworkViewModel = rememberScamNetworkViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var phishingLink by rememberSaveable { mutableStateOf("") }
    var paymentId by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    val selectedCategories = remember { mutableStateListOf(ScamCategory.ScamCall) }
    var screenshotUri by rememberSaveable { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        screenshotUri = uri?.toString()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(ColorTokens.background()),
        contentPadding = PaddingValues(SpacingTokens.screenPaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)
    ) {
        item { SectionHeader("Report a scam", "Submit suspicious numbers, links, payment requests, or messages to help the network learn faster.") }
        item {
            AppTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone number (optional)",
                leadingIcon = Icons.Rounded.Call,
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { AppTextField(value = phishingLink, onValueChange = { phishingLink = it }, label = "Phishing link (optional)", modifier = Modifier.fillMaxWidth()) }
        item { AppTextField(value = paymentId, onValueChange = { paymentId = it }, label = "Payment ID (optional)", modifier = Modifier.fillMaxWidth()) }
        item { ScamCategoryChips(selectedCategories = selectedCategories, onToggle = { if (it in selectedCategories) selectedCategories.remove(it) else selectedCategories.add(it) }) }
        item {
            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                singleLine = false,
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm), verticalAlignment = Alignment.CenterVertically) {
                AppOutlinedButton(onClick = { picker.launch("image/*") }) { Text(if (screenshotUri == null) "Pick screenshot" else "Change screenshot") }
                if (screenshotUri != null) Text("Screenshot attached", style = TypographyTokens.bodySmall, color = ColorTokens.success())
            }
        }
        uiState.reportError?.let { item { InlineError(it) } }
        uiState.reportSuccessMessage?.let { item { InlineSuccess(it) } }
        item {
            AppButton(
                onClick = {
                    viewModel.submitScamReport(
                        ReportScamRequest(
                            phoneNumber = phoneNumber.ifBlank { null },
                            phishingLink = phishingLink.ifBlank { null },
                            paymentId = paymentId.ifBlank { null },
                            categories = selectedCategories.map { it.label },
                            description = description,
                            screenshotUri = screenshotUri
                        )
                    )
                },
                enabled = (phoneNumber.isNotBlank() || phishingLink.isNotBlank() || paymentId.isNotBlank() || description.isNotBlank()) && selectedCategories.isNotEmpty() && !uiState.submittingReport,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.submittingReport) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    androidx.compose.foundation.layout.Spacer(Modifier.width(SpacingTokens.xs))
                }
                Text("Submit")
            }
        }
        item { AppOutlinedButton(onClick = onBackToHub, modifier = Modifier.fillMaxWidth()) { Text("Back to Scam Network") } }
    }
}

@Composable
fun ScamNumberCheckerScreen(viewModel: ScamNetworkViewModel = rememberScamNetworkViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(ColorTokens.background()),
        contentPadding = PaddingValues(SpacingTokens.screenPaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)
    ) {
        item { SectionHeader("Check a number", "Search the network before you answer or pay.") }
        item {
            AppTextField(
                value = uiState.phoneQuery,
                onValueChange = viewModel::updatePhoneNumberQuery,
                label = "+91 9876543210",
                leadingIcon = Icons.Rounded.Call,
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { AppButton(onClick = { viewModel.checkPhoneNumber() }, modifier = Modifier.fillMaxWidth()) { Text("Check Number") } }
        uiState.phoneCheckError?.let { item { InlineError(it) } }
        uiState.phoneCheckResult?.let { item { PhoneCheckResultCard(it) } }
    }
}

@Composable
fun ScamAlertsFeedScreen(
    onAlertClick: (String) -> Unit,
    viewModel: ScamNetworkViewModel = rememberScamNetworkViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val alerts = viewModel.alertsPager.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(ColorTokens.background()),
        contentPadding = PaddingValues(SpacingTokens.screenPaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)
    ) {
        item { SectionHeader("Trending scam campaigns", "Active campaigns, regional spread, and prevention guidance.") }
        items(uiState.trendingScams) { campaign ->
            ScamAlertCard(campaign, onClick = { onAlertClick(campaign.id) })
        }
        if (alerts.loadState.refresh is LoadState.Loading) item { CenterLoadingCard() }
        items(alerts.itemCount) { index ->
            alerts[index]?.let { ScamAlertCard(it, onClick = { onAlertClick(it.id) }) }
        }
        if (alerts.loadState.refresh is LoadState.Error) item { InlineError((alerts.loadState.refresh as LoadState.Error).error.message ?: "Unable to load scam alerts.") }
    }
}

@Composable
fun ScamAlertDetailScreen(
    alertId: String?,
    viewModel: ScamNetworkViewModel = rememberScamNetworkViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val campaign = remember(alertId, uiState.trendingScams) { viewModel.findCampaign(alertId) }

    if (campaign == null) {
        Box(modifier = Modifier.fillMaxSize().background(ColorTokens.background()), contentAlignment = Alignment.Center) {
            Text("Scam alert details are unavailable.", color = ColorTokens.textSecondary())
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(ColorTokens.background()),
        contentPadding = PaddingValues(SpacingTokens.screenPaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)
    ) {
        item { SectionHeader(campaign.scamType, campaign.category ?: "Scam campaign detail") }
        items(campaign.recentActivityTimeline.ifEmpty { defaultTimeline(campaign.reportCount) }) { point ->
            TimelineRow(point)
        }
        items(campaign.preventionTips) { tip ->
            ChecklistRow(tip)
        }
        item {
            AppButton(
                onClick = {
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${campaign.scamType}\n${campaign.explanation}")
                            },
                            null
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null)
                androidx.compose.foundation.layout.Spacer(Modifier.width(SpacingTokens.xs))
                Text("Share alert")
            }
        }
    }
}

private fun defaultTimeline(reportCount: Int): List<ScamActivityPoint> = listOf(
    ScamActivityPoint("Last 24 hours", reportCount),
    ScamActivityPoint("Past 7 days", reportCount * 2),
    ScamActivityPoint("Past 30 days", reportCount * 3)
)








