package com.gosuraksha.app.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.profile.ProfileViewModel
import com.gosuraksha.app.security.SecurityViewModel
import com.gosuraksha.app.data.SessionManager
import com.gosuraksha.app.ui.components.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {

    val viewModel: ProfileViewModel = viewModel()
    val securityViewModel: SecurityViewModel = viewModel()
    val cyberCardViewModel: CyberCardViewModel = viewModel()

    val profile by viewModel.profile.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()
    val securityMessage by securityViewModel.message.collectAsState()

    val user by SessionManager.user.collectAsState()
    val cyberCard by cyberCardViewModel.card.collectAsState()

    var showUpgradeDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        cyberCardViewModel.loadCard()
    }

    LaunchedEffect(profile) {
        profile?.let {
            name = it.name
            phone = it.phone ?: ""
        }
    }

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        user?.let { currentUser ->

            when (cyberCard?.card_status) {

                "ACTIVE" -> {
                    CyberCardContainer(
                        user = currentUser.copy(id = cyberCard!!.card_id!!),
                        cyberScore = cyberCard!!.score!!,
                        maxScore = cyberCard!!.max_score!!,
                        riskLevel = cyberCard!!.risk_level!!,
                        generatedOn = cyberCard!!.score_month!!,
                        validTill = calculateValidTill(cyberCard!!.score_month!!),
                        signals = cyberCard!!.signals
                    )
                }

                "LOCKED" -> {
                    LockedMonthCard(
                        score = cyberCard!!.score ?: 600,
                        message = cyberCard!!.message ?: "Locked this month"
                    )
                }

                "PENDING" -> {
                    PendingCard(
                        message = cyberCard!!.message ?: "Card will be available next month"
                    )
                }

                else -> {
                    LockedCyberCard {
                        showUpgradeDialog = true
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // 🔹 Profile Section
        Text(
            text = "Profile Information",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.updateProfile(name, phone)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Profile")
        }

        Spacer(Modifier.height(32.dp))

        Divider()

        Spacer(Modifier.height(24.dp))

        // 🔹 Password Section
        Text(
            text = "Security",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = currentPass,
            onValueChange = { currentPass = it },
            label = { Text("Current Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = newPass,
            onValueChange = { newPass = it },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPass,
            onValueChange = { confirmPass = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                securityViewModel.changePassword(
                    currentPass,
                    newPass,
                    confirmPass
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change Password")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                securityViewModel.logoutAll()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Logout All Sessions")
        }

        message?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        securityMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            confirmButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text("Upgrade Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Upgrade Required") },
            text = { Text("Upgrade to Premium plan to access your Cyber Card.") }
        )
    }
}

private fun calculateValidTill(scoreMonth: String): String {
    return try {
        val month = java.time.LocalDate.parse(scoreMonth.substring(0, 10))
        month.plusMonths(1).toString()
    } catch (e: Exception) {
        scoreMonth
    }
}
