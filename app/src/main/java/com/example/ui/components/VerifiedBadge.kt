package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberGold
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.Slate950

@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    isVipGold: Boolean = true,
    size: Dp = 16.dp,
    showLabel: Boolean = false
) {
    val badgeColor = if (isVipGold) CyberGold else CyberBlue
    val bgBrush = if (isVipGold) {
        Brush.linearGradient(listOf(CyberGold, Color(0xFFD97706)))
    } else {
        Brush.linearGradient(listOf(CyberBlue, NeonEmerald))
    }

    if (showLabel) {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor.copy(alpha = 0.12f))
                .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .testTag("verified_badge_pill"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(bgBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVipGold) Icons.Default.Shield else Icons.Default.Check,
                    contentDescription = "Verified Icon",
                    tint = Slate950,
                    modifier = Modifier.size(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isVipGold) "VIP GOLD" else "VERIFIED",
                color = badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(bgBrush)
                .testTag("verified_badge_icon"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isVipGold) Icons.Default.Shield else Icons.Default.Check,
                contentDescription = "Verified Icon",
                tint = Slate950,
                modifier = Modifier.size(size * 0.65f)
            )
        }
    }
}
