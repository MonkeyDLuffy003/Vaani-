package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class ChannelItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun ChannelSelectorRow(
    selectedChannel: String,
    onChannelSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val channels = listOf(
        ChannelItem("All", "All Channels", Icons.Default.Tune, PrimaryNeon),
        ChannelItem("Cooking", "Cooking", Icons.Default.Restaurant, Color(0xFFF59E0B)),
        ChannelItem("Gym", "Gym Beast", Icons.Default.FitnessCenter, Color(0xFFEF4444)),
        ChannelItem("Home", "Home Cozy", Icons.Default.Home, Color(0xFF10B981)),
        ChannelItem("Relax", "Relax 432Hz", Icons.Default.SelfImprovement, SecondaryCyan),
        ChannelItem("Motivation", "Motivation", Icons.Default.ElectricBolt, Color(0xFFA855F7)),
        ChannelItem("Storage", "Phone Storage", Icons.Default.PhoneAndroid, Color(0xFF38BDF8))
    )

    LazyRow(
        modifier = modifier.testTag("channel_selector_row"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(channels) { channel ->
            val isSelected = selectedChannel.equals(channel.id, ignoreCase = true)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) channel.accentColor.copy(alpha = 0.22f) else SurfaceCard
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) channel.accentColor else Color(0xFF262C3E),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onChannelSelected(channel.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("channel_${channel.id.lowercase()}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = channel.icon,
                        contentDescription = channel.label,
                        tint = if (isSelected) channel.accentColor else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = channel.label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                }
            }
        }
    }
}
