package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SongEntity
import com.example.ui.DanceMode
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(
    song: SongEntity?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    isRepeat: Boolean,
    isSleepTimerActive: Boolean,
    sleepTimerRemainingSec: Int,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onOfflineToggle: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onFeedback: (String) -> Unit,
    onDismiss: () -> Unit,
    isLuffyDancingEnabled: Boolean = true,
    danceMode: DanceMode = DanceMode.BEAT_BOUNCE,
    onDanceModeChange: (DanceMode) -> Unit = {},
    onLuffyClick: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showLuffyVisualizer by remember { mutableStateOf(isLuffyDancingEnabled) }

    val channelColor = when (song?.genre?.lowercase()) {
        "gym" -> Color(0xFFEF4444)
        "cooking" -> Color(0xFFF59E0B)
        "home" -> Color(0xFF10B981)
        "relax" -> SecondaryCyan
        "motivation" -> Color(0xFFA855F7)
        "storage" -> Color(0xFF38BDF8)
        else -> PrimaryNeon
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceElevated,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF334155)) },
        modifier = Modifier.testTag("full_player_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Visualizer / Luffy Dancing Stage Container
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (showLuffyVisualizer) Color(0xFFF7C325).copy(alpha = 0.25f) else channelColor.copy(alpha = 0.35f),
                                Color(0xFF0F121C)
                            )
                        )
                    )
                    .border(
                        2.dp,
                        if (showLuffyVisualizer) Color(0xFFF7C325).copy(alpha = 0.6f) else channelColor.copy(alpha = 0.5f),
                        RoundedCornerShape(28.dp)
                    )
                    .clickable {
                        if (showLuffyVisualizer) onLuffyClick() else showLuffyVisualizer = true
                    },
                contentAlignment = Alignment.Center
            ) {
                if (showLuffyVisualizer) {
                    // Full Dancing Luffy Character
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LuffyDancingCharacter(
                            isPlaying = isPlaying,
                            bpm = song?.bpm ?: 120,
                            danceMode = danceMode,
                            size = 155.dp,
                            showNotes = true,
                            showStageGlow = true,
                            onClick = onLuffyClick
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = channelColor,
                            modifier = Modifier.size(54.dp)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(channelColor.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = song?.genre ?: "Channel",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = channelColor
                            )
                        }

                        Text(
                            text = "${song?.bpm ?: 120} BPM",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }
            }

            // Visualizer Switcher & Luffy Dance Mode Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch between Channel Art and Dancing Luffy
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCard)
                        .clickable { showLuffyVisualizer = !showLuffyVisualizer }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("full_player_luffy_toggle")
                ) {
                    Text(
                        text = if (showLuffyVisualizer) "👒 Luffy Groove (Tap to Speak)" else "⚡ Switch to Luffy Dancer",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (showLuffyVisualizer) Color(0xFFFDE047) else SecondaryCyan
                    )
                }
            }

            // Song Info Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = song?.title ?: "No Song Selected",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = song?.artist ?: "Vaani AI Music",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = TextSecondary
                )
            }

            // Interactive Progress Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (durationMs > 0) currentPositionMs.toFloat() else 0f,
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..(if (durationMs > 0) durationMs.toFloat() else 1f),
                    colors = SliderDefaults.colors(
                        thumbColor = SecondaryCyan,
                        activeTrackColor = SecondaryCyan,
                        inactiveTrackColor = Color(0xFF263046)
                    ),
                    modifier = Modifier.testTag("player_progress_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(currentPositionMs),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextTertiary
                    )
                    Text(
                        text = formatDuration(durationMs),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextTertiary
                    )
                }
            }

            // Playback Control Buttons (Thumb reach zone)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.testTag("player_shuffle_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) SecondaryCyan else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_prev_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Play / Pause Primary Button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryNeon)
                        .clickable { onPlayPause() }
                        .testTag("player_play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_next_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Repeat
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.testTag("player_repeat_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (isRepeat) SecondaryCyan else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Real-Time Feedback Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeedbackChip(
                    icon = Icons.Default.Favorite,
                    label = if (song?.isFavorite == true) "Favorited" else "Favorite",
                    onClick = onFavoriteToggle,
                    modifier = Modifier.weight(1f),
                    testTag = "full_fav_toggle"
                )

                FeedbackChip(
                    icon = if (song?.isOffline == true) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                    label = if (song?.isOffline == true) "Offline" else "Download",
                    onClick = onOfflineToggle,
                    modifier = Modifier.weight(1f),
                    testTag = "full_offline_toggle"
                )

                FeedbackChip(
                    icon = Icons.Default.Bedtime,
                    label = if (isSleepTimerActive) "Timer: ${sleepTimerRemainingSec / 60}m" else "Sleep Timer",
                    onClick = onSleepTimerClick,
                    modifier = Modifier.weight(1f),
                    testTag = "full_sleep_timer_btn"
                )
            }
        }
    }
}
