package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ChannelSelectorRow
import com.example.ui.components.FullPlayerSheet
import com.example.ui.components.LuffyDancingStageCard
import com.example.ui.components.SleepTimerSheet
import com.example.ui.components.SongListItem
import com.example.ui.components.VaaniDockPlayer
import com.example.ui.components.VocalMoodCard
import com.example.ui.components.VoiceAssistantSheet
import com.example.ui.components.VoiceHistorySheet
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.launch

@Composable
fun VaaniMainScreen(
    viewModel: VaaniViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val displayedSongs by viewModel.displayedSongs.collectAsStateWithLifecycle()
    val selectedChannel by viewModel.selectedChannel.collectAsStateWithLifecycle()
    val isOfflineOnly by viewModel.isOfflineOnlyFilter.collectAsStateWithLifecycle()
    val activeMood by viewModel.activeMoodAnalysis.collectAsStateWithLifecycle()
    val vaaniMessage by viewModel.vaaniMessage.collectAsStateWithLifecycle()

    val currentSong by viewModel.player.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.player.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.player.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.player.durationMs.collectAsStateWithLifecycle()
    val isShuffle by viewModel.player.isShuffle.collectAsStateWithLifecycle()
    val isRepeat by viewModel.player.isRepeat.collectAsStateWithLifecycle()
    val sleepTimerSec by viewModel.player.sleepTimerRemainingSec.collectAsStateWithLifecycle()
    val isSleepTimerActive by viewModel.player.isSleepTimerActive.collectAsStateWithLifecycle()

    val isListening by viewModel.voiceRecognition.isListening.collectAsStateWithLifecycle()
    val rmsAudioLevel by viewModel.voiceRecognition.rmsAudioLevel.collectAsStateWithLifecycle()
    val recognizedText by viewModel.voiceRecognition.recognizedText.collectAsStateWithLifecycle()
    val isProcessingVoice by viewModel.isProcessingVoice.collectAsStateWithLifecycle()
    val recentVoiceLogs by viewModel.recentVoiceLogs.collectAsStateWithLifecycle()

    val showSleepTimerSheet by viewModel.showSleepTimerSheet.collectAsStateWithLifecycle()
    val showVoiceDialog by viewModel.showVoiceDialog.collectAsStateWithLifecycle()
    val showFullPlayerSheet by viewModel.showFullPlayerSheet.collectAsStateWithLifecycle()
    val storageScanNotification by viewModel.storageScanNotification.collectAsStateWithLifecycle()

    val isLuffyDancingEnabled by viewModel.isLuffyDancingEnabled.collectAsStateWithLifecycle()
    val luffyDanceMode by viewModel.luffyDanceMode.collectAsStateWithLifecycle()
    val luffyTapMessage by viewModel.luffyTapMessage.collectAsStateWithLifecycle()
    var isLuffyStageMinimized by remember { mutableStateOf(false) }

    var showHistorySheet by remember { mutableStateOf(false) }

    // Notify user on storage scan updates
    LaunchedEffect(storageScanNotification) {
        storageScanNotification?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.storageScanNotification.value = null
        }
    }

    // Permission launcher for microphone and device storage
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val micGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_AUDIO] == true
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }

        if (micGranted) {
            viewModel.startVoiceInput()
        } else {
            Toast.makeText(context, "Microphone permission needed for voice commands", Toast.LENGTH_SHORT).show()
        }

        if (storageGranted) {
            viewModel.scanDeviceStorage()
        }
    }

    // File picker launcher for importing songs from storage
    val audioPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEachIndexed { index, uri ->
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Custom Track ${index + 1}"
            viewModel.addStorageTrack(fileName, "Phone Storage", uri.toString(), "Storage")
        }
        if (uris.isNotEmpty()) {
            Toast.makeText(context, "Imported ${uris.size} songs from storage!", Toast.LENGTH_SHORT).show()
            viewModel.selectChannel("Storage")
        }
    }

    fun checkAndRequestMicPermission() {
        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (hasMic) {
            viewModel.startVoiceInput()
        } else {
            val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            VaaniDockPlayer(
                currentSong = currentSong,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                sleepTimerRemainingSec = sleepTimerSec,
                isSleepTimerActive = isSleepTimerActive,
                onPlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.skipNext() },
                onPrevious = { viewModel.skipPrevious() },
                onMicClick = { checkAndRequestMicPermission() },
                onSleepTimerClick = { viewModel.showSleepTimerSheet.value = true },
                onCardClick = { viewModel.showFullPlayerSheet.value = true },
                modifier = Modifier.navigationBarsPadding(),
                isLuffyDancingEnabled = isLuffyDancingEnabled,
                danceMode = luffyDanceMode
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Sleek Top Header with Logo, Title, and Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo & App Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryNeon, SecondaryCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Vaani Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Vaani",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.2).sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "AI Voice Music Agent",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = SecondaryCyan
                        )
                    }
                }

                // Top Actions: Offline Toggle Chip, Storage Import, Sleep Timer & History
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Offline Mode Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isOfflineOnly) AccentEmerald.copy(alpha = 0.2f) else SurfaceCard
                            )
                            .border(
                                1.dp,
                                if (isOfflineOnly) AccentEmerald else Color(0xFF283146),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.toggleOfflineOnly() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("offline_filter_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isOfflineOnly) AccentEmerald else TextTertiary)
                            )
                            Text(
                                text = "Offline",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (isOfflineOnly) AccentEmerald else TextSecondary
                            )
                        }
                    }

                    // Storage Import Button
                    IconButton(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("import_storage_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Import Storage Songs",
                            tint = SecondaryCyan,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Scan Phone Storage Button
                    IconButton(
                        onClick = { viewModel.scanDeviceStorage() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("scan_storage_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan Phone Storage",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Voice History Button
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("voice_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Voice History",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Luffy Dancing Mode Quick Toggle
                    IconButton(
                        onClick = { viewModel.toggleLuffyDancing() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("toggle_luffy_btn")
                    ) {
                        Text(
                            text = if (isLuffyDancingEnabled) "👒" else "🏴‍☠️",
                            fontSize = 17.sp
                        )
                    }
                }
            }

            // Conceptual Music Channels Carousel (Thumb Accessible)
            ChannelSelectorRow(
                selectedChannel = selectedChannel,
                onChannelSelected = { channel ->
                    viewModel.selectChannel(channel)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Interactive Monkey D. Luffy Dancing Beat Stage
            if (isLuffyDancingEnabled) {
                LuffyDancingStageCard(
                    isPlaying = isPlaying,
                    bpm = currentSong?.bpm ?: 120,
                    danceMode = luffyDanceMode,
                    currentSongTitle = currentSong?.title,
                    luffyMessage = luffyTapMessage,
                    onDanceModeChange = { mode -> viewModel.setLuffyDanceMode(mode) },
                    onLuffyClick = { viewModel.triggerLuffyTap() },
                    onToggleMinimize = { isLuffyStageMinimized = !isLuffyStageMinimized },
                    isMinimized = isLuffyStageMinimized,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Vocal Mood Card & Real-Time Feedback Hub
            VocalMoodCard(
                activeMood = activeMood,
                vaaniMessage = vaaniMessage,
                onFeedback = { feedback -> viewModel.provideRealtimeFeedback(feedback) },
                onAnalyzeMoodClick = { checkAndRequestMicPermission() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Track List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        isOfflineOnly -> "OFFLINE TRACKS (${displayedSongs.size})"
                        selectedChannel != "All" -> "${selectedChannel.uppercase()} CHANNEL (${displayedSongs.size})"
                        else -> "LIBRARY QUEUE (${displayedSongs.size})"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextSecondary
                )

                if (isSleepTimerActive) {
                    val min = sleepTimerSec / 60
                    Text(
                        text = "Sleep Timer: ${min}m",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Songs List
            if (displayedSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No songs found for this channel.",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap the file icon above to load songs from your phone storage, or ask Vaani to generate tracks!",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("songs_lazy_column"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedSongs, key = { it.id }) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = isPlaying,
                            isCurrent = currentSong?.id == song.id,
                            onSongClick = { viewModel.playSong(song) },
                            onFavoriteToggle = { viewModel.toggleFavorite(song) },
                            onOfflineToggle = { viewModel.toggleOfflineDownload(song) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets
    if (showVoiceDialog) {
        VoiceAssistantSheet(
            isListening = isListening,
            recognizedText = recognizedText,
            rmsAudioLevel = rmsAudioLevel,
            isProcessing = isProcessingVoice,
            onStartListening = { viewModel.voiceRecognition.startListening() },
            onStopListening = { viewModel.voiceRecognition.stopListening() },
            onSubmitCommand = { cmd -> viewModel.handleVoiceCommand(cmd) },
            onDismiss = {
                viewModel.stopVoiceInput()
                viewModel.showVoiceDialog.value = false
            }
        )
    }

    if (showSleepTimerSheet) {
        SleepTimerSheet(
            isSleepTimerActive = isSleepTimerActive,
            remainingSeconds = sleepTimerSec,
            onSetTimer = { minutes -> viewModel.setSleepTimer(minutes) },
            onCancelTimer = { viewModel.cancelSleepTimer() },
            onDismiss = { viewModel.showSleepTimerSheet.value = false }
        )
    }

    if (showFullPlayerSheet) {
        FullPlayerSheet(
            song = currentSong,
            isPlaying = isPlaying,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            isShuffle = isShuffle,
            isRepeat = isRepeat,
            isSleepTimerActive = isSleepTimerActive,
            sleepTimerRemainingSec = sleepTimerSec,
            onPlayPause = { viewModel.togglePlayPause() },
            onNext = { viewModel.skipNext() },
            onPrevious = { viewModel.skipPrevious() },
            onSeek = { pos -> viewModel.player.seekTo(pos) },
            onToggleShuffle = { viewModel.player.toggleShuffle() },
            onToggleRepeat = { viewModel.player.toggleRepeat() },
            onFavoriteToggle = { currentSong?.let { viewModel.toggleFavorite(it) } },
            onOfflineToggle = { currentSong?.let { viewModel.toggleOfflineDownload(it) } },
            onSleepTimerClick = { viewModel.showSleepTimerSheet.value = true },
            onFeedback = { feedback -> viewModel.provideRealtimeFeedback(feedback) },
            onDismiss = { viewModel.showFullPlayerSheet.value = false },
            isLuffyDancingEnabled = isLuffyDancingEnabled,
            danceMode = luffyDanceMode,
            onDanceModeChange = { mode -> viewModel.setLuffyDanceMode(mode) },
            onLuffyClick = { viewModel.triggerLuffyTap() }
        )
    }

    if (showHistorySheet) {
        VoiceHistorySheet(
            logs = recentVoiceLogs,
            onDismiss = { showHistorySheet = false }
        )
    }
}
