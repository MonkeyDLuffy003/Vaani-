package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlin.math.sin

/**
 * Vaani Hero View — Pixel-accurate recreation of Vaani's UI & Visual Identity:
 * 1. Top bar: ☰ hamburger menu + Vaani brand title & equalizer + Vyshu companion tag
 * 2. Center: VAANI — Character with glowing headphones, bindi, music necklace, and reactive animations
 * 3. Speech bubble: Glassmorphism rounded bubble ("I'm here, Teja. 🎵 / Playing your music.")
 * 4. Voice controls: Concentric glowing mic orb with sinusoidal frequency ribbons
 * 5. Status pill: "● Listening" / "● Playing" / "● Processing"
 */
@Composable
fun VaaniHeroView(
    isListening: Boolean,
    isPlaying: Boolean,
    isProcessing: Boolean,
    isSpeaking: Boolean,
    rmsAudioLevel: Float,
    mainMessage: String,
    subMessage: String,
    hasActiveTrack: Boolean,
    onMicClick: () -> Unit,
    onMenuClick: () -> Unit,
    onVyshuClick: () -> Unit,
    onBubbleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vaani_hero_anims")

    // Subtle natural breathing
    val breatheY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheY"
    )
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.012f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    // Music groove bounce
    val grooveY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) -6f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "grooveY"
    )

    // Headphone aura pulse
    val headphonePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "headphonePulse"
    )
    val headphoneAlpha by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.8f else if (isPlaying) 0.6f else 0.45f,
        targetValue = if (isListening) 1.0f else if (isPlaying) 0.8f else 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "headphoneAlpha"
    )

    // Mic orb outer ripple
    val orbRippleScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbRipple"
    )
    val orbRippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbRippleAlpha"
    )

    // Sine wave motion
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Equalizer bars
    val eq1 by infiniteTransition.animateFloat(initialValue = 0.35f, targetValue = 1.0f, animationSpec = infiniteRepeatable(tween(320, easing = LinearEasing), RepeatMode.Reverse), label = "eq1")
    val eq2 by infiniteTransition.animateFloat(initialValue = 0.85f, targetValue = 0.25f, animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse), label = "eq2")
    val eq3 by infiniteTransition.animateFloat(initialValue = 0.45f, targetValue = 1.0f, animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse), label = "eq3")
    val eq4 by infiniteTransition.animateFloat(initialValue = 0.95f, targetValue = 0.3f, animationSpec = infiniteRepeatable(tween(460, easing = LinearEasing), RepeatMode.Reverse), label = "eq4")

    val statusPillText = when {
        isProcessing -> "Processing"
        isSpeaking -> "Vaani Speaking"
        isPlaying -> "Playing"
        isListening -> "Listening"
        else -> "Listening"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF130926),
                        Color(0xFF0C061A),
                        Color(0xFF070310)
                    )
                )
            )
    ) {
        // Deep background ambient studio spotlight
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.18f),
                        Color(0xFF3B82F6).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.35f),
                    radius = size.width * 0.7f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // 1. TOP BAR: ☰ + Vaani AI Music Assistant + 💜 Vyshu
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ☰ Three Lines Hamburger Menu Button
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1038).copy(alpha = 0.6f))
                        .border(1.dp, Color(0xFF6B46C1).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .testTag("home_hamburger_btn")
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.width(22.dp).height(2.2.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
                        Box(modifier = Modifier.width(22.dp).height(2.2.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
                        Box(modifier = Modifier.width(22.dp).height(2.2.dp).clip(RoundedCornerShape(2.dp)).background(Color.White))
                    }
                }

                // Center Title: Glowing Equalizer + Italic Vaani
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Glowing mini equalizer icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.height(18.dp)
                        ) {
                            listOf(eq1, eq2, eq3, eq4, eq1).forEach { animatedH ->
                                Box(
                                    modifier = Modifier
                                        .width(2.5.dp)
                                        .height((16 * animatedH).dp.coerceAtLeast(4.dp))
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFFE879F9), Color(0xFFA855F7))
                                            )
                                        )
                                )
                            }
                        }

                        Text(
                            text = "Vaani",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = FontStyle.Italic,
                                fontSize = 24.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )
                    }

                    Text(
                        text = "AI Music Assistant",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFFC4B5FD)
                    )
                }

                // 💜 Vyshu Companion Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.22f))
                        .border(1.dp, Color(0xFFA78BFA).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onVyshuClick() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("home_vyshu_tag")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("💜", fontSize = 12.sp)
                        Text(
                            text = "Vyshu",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = Color(0xFFF5F3FF)
                        )
                    }
                }
            }

            // ==========================================
            // 2. HERO: SHE IS VAANI (THE VISUAL IDENTITY)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulsing Headphone Glow behind Vaani's head
                Canvas(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(headphonePulse)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (isListening) Color(0xFFE879F9) else if (isProcessing) Color(0xFF38BDF8) else Color(0xFFA855F7)).copy(alpha = headphoneAlpha),
                                Color(0xFF6366F1).copy(alpha = headphoneAlpha * 0.35f),
                                Color.Transparent
                            ),
                            radius = size.width * 0.5f
                        )
                    )
                }

                // Left & Right Equalizer Wave Wings (matching screenshot!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Wings
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(eq1 * 16, eq3 * 32, eq2 * 44, eq4 * 26, eq1 * 12).forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(h.dp.coerceAtLeast(6.dp))
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Brush.verticalGradient(listOf(Color(0xFFC084FC), Color(0xFF818CF8))))
                            )
                        }
                    }

                    // Right Wings
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(eq2 * 12, eq4 * 26, eq1 * 44, eq3 * 32, eq2 * 16).forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(h.dp.coerceAtLeast(6.dp))
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Brush.verticalGradient(listOf(Color(0xFF818CF8), Color(0xFFC084FC))))
                            )
                        }
                    }
                }

                // VAANI CHARACTER IMAGE (HER!)
                Image(
                    painter = painterResource(id = R.drawable.vaani_character),
                    contentDescription = "Vaani AI Music Assistant",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxHeight(0.98f)
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = breatheY + grooveY
                            scaleX = breatheScale
                            scaleY = breatheScale
                        }
                        .testTag("vaani_character_hero_image")
                )
            }

            // ==========================================
            // 3. GLASSMORPHIC AI SPEECH BUBBLE
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF1D1036).copy(alpha = 0.88f))
                    .border(1.5.dp, Color(0xFFC084FC).copy(alpha = 0.55f), RoundedCornerShape(26.dp))
                    .clickable { onBubbleClick() }
                    .padding(horizontal = 18.dp, vertical = 12.dp)
                    .testTag("vaani_speech_bubble")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Equalizer icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        listOf(eq1, eq3, eq2, eq4, eq1).forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height((22 * h).dp.coerceAtLeast(6.dp))
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Brush.verticalGradient(listOf(Color(0xFFE879F9), Color(0xFFA855F7))))
                            )
                        }
                    }

                    // Message text
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = mainMessage.ifBlank { "I'm here, Teja. 🎵" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subMessage.ifBlank { "Playing your music." },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = Color(0xFFC4B5FD),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ==========================================
            // 4. CONCENTRIC MIC ORB & SINE WAVEFORM
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (hasActiveTrack) 8.dp else 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated Sinusoidal Frequency Wave Ribbon across background
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(48.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val midY = height / 2f

                        val path1 = Path()
                        val path2 = Path()

                        val amplitude1 = 14f + (rmsAudioLevel * 20f).coerceAtMost(22f)
                        val amplitude2 = 10f + (rmsAudioLevel * 16f).coerceAtMost(18f)

                        for (x in 0..width.toInt() step 4) {
                            val xPos = x.toFloat()
                            val rad1 = (xPos / width) * 4 * Math.PI + wavePhase
                            val rad2 = (xPos / width) * 5 * Math.PI - wavePhase

                            val y1 = midY + (sin(rad1) * amplitude1).toFloat()
                            val y2 = midY + (sin(rad2) * amplitude2).toFloat()

                            if (x == 0) {
                                path1.moveTo(xPos, y1)
                                path2.moveTo(xPos, y2)
                            } else {
                                path1.lineTo(xPos, y1)
                                path2.lineTo(xPos, y2)
                            }
                        }

                        drawPath(
                            path = path1,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF38BDF8).copy(alpha = 0.1f),
                                    Color(0xFFA855F7).copy(alpha = 0.85f),
                                    Color(0xFFEC4899).copy(alpha = 0.85f),
                                    Color(0xFF38BDF8).copy(alpha = 0.1f)
                                )
                            ),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        drawPath(
                            path = path2,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.1f),
                                    Color(0xFFD946EF).copy(alpha = 0.7f),
                                    Color(0xFF3B82F6).copy(alpha = 0.1f)
                                )
                            ),
                            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Concentric Outer Pulsing Rings
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(orbRippleScale)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFFC084FC).copy(alpha = orbRippleAlpha), CircleShape)
                    )

                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .scale(orbRippleScale * 0.9f)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFF6366F1).copy(alpha = (orbRippleAlpha * 0.7f).coerceIn(0f, 1f)), CircleShape)
                    )

                    // Central Glowing Microphone Orb
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isListening) {
                                        listOf(Color(0xFFEC4899), Color(0xFF9333EA), Color(0xFF3B82F6))
                                    } else if (isProcessing) {
                                        listOf(Color(0xFF0284C7), Color(0xFF6366F1), Color(0xFFA855F7))
                                    } else {
                                        listOf(Color(0xFF9333EA), Color(0xFF6366F1), Color(0xFF3B82F6))
                                    }
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                            .clickable { onMicClick() }
                            .testTag("vaani_concentric_mic_orb"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Talk to Vaani",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ==========================================
                // 5. STATUS PILL: "● Listening" (Clean, minimal!)
                // ==========================================
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E1038).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                        .testTag("vaani_status_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val dotColor = when {
                            isListening -> Color(0xFFE879F9)
                            isProcessing -> Color(0xFF38BDF8)
                            isPlaying -> Color(0xFF10B981)
                            else -> Color(0xFFE879F9)
                        }

                        Box(
                            modifier = Modifier
                                .size(7.5.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )

                        Text(
                            text = statusPillText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                letterSpacing = 0.3.sp
                            ),
                            color = Color(0xFFF1F5F9)
                        )
                    }
                }
            }
        }
    }
}
