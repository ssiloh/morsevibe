package io.github.ssiloh.morsevibe;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private MorseSettings s;
    private MorsePlayer player;
    private LinearLayout lengthGroup;
    private LinearLayout intensityGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        s = new MorseSettings(this);
        player = MorsePlayer.get(this);
        final boolean ampOk = player.hasAmplitudeControl();
        final String ms = getString(R.string.unit_ms);

        LinearLayout root = Ui.vertical(this);
        root.addView(Ui.title(this, getString(R.string.title_settings)));

        // --- how to tell dot from dash -------------------------------------------------------
        Ui.add(root, Ui.section(this, getString(R.string.sec_mode)), 20);
        RadioGroup group = new RadioGroup(this);
        RadioButton byLength = new RadioButton(this);
        byLength.setId(View.generateViewId());
        byLength.setText(R.string.mode_length);
        RadioButton byIntensity = new RadioButton(this);
        byIntensity.setId(View.generateViewId());
        byIntensity.setText(R.string.mode_intensity);
        byIntensity.setEnabled(ampOk);
        group.addView(byLength);
        group.addView(byIntensity);
        boolean intensityMode = ampOk && s.mode() == MorseSettings.MODE_INTENSITY;
        group.check(intensityMode ? byIntensity.getId() : byLength.getId());
        Ui.add(root, group, 4);
        if (!ampOk) {
            Ui.add(root, Ui.body(this, getString(R.string.mode_intensity_unsupported)), 4);
        }

        // --- length mode ---------------------------------------------------------------------
        lengthGroup = Ui.vertical(this);
        lengthGroup.setPadding(0, 0, 0, 0);
        Ui.add(lengthGroup, Ui.section(this, getString(R.string.sec_length)), 16);
        Ui.add(lengthGroup, Ui.seekRow(this, getString(R.string.dot_len), 30, 600, s.dotMs(), ms, s::setDotMs), 8);
        Ui.add(lengthGroup, Ui.seekRow(this, getString(R.string.dash_len), 60, 1500, s.dashMs(), ms, s::setDashMs), 8);
        Ui.add(lengthGroup, Ui.seekRow(this, getString(R.string.intensity), 1, 255, s.intensity(), "/255", s::setIntensity), 8);
        root.addView(lengthGroup);

        // --- intensity mode ------------------------------------------------------------------
        intensityGroup = Ui.vertical(this);
        intensityGroup.setPadding(0, 0, 0, 0);
        Ui.add(intensityGroup, Ui.section(this, getString(R.string.sec_intensity)), 16);
        Ui.add(intensityGroup, Ui.seekRow(this, getString(R.string.dot_int), 1, 255, s.dotAmp(), "/255", s::setDotAmp), 8);
        Ui.add(intensityGroup, Ui.seekRow(this, getString(R.string.dash_int), 1, 255, s.dashAmp(), "/255", s::setDashAmp), 8);
        Ui.add(intensityGroup, Ui.seekRow(this, getString(R.string.unit_len), 40, 600, s.unitMs(), ms, s::setUnitMs), 8);
        root.addView(intensityGroup);

        showMode(intensityMode);
        group.setOnCheckedChangeListener((g, checkedId) -> {
            boolean intensity = checkedId == byIntensity.getId();
            s.setMode(intensity ? MorseSettings.MODE_INTENSITY : MorseSettings.MODE_LENGTH);
            showMode(intensity);
        });

        // --- pauses --------------------------------------------------------------------------
        Ui.add(root, Ui.section(this, getString(R.string.sec_gaps)), 20);
        Ui.add(root, Ui.seekRow(this, getString(R.string.symbol_gap), 30, 600, s.symbolGap(), ms, s::setSymbolGap), 8);
        Ui.add(root, Ui.seekRow(this, getString(R.string.letter_gap), 100, 1500, s.letterGap(), ms, s::setLetterGap), 8);
        Ui.add(root, Ui.seekRow(this, getString(R.string.word_gap), 200, 3000, s.wordGap(), ms, s::setWordGap), 8);

        // --- notifications -------------------------------------------------------------------
        Ui.add(root, Ui.section(this, getString(R.string.sec_misc)), 20);
        Switch ongoing = new Switch(this);
        ongoing.setText(R.string.ignore_ongoing);
        ongoing.setChecked(s.ignoreOngoing());
        ongoing.setOnCheckedChangeListener((CompoundButton b, boolean on) -> s.setIgnoreOngoing(on));
        Ui.add(root, ongoing, 8);
        Switch log = new Switch(this);
        log.setText(R.string.log_enabled);
        log.setChecked(s.logEnabled());
        log.setOnCheckedChangeListener((CompoundButton b, boolean on) -> s.setLogEnabled(on));
        Ui.add(root, log, 8);

        // --- test ----------------------------------------------------------------------------
        Ui.add(root, Ui.section(this, getString(R.string.sec_test)), 20);
        final EditText test = new EditText(this);
        test.setHint(R.string.test_hint);
        test.setText("SOS");
        test.setSingleLine(true);
        test.setInputType(InputType.TYPE_CLASS_TEXT);
        Ui.add(root, test, 4);
        Ui.add(root, Ui.button(this, getString(R.string.btn_test_vibe), v -> {
            if (!player.hasVibrator()) {
                Toast.makeText(this, R.string.no_vibrator, Toast.LENGTH_SHORT).show();
                return;
            }
            player.playNow(Morse.toCode(test.getText().toString()), true);
        }), 4);
        Ui.add(root, Ui.button(this, getString(R.string.btn_reset), v -> {
            s.resetVibration();
            recreate();
        }), 16);

        setContentView(Ui.scroll(this, root));
    }

    private void showMode(boolean intensity) {
        intensityGroup.setVisibility(intensity ? View.VISIBLE : View.GONE);
        lengthGroup.setVisibility(intensity ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.stop();
    }
}
