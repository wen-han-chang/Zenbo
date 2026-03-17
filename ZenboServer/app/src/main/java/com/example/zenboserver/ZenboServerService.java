package com.example.zenboserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;

import java.io.IOException;

/**
 * Foreground Service：持續在背景跑 HTTP Server，確保關閉 Activity 後仍可接收指令。
 */
public class ZenboServerService extends Service {

    private static final String TAG = "ZenboServerService";
    private static final String CHANNEL_ID = "zenbo_server_channel";

    private ZenboHttpServer httpServer;
    private RobotAPI robotAPI;

    public static final RobotCallback robotCallback = new RobotCallback() {};

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // 初始化 RobotAPI（在 Service context 中）
        robotAPI = new RobotAPI(getApplicationContext(), robotCallback);

        // 啟動 HTTP Server
        httpServer = new ZenboHttpServer(robotAPI);
        try {
            httpServer.start();
            Log.i(TAG, "HTTP Server started on port " + ZenboHttpServer.PORT);
        } catch (IOException e) {
            Log.e(TAG, "Failed to start HTTP server", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Zenbo Server")
                .setContentText("HTTP Server running on port " + ZenboHttpServer.PORT)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);

        // 如果被系統殺掉，自動重啟
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (httpServer != null) {
            httpServer.stop();
            Log.i(TAG, "HTTP Server stopped");
        }
        if (robotAPI != null) {
            robotAPI.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Zenbo Server",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Zenbo HTTP Server background service");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
