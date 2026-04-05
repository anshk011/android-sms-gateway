/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

private val DarkColors = darkColorScheme()
private val LightColors = lightColorScheme()

@Composable
fun SmsGatewayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
