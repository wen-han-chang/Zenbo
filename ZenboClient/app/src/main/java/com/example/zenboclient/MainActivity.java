package com.example.zenboclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final ZenboApi api = new ZenboApi();
    private EditText etIp, etSpeakText;
    private TextView tvLog;
    private ScrollView scrollLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etIp       = findViewById(R.id.etIp);
        etSpeakText= findViewById(R.id.etSpeakText);
        tvLog      = findViewById(R.id.tvLog);
        scrollLog  = findViewById(R.id.scrollLog);

        // 連線 / Ping
        findViewById(R.id.btnConnect).setOnClickListener(v -> {
            String ip = etIp.getText().toString().trim();
            if (ip.isEmpty()) { toast("請輸入 IP"); return; }
            api.setIp(ip);
            api.ping((ok, msg) -> log(ok ? "連線成功：" + msg : "Ping 失敗，請確認 IP"));
        });

        // 說話
        findViewById(R.id.btnSpeak).setOnClickListener(v -> {
            String text = etSpeakText.getText().toString().trim();
            if (text.isEmpty()) { toast("請輸入說話內容"); return; }
            send(() -> api.speak(text, (ok, msg) -> log("說話：" + (ok ? text : "失敗"))));
        });

        // 表情
        setExprBtn(R.id.btnHappy,     "happy");
        setExprBtn(R.id.btnConfident, "confident");
        setExprBtn(R.id.btnWorried,   "worried");
        setExprBtn(R.id.btnAngry,     "angry");
        setExprBtn(R.id.btnSleepy,    "sleepy");
        setExprBtn(R.id.btnDefault,   "default");

        // 移動
        setMoveBtn(R.id.btnForward,  "forward");
        setMoveBtn(R.id.btnBackward, "backward");
        setMoveBtn(R.id.btnLeft,     "left");
        setMoveBtn(R.id.btnRight,    "right");

        // 停止
        findViewById(R.id.btnStop).setOnClickListener(v ->
                send(() -> api.stop((ok, msg) -> log("停止：" + (ok ? "OK" : "失敗")))));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void setExprBtn(int id, String face) {
        findViewById(id).setOnClickListener(v ->
                send(() -> api.setExpression(face, (ok, msg) -> log("表情 " + face + "：" + (ok ? "OK" : "失敗")))));
    }

    private void setMoveBtn(int id, String dir) {
        ((Button) findViewById(id)).setOnClickListener(v ->
                send(() -> api.move(dir, (ok, msg) -> log("移動 " + dir + "：" + (ok ? "OK" : "失敗")))));
    }

    private void send(Runnable r) {
        if (!api.isConfigured()) { toast("請先連線"); return; }
        r.run();
    }

    private void log(String msg) {
        String cur = tvLog.getText().toString();
        String[] lines = cur.split("\n");
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, lines.length - 59);
        for (int i = start; i < lines.length; i++) sb.append(lines[i]).append("\n");
        sb.append("> ").append(msg);
        tvLog.setText(sb.toString());
        scrollLog.post(() -> scrollLog.fullScroll(View.FOCUS_DOWN));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
