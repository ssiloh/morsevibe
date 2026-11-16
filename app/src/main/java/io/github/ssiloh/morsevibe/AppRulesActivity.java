package io.github.ssiloh.morsevibe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Lists apps and lets the user attach a Morse pattern to each one. */
public class AppRulesActivity extends Activity {
    /** Optional: open the editor for this package right away (used by the notification log). */
    static final String EXTRA_PKG = "pkg";
    static final String EXTRA_LABEL = "label";

    private static final class AppItem {
        String pkg;
        String label;
        Drawable icon;
        boolean seen;
    }

    private final List<AppItem> all = new ArrayList<>();
    private final List<AppItem> shown = new ArrayList<>();
    private Map<String, AppRules.Rule> rules = new HashMap<>();
    private BaseAdapter adapter;
    private TextView status;
    private EditText filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        LinearLayout header = Ui.vertical(this);
        header.addView(Ui.title(this, getString(R.string.title_rules)));
        filter = new EditText(this);
        filter.setHint(R.string.filter_hint);
        filter.setSingleLine(true);
        filter.setInputType(InputType.TYPE_CLASS_TEXT);
        filter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { applyFilter(); }
        });
        Ui.add(header, filter, 8);
        status = Ui.body(this, getString(R.string.loading));
        Ui.add(header, status, 4);
        root.addView(header);

        ListView list = new ListView(this);
        adapter = new Adapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener((p, v, pos, id) -> {
            AppItem it = shown.get(pos);
            showEditor(it.pkg, it.label);
        });
        root.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        Ui.applyInsets(root);
        setContentView(root);

        loadApps();

        String pkg = getIntent().getStringExtra(EXTRA_PKG);
        if (pkg != null) {
            String label = getIntent().getStringExtra(EXTRA_LABEL);
            showEditor(pkg, label == null || label.isEmpty() ? pkg : label);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        rules = AppRules.all(this);
        adapter.notifyDataSetChanged();
    }

    private void loadApps() {
        final PackageManager pm = getPackageManager();
        new Thread(() -> {
            Map<String, AppItem> map = new HashMap<>();
            Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            for (ResolveInfo ri : pm.queryIntentActivities(launcher, 0)) {
                String pkg = ri.activityInfo.packageName;
                if (pkg.equals(getPackageName()) || map.containsKey(pkg)) continue;
                AppItem it = new AppItem();
                it.pkg = pkg;
                it.label = String.valueOf(ri.loadLabel(pm));
                it.icon = ri.loadIcon(pm);
                map.put(pkg, it);
            }
            // Apps that posted notifications but are not launchable, and apps that already have a rule.
            Map<String, String> seen = NotifLog.seenApps(this);
            for (Map.Entry<String, String> e : seen.entrySet()) {
                AppItem it = map.get(e.getKey());
                if (it == null) {
                    it = new AppItem();
                    it.pkg = e.getKey();
                    it.label = e.getValue().isEmpty() ? e.getKey() : e.getValue();
                    map.put(it.pkg, it);
                }
                it.seen = true;
            }
            final Map<String, AppRules.Rule> current = AppRules.all(this);
            for (String pkg : current.keySet()) {
                if (!map.containsKey(pkg)) {
                    AppItem it = new AppItem();
                    it.pkg = pkg;
                    it.label = pkg;
                    map.put(pkg, it);
                }
            }
            final List<AppItem> sorted = new ArrayList<>(map.values());
            Collections.sort(sorted, (a, b) -> {
                boolean ra = current.containsKey(a.pkg), rb = current.containsKey(b.pkg);
                if (ra != rb) return ra ? -1 : 1;
                if (a.seen != b.seen) return a.seen ? -1 : 1;
                return a.label.compareToIgnoreCase(b.label);
            });
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                all.clear();
                all.addAll(sorted);
                status.setVisibility(View.GONE);
                applyFilter();
            });
        }).start();
    }

    private void applyFilter() {
        String q = filter.getText().toString().trim().toLowerCase(Locale.ROOT);
        shown.clear();
        for (AppItem it : all) {
            if (q.isEmpty() || it.label.toLowerCase(Locale.ROOT).contains(q)
                    || it.pkg.toLowerCase(Locale.ROOT).contains(q)) {
                shown.add(it);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showEditor(final String pkg, String label) {
        final AppRules.Rule existing = AppRules.get(this, pkg);
        final MorsePlayer player = MorsePlayer.get(this);

        LinearLayout box = Ui.vertical(this);
        final Switch enabled = new Switch(this);
        enabled.setText(R.string.rule_enabled);
        enabled.setChecked(existing == null || existing.enabled);
        box.addView(enabled);

        final EditText input = new EditText(this);
        input.setHint(R.string.rule_input_hint);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (existing != null) input.setText(existing.code);
        Ui.add(box, input, 8);

        final TextView preview = Ui.body(this, "");
        Ui.add(box, preview, 4);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                preview.setText(getString(R.string.rule_preview, Morse.toCode(s.toString())));
            }
        });
        preview.setText(getString(R.string.rule_preview, Morse.toCode(input.getText().toString())));

        Ui.add(box, Ui.body(this, getString(R.string.rule_help)), 8);
        Ui.add(box, Ui.button(this, getString(R.string.btn_test), v -> {
            if (!player.hasVibrator()) {
                Toast.makeText(this, R.string.no_vibrator, Toast.LENGTH_SHORT).show();
                return;
            }
            player.playNow(Morse.toCode(input.getText().toString()), true);
        }), 8);

        ScrollView sv = new ScrollView(this);
        sv.addView(box);
        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(label)
                .setView(sv)
                .setPositiveButton(R.string.btn_save, (d, w) -> {
                    String text = input.getText().toString().trim();
                    if (Morse.toCode(text).isEmpty()) {
                        AppRules.remove(this, pkg);
                    } else {
                        AppRules.put(this, new AppRules.Rule(pkg, enabled.isChecked(), text));
                    }
                    rules = AppRules.all(this);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton(R.string.btn_cancel, null);
        if (existing != null) {
            b.setNeutralButton(R.string.btn_delete, (d, w) -> {
                AppRules.remove(this, pkg);
                rules = AppRules.all(this);
                adapter.notifyDataSetChanged();
            });
        }
        AlertDialog dialog = b.create();
        dialog.setOnDismissListener(d -> player.stop());
        dialog.show();
    }

    private final class Adapter extends BaseAdapter {
        @Override public int getCount() { return shown.size(); }
        @Override public Object getItem(int i) { return shown.get(i); }
        @Override public long getItemId(int i) { return i; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout row;
            ImageView icon;
            TextView name;
            TextView summary;
            if (convertView == null) {
                row = new LinearLayout(AppRulesActivity.this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                int p = Ui.dp(AppRulesActivity.this, 16);
                row.setPadding(p, Ui.dp(AppRulesActivity.this, 8), p, Ui.dp(AppRulesActivity.this, 8));
                icon = new ImageView(AppRulesActivity.this);
                int size = Ui.dp(AppRulesActivity.this, 40);
                row.addView(icon, new LinearLayout.LayoutParams(size, size));
                LinearLayout texts = new LinearLayout(AppRulesActivity.this);
                texts.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tp.leftMargin = Ui.dp(AppRulesActivity.this, 12);
                name = new TextView(AppRulesActivity.this);
                name.setTextSize(16);
                summary = Ui.body(AppRulesActivity.this, "");
                texts.addView(name);
                texts.addView(summary);
                row.addView(texts, tp);
                row.setTag(new TextView[]{name, summary});
            } else {
                row = (LinearLayout) convertView;
                TextView[] tv = (TextView[]) row.getTag();
                name = tv[0];
                summary = tv[1];
                icon = (ImageView) row.getChildAt(0);
            }
            AppItem it = shown.get(position);
            name.setText(it.label);
            icon.setImageDrawable(it.icon);
            AppRules.Rule r = rules.get(it.pkg);
            if (r == null) {
                summary.setText(R.string.rule_none);
            } else {
                summary.setText(getString(R.string.rule_summary, Morse.toCode(r.code),
                        getString(r.enabled ? R.string.state_on : R.string.state_off)));
            }
            return row;
        }
    }
}
