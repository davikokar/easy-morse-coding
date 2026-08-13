# Implementation Plan - Morse Messenger

Create a complete Morse code conversion and playback application using Kotlin and Jetpack Compose.

## User Review Required

> [!IMPORTANT]
> The app will require `android.permission.CAMERA` for the flashlight functionality. Users will be prompted for this permission at runtime when they attempt to enable the flashlight output.

> [!NOTE]
> Playback will stop automatically if the app is moved to the background to ensure hardware resources (Flashlight, Audio) are released safely.

## Proposed Changes

### Core Logic & Models

#### [NEW] [MorseSignal.kt](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/java/com/example/easymorsecoding/model/MorseSignal.kt)
Defines the `MorseSignal` enum (Dot, Dash, ElementGap, CharacterGap, WordGap) and their durations in units.

#### [NEW] [MorseEncoder.kt](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/java/com/example/easymorsecoding/encoder/MorseEncoder.kt)
Handles text normalization and conversion to a sequence of `MorseSignal` objects.

### Hardware Controllers

#### [NEW] [MorsePlayer.kt](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/java/com/example/easymorsecoding/playback/MorsePlayer.kt)
Interfaces for the hardware controllers to allow for unit testing without physical hardware.

#### [NEW] [FlashlightController.kt](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/java/com/example/easymorsecoding/playback/FlashlightController.kt)
Manages the phone's torch using `CameraManager`.

#### [NEW] [SoundController.kt](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/java/com/example/easymorsecoding/playback/SoundController.kt)
Generates 700Hz sine wave audio using `AudioTrack`.

#### [NEW] [PlaybackController.kt](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/java/com/example/easymorsecoding/playback/PlaybackController.kt)
Orchestrates the timing of the playback sequence, triggering the visual, sound, and flashlight controllers.

### ViewModel & State

#### [NEW] [MorseUiState.kt](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/java/com/example/easymorsecoding/viewmodel/MorseUiState.kt)
Data class representing the entire UI state (input text, settings, playback progress, etc.).

#### [NEW] [MorseViewModel.kt](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/java/com/example/easymorsecoding/viewmodel/MorseViewModel.kt)
The core coordinator for the app. Handles user input, countdown timer, and playback job lifecycle.

### UI Components

#### [MODIFY] [MainActivity.kt](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/java/com/example/easymorsecoding/MainActivity.kt)
Sets up the `MorseMessengerApp` and provides the ViewModel.

#### [NEW] [MorseMessengerApp.kt](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/java/com/example/easymorsecoding/ui/MorseMessengerApp.kt)
The root Composable for the app, managing layout and theme.

#### [NEW] [Components](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/java/com/example/easymorsecoding/ui/components/)
Individual composables for text input, settings (switches/selectors), and the visual flash panel.

### Configuration

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/david/AndroidStudioProjects/EasyMorseCoding/app/src/main/AndroidManifest.xml)
Add `<uses-permission android:name="android.permission.CAMERA" />` and `<uses-feature android:name="android.hardware.camera.flash" />`.

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - `MorseEncoderTest`: Verify character mapping and normalization.
    - `PlaybackControllerTest`: Verify timing logic and sequence generation (using mock controllers).
- **UI Tests**:
    - `MorseMessengerUiTest`: Verify text input and Play button interaction.

### Manual Verification
1. Enter "SOS" and verify Morse translation display.
2. Test different output combinations (Screen + Sound, Flashlight only, etc.).
3. Verify countdown timer (3s, 5s) works correctly.
4. Verify speed control (WPM) changes playback speed.
5. Verify "Stop" button immediately halts all output and resets state.
6. Check behavior when switching to background (playback should stop).
7. Test on a device without a flashlight (option should be disabled).
