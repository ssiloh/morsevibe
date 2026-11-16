package io.github.ssiloh.morsevibe;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Builds Morse vibration waveforms from {@link MorseSettings} and plays them one after another. */
public final class MorsePlayer {
    /** Pause between two queued notifications' patterns. */
    private static final long QUEUE_PAUSE_MS = 800;
    private static final int MAX_QUEUE = 5;

    /** A vibration waveform: alternating off/on durations starting with "off", plus amplitudes. */
    public static final class Waveform {
        public long[] timings = new long[0];
        public int[] amplitudes = new int[0];
        public long totalMs;
        boolean alarm;

        public boolean isEmpty() { return timings.length == 0; }
    }

    private static MorsePlayer instance;

    public static synchronized MorsePlayer get(Context c) {
        if (instance == null) instance = new MorsePlayer(c.getApplicationContext());
        return instance;
    }

    private final Context context;
    private final Vibrator vibrator;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayDeque<Waveform> queue = new ArrayDeque<>();
    private boolean busy;

    private MorsePlayer(Context c) {
        context = c;
        Vibrator v;
        if (Build.VERSION.SDK_INT >= 31) {
            VibratorManager vm = (VibratorManager) c.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            v = vm != null ? vm.getDefaultVibrator() : null;
        } else {
            v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        }
        vibrator = v;
    }

    public boolean hasVibrator() {
        return vibrator != null && vibrator.hasVibrator();
    }

    public boolean hasAmplitudeControl() {
        return vibrator != null && vibrator.hasAmplitudeControl();
    }

    /** Builds the waveform for a canonical code string using the current settings. */
    public Waveform build(String code) {
        MorseSettings s = new MorseSettings(context);
        int mode = hasAmplitudeControl() ? s.mode() : MorseSettings.MODE_LENGTH;
        int dotMs, dashMs, dotAmp, dashAmp;
        if (mode == MorseSettings.MODE_INTENSITY) {
            dotMs = dashMs = s.unitMs();
            dotAmp = s.dotAmp();
            dashAmp = s.dashAmp();
        } else {
            dotMs = s.dotMs();
            dashMs = s.dashMs();
            dotAmp = dashAmp = s.intensity();
        }
        return build(Morse.parse(code), dotMs, dashMs, dotAmp, dashAmp,
                s.symbolGap(), s.letterGap(), s.wordGap());
    }

    static Waveform build(List<List<String>> words, int dotMs, int dashMs, int dotAmp, int dashAmp,
                          int symbolGap, int letterGap, int wordGap) {
        List<Long> timings = new ArrayList<>();
        List<Integer> amps = new ArrayList<>();
        long pendingOff = 0;
        long total = 0;
        for (List<String> word : words) {
            for (String letter : word) {
                for (int i = 0; i < letter.length(); i++) {
                    char ch = letter.charAt(i);
                    if (ch != '.' && ch != '-') continue;
                    boolean dot = ch == '.';
                    long on = dot ? dotMs : dashMs;
                    timings.add(pendingOff);
                    amps.add(0);
                    timings.add(on);
                    amps.add(clamp(dot ? dotAmp : dashAmp));
                    total += pendingOff + on;
                    pendingOff = symbolGap;
                }
                pendingOff = letterGap;
            }
            pendingOff = wordGap;
        }
        Waveform w = new Waveform();
        w.timings = new long[timings.size()];
        w.amplitudes = new int[amps.size()];
        for (int i = 0; i < w.timings.length; i++) {
            w.timings[i] = timings.get(i);
            w.amplitudes[i] = amps.get(i);
        }
        w.totalMs = total;
        return w;
    }

    private static int clamp(int amp) {
        return Math.max(1, Math.min(255, amp));
    }

    /**
     * Queues a pattern behind whatever is playing (used for notifications).
     *
     * @param alarm play with alarm usage so it works regardless of the notification-vibration switch
     */
    public synchronized void enqueue(String code, boolean alarm) {
        Waveform w = build(code);
        if (w.isEmpty() || !hasVibrator()) return;
        w.alarm = alarm;
        if (queue.size() >= MAX_QUEUE) queue.pollFirst();
        queue.addLast(w);
        if (!busy) playNext();
    }

    /** Stops anything playing and plays this pattern right away (used for tests and practice). */
    public synchronized void playNow(String code, boolean alarm) {
        stop();
        enqueue(code, alarm);
    }

    public synchronized void stop() {
        queue.clear();
        busy = false;
        handler.removeCallbacksAndMessages(null);
        if (vibrator != null) vibrator.cancel();
    }

    private synchronized void playNext() {
        Waveform w = queue.pollFirst();
        if (w == null) {
            busy = false;
            return;
        }
        busy = true;
        vibrate(w);
        handler.postDelayed(this::playNext, w.totalMs + QUEUE_PAUSE_MS);
    }

    @SuppressWarnings("deprecation")
    private void vibrate(Waveform w) {
        VibrationEffect effect = hasAmplitudeControl()
                ? VibrationEffect.createWaveform(w.timings, w.amplitudes, -1)
                : VibrationEffect.createWaveform(w.timings, -1);
        if (Build.VERSION.SDK_INT >= 33) {
            int usage = w.alarm ? VibrationAttributes.USAGE_ALARM : VibrationAttributes.USAGE_NOTIFICATION;
            vibrator.vibrate(effect, VibrationAttributes.createForUsage(usage));
        } else {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(w.alarm ? AudioAttributes.USAGE_ALARM : AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            vibrator.vibrate(effect, attrs);
        }
    }
}
