package io.github.ssiloh.morsevibe;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class PracticeActivity extends Activity {
    private final Random random = new Random();
    private MorsePlayer player;
    private TextView score;
    private TextView result;
    private EditText answer;
    private CheckBox lettersOnly;
    private TextView chart;

    private char current;
    private boolean answered;
    private int correct;
    private int total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        player = MorsePlayer.get(this);

        LinearLayout root = Ui.vertical(this);
        root.addView(Ui.title(this, getString(R.string.title_practice)));
        Ui.add(root, Ui.body(this, getString(R.string.practice_help)), 4);

        lettersOnly = new CheckBox(this);
        lettersOnly.setText(R.string.letters_only);
        Ui.add(root, lettersOnly, 12);

        Ui.add(root, Ui.button(this, getString(R.string.btn_play_next), v -> playNext()), 8);
        Ui.add(root, Ui.button(this, getString(R.string.btn_replay), v -> replay()), 0);

        answer = new EditText(this);
        answer.setHint(R.string.answer_hint);
        answer.setSingleLine(true);
        answer.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        Ui.add(root, answer, 12);
        Ui.add(root, Ui.button(this, getString(R.string.btn_check), v -> check()), 4);

        result = Ui.section(this, "");
        Ui.add(root, result, 12);
        score = Ui.body(this, getString(R.string.score, 0, 0));
        Ui.add(root, score, 4);

        Ui.add(root, Ui.button(this, getString(R.string.btn_chart), v ->
                chart.setVisibility(chart.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE)), 20);
        chart = new TextView(this);
        chart.setTypeface(Typeface.MONOSPACE);
        chart.setTextSize(13);
        chart.setText(Morse.chart());
        chart.setVisibility(View.GONE);
        Ui.add(root, chart, 8);

        setContentView(Ui.scroll(this, root));
    }

    private void playNext() {
        if (!player.hasVibrator()) {
            Toast.makeText(this, R.string.no_vibrator, Toast.LENGTH_SHORT).show();
            return;
        }
        String pool = lettersOnly.isChecked() ? Morse.LETTERS : Morse.LETTERS + Morse.DIGITS;
        current = pool.charAt(random.nextInt(pool.length()));
        answered = false;
        result.setText("");
        answer.setText("");
        replay();
    }

    private void replay() {
        if (current == 0) {
            result.setText(R.string.result_none);
            return;
        }
        player.playNow(Morse.codeFor(current), true);
    }

    private void check() {
        if (current == 0) {
            result.setText(R.string.result_none);
            return;
        }
        String typed = answer.getText().toString().trim();
        if (typed.isEmpty()) return;
        boolean ok = Character.toUpperCase(typed.charAt(0)) == current;
        if (!answered) {
            answered = true;
            total++;
            if (ok) correct++;
        }
        String code = Morse.codeFor(current);
        result.setText(ok
                ? getString(R.string.result_correct, current + " " + code)
                : getString(R.string.result_wrong, String.valueOf(current), code));
        score.setText(getString(R.string.score, correct, total));
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.stop();
    }
}
