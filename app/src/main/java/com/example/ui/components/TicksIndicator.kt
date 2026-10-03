package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.Slate400

@Composable
fun TicksIndicator(
    delivered: Boolean,
    seen: Boolean,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    val tickColor: Color = when {
        seen -> CyberBlue // Seen: Double Blue/Cyan tick
        delivered -> Slate400 // Delivered: Double Grey tick
        else -> Slate400 // Sent: Single Grey tick
    }

    Row(
        modifier = modifier.testTag("message_ticks_indicator"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // First tick
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = if (seen) "Seen" else if (delivered) "Delivered" else "Sent",
            tint = tickColor,
            modifier = Modifier.size(size)
        )
        // Second tick if delivered or seen
        if (delivered || seen) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = tickColor,
                modifier = Modifier
                    .size(size)
                    .offset(x = (-size * 0.45f))
            )
        }
    }
}
