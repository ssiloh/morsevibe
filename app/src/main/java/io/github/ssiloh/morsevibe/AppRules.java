package io.github.ssiloh.morsevibe;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Per-app rules: which Morse pattern to vibrate when an app posts a notification. */
public final class AppRules {
    public static final class Rule {
        public final String pkg;
        public boolean enabled;
        /** What the user typed (text or raw dots/dashes). */
        public String code;

        public Rule(String pkg, boolean enabled, String code) {
            this.pkg = pkg;
            this.enabled = enabled;
            this.code = code;
        }
    }

    private AppRules() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences("rules", Context.MODE_PRIVATE);
    }

    private static JSONObject read(Context c) {
        try {
            return new JSONObject(prefs(c).getString("rules", "{}"));
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public static synchronized Map<String, Rule> all(Context c) {
        Map<String, Rule> out = new HashMap<>();
        JSONObject o = read(c);
        Iterator<String> it = o.keys();
        while (it.hasNext()) {
            String pkg = it.next();
            JSONObject r = o.optJSONObject(pkg);
            if (r == null) continue;
            out.put(pkg, new Rule(pkg, r.optBoolean("e", true), r.optString("c", "")));
        }
        return out;
    }

    public static synchronized Rule get(Context c, String pkg) {
        JSONObject r = read(c).optJSONObject(pkg);
        if (r == null) return null;
        return new Rule(pkg, r.optBoolean("e", true), r.optString("c", ""));
    }

    public static synchronized void put(Context c, Rule rule) {
        JSONObject o = read(c);
        try {
            JSONObject r = new JSONObject();
            r.put("e", rule.enabled);
            r.put("c", rule.code);
            o.put(rule.pkg, r);
        } catch (JSONException ignored) {
            return;
        }
        prefs(c).edit().putString("rules", o.toString()).apply();
    }

    public static synchronized void remove(Context c, String pkg) {
        JSONObject o = read(c);
        o.remove(pkg);
        prefs(c).edit().putString("rules", o.toString()).apply();
    }
}
