package godau.fynn.usagedirectplus;

import androidx.annotation.ColorInt;

/**
 * Backport of calls to Color method not available in low API versions
 */
public class Color extends android.graphics.Color {

    /**
     * Returns the relative luminance of a color.
     * <p>
     * Assumes sRGB encoding. Based on the formula for relative luminance
     * defined in WCAG 2.0, W3C Recommendation 11 December 2008.
     *
     * @return a value between 0 (darkest black) and 1 (lightest white)
     * @author <a href="https://cs.android.com/android/platform/superproject/+/android-7.1.2_r39:frameworks/base/graphics/java/android/graphics/Color.java;l=115-131">
     *     AOSP</a>
     */
    public static float luminance(@ColorInt int color) {
        double red = red(color) / 255.0;
        red = red < 0.03928 ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);
        double green = green(color) / 255.0;
        green = green < 0.03928 ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);
        double blue = blue(color) / 255.0;
        blue = blue < 0.03928 ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);
        return (float) ((0.2126 * red) + (0.7152 * green) + (0.0722 * blue));
    }
}
