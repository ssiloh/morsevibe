package io.github.ssiloh.morsevibe;

import android.app.Notification;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.HashMap;
import java.util.Map;

/** Watches notifications from other apps, logs them, and vibrates Morse patterns for apps with a rule. */
public class MorseNotificationListener extends NotificationListenerService {
    /** Ignore a second notification from the same app within this window (updates, bursts). */
    private static final long THROTTLE_MS = 1500;

    private final Map<String, Long> lastByPackage = new HashMap<>();

    @Override
    public void onListenerConnected() {
        // Android has no API for past notification history; the best we can do is import what is
        // in the notification shade right now.
        try {
            StatusBarNotification[] active = getActiveNotifications();
            if (active == null || !new MorseSettings(this).logEnabled()) return;
            for (StatusBarNotification sbn : active) {
                if (!getPackageName().equals(sbn.getPackageName())) log(sbn, false);
            }
        } catch (RuntimeException ignored) {
            // the system may refuse while (re)binding; nothing to import then
        }
    }

    @Override
    public void onListenerDisconnected() {
        try {
            NotificationListenerService.requestRebind(new ComponentName(this, MorseNotificationListener.class));
        } catch (RuntimeException ignored) {
            // best effort
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;
        String pkg = sbn.getPackageName();
        if (getPackageName().equals(pkg)) return;
        Notification n = sbn.getNotification();
        if (n == null) return;
        if ((n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;

        MorseSettings settings = new MorseSettings(this);
        boolean ongoing = (n.flags & Notification.FLAG_ONGOING_EVENT) != 0;
        if (ongoing && settings.ignoreOngoing()) return;

        long now = SystemClock.elapsedRealtime();
        synchronized (lastByPackage) {
            Long prev = lastByPackage.get(pkg);
            if (prev != null && now - prev < THROTTLE_MS) return;
            lastByPackage.put(pkg, now);
        }

        AppRules.Rule rule = AppRules.get(this, pkg);
        String code = rule == null ? "" : Morse.toCode(rule.code);
        boolean match = settings.enabled() && rule != null && rule.enabled && !code.isEmpty();

        if (settings.logEnabled()) log(sbn, match);
        if (match) MorsePlayer.get(this).enqueue(code, false);
    }

    private void log(StatusBarNotification sbn, boolean matched) {
        NotifLog.Entry e = new NotifLog.Entry();
        e.pkg = sbn.getPackageName();
        e.label = labelFor(e.pkg);
        e.time = sbn.getPostTime();
        e.matched = matched;
        Notification n = sbn.getNotification();
        if (n != null && n.extras != null) {
            CharSequence title = n.extras.getCharSequence(Notification.EXTRA_TITLE);
            if (title != null) e.title = title.toString();
        }
        NotifLog.add(this, e);
    }

    private String labelFor(String pkg) {
        try {
            PackageManager pm = getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (PackageManager.NameNotFoundException | RuntimeException e) {
            return pkg;
        }
    }
}
