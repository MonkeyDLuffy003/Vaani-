package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * High-fidelity, animated dancing Monkey D. Luffy character component.
 * Features customizable dance styles (Beat Bounce, Gear 5 Hype, Straw Hat Chill),
 * squash & stretch physics, rhythmic foot taps, floating musical notes,
 * and interactive speech reactions.
 */
@Composable
fun LuffyDancingCharacter(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    bpm: Int = 120,
    danceMode: DanceMode = DanceMode.BEAT_BOUNCE,
    size: Dp = 150.dp,
    showNotes: Boolean = true,
    showStageGlow: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    // Dynamic tempo calculations based on track BPM
    val beatPeriodMs = remember(bpm, danceMode) {
        val baseMs = ((60f / bpm.coerceIn(60, 220)) * 1000f).toInt()
        when (danceMode) {
            DanceMode.GEAR_5 -> (baseMs * 0.7f).toInt().coerceAtLeast(300)
            DanceMode.STRAW_HAT_CHILL -> (baseMs * 1.3f).toInt()
            DanceMode.BEAT_BOUNCE -> baseMs
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "luffy_dance_anim")

    // Vertical Jump & Bounce Animation
    val jumpBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = when {
            !isPlaying -> -4f // Idle subtle groove
            danceMode == DanceMode.GEAR_5 -> -36f // Big hyper cartoon bounce
            danceMode == DanceMode.STRAW_HAT_CHILL -> -12f // Gentle sway bob
            else -> -24f // Energetic rhythm jump
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = beatPeriodMs / 2,
                easing = if (isPlaying) FastOutSlowInEasing else LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jump_bounce"
    )

    // Side-to-side sway rotation angle
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = when {
            !isPlaying -> -3f
            danceMode == DanceMode.GEAR_5 -> -16f
            danceMode == DanceMode.STRAW_HAT_CHILL -> -6f
            else -> -11f
        },
        targetValue = when {
            !isPlaying -> 3f
            danceMode == DanceMode.GEAR_5 -> 16f
            danceMode == DanceMode.STRAW_HAT_CHILL -> 6f
            else -> 11f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = beatPeriodMs,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway_angle"
    )

    // Squash & Stretch physics (Rubber property of Luffy!)
    val squashFactor by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = if (isPlaying) {
            if (danceMode == DanceMode.GEAR_5) 1.15f else 1.08f
        } else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = beatPeriodMs / 2,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "squash_factor"
    )

    // Arm pump groove angle
    val armGrooveAngle by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = if (isPlaying) 24f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = beatPeriodMs / 2,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arm_groove"
    )

    // Floating musical note offsets (Notes 1, 2, 3)
    val noteFloat1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "note1"
    )

    val noteFloat2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "note2"
    )

    val noteFloat3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, delayMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "note3"
    )

    // Stage spotlight pulse
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (isPlaying) 1.25f else 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(beatPeriodMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    val scaleX = squashFactor
    val scaleY = 2f - squashFactor // Preserves volume mass

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick?.invoke() }
            .testTag("luffy_dancing_character"),
        contentAlignment = Alignment.Center
    ) {
        // Stage Spotlight Glow / Aura
        if (showStageGlow) {
            val auraColor = when (danceMode) {
                DanceMode.GEAR_5 -> Color(0xFFE0E7FF)
                DanceMode.STRAW_HAT_CHILL -> SecondaryCyan.copy(alpha = 0.3f)
                DanceMode.BEAT_BOUNCE -> PrimaryNeon.copy(alpha = 0.35f)
            }
            Box(
                modifier = Modifier
                    .size(size * 0.9f)
                    .align(Alignment.BottomCenter)
                    .offset(y = 6.dp)
                    .scale(auraScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                auraColor,
                                auraColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Dynamic Floor Shadow (shrinks and lightens as Luffy jumps higher)
        val shadowWidth = (size * 0.62f) * (1f + (jumpBounce / 90f))
        val shadowAlpha = (0.45f + (jumpBounce / 60f)).coerceIn(0.15f, 0.55f)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-4).dp)
                .width(shadowWidth)
                .height(size * 0.12f)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = shadowAlpha))
        )

        // Floating Musical Notes & Beat Sparks
        if (showNotes && isPlaying) {
            // Note 1 (Left top)
            Text(
                text = "♪",
                color = PrimaryNeon,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 6.dp, y = (noteFloat1 * (size / 150.dp)).dp)
                    .scale((1f - (noteFloat1 / -45f)).coerceIn(0.4f, 1.2f))
                    .rotate(-15f)
            )

            // Note 2 (Right top)
            Text(
                text = "♫",
                color = SecondaryCyan,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = (noteFloat2 * (size / 150.dp)).dp)
                    .scale((1f - (noteFloat2 / -50f)).coerceIn(0.4f, 1.2f))
                    .rotate(18f)
            )

            // Sparkle / Star note (Center top)
            Text(
                text = if (danceMode == DanceMode.GEAR_5) "✦" else "♬",
                color = if (danceMode == DanceMode.GEAR_5) Color(0xFFFDE047) else AccentRose,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 12.dp, y = (noteFloat3 * (size / 150.dp)).dp)
                    .scale((1f - (noteFloat3 / -45f)).coerceIn(0.3f, 1.1f))
            )
        }

        // Animated Luffy Body Container with Squash, Stretch, Tilt and Jump
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (jumpBounce * (size / 150.dp)).dp)
                .rotate(swayAngle)
                .scale(scaleX = scaleX, scaleY = scaleY),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLuffyCharacter(
                    canvasWidth = this.size.width,
                    canvasHeight = this.size.height,
                    isPlaying = isPlaying,
                    armAngle = armGrooveAngle,
                    danceMode = danceMode
                )
            }
        }
    }
}

/**
 * Detailed Vector Illustration of Monkey D. Luffy wearing his iconic straw hat,
 * glowing neon DJ headphones, red vest, chest X/smile, and joyful expression.
 */
private fun DrawScope.drawLuffyCharacter(
    canvasWidth: Float,
    canvasHeight: Float,
    isPlaying: Boolean,
    armAngle: Float,
    danceMode: DanceMode
) {
    val cx = canvasWidth / 2f
    val cy = canvasHeight / 2f
    val unit = canvasWidth / 120f // Base scalable coordinate grid

    // Palette
    val skinTone = Color(0xFFFFDFC4)
    val skinShadow = Color(0xFFF1BEA0)
    val strawHatYellow = Color(0xFFF7C325)
    val strawHatShadow = Color(0xFFD99B16)
    val strawRibbonRed = Color(0xFFDC2626)
    val vestRed = Color(0xFFDC2626)
    val vestDarkRed = Color(0xFF991B1B)
    val hairBlack = Color(0xFF18181B)
    val shortsBlue = Color(0xFF2563EB)
    val shortsDark = Color(0xFF1D4ED8)
    val sashYellow = Color(0xFFFBBF24)
    val headphoneNeon = when (danceMode) {
        DanceMode.GEAR_5 -> Color(0xFF67E8F9)
        DanceMode.STRAW_HAT_CHILL -> Color(0xFF34D399)
        DanceMode.BEAT_BOUNCE -> Color(0xFFF43F5E)
    }

    // 1. LEGS & SANDALS
    val legY = cy + 26f * unit
    val legWidth = 7f * unit
    val legHeight = 16f * unit

    // Left leg
    drawRoundRect(
        color = shortsBlue,
        topLeft = Offset(cx - 15f * unit, legY - 2f * unit),
        size = Size(11f * unit, 14f * unit),
        cornerRadius = CornerRadius(3f * unit, 3f * unit)
    )
    // Left leg skin & sandal
    drawRoundRect(
        color = skinTone,
        topLeft = Offset(cx - 13f * unit, legY + 8f * unit),
        size = Size(7f * unit, 10f * unit),
        cornerRadius = CornerRadius(2f * unit, 2f * unit)
    )
    // Left Sandal strap
    drawCircle(
        color = strawHatYellow,
        radius = 3f * unit,
        center = Offset(cx - 10f * unit, legY + 16f * unit)
    )

    // Right leg (kicking or grooving)
    drawRoundRect(
        color = shortsBlue,
        topLeft = Offset(cx + 4f * unit, legY - 2f * unit),
        size = Size(11f * unit, 14f * unit),
        cornerRadius = CornerRadius(3f * unit, 3f * unit)
    )
    // Right leg skin & sandal
    drawRoundRect(
        color = skinTone,
        topLeft = Offset(cx + 6f * unit, legY + 8f * unit),
        size = Size(7f * unit, 10f * unit),
        cornerRadius = CornerRadius(2f * unit, 2f * unit)
    )
    // Right Sandal strap
    drawCircle(
        color = strawHatYellow,
        radius = 3f * unit,
        center = Offset(cx + 9f * unit, legY + 16f * unit)
    )

    // 2. TORSO & VEST
    val torsoY = cy + 6f * unit
    val torsoWidth = 26f * unit
    val torsoHeight = 22f * unit

    // Red open vest background
    drawRoundRect(
        color = vestRed,
        topLeft = Offset(cx - torsoWidth / 2f, torsoY),
        size = Size(torsoWidth, torsoHeight),
        cornerRadius = CornerRadius(5f * unit, 5f * unit)
    )

    // Exposed chest & abs (Skin)
    val chestPath = Path().apply {
        moveTo(cx - 6f * unit, torsoY)
        lineTo(cx + 6f * unit, torsoY)
        lineTo(cx + 4f * unit, torsoY + 15f * unit)
        lineTo(cx - 4f * unit, torsoY + 15f * unit)
        close()
    }
    drawPath(chestPath, skinTone)

    // Iconic X chest scar (subtle line or mark)
    drawLine(
        color = Color(0xFFD97706),
        start = Offset(cx - 3f * unit, torsoY + 4f * unit),
        end = Offset(cx + 3f * unit, torsoY + 10f * unit),
        strokeWidth = 1.2f * unit,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFD97706),
        start = Offset(cx + 3f * unit, torsoY + 4f * unit),
        end = Offset(cx - 3f * unit, torsoY + 10f * unit),
        strokeWidth = 1.2f * unit,
        cap = StrokeCap.Round
    )

    // Yellow waist sash
    drawRoundRect(
        color = sashYellow,
        topLeft = Offset(cx - 14f * unit, torsoY + 17f * unit),
        size = Size(28f * unit, 5f * unit),
        cornerRadius = CornerRadius(2f * unit, 2f * unit)
    )

    // 3. ARMS (Animated to dance rhythm)
    // Left Arm (Pumping up in rhythm)
    val leftArmOffsetY = sin(armAngle * Math.PI.toFloat() / 180f) * 6f * unit
    drawRoundRect(
        color = skinTone,
        topLeft = Offset(cx - 24f * unit, torsoY + 2f * unit - leftArmOffsetY),
        size = Size(9f * unit, 18f * unit),
        cornerRadius = CornerRadius(4f * unit, 4f * unit)
    )
    // Left fist
    drawCircle(
        color = skinTone,
        radius = 5f * unit,
        center = Offset(cx - 20f * unit, torsoY + 1f * unit - leftArmOffsetY)
    )
    // Left vest sleeve edge
    drawCircle(
        color = vestDarkRed,
        radius = 4f * unit,
        center = Offset(cx - 14f * unit, torsoY + 4f * unit)
    )

    // Right Arm (Grooving / Peace sign gesture)
    val rightArmOffsetY = -sin(armAngle * Math.PI.toFloat() / 180f) * 6f * unit
    drawRoundRect(
        color = skinTone,
        topLeft = Offset(cx + 15f * unit, torsoY + 2f * unit + rightArmOffsetY),
        size = Size(9f * unit, 18f * unit),
        cornerRadius = CornerRadius(4f * unit, 4f * unit)
    )
    // Right hand / peace gesture
    drawCircle(
        color = skinTone,
        radius = 5f * unit,
        center = Offset(cx + 20f * unit, torsoY + 18f * unit + rightArmOffsetY)
    )
    // Right vest sleeve edge
    drawCircle(
        color = vestDarkRed,
        radius = 4f * unit,
        center = Offset(cx + 14f * unit, torsoY + 4f * unit)
    )

    // 4. NECK & HEAD
    val headCenterY = cy - 12f * unit
    val headRadius = 18f * unit

    // Neck
    drawRect(
        color = skinShadow,
        topLeft = Offset(cx - 5f * unit, headCenterY + 10f * unit),
        size = Size(10f * unit, 8f * unit)
    )

    // Black Hair tufts (Back & sides)
    val hairPathBack = Path().apply {
        moveTo(cx - 20f * unit, headCenterY - 2f * unit)
        lineTo(cx - 24f * unit, headCenterY + 10f * unit)
        lineTo(cx - 16f * unit, headCenterY + 14f * unit)
        lineTo(cx - 18f * unit, headCenterY + 18f * unit)
        lineTo(cx - 10f * unit, headCenterY + 17f * unit)
        lineTo(cx + 10f * unit, headCenterY + 17f * unit)
        lineTo(cx + 18f * unit, headCenterY + 18f * unit)
        lineTo(cx + 16f * unit, headCenterY + 14f * unit)
        lineTo(cx + 24f * unit, headCenterY + 10f * unit)
        lineTo(cx + 20f * unit, headCenterY - 2f * unit)
        close()
    }
    drawPath(hairPathBack, hairBlack)

    // Face Circle
    drawCircle(
        color = skinTone,
        radius = headRadius,
        center = Offset(cx, headCenterY)
    )

    // Cheerful Anime Hair Bangs (Forehead)
    val hairBangs = Path().apply {
        moveTo(cx - 16f * unit, headCenterY - 6f * unit)
        lineTo(cx - 10f * unit, headCenterY + 2f * unit)
        lineTo(cx - 6f * unit, headCenterY - 4f * unit)
        lineTo(cx, headCenterY + 3f * unit)
        lineTo(cx + 6f * unit, headCenterY - 4f * unit)
        lineTo(cx + 12f * unit, headCenterY + 2f * unit)
        lineTo(cx + 16f * unit, headCenterY - 6f * unit)
        close()
    }
    drawPath(hairBangs, hairBlack)

    // 5. ICONIC LUFFY EYE SCAR (Under left eye)
    val scarStartX = cx - 9f * unit
    val scarStartY = headCenterY + 2.5f * unit
    drawLine(
        color = Color(0xFF991B1B),
        start = Offset(scarStartX, scarStartY),
        end = Offset(scarStartX + 5f * unit, scarStartY + 3.5f * unit),
        strokeWidth = 1.4f * unit,
        cap = StrokeCap.Round
    )
    // Stitches
    drawLine(
        color = Color(0xFF991B1B),
        start = Offset(scarStartX + 1.5f * unit, scarStartY - 1f * unit),
        end = Offset(scarStartX + 2.5f * unit, scarStartY + 2.5f * unit),
        strokeWidth = 1f * unit,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF991B1B),
        start = Offset(scarStartX + 3.5f * unit, scarStartY + 0.5f * unit),
        end = Offset(scarStartX + 4.5f * unit, scarStartY + 3.5f * unit),
        strokeWidth = 1f * unit,
        cap = StrokeCap.Round
    )

    // 6. EYES (Happy curved anime crescent arcs when dancing!)
    if (isPlaying) {
        // Joyful closed happy eyes
        drawArc(
            color = hairBlack,
            startAngle = 190f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(cx - 13f * unit, headCenterY - 4f * unit),
            size = Size(8f * unit, 6f * unit),
            style = Stroke(width = 2.2f * unit, cap = StrokeCap.Round)
        )
        drawArc(
            color = hairBlack,
            startAngle = 190f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(cx + 5f * unit, headCenterY - 4f * unit),
            size = Size(8f * unit, 6f * unit),
            style = Stroke(width = 2.2f * unit, cap = StrokeCap.Round)
        )
    } else {
        // Attentive wide eyes when paused
        drawCircle(color = Color.White, radius = 4f * unit, center = Offset(cx - 8f * unit, headCenterY - 2f * unit))
        drawCircle(color = hairBlack, radius = 2.5f * unit, center = Offset(cx - 8f * unit, headCenterY - 2f * unit))
        drawCircle(color = Color.White, radius = 1f * unit, center = Offset(cx - 7.5f * unit, headCenterY - 2.5f * unit))

        drawCircle(color = Color.White, radius = 4f * unit, center = Offset(cx + 8f * unit, headCenterY - 2f * unit))
        drawCircle(color = hairBlack, radius = 2.5f * unit, center = Offset(cx + 8f * unit, headCenterY - 2f * unit))
        drawCircle(color = Color.White, radius = 1f * unit, center = Offset(cx + 8.5f * unit, headCenterY - 2.5f * unit))
    }

    // Rosy anime cheek blush
    drawCircle(color = Color(0xFFFB7185).copy(alpha = 0.5f), radius = 3.5f * unit, center = Offset(cx - 11f * unit, headCenterY + 4f * unit))
    drawCircle(color = Color(0xFFFB7185).copy(alpha = 0.5f), radius = 3.5f * unit, center = Offset(cx + 11f * unit, headCenterY + 4f * unit))

    // 7. HUGE SIGNATURE D. SMILE (The legendary ear-to-ear Luffy grin!)
    val mouthPath = Path().apply {
        moveTo(cx - 11f * unit, headCenterY + 6f * unit)
        quadraticTo(
            cx,
            headCenterY + 17f * unit,
            cx + 11f * unit,
            headCenterY + 6f * unit
        )
        close()
    }
    // Mouth cavity
    drawPath(mouthPath, Color(0xFF991B1B))
    // Teeth line (white radiant teeth)
    val teethPath = Path().apply {
        moveTo(cx - 9.5f * unit, headCenterY + 7f * unit)
        quadraticTo(
            cx,
            headCenterY + 11f * unit,
            cx + 9.5f * unit,
            headCenterY + 7f * unit
        )
        lineTo(cx + 9.5f * unit, headCenterY + 6f * unit)
        lineTo(cx - 9.5f * unit, headCenterY + 6f * unit)
        close()
    }
    drawPath(teethPath, Color.White)

    // Tongue
    drawCircle(
        color = Color(0xFFF43F5E),
        radius = 3.5f * unit,
        center = Offset(cx, headCenterY + 12.5f * unit)
    )

    // 8. THE ICONIC STRAW HAT (Mugiwara!)
    val hatY = headCenterY - 14f * unit

    // Straw Hat Brim (wide tilted curved yellow disc)
    val brimPath = Path().apply {
        moveTo(cx - 38f * unit, hatY + 6f * unit)
        quadraticTo(
            cx,
            hatY - 2f * unit,
            cx + 38f * unit,
            hatY + 6f * unit
        )
        quadraticTo(
            cx,
            hatY + 12f * unit,
            cx - 38f * unit,
            hatY + 6f * unit
        )
        close()
    }
    drawPath(brimPath, strawHatYellow)

    // Straw Hat Crown Dome
    val crownPath = Path().apply {
        moveTo(cx - 18f * unit, hatY + 5f * unit)
        cubicTo(
            cx - 19f * unit,
            hatY - 18f * unit,
            cx + 19f * unit,
            hatY - 18f * unit,
            cx + 18f * unit,
            hatY + 5f * unit
        )
        close()
    }
    drawPath(crownPath, strawHatYellow)

    // Straw Hat Red Ribbon Band
    val ribbonPath = Path().apply {
        moveTo(cx - 18f * unit, hatY + 5f * unit)
        lineTo(cx - 17.5f * unit, hatY - 1f * unit)
        quadraticTo(
            cx,
            hatY - 4f * unit,
            cx + 17.5f * unit,
            hatY - 1f * unit
        )
        lineTo(cx + 18f * unit, hatY + 5f * unit)
        quadraticTo(
            cx,
            hatY + 2f * unit,
            cx - 18f * unit,
            hatY + 5f * unit
        )
        close()
    }
    drawPath(ribbonPath, strawRibbonRed)

    // Straw Hat Weave Texture Lines
    drawLine(
        color = strawHatShadow,
        start = Offset(cx - 10f * unit, hatY - 10f * unit),
        end = Offset(cx + 10f * unit, hatY - 10f * unit),
        strokeWidth = 1f * unit
    )
    drawLine(
        color = strawHatShadow,
        start = Offset(cx - 14f * unit, hatY - 5f * unit),
        end = Offset(cx + 14f * unit, hatY - 5f * unit),
        strokeWidth = 1f * unit
    )

    // 9. DJ NEON MUSIC HEADPHONES (Curved headband & glowing earcups)
    // Headband over hat
    drawArc(
        color = Color(0xFF1E293B),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx - 26f * unit, hatY - 14f * unit),
        size = Size(52f * unit, 34f * unit),
        style = Stroke(width = 3.5f * unit, cap = StrokeCap.Round)
    )

    // Left Ear Cup (Illuminated neon circle)
    drawCircle(
        color = Color(0xFF0F172A),
        radius = 7.5f * unit,
        center = Offset(cx - 24f * unit, headCenterY + 2f * unit)
    )
    drawCircle(
        color = headphoneNeon,
        radius = 5.5f * unit,
        center = Offset(cx - 24f * unit, headCenterY + 2f * unit)
    )
    drawCircle(
        color = Color.White,
        radius = 2f * unit,
        center = Offset(cx - 24f * unit, headCenterY + 2f * unit)
    )

    // Right Ear Cup (Illuminated neon circle)
    drawCircle(
        color = Color(0xFF0F172A),
        radius = 7.5f * unit,
        center = Offset(cx + 24f * unit, headCenterY + 2f * unit)
    )
    drawCircle(
        color = headphoneNeon,
        radius = 5.5f * unit,
        center = Offset(cx + 24f * unit, headCenterY + 2f * unit)
    )
    drawCircle(
        color = Color.White,
        radius = 2f * unit,
        center = Offset(cx + 24f * unit, headCenterY + 2f * unit)
    )
}

/**
 * Interactive Stage Card displaying Luffy dancing in real-time to the current music.
 * Placed neatly on the main screen dashboard.
 */
@Composable
fun LuffyDancingStageCard(
    isPlaying: Boolean,
    bpm: Int,
    danceMode: DanceMode,
    currentSongTitle: String?,
    luffyMessage: String?,
    onDanceModeChange: (DanceMode) -> Unit,
    onLuffyClick: () -> Unit,
    onToggleMinimize: () -> Unit,
    isMinimized: Boolean,
    modifier: Modifier = Modifier
) {
    var displaySpeech by remember { mutableStateOf<String?>(null) }

    // Auto-clear speech bubble after 4.5 seconds
    LaunchedEffect(luffyMessage) {
        if (!luffyMessage.isNullOrEmpty()) {
            displaySpeech = luffyMessage
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
                        Color(0xFF131722)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFF7C325).copy(alpha = 0.6f),
                        PrimaryNeon.copy(alpha = 0.5f),
                        SecondaryCyan.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("luffy_stage_card")
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
                    // Straw Hat badge icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👒",
                            fontSize = 15.sp
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Luffy Dancing Beat",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = TextPrimary
                            )

                            // Live Dancing Badge
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
                                    text = if (isPlaying) "DANCING" else "IDLE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = if (isPlaying) AccentEmerald else TextTertiary
                                )
                            }
                        }

                        Text(
                            text = if (isPlaying) "Syncing with ${bpm} BPM groove" else "Tap play to start the dance!",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }

                // Minimize / Expand Stage button
                IconButton(
                    onClick = onToggleMinimize,
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

            // Expandable Dancing Stage Body
            AnimatedVisibility(visible = !isMinimized) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Speech Bubble if Luffy speaks or is tapped
                    AnimatedVisibility(
                        visible = displaySpeech != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFDE047))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = displaySpeech ?: "",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = Color(0xFF1E1B4B)
                            )
                        }
                    }

                    // Main Animated Dancing Luffy
                    LuffyDancingCharacter(
                        isPlaying = isPlaying,
                        bpm = bpm,
                        danceMode = danceMode,
                        size = 140.dp,
                        showNotes = true,
                        showStageGlow = true,
                        onClick = onLuffyClick
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Animated Equalizer Frequency Dance Floor
                    EqualizerFloorAnimation(isPlaying = isPlaying)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dance Mode Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DanceMode.values().forEach { mode ->
                            val isSelected = mode == danceMode
                            FilterChip(
                                selected = isSelected,
                                onClick = { onDanceModeChange(mode) },
                                label = {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (mode) {
                                        DanceMode.GEAR_5 -> Color(0xFF6366F1).copy(alpha = 0.3f)
                                        DanceMode.STRAW_HAT_CHILL -> SecondaryCyan.copy(alpha = 0.25f)
                                        DanceMode.BEAT_BOUNCE -> PrimaryNeon.copy(alpha = 0.25f)
                                    },
                                    selectedLabelColor = Color.White,
                                    containerColor = SurfaceCard,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) PrimaryNeon else Color(0xFF283146)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated Equalizer Bars under the dance floor that pulse rhythmically.
 */
@Composable
private fun EqualizerFloorAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floor_eq")
    val barCount = 18

    Row(
        modifier = modifier
            .fillMaxWidth(0.75f)
            .height(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val animDuration = 400 + (i * 45) % 350
            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = 3f,
                targetValue = if (isPlaying) 14f else 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            val barColor = when {
                i < 6 -> PrimaryNeon
                i < 12 -> Color(0xFFF7C325)
                else -> SecondaryCyan
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isPlaying) barColor else Color(0xFF334155))
            )
        }
    }
}
