# EduPrep Offline: UI & Scrolling Bug Fixes

## Bug Fix: Settings Screen Vertical Scrolling
- **Issue**: The Settings Screen layout became cut off/stuck on smaller screens because it did not support vertical scrolling, rendering users unable to access preferences at the bottom.
- **Solution**: Imported `rememberScrollState` and `verticalScroll` from `androidx.compose.foundation` in `SettingsScreen.kt` and applied `.verticalScroll(rememberScrollState())` to the main outer `Column` layout modifier.
- **Result**: The Settings Screen now scrolls perfectly and conforms to spatial/ergonomic guidelines of Jetpack Compose.
