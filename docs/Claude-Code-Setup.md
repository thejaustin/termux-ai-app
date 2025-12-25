# Claude Code Integration

This document details the Claude Code integration in Termux AI and how to use it effectively.

## Overview

Claude Code integration brings Anthropic's powerful AI assistant directly into your terminal workflow. The integration detects when Claude is active and provides enhanced UI features and feedback.

## Activation

Claude Code is automatically detected when you run:
```
claude code
```

The app will show visual indicators when Claude is active, including:
- Tab icon changes to Claude indicator
- Claude status overlay appears
- Progress indicators for operations
- File operation highlights

## Mobile-Specific Features

### Claude Status Overlay
- Shows current Claude operation status
- Displays progress percentage
- Provides visual feedback during AI processing
- Can be dismissed with swipe-down gesture

### File Operations Highlighting
- Files created by Claude are highlighted in green
- Files modified by Claude are highlighted in yellow
- Files deleted by Claude are highlighted in red
- Visual indicators fade out after 5 seconds

### Quick Actions
Access Claude quick actions through the Claude FAB:
- File Operations: List and analyze project files
- Code Generation: Generate code snippets
- Project Analysis: Analyze project structure
- Debug Help: Get assistance with errors
- Voice Input: Speak commands to Claude
- Stop Claude: Cancel current operation

### Context Awareness
- Claude analyzes your current project automatically
- Detects project type (Node.js, Python, Rust, Go, Java, etc.)
- Provides project-specific suggestions
- Maintains context between commands

## Settings

Configure Claude behavior in the settings:
- API Key: Enter your Claude API key
- Model Selection: Choose Claude model (Sonnet, Opus, Haiku, etc.)
- Token Limit: Set maximum tokens per operation
- Auto Suggestions: Enable/disable automatic suggestions
- Command Filtering: Filter sensitive commands before sending to AI

## Voice Input

Activate voice input for Claude:
1. Triple tap on the terminal
OR
2. Long press the settings button and select voice input
3. Speak your command or question
4. Claude will process your voice input

## Project Insights

The Project Insights feature provides:
- Project type detection
- Language identification
- Framework detection
- Dependency analysis
- AI recommendations for improvements
- Detailed project reports

Access through the project info button (ℹ️) in the bottom panel.

## Troubleshooting

### Claude Not Detected
- Ensure Claude Code CLI is installed: `npm install -g @anthropic/claude-code`
- Check that you're running `claude code` in the terminal
- Verify API key is properly configured in settings

### Slow Response Times
- Check your internet connection
- Verify API quota hasn't been exceeded
- Consider using a lighter Claude model for faster responses

### Incorrect Project Context
- Claude analyzes the current working directory
- Change to the appropriate project directory before starting Claude
- Use `cd` command to navigate to your project folder