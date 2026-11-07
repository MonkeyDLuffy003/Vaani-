package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.SongEntity
import com.example.data.VoiceLogEntity
import com.example.ui.MenuSection
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * Full Hamburger Menu (☰) Drawer / Sheet
 * Hosts all 9 required secondary features:
 * 1. Music Library
 * 2. Add Music
 * 3. Playlists
 * 4. Voice Settings
 * 5. Playback Settings
 * 6. Features
 * 7. Updates
 * 8. About Vaani
 * 9. Debug Tools
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaaniHamburgerDrawer(
    activeSection: MenuSection,
    onSectionSelected: (MenuSection) -> Unit,
    displayedSongs: List<SongEntity>,
    allSongs: List<SongEntity>,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    isContinuousVoiceEnabled: Boolean,
    isMicEnabled: Boolean,
    isShuffle: Boolean,
    isRepeat: Boolean,
    sleepTimerRemainingSec: Int,
    isSleepTimerActive: Boolean,
    voiceLogs: List<VoiceLogEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSongSelected: (SongEntity) -> Unit,
    onScanStorageClick: () -> Unit,
    onImportFilesClick: () -> Unit,
    onSearchOnlineMusic: (String) -> Unit = {},
    onToggleContinuousVoice: (Boolean) -> Unit,
    onToggleMicEnabled: (Boolean) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onTestVoiceClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDark,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.94f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .navigationBarsPadding()
        ) {
            // Header Bar with Title and Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Vaani Menu",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Secondary Controls & Library",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = SecondaryCyan
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .testTag("menu_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Menu",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Horizontal Menu Section Selector Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(MenuSection.values()) { section ->
                    val isSelected = section == activeSection
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) PrimaryNeon.copy(alpha = 0.25f) else SurfaceCard
                            )
                            .border(
                                1.dp,
                                if (isSelected) PrimaryNeon else Color(0xFF282042),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSectionSelected(section) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("menu_tab_${section.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = getSectionIcon(section),
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFFE879F9) else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = getSectionTitle(section),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            // Divider Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF281E40))
                    .padding(bottom = 6.dp)
            )

            // Section Body Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                when (activeSection) {
                    MenuSection.LIBRARY -> {
                        MusicLibrarySection(
                            songs = displayedSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            searchQuery = searchQuery,
                            onSearchQueryChange = onSearchQueryChange,
                            onSongSelected = { song ->
                                onSongSelected(song)
                                onDismiss()
                            },
                            onScanClick = onScanStorageClick,
                            onImportClick = onImportFilesClick
                        )
                    }
                    MenuSection.ADD_MUSIC -> {
                        AddMusicSection(
                            totalSongs = allSongs.size,
                            storageSongsCount = allSongs.count { it.genre == "Storage" },
                            onScanStorage = onScanStorageClick,
                            onImportFiles = onImportFilesClick,
                            onSearchOnlineMusic = onSearchOnlineMusic
                        )
                    }
                    MenuSection.PLAYLISTS -> {
                        PlaylistsSection(
                            allSongs = allSongs,
                            onSelectSong = { song ->
                                onSongSelected(song)
                                onDismiss()
                            }
                        )
                    }
                    MenuSection.VOICE_SETTINGS -> {
                        VoiceSettingsSection(
                            isContinuous = isContinuousVoiceEnabled,
                            isMicEnabled = isMicEnabled,
                            onToggleContinuous = onToggleContinuousVoice,
                            onToggleMic = onToggleMicEnabled,
                            onTestVoice = onTestVoiceClick
                        )
                    }
                    MenuSection.PLAYBACK_SETTINGS -> {
                        PlaybackSettingsSection(
                            isShuffle = isShuffle,
                            isRepeat = isRepeat,
                            sleepTimerRemainingSec = sleepTimerRemainingSec,
                            isSleepTimerActive = isSleepTimerActive,
                            onToggleShuffle = onToggleShuffle,
                            onToggleRepeat = onToggleRepeat,
                            onSetSleepTimer = onSetSleepTimer,
                            onCancelSleepTimer = onCancelSleepTimer
                        )
                    }
                    MenuSection.FEATURES -> {
                        FeaturesSection()
                    }
                    MenuSection.UPDATES -> {
                        UpdatesSection()
                    }
                    MenuSection.ABOUT -> {
                        AboutSection()
                    }
                    MenuSection.DEBUG -> {
                        DebugSection(
                            context = context,
                            voiceLogs = voiceLogs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onTestVoice = onTestVoiceClick
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Section 1: Music Library
// -------------------------------------------------------------
@Composable
private fun MusicLibrarySection(
    songs: List<SongEntity>,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSongSelected: (SongEntity) -> Unit,
    onScanClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search songs or artists...", color = TextTertiary, fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryNeon,
                unfocusedBorderColor = Color(0xFF382958),
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Song Count header & quick scan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DEVICE AUDIO TRACKS (${songs.size})",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Scan Phone",
                    style = MaterialTheme.typography.labelSmall.copy(color = SecondaryCyan, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.clickable { onScanClick() }
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                )
                Text(
                    text = "Import",
                    style = MaterialTheme.typography.labelSmall.copy(color = PrimaryNeon, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.clickable { onImportClick() }
                )
            }
        }

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(42.dp)
                    )
                    Text("No Songs in Library", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(
                        "Tap below to auto-scan audio from your phone storage or import files.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onScanClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                    ) {
                        Text("Scan Phone Storage", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(songs, key = { it.id }) { song ->
                    val isCurrent = currentSong?.id == song.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isCurrent) PrimaryNeon.copy(alpha = 0.2f) else SurfaceCard)
                            .border(1.dp, if (isCurrent) PrimaryNeon else Color(0xFF282042), RoundedCornerShape(16.dp))
                            .clickable { onSongSelected(song) }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isCurrent) PrimaryNeon else SurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCurrent && isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (isCurrent) Color.White else SecondaryCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isCurrent) Color(0xFFF5D0FE) else TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${song.artist} • ${formatDrawerDuration(song.durationMs)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (isCurrent && isPlaying) {
                                Text(
                                    text = "PLAYING",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Section 2: Add Music
// -------------------------------------------------------------
@Composable
private fun AddMusicSection(
    totalSongs: Int,
    storageSongsCount: Int,
    onScanStorage: () -> Unit,
    onImportFiles: () -> Unit,
    onSearchOnlineMusic: (String) -> Unit
) {
    var onlineSearchTerm by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "IMPORT & FREE ONLINE MUSIC APIS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
        }

        // Card 1: Free Music APIs (iTunes & Jamendo)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PrimaryNeon.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text("Free Music APIs (iTunes & Jamendo)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text("Search and stream millions of tracks instantly with real audio preview streams.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }

                    OutlinedTextField(
                        value = onlineSearchTerm,
                        onValueChange = { onlineSearchTerm = it },
                        placeholder = { Text("Artist or song (e.g. Ed Sheeran, Lo-Fi, Coldplay)", color = TextTertiary, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNeon,
                            unfocusedBorderColor = Color(0xFF382958),
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Button(
                        onClick = {
                            if (onlineSearchTerm.isNotBlank()) {
                                onSearchOnlineMusic(onlineSearchTerm.trim())
                            } else {
                                onSearchOnlineMusic("trending hits")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (onlineSearchTerm.isNotBlank()) "Search & Load '$onlineSearchTerm'" else "Load Trending Free Songs", color = Color.White)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382958)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SecondaryCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text("Scan Phone Storage (MediaStore)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text("Auto-detect all music stored on device memory or SD card.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }

                    Button(
                        onClick = onScanStorage,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Phone Storage Now", color = Color.White)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382958)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PrimaryNeon.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text("Browse Files / Folders", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text("Pick specific .mp3, .m4a, .flac audio files using Android system picker.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }

                    Button(
                        onClick = onImportFiles,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Browse & Import Audio Files", color = Color.White)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalSongs", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("Total Songs", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$storageSongsCount", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = SecondaryCyan)
                        Text("From Phone", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Section 3: Playlists
// -------------------------------------------------------------
@Composable
private fun PlaylistsSection(
    allSongs: List<SongEntity>,
    onSelectSong: (SongEntity) -> Unit
) {
    val playlists = listOf(
        Triple("All Songs", "${allSongs.size} tracks", Icons.Default.MusicNote),
        Triple("Phone Storage", "${allSongs.count { it.genre == "Storage" }} tracks", Icons.Default.PhoneAndroid),
        Triple("Favorites", "${allSongs.count { it.isFavorite }} tracks", Icons.Default.Favorite),
        Triple("Relax & Calming", "${allSongs.count { it.genre == "Relax" }} tracks", Icons.Default.Bedtime),
        Triple("Gym & Energy", "${allSongs.count { it.genre == "Gym" }} tracks", Icons.Default.Star)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "MANAGED PLAYLISTS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
        }

        items(playlists) { (title, count, icon) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382958)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val track = when (title) {
                            "Favorites" -> allSongs.firstOrNull { it.isFavorite }
                            "Phone Storage" -> allSongs.firstOrNull { it.genre == "Storage" }
                            "Relax & Calming" -> allSongs.firstOrNull { it.genre == "Relax" }
                            "Gym & Energy" -> allSongs.firstOrNull { it.genre == "Gym" }
                            else -> allSongs.firstOrNull()
                        }
                        if (track != null) {
                            onSelectSong(track)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryNeon.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                            Text(count, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }

                    Icon(Icons.Default.PlaylistPlay, contentDescription = "Play", tint = SecondaryCyan)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Section 4: Voice Settings
// -------------------------------------------------------------
@Composable
private fun VoiceSettingsSection(
    isContinuous: Boolean,
    isMicEnabled: Boolean,
    onToggleContinuous: (Boolean) -> Unit,
    onToggleMic: (Boolean) -> Unit,
    onTestVoice: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "VOICE RECOGNITION & MICROPHONE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382958))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Continuous Active Microphone", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text("Keep microphone continuously listening during app use so you can talk to Vaani hands-free.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = isContinuous,
                            onCheckedChange = onToggleContinuous,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryNeon
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF281E40)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Master Microphone Switch", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text("Turn microphone hardware recording ON/OFF completely.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = isMicEnabled,
                            onCheckedChange = onToggleMic,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isMicEnabled) AccentEmerald else TextTertiary
                            )
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382958))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Speech Synthesis & Voice Test", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    Text("Test Vaani's realistic speech audio and audio looper.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                    Button(
                        onClick = onTestVoice,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Vaani Speech Voice", color = Color.White)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Section 5: Playback Settings
// -------------------------------------------------------------
@Composable
private fun PlaybackSettingsSection(
    isShuffle: Boolean,
    isRepeat: Boolean,
    sleepTimerRemainingSec: Int,
    isSleepTimerActive: Boolean,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "PLAYBACK & QUEUE CONTROLS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382958))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Shuffle Playback", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text("Play songs in randomized order.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = isShuffle,
                            onCheckedChange = { onToggleShuffle() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SecondaryCyan)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF281E40)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Repeat Mode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text("Loop current song indefinitely.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = isRepeat,
                            onCheckedChange = { onToggleRepeat() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SecondaryCyan)
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382958))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Sleep Timer", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    Text(
                        if (isSleepTimerActive) "Active: ${sleepTimerRemainingSec / 60}m remaining (smooth fade)"
                        else "Automatically stops music after selected duration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSleepTimerActive) AccentEmerald else TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { min ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceElevated)
                                    .clickable { onSetSleepTimer(min) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${min}m", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    if (isSleepTimerActive) {
                        Button(
                            onClick = onCancelSleepTimer,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Text("Cancel Sleep Timer", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Section 6: Features
// -------------------------------------------------------------
@Composable
private fun FeaturesSection() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("IMPLEMENTED CAPABILITIES", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = AccentEmerald)
        }

        val implemented = listOf(
            "Voice-First Minimal Interface: Clean screen showing only Vaani and active mic.",
            "Continuous Microphone: Listens continuously for 'Vaani, play music' or commands.",
            "Realistic Character Animation: Blinking, breathing, listening, and music groove.",
            "Dynamic Bottom Player: Visible only when a track is actively playing or selected.",
            "Phone Storage MediaStore: Auto-detects local MP3 and audio tracks on device.",
            "Speech Synthesis (TTS): Responsive vocal feedback for every action.",
            "Sleep Timer: Gradual volume fade-out at sleep time.",
            "Dual-Agent Synergy: Seamless routing to Vyshu AI Companion for chat."
        )

        items(implemented) { feat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(18.dp))
                Text(feat, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("PLANNED CAPABILITIES", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = SecondaryCyan)
        }

        val planned = listOf(
            "Bluetooth Auto-Detection: Automatically resume playback when headphones connect.",
            "Custom Wake-Word: On-device offline wake engine for 'Hey Vaani'.",
            "Multi-Room Casting: Synchronize audio across multiple Android devices."
        )

        items(planned) { plan ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SecondaryCyan)
                        .align(Alignment.CenterVertically)
                )
                Text(plan, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

// -------------------------------------------------------------
// Section 7: Updates
// -------------------------------------------------------------
@Composable
private fun UpdatesSection() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("RELEASE NOTES", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = TextSecondary)

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382958))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Version 0.0.2 - Voice-First Edition", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryNeon)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Current", color = AccentEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    """
                    • Redesigned Minimal Main Screen: No clutter, no permanent song list.
                    • Vaani Character Animation: Blinking, breathing, attentive listening, and music groove.
                    • Continuous Microphone Active: Speak naturally hands-free without tapping.
                    • Conditional Music Player: Compact dock appears only when a song is selected or playing.
                    • 3-Line Menu (☰): All secondary controls, music library, and settings accessible in one place.
                    • Phone Storage Scanner: Detects local songs automatically via MediaStore.
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Section 8: About Vaani
// -------------------------------------------------------------
@Composable
private fun AboutSection() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("APPLICATION INFORMATION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = TextSecondary)

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382958))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Vaani AI Music Assistant", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text("A voice-first, animated AI companion designed to provide seamless music playback from phone storage with zero physical touch interaction.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF281E40)))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Version", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    Text("0.0.2 (Build 2026)", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("User Profile", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    Text("Teja", style = MaterialTheme.typography.bodySmall, color = PrimaryNeon, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("AI Companion", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    Text("Vyshu AI Synergy", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA78BFA))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Section 9: Debug Tools
// -------------------------------------------------------------
@Composable
private fun DebugSection(
    context: android.content.Context,
    voiceLogs: List<VoiceLogEntity>,
    currentSong: SongEntity?,
    isPlaying: Boolean,
    onTestVoice: () -> Unit
) {
    val micPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    val storagePerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("SYSTEM DIAGNOSTICS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = TextSecondary)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382958))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Android Permissions", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Microphone (RECORD_AUDIO)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(if (micPerm) "GRANTED" else "DENIED", color = if (micPerm) AccentEmerald else Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Audio Storage Access", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(if (storagePerm) "GRANTED" else "DENIED", color = if (storagePerm) AccentEmerald else Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Playback Status", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(if (isPlaying) "PLAYING" else "IDLE", color = if (isPlaying) AccentEmerald else TextTertiary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    if (currentSong != null) {
                        Text("Track: ${currentSong.title} (${currentSong.uri})", style = MaterialTheme.typography.labelSmall, color = SecondaryCyan)
                    }
                }
            }
        }

        item {
            Text("RECENT VOICE RECOGNITION LOGS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = TextSecondary)
        }

        if (voiceLogs.isEmpty()) {
            item {
                Text("No voice commands logged yet.", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
        } else {
            items(voiceLogs.take(5)) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Command: \"${log.command}\"", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = PrimaryNeon)
                        Text("Response: ${log.response}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("Action: ${log.actionTaken}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary)
                    }
                }
            }
        }
    }
}

private fun getSectionIcon(section: MenuSection): ImageVector = when (section) {
    MenuSection.LIBRARY -> Icons.Default.LibraryMusic
    MenuSection.ADD_MUSIC -> Icons.Default.UploadFile
    MenuSection.PLAYLISTS -> Icons.Default.PlaylistPlay
    MenuSection.VOICE_SETTINGS -> Icons.Default.Mic
    MenuSection.PLAYBACK_SETTINGS -> Icons.Default.Tune
    MenuSection.FEATURES -> Icons.Default.Star
    MenuSection.UPDATES -> Icons.Default.SystemUpdate
    MenuSection.ABOUT -> Icons.Default.Info
    MenuSection.DEBUG -> Icons.Default.BugReport
}

private fun getSectionTitle(section: MenuSection): String = when (section) {
    MenuSection.LIBRARY -> "Library"
    MenuSection.ADD_MUSIC -> "Add Music"
    MenuSection.PLAYLISTS -> "Playlists"
    MenuSection.VOICE_SETTINGS -> "Voice Settings"
    MenuSection.PLAYBACK_SETTINGS -> "Playback"
    MenuSection.FEATURES -> "Features"
    MenuSection.UPDATES -> "Updates"
    MenuSection.ABOUT -> "About"
    MenuSection.DEBUG -> "Debug"
}

private fun formatDrawerDuration(ms: Long): String {
    val sec = (ms / 1000) % 60
    val min = (ms / 1000) / 60
    return String.format("%02d:%02d", min, sec)
}
