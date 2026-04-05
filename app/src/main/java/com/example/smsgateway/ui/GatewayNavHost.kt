/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.smsgateway.ui.screens.*

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Auth : Screen("auth", "Auth", Icons.Default.Key)
    object Webhook : Screen("webhook", "Webhook", Icons.Default.Webhook)
    object Cloud : Screen("cloud", "Cloud", Icons.Default.Cloud)
    object Logs : Screen("logs", "Logs", Icons.Default.List)
}

@Composable
fun GatewayNavHost(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Auth, Screen.Webhook, Screen.Cloud, Screen.Logs)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { HomeScreen(viewModel) }
            composable(Screen.Auth.route) { AuthScreen(viewModel) }
            composable(Screen.Webhook.route) { WebhookScreen(viewModel) }
            composable(Screen.Cloud.route) { CloudScreen(viewModel) }
            composable(Screen.Logs.route) { LogsScreen(viewModel) }
        }
    }
}
