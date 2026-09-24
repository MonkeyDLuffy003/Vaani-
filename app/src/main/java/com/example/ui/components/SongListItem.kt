package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SongEntity
import com.example.ui.theme.AccentRose
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun SongListItem(
    song: SongEntity,
    isPlaying: Boolean,
    isCurrent: Boolean,
    onSongClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onOfflineToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val channelColor = when (song.genre.lowercase()) {
        "gym" -> Color(0xFFEF4444)
        "cooking" -> Color(0xFFF59E0B)
        "home" -> Color(0xFF10B981)
        "relax" -> SecondaryCyan
        "motivation" -> Color(0xFFA855F7)
        "storage" -> Color(0xFF38BDF8)
        else -> PrimaryNeon
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isCurrent) PrimaryNeon.copy(alpha = 0.12f) else SurfaceCard
            )
            .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                color = if (isCurrent) PrimaryNeon else DarkBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSongClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("song_item_${song.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Artwork placeholder / Wave indicator
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(channelColor.copy(alpha = 0.2f))
                    .border(1.dp, channelColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrent && isPlaying) {
                    EqualizerWaveAnimation(color = channelColor)
                } else {
                    Icon(
                        imageVector = if (song.genre == "Storage") Icons.Default.PhoneAndroid else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = channelColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Song Info (High contrast modern sans-serif)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (isCurrent) PrimaryNeon else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        color = TextTertiary
                    )

                    // Channel Pill Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(channelColor.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = song.genre,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            ),
                            color = channelColor
                        )
                    }

                    if (song.isOffline) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Offline ready",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Duration text
            Text(
                text = formatDuration(song.durationMs),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextTertiary
            )

            // Offline Download Button
            IconButton(
                onClick = onOfflineToggle,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("offline_toggle_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isOffline) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                    contentDescription = if (song.isOffline) "Available Offline" else "Download Offline",
                    tint = if (song.isOffline) Color(0xFF10B981) else TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Heart Favorite
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("fav_button_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (song.isFavorite) AccentRose else TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EqualizerWaveAnimation(color: Color) {
    val transition = rememberInfiniteTransition(label = "eq_transition")
    val h1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.height(20.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height((20 * h1).dp)
                .clip(CircleShape)
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height((20 * h2).dp)
                .clip(CircleShape)
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height((20 * h3).dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}
