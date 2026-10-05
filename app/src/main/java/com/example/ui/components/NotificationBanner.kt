package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.GoldPrimary

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val isAlert: Boolean = false
)

@Composable
fun NotificationBanner(
    notification: AppNotification?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (notification != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("app_notification_banner"),
                shape = RoundedCornerShape(12.dp),
                color = if (notification.isAlert) Color(0xFF451A03) else Color(0xFF0F2535),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (notification.isAlert) AccentOrange else GoldPrimary
                ),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notification",
                        tint = if (notification.isAlert) AccentOrange else GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = notification.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (notification.isAlert) AccentOrange else GoldPrimary
                        )
                        Text(
                            text = notification.message,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
