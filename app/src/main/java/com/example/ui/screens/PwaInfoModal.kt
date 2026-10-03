package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberButton
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun PwaInfoModal(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        titleContentColor = Slate100,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.InstallMobile,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WEBAPK & PWA ARCHITECTURE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "NIL DARK is architected with complete WebAPK and Native Android PWA runtime capabilities:",
                    color = Slate300,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                PwaFeatureItem(
                    icon = Icons.Default.Security,
                    title = "Zero Identity Footprint",
                    desc = "No phone, email, or Google sign-in required. 100% anonymous Chat ID identity."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PwaFeatureItem(
                    icon = Icons.Default.Android,
                    title = "WebAPK Standalone Runtime",
                    desc = "Operates full viewport standalone with viewport-fit=cover, custom splash, and edge-to-edge rendering."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PwaFeatureItem(
                    icon = Icons.Default.WifiOff,
                    title = "Offline Persistence",
                    desc = "Encrypted local SQLite/Room and Firebase offline caching keep previous messages accessible without cellular data."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PwaFeatureItem(
                    icon = Icons.Default.CheckCircle,
                    title = "Instant Installation",
                    desc = "Directly install as a native Android APK or via Chrome/Brave 'Install App' prompt."
                )
            }
        },
        confirmButton = {
            CyberButton(
                text = "ACKNOWLEDGE",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                tag = "btn_pwa_ack"
            )
        },
        dismissButton = {}
    )
}

@Composable
private fun PwaFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Slate950)
            .border(1.dp, Slate800, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Slate100,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    color = Slate400,
                    fontSize = 11.sp
                )
            }
        }
    }
}
