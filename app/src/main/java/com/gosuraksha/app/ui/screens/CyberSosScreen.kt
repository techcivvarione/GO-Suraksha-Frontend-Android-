package com.gosuraksha.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.data.remote.dto.CyberSosRequest
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.ui.screens.components.CyberSosForm
import com.gosuraksha.app.ui.screens.components.CyberSosSelectStep
import com.gosuraksha.app.ui.screens.components.CyberSosSuccess
import com.gosuraksha.app.ui.screens.model.CyberSosSuccessUiState
import com.gosuraksha.app.ui.viewmodel.CyberSosViewModel
import com.gosuraksha.app.ui.viewmodel.CyberSosViewModelFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CyberSosScreen(
    viewModel: CyberSosViewModel = viewModel(factory = CyberSosViewModelFactory()),
    onBack: (() -> Unit)? = null
) {
    val state = viewModel.uiState
    var scamType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var lossAmount by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) }

    val successState = remember {
        CyberSosSuccessUiState(
            scamType = "",
            referenceId = "GOS-${LocalDate.now().year}-${(1000..9999).random()}",
            submittedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm"))
        )
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            step = 3
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorTokens.background())
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally { -it } + fadeOut(tween(180)))
                } else {
                    (slideInHorizontally { -it } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally { it } + fadeOut(tween(180)))
                }
            },
            label = "step_transition"
        ) { currentStep ->
            when (currentStep) {
                1 -> CyberSosSelectStep(
                    selectedType = scamType,
                    onTypeSelect = { scamType = it },
                    onContinue = { if (scamType.isNotBlank()) step = 2 },
                    onBack = onBack
                )

                2 -> CyberSosForm(
                    scamType = scamType,
                    description = description,
                    lossAmount = lossAmount,
                    source = source,
                    isLoading = state.isLoading,
                    error = state.error,
                    onDescChange = { description = it },
                    onLossChange = { lossAmount = it },
                    onSourceChange = { source = it },
                    onBack = { step = 1 },
                    onSubmit = {
                        viewModel.triggerSos(
                            CyberSosRequest(
                                scam_type = scamType,
                                incident_date = LocalDate.now().toString(),
                                description = description,
                                loss_amount = lossAmount.ifBlank { null },
                                source = source.ifBlank { null }
                            )
                        )
                    }
                )

                3 -> CyberSosSuccess(
                    successState = successState.copy(scamType = scamType),
                    onDone = {
                        step = 1
                        scamType = ""
                        description = ""
                        lossAmount = ""
                        source = ""
                    }
                )

                else -> CyberSosSelectStep(
                    selectedType = scamType,
                    onTypeSelect = { scamType = it },
                    onContinue = { if (scamType.isNotBlank()) step = 2 },
                    onBack = onBack
                )
            }
        }
    }
}
