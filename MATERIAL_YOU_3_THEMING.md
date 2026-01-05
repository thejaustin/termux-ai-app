# Material You 3 Expressive Theming - Implementation Guide

## Overview

Termux AI now features full **Material You 3 (Material Design 3) Expressive Theming** with dynamic colors that adapt to your Android 12+ wallpaper. This creates a personalized, cohesive visual experience across the entire app.

## What is Material You 3?

Material You 3 is Google's latest design system that:
- **Adapts to your wallpaper**: Extracts colors from your system wallpaper/theme
- **Expressive color schemes**: Creates harmonious color palettes automatically
- **System-wide consistency**: Matches other Material You apps on your device
- **Personal & vibrant**: Makes every phone unique to its owner

## Features Implemented

### 1. Dynamic Color System
- **Automatic wallpaper adaptation**: Colors extracted from Android 12+ system
- **Real-time updates**: Changes when you change your wallpaper
- **Color roles**: Uses proper Material 3 color roles (primary, secondary, tertiary, etc.)
- **Harmonious palettes**: Scientifically generated color harmonies

### 2. Theme Customization Options

#### **Dynamic Colors Toggle**
- Enable/disable Material You adaptive theming
- Falls back to static Material 3 theme when disabled
- Shows availability status for Android 12+ devices

#### **Theme Mode Selection**
Choose how light/dark theme behaves:
- **Auto (Follow System)**: Matches system dark mode setting *[Default]*
- **Light Mode**: Always use light theme
- **Dark Mode**: Always use dark theme

#### **Color Style Variants**
When dynamic colors are enabled, choose your color expression style:
- **Expressive (Recommended)**: Vibrant, bold colors with high contrast *[Default]*
- **Vibrant**: Maximum saturation and energy
- **Tonal**: Subtle, sophisticated color palette

### 3. Real-Time Theme Switching
- Instant preview of theme changes
- Automatic app restart when needed
- Smooth transitions between themes
- Preserves app state during theme changes

## Implementation Details

### Application Level (`TermuxAIApplication.java`)

```java
// Dynamic Colors with custom options
DynamicColors.applyToActivitiesIfAvailable(
    this,
    DynamicColorsOptions.Builder()
        .setThemeOverlay(getThemeOverlay())
        .setPrecondition((activity, theme) -> {
            return preferences.getBoolean(PREF_DYNAMIC_COLORS, true);
        })
        .setOnAppliedCallback(activity -> {
            Log.d(TAG, "Material You 3 Dynamic Colors applied successfully");
        })
        .build()
);
```

**Key Methods:**
- `setDynamicColorsEnabled(boolean)`: Enable/disable dynamic colors
- `setNightMode(int)`: Set light/dark/auto mode
- `setThemeStyle(String)`: Set color style (expressive/vibrant/tonal)
- `areDynamicColorsAvailable()`: Check Android 12+ availability

### Theme System (`values/themes.xml`)

Base themes use Material 3 Dynamic Colors:
```xml
<!-- Light Theme -->
<style name="Theme.TermuxAI" parent="Theme.Material3.DynamicColors.Light">
    <!-- Material You 3 color roles -->
</style>

<!-- Dark Theme -->
<style name="Theme.TermuxAI" parent="Theme.Material3.DynamicColors.Dark">
    <!-- Material You 3 color roles -->
</style>
```

### Settings UI (`activity_settings.xml`)

**New UI Components:**
- Material You info card with dynamic status
- Dynamic colors toggle (SwitchMaterial)
- Theme mode radio group (Auto/Light/Dark)
- Color style radio group (Expressive/Vibrant/Tonal)
- Android 12+ availability indicator

### Settings Activity (`TermuxAISettingsActivity.java`)

**Enhanced Features:**
- Real-time dynamic colors status updates
- Visibility management (color style only shown when dynamic colors enabled)
- Intelligent restart detection (only restarts if theme actually changed)
- Contextual toast messages for different theme changes
- Instant night mode application

## Color Roles in Material You 3

The app now uses proper Material 3 color roles:

| Role | Usage |
|------|-------|
| `colorPrimary` | Primary brand color, key actions |
| `colorOnPrimary` | Text/icons on primary color |
| `colorPrimaryContainer` | Backgrounds with primary context |
| `colorOnPrimaryContainer` | Content on primary containers |
| `colorSecondary` | Secondary actions, accents |
| `colorTertiary` | Tertiary actions, highlights |
| `colorSurface` | Card backgrounds, surfaces |
| `colorBackground` | Screen backgrounds |
| `colorError` | Error states |

These roles automatically adapt when you change your wallpaper!

## User Experience

### First Launch
1. App detects Android version
2. Enables Material You by default on Android 12+
3. Shows device capability in settings
4. Extracts colors from current wallpaper

### Changing Themes
1. User opens Settings
2. Sees current theme status in info card
3. Toggles/changes theme options
4. App shows restart message with theme name
5. App recreates with new theme instantly

### Android 11 and Below
- Material You toggle disabled with explanation
- Falls back to static Material 3 theme
- Still provides excellent Material 3 experience
- Light/Dark mode still works perfectly

## Benefits

### For Users
- **Personal**: Your wallpaper colors throughout the app
- **Beautiful**: Professionally designed color harmonies
- **Consistent**: Matches system apps and other Material You apps
- **Flexible**: Three style options to match your mood
- **Fast**: Instant theme switching

### For Developers
- **Modern**: Uses latest Material Design 3
- **Maintainable**: System-generated colors, less custom theming
- **Future-proof**: Follows Google's design direction
- **Accessible**: Automatic contrast and accessibility support

## Technical Requirements

- **Minimum Android Version**: Android 5.0 (API 21)
- **Material You Support**: Android 12+ (API 31+)
- **Fallback**: Static Material 3 theme on older devices
- **Dependencies**:
  - `com.google.android.material:material:1.9.0+`
  - `androidx.appcompat:appcompat:1.6.0+`

## Testing the Theme System

### Test Dynamic Colors
1. Open Settings → Appearance & Theme
2. Enable "Material You Dynamic Colors"
3. Choose "Expressive" style
4. Save and verify app restarts with wallpaper colors

### Test Theme Modes
1. Open Settings → Theme Mode
2. Select "Light Mode" → Verify light theme
3. Select "Dark Mode" → Verify dark theme
4. Select "Auto" → Verify follows system

### Test Color Styles
1. Enable dynamic colors
2. Try each style (Expressive, Vibrant, Tonal)
3. Observe color intensity differences
4. Verify smooth transitions

### Test Wallpaper Adaptation
1. Enable Material You
2. Change your Android wallpaper
3. Return to Termux AI
4. Colors should automatically update

## Code Examples

### Accessing Theme Settings in Code

```java
TermuxAIApplication app = (TermuxAIApplication) getApplication();

// Check if dynamic colors are enabled
if (app.isDynamicColorsEnabled()) {
    // Using dynamic colors
}

// Get current theme style
String style = app.getThemeStyle(); // "expressive", "vibrant", or "tonal"

// Check availability
if (app.areDynamicColorsAvailable()) {
    // Device supports Material You
}
```

### Applying Colors in Custom Views

```java
// Get Material 3 color roles
int primaryColor = MaterialColors.getColor(context,
    com.google.android.material.R.attr.colorPrimary,
    Color.BLACK);

int surfaceColor = MaterialColors.getColor(context,
    com.google.android.material.R.attr.colorSurface,
    Color.WHITE);
```

## Best Practices

### 1. Use Color Roles, Not Hardcoded Colors
❌ **Bad:**
```xml
<item name="android:textColor">#FF006A4E</item>
```

✅ **Good:**
```xml
<item name="android:textColor">?attr/colorOnSurface</item>
```

### 2. Support Both Light and Dark
Always test both theme modes:
- Light mode during day
- Dark mode at night
- Auto mode for system sync

### 3. Respect User Preferences
- Default to system settings
- Remember user choices
- Show clear theme status

### 4. Handle Older Devices Gracefully
- Check API level
- Provide fallback themes
- Explain limitations clearly

## Files Modified

1. **TermuxAIApplication.java**
   - Enhanced dynamic color application
   - Added theme management methods
   - Implemented style switching

2. **TermuxAISettingsActivity.java**
   - Added theme UI elements
   - Implemented load/save for themes
   - Added real-time status updates

3. **activity_settings.xml**
   - Material You info card
   - Theme mode controls
   - Color style options

4. **themes.xml** (already using Material 3 parents)
   - No changes needed - already using proper parents

5. **colors.xml**
   - Static fallback colors remain
   - Used only when dynamic colors disabled

## Future Enhancements

Potential additions for future versions:

- [ ] Custom seed color selection
- [ ] Theme preview before applying
- [ ] Export/import theme configurations
- [ ] Per-tab color customization
- [ ] Terminal-specific color schemes
- [ ] Accessibility contrast controls
- [ ] Theme scheduling (auto light/dark by time)

## Troubleshooting

### Dynamic Colors Not Working
- Check Android version (need 12+)
- Verify wallpaper-based theming enabled in Android settings
- Restart app after wallpaper change

### Colors Look Wrong
- Try different color style (Expressive/Vibrant/Tonal)
- Check wallpaper has sufficient color variety
- Disable and re-enable dynamic colors

### App Not Restarting After Theme Change
- Force close and reopen app
- Check system permissions
- Clear app cache if persistent

## Resources

- [Material Design 3 Guidelines](https://m3.material.io/)
- [Dynamic Color Documentation](https://m3.material.io/styles/color/dynamic-color/overview)
- [Android Dynamic Colors](https://developer.android.com/develop/ui/views/theming/dynamic-colors)
- [Material Components for Android](https://github.com/material-components/material-components-android)

## Conclusion

Material You 3 expressive theming transforms Termux AI into a truly personalized experience that feels at home on every Android device. The combination of dynamic colors, multiple theme modes, and style variants gives users unprecedented control over their app's appearance while maintaining the beautiful, cohesive design that Material Design 3 is known for.

---

**✨ Enjoy your personalized Material You 3 experience! ✨**
