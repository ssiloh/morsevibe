package io.github.ssiloh.morsevibe;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Small on-device log of received notifications (app, title, time). Stored locally only. */
public final class NotifLog {
    public static final class Entry {
        public String pkg = "";
        public String label = "";
        public String title = "";
        public long time;
        /** True if a rule vibrated for this notification. */
        public boolean matched;
    }

    private static final int MAX = 300;

    private NotifLog() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences("log", Context.MODE_PRIVATE);
    }

    /** Newest first. */
    public static synchronized List<Entry> list(Context c) {
        List<Entry> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(prefs(c).getString("log", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.optJSONObject(i);
                if (o == null) continue;
                Entry e = new Entry();
                e.pkg = o.optString("p");
                e.label = o.optString("l");
                e.title = o.optString("t");
                e.time = o.optLong("ts");
                e.matched = o.optBoolean("m");
                out.add(e);
            }
        } catch (JSONException ignored) {
            // corrupted log: start over
        }
        return out;
    }

    public static synchronized void add(Context c, Entry e) {
        List<Entry> entries = list(c);
        for (Entry x : entries) {
            if (x.time == e.time && x.pkg.equals(e.pkg)) return; // already logged (e.g. imported twice)
        }
        entries.add(e);
        Collections.sort(entries, (a, b) -> Long.compare(b.time, a.time));
        if (entries.size() > MAX) entries = new ArrayList<>(entries.subList(0, MAX));
        JSONArray a = new JSONArray();
        try {
            for (Entry x : entries) {
                JSONObject o = new JSONObject();
                o.put("p", x.pkg);
                o.put("l", x.label);
                o.put("t", x.title);
                o.put("ts", x.time);
                o.put("m", x.matched);
                a.put(o);
            }
        } catch (JSONException ignored) {
            return;
        }
        prefs(c).edit().putString("log", a.toString()).apply();
    }

    public static synchronized void clear(Context c) {
        prefs(c).edit().remove("log").apply();
    }

    /** Apps seen in the log: package -> label, most recent first. */
    public static synchronized Map<String, String> seenApps(Context c) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Entry e : list(c)) {
            if (!out.containsKey(e.pkg)) out.put(e.pkg, e.label);
        }
        return out;
    }
}
