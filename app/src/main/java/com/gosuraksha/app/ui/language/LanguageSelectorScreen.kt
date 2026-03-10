package com.gosuraksha.app.ui.language

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.gosuraksha.app.R
import com.gosuraksha.app.design.components.AppButton
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens

@Composable
fun LanguageSelectorScreen(
    onLanguageSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ColorTokens.accent().copy(alpha = 0.12f),
                        ColorTokens.background()
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            modifier = Modifier.padding(horizontal = SpacingTokens.xl)
        ) {
            Text(
                text = stringResource(R.string.language_select_title),
                style = TypographyTokens.screenTitle,
                color = ColorTokens.textPrimary()
            )

            Spacer(modifier = Modifier.height(SpacingTokens.lg))

            LanguageButton(stringResource(R.string.language_english)) { onLanguageSelected("en") }
            LanguageButton(stringResource(R.string.language_hindi_native)) { onLanguageSelected("hi") }
            LanguageButton(stringResource(R.string.language_telugu_native)) { onLanguageSelected("te") }

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            Text(
                text = stringResource(R.string.language_more_soon),
                style = TypographyTokens.bodySmall,
                color = ColorTokens.textSecondary()
            )
        }
    }
}

@Composable
private fun LanguageButton(
    label: String,
    onClick: () -> Unit
) {
    AppButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(SpacingTokens.authButtonHeight)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = TypographyTokens.buttonText
            )
        }
    }
}
