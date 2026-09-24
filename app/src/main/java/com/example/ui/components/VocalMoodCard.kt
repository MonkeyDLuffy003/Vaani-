package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.VocalMoodAnalysis
import com.example.ui.theme.AccentRose
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VocalMoodCard(
    activeMood: VocalMoodAnalysis?,
    vaaniMessage: String,
    onFeedback: (String) -> Unit,
    onAnalyzeMoodClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF161B2E),
            Color(0xFF101322),
            Color(0xFF191D30)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradientBrush)
            .border(1.dp, PrimaryNeon.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("vocal_mood_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Vaani AI Badge & Mood indicator
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
                            .background(PrimaryNeon.copy(alpha = 0.2f))
                            .border(1.dp, PrimaryNeon, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SecondaryCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Text(
                        text = "VAANI • VOCAL MOOD AGENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = SecondaryCyan
                    )
                }

                // Analyze button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryNeon.copy(alpha = 0.2f))
                        .border(1.dp, PrimaryNeon.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { onAnalyzeMoodClick() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("analyze_mood_action")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Check Mood",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = TextPrimary
                        )
                    }
                }
            }

            // Message / Vocal Insight
            Text(
                text = vaaniMessage,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = TextPrimary
            )

            // Analysis Characteristics Detail (if analyzed)
            AnimatedVisibility(visible = activeMood != null) {
                activeMood?.let { mood ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F1320))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Detected Mood: ${mood.mood}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = AccentRose
                            )
                            Text(
                                text = "${(mood.confidence * 100).toInt()}% Match",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = SecondaryCyan
                            )
                        }

                        Text(
                            text = mood.vocalCharacteristics,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }
            }

            // Real-Time User Feedback Chips (One-hand accessible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Thumbs up feedback
                FeedbackChip(
                    icon = Icons.Default.ThumbUp,
                    label = "Love it",
                    onClick = { onFeedback("THUMBS_UP") },
                    modifier = Modifier.weight(1f),
                    testTag = "feedback_thumbs_up"
                )

                // More Energy feedback
                FeedbackChip(
                    icon = Icons.Default.Speed,
                    label = "+Energy",
                    onClick = { onFeedback("MORE_ENERGY") },
                    modifier = Modifier.weight(1f),
                    testTag = "feedback_more_energy"
                )

                // Relax tempo feedback
                FeedbackChip(
                    icon = Icons.Default.SelfImprovement,
                    label = "Calm Down",
                    onClick = { onFeedback("RELAX_TEMPO") },
                    modifier = Modifier.weight(1f),
                    testTag = "feedback_relax_tempo"
                )
            }
        }
    }
}

@Composable
fun FeedbackChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .border(1.dp, Color(0xFF2A334B), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SecondaryCyan,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = TextPrimary
            )
        }
    }
}
