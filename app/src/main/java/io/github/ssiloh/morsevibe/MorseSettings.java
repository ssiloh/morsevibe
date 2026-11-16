package io.github.ssiloh.morsevibe;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * User-tunable vibration parameters.
 *
 * <p>Two ways to tell a dot from a dash:
 * <ul>
 *   <li>{@link #MODE_LENGTH}: dot and dash differ in length and share one intensity.</li>
 *   <li>{@link #MODE_INTENSITY}: dot and dash share one length and differ in intensity.
 *       Only usable when the device can control vibration amplitude.</li>
 * </ul>
 */
public final class MorseSettings {
    public static final int MODE_LENGTH = 0;
    public static final int MODE_INTENSITY = 1;

    public static final int DEF_DOT_MS = 100;
    public static final int DEF_DASH_MS = 300;
    public static final int DEF_INTENSITY = 255;
    public static final int DEF_DOT_AMP = 80;
    public static final int DEF_DASH_AMP = 255;
    public static final int DEF_UNIT_MS = 150;
    public static final int DEF_SYMBOL_GAP = 100;
    public static final int DEF_LETTER_GAP = 300;
    public static final int DEF_WORD_GAP = 700;

    private final SharedPreferences p;

    public MorseSettings(Context c) {
        p = c.getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public boolean enabled() { return p.getBoolean("enabled", true); }
    public void setEnabled(boolean v) { p.edit().putBoolean("enabled", v).apply(); }

    public int mode() { return p.getInt("mode", MODE_LENGTH); }
    public void setMode(int v) { p.edit().putInt("mode", v).apply(); }

    public int dotMs() { return p.getInt("dotMs", DEF_DOT_MS); }
    public void setDotMs(int v) { p.edit().putInt("dotMs", v).apply(); }

    public int dashMs() { return p.getInt("dashMs", DEF_DASH_MS); }
    public void setDashMs(int v) { p.edit().putInt("dashMs", v).apply(); }

    /** Shared intensity (1-255) used in length mode. */
    public int intensity() { return p.getInt("intensity", DEF_INTENSITY); }
    public void setIntensity(int v) { p.edit().putInt("intensity", v).apply(); }

    public int dotAmp() { return p.getInt("dotAmp", DEF_DOT_AMP); }
    public void setDotAmp(int v) { p.edit().putInt("dotAmp", v).apply(); }

    public int dashAmp() { return p.getInt("dashAmp", DEF_DASH_AMP); }
    public void setDashAmp(int v) { p.edit().putInt("dashAmp", v).apply(); }

    /** Shared dot/dash length used in intensity mode. */
    public int unitMs() { return p.getInt("unitMs", DEF_UNIT_MS); }
    public void setUnitMs(int v) { p.edit().putInt("unitMs", v).apply(); }

    public int symbolGap() { return p.getInt("symbolGap", DEF_SYMBOL_GAP); }
    public void setSymbolGap(int v) { p.edit().putInt("symbolGap", v).apply(); }

    public int letterGap() { return p.getInt("letterGap", DEF_LETTER_GAP); }
    public void setLetterGap(int v) { p.edit().putInt("letterGap", v).apply(); }

    public int wordGap() { return p.getInt("wordGap", DEF_WORD_GAP); }
    public void setWordGap(int v) { p.edit().putInt("wordGap", v).apply(); }

    public boolean ignoreOngoing() { return p.getBoolean("ignoreOngoing", true); }
    public void setIgnoreOngoing(boolean v) { p.edit().putBoolean("ignoreOngoing", v).apply(); }

    public boolean logEnabled() { return p.getBoolean("logEnabled", true); }
    public void setLogEnabled(boolean v) { p.edit().putBoolean("logEnabled", v).apply(); }

    /** Restores the vibration parameters (not the on/off switches) to their defaults. */
    public void resetVibration() {
        p.edit()
                .remove("mode").remove("dotMs").remove("dashMs").remove("intensity")
                .remove("dotAmp").remove("dashAmp").remove("unitMs")
                .remove("symbolGap").remove("letterGap").remove("wordGap")
                .apply();
    }
}
