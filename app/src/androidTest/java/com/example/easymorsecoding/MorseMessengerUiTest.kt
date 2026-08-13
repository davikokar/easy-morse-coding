package com.example.easymorsecoding

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MorseMessengerUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testTextInputPlayPauseStop() {
        // Find message input and enter text
        composeTestRule.onNodeWithText("Message to Encode").performTextInput("SOS")
        
        // Verify Morse translation appears
        composeTestRule.onNodeWithText("... --- ...").assertExists()
        
        // Find Play button and click
        composeTestRule.onNodeWithText("Play").performClick()
        
        // Verify Pause button appears
        composeTestRule.onNodeWithText("Pause").assertExists()
        
        // Click Pause
        composeTestRule.onNodeWithText("Pause").performClick()
        
        // Verify Resume button appears
        composeTestRule.onNodeWithText("Resume").assertExists()
        
        // Click Stop
        composeTestRule.onNodeWithText("Stop").performClick()
        
        // Verify Play button appears again
        composeTestRule.onNodeWithText("Play").assertExists()
    }
}
