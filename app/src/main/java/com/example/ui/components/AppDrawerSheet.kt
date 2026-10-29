package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.HarmonicToneGenerator
import com.example.ui.theme.EchoBg

/**
 * Full-screen modal ATHENA Sovereign OS App Drawer Dialog.
 * Hosts the dedicated [LauncherAppDrawerGrid] with adaptive columns,
 * A-Z spanning section headers, and fast scroller rail.
 */
@Composable
fun AppDrawerSheet(
    onDismiss: () -> Unit,
    toneGenerator: HarmonicToneGenerator? = null,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .background(EchoBg)
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = EchoBg
        ) {
            LauncherAppDrawerGrid(
                onDismiss = onDismiss,
                toneGenerator = toneGenerator,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
