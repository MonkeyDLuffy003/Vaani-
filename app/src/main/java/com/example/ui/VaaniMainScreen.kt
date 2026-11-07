package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FullPlayerSheet
import com.example.ui.components.VaaniDockPlayer
import com.example.ui.components.VaaniHamburgerDrawer
import com.example.ui.components.VaaniHeroView
import com.example.ui.components.VyshuAiSheet
import com.example.ui.theme.BackgroundDark

/**
 * Vaani Main Screen — Voice-First AI Music Assistant Interface.
 * Clean, minimal interface displaying:
 * - ☰ Hamburger menu
 * - Vaani AI Music Assistant title
 * - Realistic animated Vaani character
 * - AI voice interaction speech bubble & waveforms
 * - Continuous active microphone orb
 * - Conditional bottom music player (visible ONLY when a track is selected/playing)
 *
 * All secondary features (Library, Add Music, Playlists, Settings, Debug) are inside ☰.
 */
@Composable
fun VaaniMainScreen(
    viewModel: VaaniViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
    val displayedSongs by viewModel.displayedSongs.collectAsStateWithLifecycle()
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
    val isProcessingVoice by viewModel.isProcessingVoice.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.aiAgent.isSpeaking.collectAsStateWithLifecycle()
    val recentVoiceLogs by viewModel.recentVoiceLogs.collectAsStateWithLifecycle()

    val isContinuousVoice by viewModel.isContinuousVoiceEnabled.collectAsStateWithLifecycle()
    val isMicEnabled by viewModel.isMicEnabled.collectAsStateWithLifecycle()
    val showHamburgerMenu by viewModel.showHamburgerMenu.collectAsStateWithLifecycle()
    val activeMenuSection by viewModel.activeMenuSection.collectAsStateWithLifecycle()
    val librarySearchQuery by viewModel.librarySearchQuery.collectAsStateWithLifecycle()

    val showFullPlayerSheet by viewModel.showFullPlayerSheet.collectAsStateWithLifecycle()
    val showVyshuSheet by viewModel.showVyshuSheet.collectAsStateWithLifecycle()
    val vyshuMessages by viewModel.vyshuBridge.conversationHistory.collectAsStateWithLifecycle()
    val vyshuMood by viewModel.vyshuBridge.vyshuMood.collectAsStateWithLifecycle()
    val storageScanNotification by viewModel.storageScanNotification.collectAsStateWithLifecycle()

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

        viewModel.hasMicPermission.value = micGranted

        if (micGranted) {
            viewModel.checkAndRestartContinuousVoice()
        } else {
            Toast.makeText(context, "Microphone permission needed for hands-free voice commands", Toast.LENGTH_SHORT).show()
        }

        if (storageGranted) {
            viewModel.scanDeviceStorage()
        }
    }

    // Check permissions and auto-initiate storage scan & continuous voice on startup
    LaunchedEffect(Unit) {
        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val hasStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        viewModel.hasMicPermission.value = hasMic

        if (hasMic && hasStorage) {
            viewModel.scanDeviceStorage()
            viewModel.checkAndRestartContinuousVoice()
        } else {
            val requests = mutableListOf<String>()
            if (!hasMic) requests.add(Manifest.permission.RECORD_AUDIO)
            if (!hasStorage) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requests.add(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    requests.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            if (requests.isNotEmpty()) {
                permissionLauncher.launch(requests.toTypedArray())
            }
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
            viewModel.hasMicPermission.value = true
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
            // CONDITIONAL DISPLAY: Music player appears ONLY when a song is selected or playing!
            if (currentSong != null) {
                VaaniDockPlayer(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.skipNext() },
                    onPrevious = { viewModel.skipPrevious() },
                    onCloseSession = { viewModel.clearPlaybackSession() },
                    onCardClick = { viewModel.showFullPlayerSheet.value = true },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            val subMessageText = currentSong?.let {
                "Playing: ${it.title} by ${it.artist}"
            } ?: "Playing your music."

            VaaniHeroView(
                isListening = isListening,
                isPlaying = isPlaying,
                isProcessing = isProcessingVoice,
                isSpeaking = isSpeaking,
                rmsAudioLevel = rmsAudioLevel,
                mainMessage = vaaniMessage,
                subMessage = subMessageText,
                hasActiveTrack = currentSong != null,
                onMicClick = { checkAndRequestMicPermission() },
                onMenuClick = { viewModel.showHamburgerMenu.value = true },
                onVyshuClick = { viewModel.showVyshuSheet.value = true },
                onBubbleClick = { checkAndRequestMicPermission() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Hamburger Menu Drawer (☰)
    if (showHamburgerMenu) {
        VaaniHamburgerDrawer(
            activeSection = activeMenuSection,
            onSectionSelected = { section -> viewModel.activeMenuSection.value = section },
            displayedSongs = displayedSongs,
            allSongs = allSongs,
            currentSong = currentSong,
            isPlaying = isPlaying,
            isContinuousVoiceEnabled = isContinuousVoice,
            isMicEnabled = isMicEnabled,
            isShuffle = isShuffle,
            isRepeat = isRepeat,
            sleepTimerRemainingSec = sleepTimerSec,
            isSleepTimerActive = isSleepTimerActive,
            voiceLogs = recentVoiceLogs,
            searchQuery = librarySearchQuery,
            onSearchQueryChange = { q -> viewModel.librarySearchQuery.value = q },
            onSongSelected = { song -> viewModel.playSong(song) },
            onScanStorageClick = { viewModel.scanDeviceStorage() },
            onImportFilesClick = { audioPickerLauncher.launch("audio/*") },
            onSearchOnlineMusic = { query -> viewModel.searchAndAddOnlineTracks(query) },
            onToggleContinuousVoice = { enabled -> viewModel.setContinuousVoice(enabled) },
            onToggleMicEnabled = { enabled -> viewModel.setMicEnabled(enabled) },
            onToggleShuffle = { viewModel.player.toggleShuffle() },
            onToggleRepeat = { viewModel.player.toggleRepeat() },
            onSetSleepTimer = { min -> viewModel.setSleepTimer(min) },
            onCancelSleepTimer = { viewModel.cancelSleepTimer() },
            onTestVoiceClick = {
                viewModel.aiAgent.speak("Hello Teja, all voice recognition and speech audio systems are operating nominally.") {
                    viewModel.checkAndRestartContinuousVoice()
                }
            },
            onDismiss = { viewModel.showHamburgerMenu.value = false }
        )
    }

    // Full Player Details Sheet
    if (showFullPlayerSheet && currentSong != null) {
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
            onDismiss = { viewModel.showFullPlayerSheet.value = false }
        )
    }

    // Vyshu AI Synergy Sheet
    if (showVyshuSheet) {
        VyshuAiSheet(
            messages = vyshuMessages,
            vyshuMood = vyshuMood,
            onSendMessage = { query -> viewModel.askVyshuDirectly(query) },
            onSetWebhookUrl = { url -> viewModel.vyshuBridge.customWebhookUrl = url },
            currentWebhookUrl = viewModel.vyshuBridge.customWebhookUrl,
            onDismiss = { viewModel.showVyshuSheet.value = false }
        )
    }
}
