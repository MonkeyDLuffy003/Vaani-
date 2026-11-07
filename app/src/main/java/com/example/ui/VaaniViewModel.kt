package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.VaaniAiAgent
import com.example.ai.VocalMoodAnalysis
import com.example.ai.VoiceCommandResult
import com.example.ai.VoiceRecognitionManager
import com.example.ai.VyshuAiBridge
import com.example.audio.VaaniAudioPlayer
import com.example.data.AppDatabase
import com.example.data.SongEntity
import com.example.data.SongRepository
import com.example.data.VoiceLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VaaniViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = SongRepository(database.songDao(), database.voiceLogDao(), application)
    val player = VaaniAudioPlayer(application)
    val aiAgent = VaaniAiAgent(application)
    val voiceRecognition = VoiceRecognitionManager(application)
    val vyshuBridge = VyshuAiBridge(application)

    val allSongs: StateFlow<List<SongEntity>> = repository.allSongs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val offlineSongs: StateFlow<List<SongEntity>> = repository.offlineSongs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentVoiceLogs: StateFlow<List<VoiceLogEntity>> = repository.recentVoiceLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current selected channel filter
    private val _selectedChannel = MutableStateFlow("All")
    val selectedChannel: StateFlow<String> = _selectedChannel.asStateFlow()

    // Continuous voice & Settings state
    val isContinuousVoiceEnabled = MutableStateFlow(true)
    val isMicEnabled = MutableStateFlow(true)
    val hasMicPermission = MutableStateFlow(false)

    // Hamburger Menu state
    val showHamburgerMenu = MutableStateFlow(false)
    val activeMenuSection = MutableStateFlow(MenuSection.LIBRARY)
    val librarySearchQuery = MutableStateFlow("")

    // Vyshu AI Companion Stage state
    val vyshuTapMessage = MutableStateFlow<String?>(null)
    val isVyshuStageEnabled = MutableStateFlow(true)

    // Vyshu AI Synergy Sheet
    val showVyshuSheet = MutableStateFlow(false)

    // Offline only filter toggle
    private val _isOfflineOnlyFilter = MutableStateFlow(false)
    val isOfflineOnlyFilter: StateFlow<Boolean> = _isOfflineOnlyFilter.asStateFlow()

    // Filtered display list for library
    val displayedSongs: StateFlow<List<SongEntity>> = combine(
        allSongs,
        _selectedChannel,
        _isOfflineOnlyFilter,
        librarySearchQuery
    ) { songs, channel, offlineOnly, query ->
        var list = if (offlineOnly) songs.filter { it.isOffline } else songs
        if (channel != "All") {
            list = list.filter { it.genre.equals(channel, ignoreCase = true) }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
            }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // AI Status & Mood State
    private val _activeMoodAnalysis = MutableStateFlow<VocalMoodAnalysis?>(null)
    val activeMoodAnalysis: StateFlow<VocalMoodAnalysis?> = _activeMoodAnalysis.asStateFlow()

    private val _vaaniMessage = MutableStateFlow("I'm here, Teja. 🎵")
    val vaaniMessage: StateFlow<String> = _vaaniMessage.asStateFlow()

    private val _isProcessingVoice = MutableStateFlow(false)
    val isProcessingVoice: StateFlow<Boolean> = _isProcessingVoice.asStateFlow()

    // UI Dialog & Sheet Visibility
    val showSleepTimerSheet = MutableStateFlow(false)
    val showVoiceDialog = MutableStateFlow(false)
    val showFullPlayerSheet = MutableStateFlow(false)
    val storageScanNotification = MutableStateFlow<String?>(null)

    init {
        // Initialize default songs and prefetch free streaming music
        viewModelScope.launch {
            repository.initializeDefaultSongsIfEmpty()
            repository.prefetchOnlineTrendingMusic()
        }

        // Handle auto-advancing tracks
        player.onTrackFinished = {
            skipNext()
        }

        // Connect voice recognizer callbacks
        voiceRecognition.onSpeechComplete = { spokenText ->
            handleVoiceCommand(spokenText)
        }

        voiceRecognition.onTimeoutOrNoMatch = {
            checkAndRestartContinuousVoice()
        }

        voiceRecognition.onError = {
            checkAndRestartContinuousVoice()
        }
    }

    fun selectChannel(channel: String) {
        _selectedChannel.value = channel
    }

    fun toggleOfflineOnly() {
        _isOfflineOnlyFilter.value = !_isOfflineOnlyFilter.value
    }

    fun playSong(song: SongEntity) {
        player.playSong(song)
    }

    fun togglePlayPause() {
        player.togglePlayPause()
    }

    fun skipNext() {
        val list = displayedSongs.value.ifEmpty { allSongs.value }
        if (list.isEmpty()) return

        val current = player.currentSong.value
        val currentIndex = list.indexOfFirst { it.id == current?.id }

        val nextSong = if (player.isShuffle.value) {
            list.filter { it.id != current?.id }.randomOrNull() ?: list.first()
        } else {
            val nextIndex = (currentIndex + 1) % list.size
            list[nextIndex]
        }
        player.playSong(nextSong)
    }

    fun skipPrevious() {
        val list = displayedSongs.value.ifEmpty { allSongs.value }
        if (list.isEmpty()) return

        val current = player.currentSong.value
        val currentIndex = list.indexOfFirst { it.id == current?.id }
        val prevIndex = if (currentIndex <= 0) list.size - 1 else currentIndex - 1
        player.playSong(list[prevIndex])
    }

    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun toggleOfflineDownload(song: SongEntity) {
        viewModelScope.launch {
            repository.toggleOffline(song.id, song.isOffline)
        }
    }

    fun setSleepTimer(minutes: Int) {
        player.setSleepTimer(minutes)
        aiAgent.speak("Sleep timer set for $minutes minutes. Rest well.")
        _vaaniMessage.value = "Sleep timer active ($minutes min)"
        showSleepTimerSheet.value = false
    }

    fun cancelSleepTimer() {
        player.cancelSleepTimer()
        aiAgent.speak("Sleep timer cancelled.")
        _vaaniMessage.value = "Sleep timer cancelled"
    }

    /**
     * User real-time feedback: Thumbs up / down / More energy / Slower
     */
    fun provideRealtimeFeedback(feedbackType: String) {
        when (feedbackType) {
            "THUMBS_UP" -> {
                val current = player.currentSong.value ?: return
                viewModelScope.launch {
                    repository.toggleFavorite(current.id, false)
                }
                aiAgent.speak("Got it! Saved to your favorites.")
                _vaaniMessage.value = "Feedback: Loved '${current.title}'"
            }
            "MORE_ENERGY" -> {
                player.boostEnergy(true)
                aiAgent.speak("Boosting tempo and energy.")
                _vaaniMessage.value = "Feedback: Elevated energy (+10 BPM)"
            }
            "RELAX_TEMPO" -> {
                player.boostEnergy(false)
                aiAgent.speak("Relaxing tempo for you.")
                _vaaniMessage.value = "Feedback: Slowed tempo for calm vibe"
            }
            "NEXT_DIFFERENT" -> {
                aiAgent.speak("Switching vibe.")
                skipNext()
            }
        }
    }

    /**
     * Scans phone storage for songs and auto-loads them
     */
    fun scanDeviceStorage() {
        viewModelScope.launch {
            val count = repository.scanDeviceStorageSongs()
            val msg = if (count > 0) "Found and loaded $count songs from phone storage!" else "Phone storage scanned (all songs up to date)"
            storageScanNotification.value = msg
            aiAgent.speak("Found $count new songs from phone storage.")
        }
    }

    /**
     * Processes spoken or typed voice command through Vaani AI Agent
     */
    fun handleVoiceCommand(input: String) {
        if (input.isBlank()) return
        _isProcessingVoice.value = true

        viewModelScope.launch {
            // Check if conversational query should be delegated to Vyshu AI Companion
            if (vyshuBridge.shouldDelegateToVyshu(input)) {
                val vyshuReply = vyshuBridge.askVyshuAi(input)
                _isProcessingVoice.value = false
                aiAgent.speak(vyshuReply)
                _vaaniMessage.value = "Vyshu AI: $vyshuReply"
                repository.logVoiceAction(input, "Vyshu Companion", vyshuReply, "VYSHU_DELEGATE")
                return@launch
            }

            val result = aiAgent.processVoiceUtterance(input)
            _isProcessingVoice.value = false

            when (result) {
                is VoiceCommandResult.PlayRandom -> {
                    val available = allSongs.value
                    if (available.isNotEmpty()) {
                        val track = available.random()
                        player.playSong(track)
                        val resp = "Playing '${track.title}' by ${track.artist}."
                        aiAgent.speak(resp) {
                            checkAndRestartContinuousVoice()
                        }
                        _vaaniMessage.value = resp
                        repository.logVoiceAction(input, "PlayRandom", resp, "PLAY_TRACK_${track.id}")
                    } else {
                        val noSongsResp = "No songs found in your library. Please open the menu with the three lines to scan or add songs from your phone storage."
                        aiAgent.speak(noSongsResp) {
                            checkAndRestartContinuousVoice()
                        }
                        _vaaniMessage.value = noSongsResp
                        repository.logVoiceAction(input, "PlayRandom", noSongsResp, "NO_SONGS")
                    }
                }

                is VoiceCommandResult.PlaySpecific -> {
                    val q = result.query.lowercase()
                    val match = allSongs.value.firstOrNull {
                        it.title.lowercase().contains(q) ||
                        it.artist.lowercase().contains(q) ||
                        it.genre.lowercase().contains(q)
                    }
                    if (match != null) {
                        player.playSong(match)
                        val resp = "Playing '${match.title}' by ${match.artist}."
                        aiAgent.speak(resp) {
                            checkAndRestartContinuousVoice()
                        }
                        _vaaniMessage.value = resp
                        repository.logVoiceAction(input, "PlaySpecific: $q", resp, "PLAY_LOCAL_MATCH")
                    } else {
                        // Search free music APIs (iTunes + Jamendo)
                        val onlineMatches = repository.searchOnlineFreeMusic(result.query)
                        if (onlineMatches.isNotEmpty()) {
                            val onlineTrack = onlineMatches.first()
                            player.playSong(onlineTrack)
                            val resp = "Streaming '${onlineTrack.title}' by ${onlineTrack.artist} from free music API."
                            aiAgent.speak(resp) {
                                checkAndRestartContinuousVoice()
                            }
                            _vaaniMessage.value = resp
                            repository.logVoiceAction(input, "PlayOnline: $q", resp, "PLAY_ONLINE_MATCH")
                        } else {
                            val notFoundResp = "I couldn't find '$q' in your library or free music streams. Would you like to scan phone storage?"
                            aiAgent.speak(notFoundResp) {
                                checkAndRestartContinuousVoice()
                            }
                            _vaaniMessage.value = notFoundResp
                            repository.logVoiceAction(input, "PlaySpecific: $q", notFoundResp, "NOT_FOUND")
                        }
                    }
                }

                is VoiceCommandResult.ConversationalReply -> {
                    aiAgent.speak(result.speechResponse) {
                        checkAndRestartContinuousVoice()
                    }
                    _vaaniMessage.value = result.speechResponse
                    repository.logVoiceAction(input, "Gemini Conversation", result.speechResponse, "GEMINI_TALK")
                }

                is VoiceCommandResult.ControlAction -> {
                    aiAgent.speak(result.speechResponse) {
                        checkAndRestartContinuousVoice()
                    }
                    _vaaniMessage.value = result.speechResponse
                    when (result.action) {
                        VoiceCommandResult.ActionType.PLAY -> player.resume()
                        VoiceCommandResult.ActionType.PAUSE -> player.pause()
                        VoiceCommandResult.ActionType.NEXT -> skipNext()
                        VoiceCommandResult.ActionType.PREVIOUS -> skipPrevious()
                        VoiceCommandResult.ActionType.SHUFFLE -> player.toggleShuffle()
                        VoiceCommandResult.ActionType.OFFLINE_MODE -> {
                            _isOfflineOnlyFilter.value = true
                            _selectedChannel.value = "Storage"
                        }
                    }
                    repository.logVoiceAction(input, "Command", result.speechResponse, result.action.name)
                }

                is VoiceCommandResult.PlayChannel -> {
                    _selectedChannel.value = result.channel
                    val channelSongs = allSongs.value.filter { it.genre.equals(result.channel, ignoreCase = true) }
                    if (channelSongs.isNotEmpty()) {
                        player.playSong(channelSongs.first())
                    }
                    aiAgent.speak(result.speechResponse) {
                        checkAndRestartContinuousVoice()
                    }
                    _vaaniMessage.value = result.speechResponse
                    repository.logVoiceAction(input, "Channel: ${result.channel}", result.speechResponse, "PLAY_CHANNEL")
                }

                is VoiceCommandResult.SetSleepTimer -> {
                    setSleepTimer(result.minutes)
                    repository.logVoiceAction(input, "Timer", result.speechResponse, "SLEEP_TIMER_${result.minutes}")
                }

                is VoiceCommandResult.RealtimeFeedback -> {
                    when (result.feedbackType) {
                        "FASTER" -> player.boostEnergy(true)
                        "SLOWER" -> player.boostEnergy(false)
                    }
                    aiAgent.speak(result.speechResponse) {
                        checkAndRestartContinuousVoice()
                    }
                    _vaaniMessage.value = result.speechResponse
                    repository.logVoiceAction(input, "Feedback: ${result.feedbackType}", result.speechResponse, "TEMPO_ADJUST")
                }

                is VoiceCommandResult.MoodAnalyzed -> {
                    _activeMoodAnalysis.value = result.analysis
                    _selectedChannel.value = result.analysis.suggestedChannel

                    // Play best matching track for the mood
                    val matchingSongs = allSongs.value.filter {
                        it.genre.equals(result.analysis.suggestedChannel, ignoreCase = true)
                    }
                    if (matchingSongs.isNotEmpty()) {
                        player.playSong(matchingSongs.first())
                    }

                    aiAgent.speak(result.analysis.vaaniResponse) {
                        checkAndRestartContinuousVoice()
                    }
                    _vaaniMessage.value = "Mood: ${result.analysis.mood} • ${result.analysis.vaaniResponse}"
                    repository.logVoiceAction(input, result.analysis.mood, result.analysis.vaaniResponse, "MOOD_CURATION")
                }

                is VoiceCommandResult.Unknown -> {
                    aiAgent.speak(result.speechResponse) {
                        checkAndRestartContinuousVoice()
                    }
                    _vaaniMessage.value = result.speechResponse
                }
            }
        }
    }

    /**
     * Seamless Continuous Voice re-arming when enabled in settings
     */
    fun checkAndRestartContinuousVoice() {
        if (!isContinuousVoiceEnabled.value || !isMicEnabled.value || aiAgent.isSpeaking.value || !hasMicPermission.value) return
        viewModelScope.launch {
            kotlinx.coroutines.delay(500L)
            if (isContinuousVoiceEnabled.value && isMicEnabled.value && !aiAgent.isSpeaking.value && hasMicPermission.value) {
                voiceRecognition.startListening()
            }
        }
    }

    fun setContinuousVoice(enabled: Boolean) {
        isContinuousVoiceEnabled.value = enabled
        if (enabled && isMicEnabled.value && hasMicPermission.value) {
            checkAndRestartContinuousVoice()
        }
    }

    fun setMicEnabled(enabled: Boolean) {
        isMicEnabled.value = enabled
        if (enabled) {
            if (hasMicPermission.value) {
                voiceRecognition.startListening()
            }
        } else {
            voiceRecognition.stopListening()
        }
    }

    fun clearPlaybackSession() {
        player.clearSession()
        _vaaniMessage.value = "Playback session cleared"
    }

    fun startVoiceInput() {
        if (!isMicEnabled.value) {
            setMicEnabled(true)
        }
        voiceRecognition.startListening()
    }

    fun stopVoiceInput() {
        voiceRecognition.stopListening()
    }

    fun askVyshuDirectly(query: String) {
        viewModelScope.launch {
            val reply = vyshuBridge.askVyshuAi(query)
            aiAgent.speak(reply) {
                checkAndRestartContinuousVoice()
            }
            _vaaniMessage.value = "Vyshu: $reply"
            repository.logVoiceAction(query, "Vyshu AI", reply, "VYSHU_DIRECT")
        }
    }

    fun addStorageTrack(title: String, artist: String, uriString: String, genre: String = "Storage") {
        viewModelScope.launch {
            repository.addCustomTrack(title, artist, uriString, genre)
        }
    }

    fun searchAndAddOnlineTracks(query: String) {
        viewModelScope.launch {
            val tracks = repository.searchOnlineFreeMusic(query)
            val msg = if (tracks.isNotEmpty()) "Loaded ${tracks.size} online tracks for '$query'!" else "No online tracks found for '$query'"
            storageScanNotification.value = msg
        }
    }

    fun toggleVyshuStage() {
        isVyshuStageEnabled.value = !isVyshuStageEnabled.value
    }

    fun triggerVyshuTap() {
        val quotes = listOf(
            "Hey! Loving the rhythm with you!",
            "Vaani and I have your soundtrack ready. How are you feeling today?",
            "Your music taste is amazing! Let's keep this vibe going.",
            "I'm right here with you! Tap the chat button if you ever want to talk.",
            "Music sounds so much sweeter when we listen together!"
        )
        val selected = quotes.random()
        vyshuTapMessage.value = selected
        aiAgent.speak(selected) {
            checkAndRestartContinuousVoice()
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
        aiAgent.release()
        voiceRecognition.stopListening()
    }
}

enum class MenuSection {
    LIBRARY,
    ADD_MUSIC,
    PLAYLISTS,
    VOICE_SETTINGS,
    PLAYBACK_SETTINGS,
    FEATURES,
    UPDATES,
    ABOUT,
    DEBUG
}

