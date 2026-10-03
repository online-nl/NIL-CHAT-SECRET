package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate950

@Composable
fun AvatarView(
    name: String,
    photoUrl: String? = null,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    showOnlineStatus: Boolean = true,
    modifier: Modifier = Modifier
) {
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
    // Deterministic cyber gradient based on name hash
    val colorPair = when (Math.abs(name.hashCode()) % 4) {
        0 -> listOf(NeonEmerald, Color(0xFF0D9488))
        1 -> listOf(IndigoAccent, Color(0xFF8B5CF6))
        2 -> listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
        else -> listOf(Color(0xFFF59E0B), Color(0xFFD97706))
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("avatar_view")
    ) {
        if (!photoUrl.isNullOrEmpty() && (photoUrl.startsWith("http") || photoUrl.startsWith("content://"))) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "$name Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(1.5.dp, Slate700, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(colorPair))
                    .border(1.5.dp, Slate850, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Slate950,
                    fontWeight = FontWeight.Black,
                    fontSize = (size.value * 0.44f).sp
                )
            }
        }

        if (showOnlineStatus) {
            val dotSize = (size.value * 0.28f).coerceIn(10f, 14f).dp
            val statusColor = if (isOnline) NeonEmerald else Slate600
            val glowColor = if (isOnline) EmeraldGlow else Color.Transparent

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(statusColor)
                    .border(2.dp, Slate950, CircleShape)
                    .testTag("avatar_online_status_dot")
            )
        }
    }
}
