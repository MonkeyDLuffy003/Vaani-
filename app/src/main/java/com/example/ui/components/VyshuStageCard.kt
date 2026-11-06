package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay

@Composable
fun VyshuStageCard(
    isPlaying: Boolean,
    bpm: Int,
    currentSongTitle: String?,
    vyshuMessage: String?,
    onVyshuClick: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMinimized by remember { mutableStateOf(false) }
    var displaySpeech by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vyshuMessage) {
        if (!vyshuMessage.isNullOrEmpty()) {
            displaySpeech = vyshuMessage
            delay(4500)
            displaySpeech = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SurfaceElevated,
                        Color(0xFF131124)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFA855F7).copy(alpha = 0.6f),
                        Color(0xFFEC4899).copy(alpha = 0.5f),
                        SecondaryCyan.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("vyshu_stage_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Stage Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💜", fontSize = 15.sp)
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Vyshu AI • Companion Stage",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = TextPrimary
                            )

                            // Status badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isPlaying) AccentEmerald.copy(alpha = 0.25f)
                                        else Color(0xFF334155)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isPlaying) "GROOVE SYNC" else "IDLE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = if (isPlaying) AccentEmerald else TextTertiary
                                )
                            }
                        }

                        Text(
                            text = if (isPlaying) "Swaying to ${bpm} BPM rhythm" else "Tap Vyshu to chat & groove!",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Chat button
                    IconButton(
                        onClick = onOpenChat,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Chat with Vyshu",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Minimize / Expand Stage button
                    IconButton(
                        onClick = { isMinimized = !isMinimized },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isMinimized) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = if (isMinimized) "Expand Stage" else "Collapse Stage",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable Vyshu Animation Stage Body
            AnimatedVisibility(visible = !isMinimized) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Speech Bubble if Vyshu speaks or is tapped
                    AnimatedVisibility(
                        visible = displaySpeech != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF8B5CF6))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = displaySpeech ?: "",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    // HTML5, CSS3 & JavaScript Animated Vyshu AI Character
                    VyshuWebAnimationStage(
                        isPlaying = isPlaying,
                        bpm = bpm,
                        height = 190.dp,
                        onVyshuClick = onVyshuClick
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Companion Action Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "💬 Chat with Vyshu" to onOpenChat,
                            "✨ Cheer Me Up" to { onVyshuClick() },
                            "🎵 Vibe Check" to { onVyshuClick() }
                        ).forEach { (label, action) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, Color(0xFF33294F), RoundedCornerShape(14.dp))
                                    .clickable { action() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color(0xFFA78BFA)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
