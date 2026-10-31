package com.example.e_winger.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.e_winger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLanguageChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSupportClick: () -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SettingsCategory(title = stringResource(R.string.account))
            SettingsItem(icon = Icons.Default.Person, title = stringResource(R.string.edit_profile), onClick = onEditProfileClick)
            SettingsItem(icon = Icons.Default.Security, title = stringResource(R.string.security), onClick = onSecurityClick)
            
            HorizontalDivider()

            SettingsCategory(title = stringResource(R.string.language))
            SettingsItem(icon = Icons.Default.Language, title = stringResource(R.string.language), onClick = { showLanguageDialog = true })

            HorizontalDivider()
            
            SettingsCategory(title = stringResource(R.string.more))
            SettingsItem(icon = Icons.Default.Call, title = stringResource(R.string.customer_support), onClick = onSupportClick)
            SettingsItem(icon = Icons.Default.Info, title = stringResource(R.string.about_fast_food), onClick = { })
            SettingsItem(icon = Icons.Default.PrivacyTip, title = stringResource(R.string.privacy_policy), onClick = { })
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    TextButton(onClick = { 
                        onLanguageChange("en")
                        showLanguageDialog = false 
                    }) {
                        Text(stringResource(R.string.english))
                    }
                    TextButton(onClick = { 
                        onLanguageChange("sw")
                        showLanguageDialog = false 
                    }) {
                        Text(stringResource(R.string.swahili))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
