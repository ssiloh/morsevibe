package io.github.ssiloh.morsevibe;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView accessStatus;
    private TextView vibInfo;
    private Button openAccess;
    private TextView restrictedHint;
    private Button openAppInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = Ui.vertical(this);

        root.addView(Ui.title(this, getString(R.string.app_name)));
        Ui.add(root, Ui.body(this, getString(R.string.tagline)), 4);

        accessStatus = Ui.section(this, "");
        Ui.add(root, accessStatus, 24);
        openAccess = Ui.button(this, getString(R.string.btn_open_access),
                v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        Ui.add(root, openAccess, 8);
        restrictedHint = Ui.body(this, getString(R.string.hint_restricted));
        Ui.add(root, restrictedHint, 4);
        openAppInfo = Ui.button(this, getString(R.string.btn_open_appinfo),
                v -> startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()))));
        Ui.add(root, openAppInfo, 4);

        vibInfo = Ui.body(this, "");
        Ui.add(root, vibInfo, 16);

        final MorseSettings settings = new MorseSettings(this);
        Switch master = new Switch(this);
        master.setText(R.string.master_switch);
        master.setChecked(settings.enabled());
        master.setOnCheckedChangeListener((b, checked) -> settings.setEnabled(checked));
        Ui.add(root, master, 16);

        Ui.add(root, Ui.button(this, getString(R.string.btn_rules), v -> open(AppRulesActivity.class)), 24);
        Ui.add(root, Ui.button(this, getString(R.string.btn_log), v -> open(NotificationLogActivity.class)), 0);
        Ui.add(root, Ui.button(this, getString(R.string.btn_settings), v -> open(SettingsActivity.class)), 0);
        Ui.add(root, Ui.button(this, getString(R.string.btn_practice), v -> open(PracticeActivity.class)), 0);

        setContentView(Ui.scroll(this, root));
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean granted = Ui.hasListenerAccess(this);
        accessStatus.setText(granted ? R.string.access_on : R.string.access_off);
        int vis = granted ? android.view.View.GONE : android.view.View.VISIBLE;
        openAccess.setVisibility(vis);
        restrictedHint.setVisibility(vis);
        openAppInfo.setVisibility(vis);

        MorsePlayer player = MorsePlayer.get(this);
        vibInfo.setText(getString(R.string.vib_info,
                getString(player.hasVibrator() ? R.string.yes : R.string.no),
                getString(player.hasAmplitudeControl() ? R.string.yes : R.string.no)));
    }

    private void open(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }
}
