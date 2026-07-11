package com.opfis.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark mode is first-class, not an afterthought (SystemPrompt Part 3).
 * Every screen must be composed against both schemes below.
 */
private val OpfisLightColorScheme =
    lightColorScheme(
        primary = OpfisColors.ProfessionalBlue,
        error = OpfisColors.Error,
        background = OpfisColors.BackgroundNeutralGray,
    )

private val OpfisDarkColorScheme =
    darkColorScheme(
        primary = OpfisColors.ProfessionalBlue,
        error = OpfisColors.Error,
    )

@Composable
fun OpfisTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (useDarkTheme) OpfisDarkColorScheme else OpfisLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
