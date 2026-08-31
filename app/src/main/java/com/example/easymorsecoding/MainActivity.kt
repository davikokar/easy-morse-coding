package com.example.easymorsecoding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
    private var billingHelper: BillingHelper? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        viewModel.onToggleFlashlight(enabled = isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingHelper = BillingHelper(this)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
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
                    onLanguageSelected = { languageTag ->
                        LocaleHelper.persistLanguageTag(this, languageTag)
                        recreate()
                    },
                    onShareApp = { shareApp() },
                    onRateApp = { rateApp() },
                    onCustomerSupport = { contactSupport() },
                    onBuyCoffee = { billingHelper?.buyCoffee() },
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

    override fun onDestroy() {
        super.onDestroy()
        billingHelper?.endConnection()
    }

    private fun shareApp() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text, packageName))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    private fun rateApp() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun contactSupport() {
        val email = getString(R.string.support_email_address)
        val subject = Uri.encode(getString(R.string.support_email_subject))
        try {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email?subject=$subject")))
        } catch (_: Exception) {
            Toast.makeText(this, R.string.error_no_email_app, Toast.LENGTH_SHORT).show()
        }
    }
}
