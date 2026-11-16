package io.github.ssiloh.morsevibe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Shows notifications MorseVibe has seen; tapping one opens the rule editor for that app. */
public class NotificationLogActivity extends Activity {
    private final List<NotifLog.Entry> entries = new ArrayList<>();
    private BaseAdapter adapter;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        LinearLayout header = Ui.vertical(this);
        header.addView(Ui.title(this, getString(R.string.title_log)));
        Ui.add(header, Ui.body(this, getString(R.string.log_hint)), 4);
        Ui.add(header, Ui.button(this, getString(R.string.btn_clear), v -> {
            NotifLog.clear(this);
            reload();
        }), 8);
        empty = Ui.body(this, getString(R.string.log_empty));
        Ui.add(header, empty, 8);
        root.addView(header);

        ListView list = new ListView(this);
        adapter = new BaseAdapter() {
            @Override public int getCount() { return entries.size(); }
            @Override public Object getItem(int i) { return entries.get(i); }
            @Override public long getItemId(int i) { return i; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv;
                if (convertView == null) {
                    tv = new TextView(NotificationLogActivity.this);
                    int p = Ui.dp(NotificationLogActivity.this, 16);
                    tv.setPadding(p, Ui.dp(NotificationLogActivity.this, 10), p, Ui.dp(NotificationLogActivity.this, 10));
                    tv.setTextSize(14);
                } else {
                    tv = (TextView) convertView;
                }
                NotifLog.Entry e = entries.get(position);
                String when = DateFormat.format("MM-dd HH:mm:ss", e.time).toString();
                String line = (e.matched ? "♪ " : "") + e.label + "  ·  " + when;
                if (!e.title.isEmpty()) line += "\n" + e.title;
                tv.setText(line);
                return tv;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((p, v, pos, id) -> {
            NotifLog.Entry e = entries.get(pos);
            Intent i = new Intent(this, AppRulesActivity.class);
            i.putExtra(AppRulesActivity.EXTRA_PKG, e.pkg);
            i.putExtra(AppRulesActivity.EXTRA_LABEL, e.label);
            startActivity(i);
        });
        root.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        Ui.applyInsets(root);
        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        entries.clear();
        entries.addAll(NotifLog.list(this));
        empty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }
}
