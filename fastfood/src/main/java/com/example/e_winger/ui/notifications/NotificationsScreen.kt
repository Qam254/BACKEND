package com.example.e_winger.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<NotificationItem>,
    onBackClick: () -> Unit,
    onNotificationRead: (String) -> Unit,
    onMarkAllRead: () -> Unit
) {
    var selectedNotification by remember { mutableStateOf<NotificationItem?>(null) }

    if (selectedNotification != null) {
        NotificationDetailView(
            notification = selectedNotification!!,
            onBackClick = { 
                selectedNotification = null 
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Notifications") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (notifications.any { !it.isRead }) {
                            TextButton(onClick = onMarkAllRead) {
                                Text("Mark all read")
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            if (notifications.isEmpty()) {
                EmptyNotificationsView(modifier = Modifier.padding(innerPadding))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    items(notifications) { notification ->
                        NotificationRow(
                            notification = notification,
                            onClick = { 
                                onNotificationRead(notification.id)
                                selectedNotification = notification 
                            }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

data class NotificationItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String, 
    val message: String, 
    val time: String,
    val fullContent: String = "",
    var isRead: Boolean = false
)

@Composable
fun NotificationRow(notification: NotificationItem, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notification.title, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message, 
                style = MaterialTheme.typography.bodyMedium, 
                maxLines = 1,
                color = if (notification.isRead) Color.Gray else Color.Unspecified
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = notification.time, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailView(notification: NotificationItem, onBackClick: () -> Unit) {
    val content = if (notification.fullContent.isNotEmpty()) notification.fullContent else notification.message
    val phoneNumber = "0612826255"
    
    val annotatedString = buildAnnotatedString {
        val startIndex = content.indexOf(phoneNumber)
        if (startIndex != -1) {
            append(content.substring(0, startIndex))
            val link = LinkAnnotation.Url(
                url = "tel:$phoneNumber",
                styles = TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline))
            )
            pushLink(link)
            append(phoneNumber)
            pop()
            append(content.substring(startIndex + phoneNumber.length))
        } else {
            append(content)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Text(
                text = notification.title, 
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = notification.time, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun EmptyNotificationsView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No notifications yet", color = Color.Gray)
        }
    }
}
