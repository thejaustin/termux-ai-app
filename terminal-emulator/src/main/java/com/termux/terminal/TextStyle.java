package com.termux.terminal;

/**
 * Text style encoding for terminal characters.
 *
 * Encodes foreground color, background color, and text attributes (bold, italic, etc.)
 * into a single int for efficient storage.
 *
 * Encoding format (32 bits):
 * - Bits 0-8: Foreground color (9 bits, 0-511)
 * - Bits 9-17: Background color (9 bits, 0-511)
 * - Bits 18-23: Text attributes (6 bits)
 */
public class TextStyle {
    // Color indices
    public static final int COLOR_INDEX_FOREGROUND = 256;
    public static final int COLOR_INDEX_BACKGROUND = 257;
    public static final int COLOR_INDEX_CURSOR = 258;

    // Text attribute flags
    private static final int CHARACTER_ATTRIBUTE_BOLD = 1;
    private static final int CHARACTER_ATTRIBUTE_ITALIC = 1 << 1;
    private static final int CHARACTER_ATTRIBUTE_UNDERLINE = 1 << 2;
    private static final int CHARACTER_ATTRIBUTE_BLINK = 1 << 3;
    private static final int CHARACTER_ATTRIBUTE_INVERSE = 1 << 4;
    private static final int CHARACTER_ATTRIBUTE_INVISIBLE = 1 << 5;
    private static final int CHARACTER_ATTRIBUTE_STRIKETHROUGH = 1 << 6;
    private static final int CHARACTER_ATTRIBUTE_DIM = 1 << 7;

    // Default ANSI color palette (0-15: standard colors, 16-255: extended colors)
    public static final int[] DEFAULT_COLORSCHEME = {
        // Standard colors (0-7)
        0xff000000, // black
        0xffcd0000, // red
        0xff00cd00, // green
        0xffcdcd00, // yellow
        0xff0000ee, // blue
        0xffcd00cd, // magenta
        0xff00cdcd, // cyan
        0xffe5e5e5, // white (light gray)

        // Bright colors (8-15)
        0xff7f7f7f, // bright black (gray)
        0xffff0000, // bright red
        0xff00ff00, // bright green
        0xffffff00, // bright yellow
        0xff5c5cff, // bright blue
        0xffff00ff, // bright magenta
        0xff00ffff, // bright cyan
        0xffffffff  // bright white
    };

    /**
     * Encode text style into a single int.
     *
     * @param foreColor Foreground color index (0-511)
     * @param backColor Background color index (0-511)
     * @param effect Text attributes bitmask
     * @return Encoded style
     */
    public static int encode(int foreColor, int backColor, int effect) {
        return ((effect & 0xFF) << 18) | ((backColor & 0x1FF) << 9) | (foreColor & 0x1FF);
    }

    /**
     * Decode foreground color from style.
     */
    public static int decodeForeColor(int style) {
        return style & 0x1FF;
    }

    /**
     * Decode background color from style.
     */
    public static int decodeBackColor(int style) {
        return (style >> 9) & 0x1FF;
    }

    /**
     * Decode effect/attributes from style.
     */
    public static int decodeEffect(int style) {
        return (style >> 18) & 0xFF;
    }

    /**
     * Check if style has bold attribute.
     */
    public static boolean isBold(int style) {
        return (decodeEffect(style) & CHARACTER_ATTRIBUTE_BOLD) != 0;
    }

    /**
     * Check if style has italic attribute.
     */
    public static boolean isItalic(int style) {
        return (decodeEffect(style) & CHARACTER_ATTRIBUTE_ITALIC) != 0;
    }

    /**
     * Check if style has underline attribute.
     */
    public static boolean isUnderline(int style) {
        return (decodeEffect(style) & CHARACTER_ATTRIBUTE_UNDERLINE) != 0;
    }

    /**
     * Check if style has blink attribute.
     */
    public static boolean isBlink(int style) {
        return (decodeEffect(style) & CHARACTER_ATTRIBUTE_BLINK) != 0;
    }

    /**
     * Check if style has inverse attribute.
     */
    public static boolean isInverse(int style) {
        return (decodeEffect(style) & CHARACTER_ATTRIBUTE_INVERSE) != 0;
    }

    /**
     * Check if style has invisible attribute.
     */
    public static boolean isInvisible(int style) {
        return (decodeEffect(style) & CHARACTER_ATTRIBUTE_INVISIBLE) != 0;
    }

    /**
     * Check if style has dim attribute.
     */
    public static boolean isDim(int style) {
        return (decodeEffect(style) & CHARACTER_ATTRIBUTE_DIM) != 0;
    }

    /**
     * Create style with bold attribute.
     */
    public static int setBold(int style, boolean bold) {
        int effect = decodeEffect(style);
        if (bold) {
            effect |= CHARACTER_ATTRIBUTE_BOLD;
        } else {
            effect &= ~CHARACTER_ATTRIBUTE_BOLD;
        }
        return encode(decodeForeColor(style), decodeBackColor(style), effect);
    }

    /**
     * Create style with underline attribute.
     */
    public static int setUnderline(int style, boolean underline) {
        int effect = decodeEffect(style);
        if (underline) {
            effect |= CHARACTER_ATTRIBUTE_UNDERLINE;
        } else {
            effect &= ~CHARACTER_ATTRIBUTE_UNDERLINE;
        }
        return encode(decodeForeColor(style), decodeBackColor(style), effect);
    }

    /**
     * Create style with inverse attribute.
     */
    public static int setInverse(int style, boolean inverse) {
        int effect = decodeEffect(style);
        if (inverse) {
            effect |= CHARACTER_ATTRIBUTE_INVERSE;
        } else {
            effect &= ~CHARACTER_ATTRIBUTE_INVERSE;
        }
        return encode(decodeForeColor(style), decodeBackColor(style), effect);
    }

    /**
     * Get ARGB color value from color index.
     *
     * @param colorIndex Color index (0-255)
     * @param colorScheme Color palette array
     * @return ARGB color value
     */
    public static int getColor(int colorIndex, int[] colorScheme) {
        if (colorIndex < 0 || colorIndex >= 256) {
            return 0xFFFFFFFF; // White for invalid indices
        }

        if (colorIndex < colorScheme.length) {
            return colorScheme[colorIndex];
        }

        // Generate color for extended palette (16-255)
        if (colorIndex >= 16 && colorIndex < 232) {
            // 216 colors (6x6x6 color cube)
            int index = colorIndex - 16;
            int r = (index / 36) * 51; // 0, 51, 102, 153, 204, 255
            int g = ((index / 6) % 6) * 51;
            int b = (index % 6) * 51;
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        } else if (colorIndex >= 232) {
            // 24 grayscale colors
            int gray = 8 + (colorIndex - 232) * 10;
            return 0xFF000000 | (gray << 16) | (gray << 8) | gray;
        }

        return 0xFFFFFFFF; // White as fallback
    }
}
