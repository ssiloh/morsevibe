package io.github.ssiloh.morsevibe;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

/** Tiny helpers for building the (code-only) UI. */
final class Ui {
    interface IntCb {
        void on(int value);
    }

    private Ui() {}

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    /** Pads {@code v} by the system bars (the app draws edge-to-edge on Android 15+). */
    static void applyInsets(View v) {
        final int l = v.getPaddingLeft(), t = v.getPaddingTop(), r = v.getPaddingRight(), b = v.getPaddingBottom();
        v.setOnApplyWindowInsetsListener((view, insets) -> {
            int il, it, ir, ib;
            if (Build.VERSION.SDK_INT >= 30) {
                Insets i = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                il = i.left;
                it = i.top;
                ir = i.right;
                ib = i.bottom;
            } else {
                il = insets.getSystemWindowInsetLeft();
                it = insets.getSystemWindowInsetTop();
                ir = insets.getSystemWindowInsetRight();
                ib = insets.getSystemWindowInsetBottom();
            }
            view.setPadding(l + il, t + it, r + ir, b + ib);
            return insets;
        });
        v.requestApplyInsets();
    }

    static LinearLayout vertical(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        int p = dp(c, 16);
        l.setPadding(p, p, p, p);
        return l;
    }

    /** Wraps {@code content} in a scroll view that handles the system-bar insets. */
    static ScrollView scroll(Context c, View content) {
        ScrollView sv = new ScrollView(c);
        sv.setClipToPadding(false);
        sv.addView(content, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        applyInsets(sv);
        return sv;
    }

    static LinearLayout.LayoutParams lp(Context c, int topDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(c, topDp);
        return p;
    }

    static void add(LinearLayout parent, View child, int topDp) {
        parent.addView(child, lp(parent.getContext(), topDp));
    }

    static TextView title(Context c, String text) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextSize(26);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    static TextView section(Context c, String text) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextSize(16);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    static TextView body(Context c, String text) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextSize(14);
        t.setAlpha(0.75f);
        return t;
    }

    static Button button(Context c, String text, View.OnClickListener l) {
        Button b = new Button(c);
        b.setText(text);
        b.setAllCaps(false);
        b.setOnClickListener(l);
        return b;
    }

    /** A "Label: value unit" caption above a seek bar over [min, max]. */
    static View seekRow(Context c, String label, int min, int max, int value, String unit, IntCb onChange) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.VERTICAL);
        final TextView caption = new TextView(c);
        caption.setTextSize(14);
        final SeekBar bar = new SeekBar(c);
        bar.setMax(max - min);
        bar.setProgress(Math.max(0, Math.min(max - min, value - min)));
        caption.setText(label + ": " + value + " " + unit);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                int v = min + progress;
                caption.setText(label + ": " + v + " " + unit);
                if (fromUser) onChange.on(v);
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {}

            @Override
            public void onStopTrackingTouch(SeekBar s) {}
        });
        row.addView(caption);
        row.addView(bar);
        return row;
    }

    /** True if the user has enabled notification access for this app. */
    static boolean hasListenerAccess(Context c) {
        String flat = Settings.Secure.getString(c.getContentResolver(), "enabled_notification_listeners");
        if (flat == null) return false;
        for (String s : flat.split(":")) {
            ComponentName cn = ComponentName.unflattenFromString(s);
            if (cn != null && cn.getPackageName().equals(c.getPackageName())) return true;
        }
        return false;
    }
}
