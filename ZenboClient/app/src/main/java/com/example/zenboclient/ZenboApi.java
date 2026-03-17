package com.example.zenboclient;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 封裝所有對 ZenboServer 的 HTTP 呼叫。
 * 所有請求在背景執行緒送出，callback 在主執行緒回傳。
 */
public class ZenboApi {

    private static final String TAG = "ZenboApi";
    private static final int TIMEOUT_MS = 3000;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String baseUrl;

    public interface Callback {
        void onResult(boolean success, String message);
    }

    public void setIp(String ip) {
        this.baseUrl = "http://" + ip + ":8080";
    }

    public boolean isConfigured() {
        return baseUrl != null;
    }

    /** 測試連線 */
    public void ping(Callback cb) {
        get("/ping", cb);
    }

    /** 讓 Zenbo 說話（自動帶 HAPPY 表情） */
    public void speak(String text, Callback cb) {
        get("/speak?text=" + encode(text), cb);
    }

    /** 設定表情：happy / confident / worried / angry / sleepy / default */
    public void setExpression(String face, Callback cb) {
        get("/expression?face=" + face, cb);
    }

    /** 移動：forward / backward / left / right */
    public void move(String direction, Callback cb) {
        get("/move?dir=" + direction, cb);
    }

    /** 停止 */
    public void stop(Callback cb) {
        get("/stop", cb);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void get(String path, Callback cb) {
        if (baseUrl == null) {
            if (cb != null) cb.onResult(false, "請先設定 Zenbo IP");
            return;
        }
        String url = baseUrl + path;
        executor.execute(() -> {
            String result = httpGet(url);
            boolean ok = result != null;
            String msg = ok ? result : "連線失敗";
            if (cb != null) mainHandler.post(() -> cb.onResult(ok, msg));
        });
    }

    private String httpGet(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.connect();

            int code = conn.getResponseCode();
            if (code == 200) {
                byte[] buf = conn.getInputStream().readAllBytes();
                return new String(buf);
            }
            return null;
        } catch (IOException e) {
            Log.e(TAG, "HTTP error: " + urlStr, e);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String encode(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text;
        }
    }
}
