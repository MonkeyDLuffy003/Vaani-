package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantSheet(
    isListening: Boolean,
    recognizedText: String,
    rmsAudioLevel: Float,
    isProcessing: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSubmitCommand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var typedCommand by remember { mutableStateOf("") }

    val quickCommands = listOf(
        "Skip track",
        "Play Gym channel",
        "Play Cooking channel",
        "Play Relax 432Hz",
        "Play Motivation channel",
        "Sleep timer 30m",
        "My phone songs",
        "More energy",
        "Analyze my mood"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceElevated,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF334155)) },
        modifier = Modifier.testTag("voice_assistant_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vaani Voice Assistant",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = if (isListening) "Listening to your voice..." else if (isProcessing) "Vaani is analyzing..." else "Ready for your command",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = if (isListening) SecondaryCyan else TextSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_voice_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextTertiary
                    )
                }
            }

            // Live Animated Soundwave Waveform
            VocalVisualizerWave(
                isListening = isListening,
                rmsLevel = rmsAudioLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            )

            // Spoken Transcription or Status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundDark)
                    .border(1.dp, Color(0xFF232B3E), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (recognizedText.isNotBlank()) "\"$recognizedText\"" else if (isListening) "Speak now (e.g. \"Next song\", \"Play Cooking\", \"Sleep timer 30 min\")" else "Tap microphone below or type your command",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = if (recognizedText.isNotBlank()) SecondaryCyan else TextSecondary
                )
            }

            // Central Voice Toggle Button
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (isListening) Color(0xFFEF4444) else PrimaryNeon)
                        .clickable {
                            if (isListening) onStopListening() else onStartListening()
                        }
                        .testTag("voice_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Quick Command Chips
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "QUICK COMMANDS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextTertiary
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickCommands) { cmd ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceCard)
                                .border(1.dp, Color(0xFF283248), RoundedCornerShape(16.dp))
                                .clickable {
                                    onSubmitCommand(cmd)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("quick_cmd_${cmd.replace(" ", "_").lowercase()}")
                        ) {
                            Text(
                                text = cmd,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Manual Text Command Input Field (Ideal for testing or quiet environments)
            OutlinedTextField(
                value = typedCommand,
                onValueChange = { typedCommand = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_command_input"),
                placeholder = {
                    Text(
                        text = "Or type command here...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                },
                trailingIcon = {
                    if (typedCommand.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onSubmitCommand(typedCommand)
                                typedCommand = ""
                                onDismiss()
                            },
                            modifier = Modifier.testTag("submit_manual_command")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = SecondaryCyan
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryNeon,
                    unfocusedBorderColor = Color(0xFF2A344C),
                    focusedContainerColor = BackgroundDark,
                    unfocusedContainerColor = BackgroundDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (typedCommand.isNotBlank()) {
                        onSubmitCommand(typedCommand)
                        typedCommand = ""
                        onDismiss()
                    }
                })
            )
        }
    }
}

@Composable
fun VocalVisualizerWave(
    isListening: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "wave_anim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val points = 32

        val amplitude = if (isListening) {
            (height * 0.4f * (0.3f + rmsLevel * 0.7f)).coerceAtMost(height * 0.45f)
        } else {
            height * 0.08f
        }

        var prevX = 0f
        var prevY = centerY

        for (i in 0..points) {
            val x = (width / points) * i
            val normX = (i.toFloat() / points) * 4f
            val y = centerY + sin(normX * 3.14f + phase) * amplitude * sin(i.toFloat() / points * 3.14f)

            if (i > 0) {
                drawLine(
                    color = if (isListening) SecondaryCyan else PrimaryNeon.copy(alpha = 0.5f),
                    start = Offset(prevX, prevY),
                    end = Offset(x, y.toFloat()),
                    strokeWidth = if (isListening) 4f else 2f
                )
            }
            prevX = x
            prevY = y.toFloat()
        }
    }
}
