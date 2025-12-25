# Accessibility Features

Termux AI includes numerous accessibility features to ensure the app is usable by everyone, including users with disabilities.

## Touch Target Optimization

All interactive elements meet the minimum 48dp touch target requirement:
- Tab close buttons: 48dp minimum
- Floating action buttons: Standard FAB sizes
- Menu items: 48dp minimum
- Toolbar buttons: 48dp minimum

Implemented using `MobileGesturesHelper.setupTouchTarget()` method to ensure proper sizing.

## Content Descriptions

Every interactive element has meaningful content descriptions:
- FABs: "Create new terminal tab", "Claude Code quick actions", etc.
- Tab elements: "Tab for [project name]", "Close tab [project name]"
- Claude indicators: "Claude AI status indicator", "Claude operation progress"
- Menu items: "Settings", "Help", "New Tab", etc.

## Color and Contrast

The app follows Material 3 accessibility guidelines:
- Sufficient color contrast ratios (minimum 4.5:1 for normal text)
- High contrast mode support
- Dynamic color support for Material You themes
- Clear visual distinction between active and inactive elements

## Screen Reader Support

The app is optimized for screen readers:
- Semantic structure with proper heading hierarchy
- Live regions for Claude status updates
- Proper labeling of interactive elements
- Sequential navigation order

## Gesture Accessibility

Gestures are designed to be accessible:
- Large gesture detection areas
- Visual feedback for gesture recognition
- Alternative button-based controls for all gesture functions
- Adjustable sensitivity settings (planned feature)

## Voice Input

Voice input is available for hands-free operation:
- Accessible via triple-tap gesture
- Alternative voice input button in bottom panel
- Proper audio feedback for voice recognition

## Keyboard Navigation

Though primarily a terminal app, keyboard navigation is supported:
- Tab navigation between interactive elements
- Proper focus indicators
- Shortcut keys for common actions
- External keyboard support