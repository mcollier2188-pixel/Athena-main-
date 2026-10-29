package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.data.model.CreativePlaylistItem
import com.example.data.model.CreativeVideoItem
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoBgAlt
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoDarkSurface
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText

@Composable
fun PlaylistMediaDetailDialog(
    playlist: CreativePlaylistItem,
    toneGenerator: HarmonicToneGenerator,
    onPlayVideo: (CreativeVideoItem) -> Unit,
    onPlayPlaylist: (CreativePlaylistItem) -> Unit,
    onOpenExternalYouTube: (String, Boolean) -> Unit,
    onUpdatePlaylist: (CreativePlaylistItem) -> Unit,
    onDeletePlaylist: (CreativePlaylistItem) -> Unit,
    onSaveToNote: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var playlistTitle by remember { mutableStateOf(playlist.title) }
    var playlistDescription by remember { mutableStateOf(playlist.description) }
    val mediaItems = remember { mutableStateListOf<CreativeVideoItem>().apply { addAll(playlist.videos) } }

    var showEditPlaylistDialog by remember { mutableStateOf(false) }
    var showAddMediaDialog by remember { mutableStateOf(false) }
    var showDeletePlaylistConfirm by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<CreativeVideoItem?>(null) }
    var itemToDelete by remember { mutableStateOf<CreativeVideoItem?>(null) }

    fun notifyUpdated() {
        onUpdatePlaylist(
            playlist.copy(
                title = playlistTitle,
                description = playlistDescription,
                videoCount = "${mediaItems.size} Videos",
                videos = mediaItems.toList()
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = EchoBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                onDismiss()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EchoCyan)
                        }
                        Column {
                            Text(
                                text = "Playlist Media Vault",
                                color = EchoCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = playlistTitle,
                                color = EchoText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                showEditPlaylistDialog = true
                            },
                            modifier = Modifier.testTag("btn_edit_playlist")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Playlist", tint = EchoCyan)
                        }

                        IconButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                showDeletePlaylistConfirm = true
                            },
                            modifier = Modifier.testTag("btn_delete_playlist")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = EchoRed)
                        }

                        IconButton(
                            onClick = {
                                toneGenerator.playChime(SoundEffectType.TAP)
                                onDismiss()
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = EchoMuted)
                        }
                    }
                }

                HorizontalDivider(color = EchoCardStroke, thickness = 1.dp)

                // Playlist Hero Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    colors = CardDefaults.cardColors(containerColor = EchoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Cover art
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EchoDarkSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data("https://img.youtube.com/vi/${playlist.sampleVideoId}/hqdefault.jpg")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = playlistTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .border(1.dp, EchoCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(20.dp))
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlistTitle,
                                    color = EchoText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Curator: ${playlist.author}",
                                    color = EchoCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${mediaItems.size} Media Tracks & Videos",
                                    color = EchoMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = playlistDescription,
                                    color = EchoText.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    maxLines = 3
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Header Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    onPlayPlaylist(playlist)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1.2f).height(36.dp).testTag("btn_play_entire_playlist")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Play Playlist", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    onOpenExternalYouTube(playlist.id, true)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoRed, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1.1f).height(36.dp).testTag("btn_open_playlist_youtube")
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("YouTube App", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    toneGenerator.playChime(SoundEffectType.TAP)
                                    showAddMediaDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EchoBgAlt, contentColor = EchoCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EchoCyan.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp).testTag("btn_add_media_to_playlist")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Inner Media Items List Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tracks & Videos in this Playlist (${mediaItems.size})",
                        color = EchoText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to play · Edit or remove below",
                        color = EchoMuted,
                        fontSize = 11.sp
                    )
                }

                // Media Items LazyColumn
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                        .testTag("playlist_media_items_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(mediaItems, key = { it.id }) { video ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("media_item_${video.id}"),
                            colors = CardDefaults.cardColors(containerColor = EchoBgAlt),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EchoCardStroke),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail with Play overlay
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp, 48.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EchoDarkSurface)
                                            .clickable {
                                                toneGenerator.playChime(SoundEffectType.TAP)
                                                onPlayVideo(video)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data("https://img.youtube.com/vi/${video.youtubeVideoId}/hqdefault.jpg")
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = video.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.6f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EchoCyan, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = video.title,
                                            color = EchoText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = video.channel, color = EchoCyan, fontSize = 11.sp)
                                            Text(text = "•", color = EchoMuted, fontSize = 11.sp)
                                            Text(text = video.duration, color = EchoMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Item Action Bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            onPlayVideo(video)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1.1f).height(32.dp).testTag("btn_play_item_${video.id}")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(2.dp))
                                        Text("Play In-App", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            onOpenExternalYouTube(video.youtubeVideoId, false)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EchoRed.copy(alpha = 0.85f), contentColor = Color.White),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("btn_yt_item_${video.id}")
                                    ) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(2.dp))
                                        Text("YouTube", fontSize = 10.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            itemToEdit = video
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("btn_edit_item_${video.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Title", modifier = Modifier.size(12.dp), tint = EchoCyan)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            onSaveToNote(
                                                "Track: ${video.title.take(30)}",
                                                "Playlist: $playlistTitle\nTrack: ${video.title}\nChannel: ${video.channel}\nDuration: ${video.duration}\nLink: https://www.youtube.com/watch?v=${video.youtubeVideoId}\n\nSummary:\n${video.summary}"
                                            )
                                            Toast.makeText(context, "Saved Track to Notes!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("btn_save_note_item_${video.id}")
                                    ) {
                                        Icon(Icons.Default.NoteAdd, contentDescription = "Note", modifier = Modifier.size(12.dp), tint = EchoText)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            toneGenerator.playChime(SoundEffectType.TAP)
                                            itemToDelete = video
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp).testTag("btn_delete_item_${video.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(12.dp), tint = EchoRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Edit Playlist (Title & Description)
    if (showEditPlaylistDialog) {
        var tempTitle by remember { mutableStateOf(playlistTitle) }
        var tempDesc by remember { mutableStateOf(playlistDescription) }

        AlertDialog(
            onDismissRequest = { showEditPlaylistDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = EchoCyan)
                    Text("Edit Playlist Info", color = EchoCyan, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        label = { Text("Playlist Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = tempDesc,
                        onValueChange = { tempDesc = it },
                        label = { Text("Description & Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        playlistTitle = tempTitle.ifBlank { "Untitled Playlist" }
                        playlistDescription = tempDesc
                        notifyUpdated()
                        showEditPlaylistDialog = false
                        Toast.makeText(context, "Playlist updated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditPlaylistDialog = false }) {
                    Text("Cancel", color = EchoMuted)
                }
            },
            containerColor = EchoDarkSurface
        )
    }

    // Dialog: Add Media Item to Playlist
    if (showAddMediaDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newUrlOrId by remember { mutableStateOf("") }
        var newChannel by remember { mutableStateOf("") }
        var newDuration by remember { mutableStateOf("Custom Track") }

        AlertDialog(
            onDismissRequest = { showAddMediaDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = EchoCyan)
                    Text("Add Video / Media Track", color = EchoCyan, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = { Text("Video Title (e.g. Masterclass Lecture)", color = EchoMuted) },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newUrlOrId,
                        onValueChange = { newUrlOrId = it },
                        placeholder = { Text("YouTube URL or 11-char Video ID", color = EchoMuted) },
                        label = { Text("Video Link or ID") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newChannel,
                        onValueChange = { newChannel = it },
                        placeholder = { Text("Channel / Artist name", color = EchoMuted) },
                        label = { Text("Channel Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoCyan,
                            focusedTextColor = EchoText,
                            unfocusedTextColor = EchoText
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val videoId = parseYouTubeVideoId(newUrlOrId)
                        if (newTitle.isNotBlank() && videoId.isNotBlank()) {
                            toneGenerator.playChime(SoundEffectType.SUCCESS)
                            val newItem = CreativeVideoItem(
                                id = "track_${System.currentTimeMillis()}",
                                title = newTitle,
                                channel = newChannel.ifBlank { "YouTube Creator" },
                                duration = newDuration,
                                views = "Playlist Item",
                                category = "Playlist Track",
                                summary = "Added to playlist $playlistTitle",
                                youtubeVideoId = videoId
                            )
                            mediaItems.add(newItem)
                            notifyUpdated()
                            showAddMediaDialog = false
                            Toast.makeText(context, "Added track to playlist!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter title and valid video ID", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
                ) {
                    Text("Add Track")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddMediaDialog = false }) {
                    Text("Cancel", color = EchoMuted)
                }
            },
            containerColor = EchoDarkSurface
        )
    }

    // Dialog: Edit individual media title
    itemToEdit?.let { item ->
        var editItemTitle by remember { mutableStateOf(item.title) }

        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = { Text("Edit Track Title", color = EchoCyan, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editItemTitle,
                    onValueChange = { editItemTitle = it },
                    label = { Text("Track Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EchoCyan,
                        focusedTextColor = EchoText,
                        unfocusedTextColor = EchoText
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        val index = mediaItems.indexOfFirst { it.id == item.id }
                        if (index != -1) {
                            mediaItems[index] = item.copy(title = editItemTitle.ifBlank { item.title })
                            notifyUpdated()
                        }
                        itemToEdit = null
                        Toast.makeText(context, "Updated track title", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToEdit = null }) {
                    Text("Cancel", color = EchoMuted)
                }
            },
            containerColor = EchoDarkSurface
        )
    }

    // Dialog: Remove Media Item from Playlist
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Remove from Playlist?", color = EchoRed, fontWeight = FontWeight.Bold) },
            text = { Text("Remove \"${item.title}\" from this playlist? (The original video remains online).", color = EchoText) },
            confirmButton = {
                Button(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.TAP)
                        mediaItems.removeIf { it.id == item.id }
                        notifyUpdated()
                        itemToDelete = null
                        Toast.makeText(context, "Removed track from playlist", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoRed, contentColor = Color.White)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = EchoMuted)
                }
            },
            containerColor = EchoDarkSurface
        )
    }

    // Dialog: Delete Entire Playlist
    if (showDeletePlaylistConfirm) {
        AlertDialog(
            onDismissRequest = { showDeletePlaylistConfirm = false },
            title = { Text("Delete This Entire Playlist?", color = EchoRed, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"$playlistTitle\" and all its listed items?", color = EchoText) },
            confirmButton = {
                Button(
                    onClick = {
                        toneGenerator.playChime(SoundEffectType.SUCCESS)
                        onDeletePlaylist(playlist)
                        showDeletePlaylistConfirm = false
                        onDismiss()
                        Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EchoRed, contentColor = Color.White)
                ) {
                    Text("Delete Playlist")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeletePlaylistConfirm = false }) {
                    Text("Cancel", color = EchoMuted)
                }
            },
            containerColor = EchoDarkSurface
        )
    }
}

private fun parseYouTubeVideoId(input: String): String {
    val trimmed = input.trim()
    return if (trimmed.contains("v=")) {
        trimmed.substringAfter("v=").substringBefore("&").substringBefore("#")
    } else if (trimmed.contains("youtu.be/")) {
        trimmed.substringAfter("youtu.be/").substringBefore("?").substringBefore("#")
    } else if (trimmed.contains("embed/")) {
        trimmed.substringAfter("embed/").substringBefore("?").substringBefore("#")
    } else {
        trimmed
    }
}
