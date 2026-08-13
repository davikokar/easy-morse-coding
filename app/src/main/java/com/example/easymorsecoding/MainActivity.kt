package com.example.easymorsecoding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.easymorsecoding.ui.MorseMessengerApp
import com.example.easymorsecoding.ui.theme.EasyMorseCodingTheme
import com.example.easymorsecoding.viewmodel.MorseViewModel
import com.example.easymorsecoding.viewmodel.PlaybackState

class MainActivity : ComponentActivity() {

    private val viewModel: MorseViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        viewModel.onToggleFlashlight(enabled = isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            // Keep screen awake during playback or countdown
            LaunchedEffect(uiState.playbackState) {
                if (uiState.playbackState != PlaybackState.IDLE) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            EasyMorseCodingTheme {
                MorseMessengerApp(
                    viewModel = viewModel,
                    onRequestPermission = { checkAndRequestPermission() },
                )
            }
        }
    }

    private fun checkAndRequestPermission() {
        val permission = Manifest.permission.CAMERA
        when (ContextCompat.checkSelfPermission(this, permission)) {
            PackageManager.PERMISSION_GRANTED -> {
                viewModel.onToggleFlashlight(enabled = true)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop playback when activity is paused to release hardware resources
        viewModel.stopPlayback()
    }
}
