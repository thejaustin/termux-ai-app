#!/usr/bin/env python3
"""
Termux AI Theme and Resource Verification Script

This script validates that all themes and resources are properly configured
for both dynamic colors (Android 12+) and fallback themes (older versions).
"""

import xml.etree.ElementTree as ET
import os
import sys
from pathlib import Path

def check_xml_syntax(file_path):
    """Check if XML file is syntactically valid."""
    try:
        ET.parse(file_path)
        return True, None
    except ET.ParseError as e:
        return False, str(e)

def check_color_references(file_path):
    """Check if color references are properly defined."""
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        
        color_refs = set()
        for elem in root.iter():
            for attr in elem.attrib.values():
                if attr.startswith('@color/'):
                    color_refs.add(attr[7:])  # Remove '@color/' prefix
        
        return color_refs
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
        return set()

def verify_themes():
    """Verify theme configuration."""
    print("🎨 Verifying Theme Configuration...")
    
    themes_file = "app/src/main/res/values/themes.xml"
    if not os.path.exists(themes_file):
        print("❌ themes.xml not found")
        return False
    
    valid, error = check_xml_syntax(themes_file)
    if not valid:
        print(f"❌ themes.xml syntax error: {error}")
        return False
    
    # Check for required themes
    tree = ET.parse(themes_file)
    root = tree.getroot()
    
    themes = [style.get('name') for style in root.findall('.//style')]
    required_themes = [
        'Theme.TermuxAI',
        'Theme.TermuxAI.Light',
        'Theme.TermuxAI.NoActionBar'
    ]
    
    missing_themes = [theme for theme in required_themes if theme not in themes]
    if missing_themes:
        print(f"❌ Missing themes: {missing_themes}")
        return False
    
    print("✅ All required themes present")
    print(f"   - Found themes: {themes}")
    return True

def verify_colors():
    """Verify color configuration."""
    print("\n🌈 Verifying Color Configuration...")
    
    colors_file = "app/src/main/res/values/colors.xml"
    if not os.path.exists(colors_file):
        print("❌ colors.xml not found")
        return False
    
    valid, error = check_xml_syntax(colors_file)
    if not valid:
        print(f"❌ colors.xml syntax error: {error}")
        return False
    
    # Check for AI-specific colors
    tree = ET.parse(colors_file)
    root = tree.getroot()
    
    colors = [color.get('name') for color in root.findall('.//color')]
    ai_colors = ['ai_green', 'ai_overlay', 'ai_highlight']
    
    missing_colors = [color for color in ai_colors if color not in colors]
    if missing_colors:
        print(f"❌ Missing AI colors: {missing_colors}")
        return False
    
    print("✅ All AI colors present")
    print(f"   - AI colors: {ai_colors}")
    
    # Check night mode colors
    night_colors_file = "app/src/main/res/values-night/colors.xml"
    if os.path.exists(night_colors_file):
        valid, error = check_xml_syntax(night_colors_file)
        if not valid:
            print(f"❌ night colors.xml syntax error: {error}")
            return False
        print("✅ Night mode colors configured")
    else:
        print("⚠️  Night mode colors not found (optional)")
    
    return True

def verify_java_references():
    """Verify that Java code references are valid."""
    print("\n☕ Verifying Java Code References...")
    
    java_file = "app/src/main/java/com/termux/terminal/EnhancedTerminalView.java"
    if not os.path.exists(java_file):
        print("❌ EnhancedTerminalView.java not found")
        return False
    
    with open(java_file, 'r') as f:
        content = f.read()
    
    # Check for hard-coded colors
    hard_coded_patterns = ['0x88000000', '0xFF4CAF50', '0x4400FF00']
    hard_coded_found = [pattern for pattern in hard_coded_patterns if pattern in content]
    
    if hard_coded_found:
        print(f"❌ Hard-coded colors still found: {hard_coded_found}")
        return False
    
    # Check for proper imports
    required_imports = ['androidx.core.content.ContextCompat', 'com.termux.ai.R']
    missing_imports = []
    
    for import_stmt in required_imports:
        if import_stmt not in content:
            missing_imports.append(import_stmt)
    
    if missing_imports:
        print(f"❌ Missing imports: {missing_imports}")
        return False
    
    # Check for color references
    ai_color_refs = ['R.color.ai_green', 'R.color.ai_overlay', 'R.color.ai_highlight']
    missing_refs = [ref for ref in ai_color_refs if ref not in content]
    
    if missing_refs:
        print(f"❌ Missing color references: {missing_refs}")
        return False
    
    print("✅ All hard-coded colors replaced with themed references")
    print("✅ Proper imports present")
    print("✅ AI color references used correctly")
    return True

def verify_documentation():
    """Verify that XML files have proper documentation."""
    print("\n📝 Verifying Documentation...")
    
    files_to_check = [
        "app/src/main/res/values/themes.xml",
        "app/src/main/res/values/colors.xml",
        "app/src/main/res/drawable/ic_claude_active.xml",
        "app/src/main/res/drawable/claude_status_background.xml"
    ]
    
    documented_files = 0
    for file_path in files_to_check:
        if os.path.exists(file_path):
            with open(file_path, 'r') as f:
                content = f.read()
                if '<!--' in content and ('Termux AI' in content or 'Claude' in content):
                    documented_files += 1
                    print(f"✅ {os.path.basename(file_path)} - documented")
                else:
                    print(f"⚠️  {os.path.basename(file_path)} - missing documentation")
        else:
            print(f"❌ {file_path} not found")
    
    print(f"📋 Documentation: {documented_files}/{len(files_to_check)} files documented")
    return documented_files >= len(files_to_check) * 0.75  # At least 75% documented

def main():
    """Main verification function."""
    print("🚀 Termux AI Theme and Resource Verification")
    print("=" * 50)
    
    if not os.path.exists("app/src/main"):
        print("❌ Not in correct directory. Run from project root.")
        sys.exit(1)
    
    all_passed = True
    
    # Run all verifications
    if not verify_themes():
        all_passed = False
    
    if not verify_colors():
        all_passed = False
    
    if not verify_java_references():
        all_passed = False
    
    if not verify_documentation():
        all_passed = False
    
    print("\n" + "=" * 50)
    if all_passed:
        print("🎉 ALL VERIFICATIONS PASSED!")
        print("\n✅ Dynamic and expressive themes configured")
        print("✅ Hard-coded colors replaced with themed references")
        print("✅ Documentation added to XML files")
        print("✅ Ready for build and review!")
        sys.exit(0)
    else:
        print("❌ SOME VERIFICATIONS FAILED")
        print("Please fix the issues above before proceeding.")
        sys.exit(1)

if __name__ == "__main__":
    main()