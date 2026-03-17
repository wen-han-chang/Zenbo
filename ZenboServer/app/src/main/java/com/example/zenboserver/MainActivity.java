package com.example.zenboserver;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

/**
 * 最簡 Activity：啟動 Service 並顯示 Zenbo 的 IP 給遠端 Client 輸入用。
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 啟動 foreground service
        Intent serviceIntent = new Intent(this, ZenboServerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // 顯示本機 IP
        TextView tvIp = findViewById(R.id.tvIp);
        String ip = getWifiIpAddress();
        tvIp.setText("Server IP: " + ip + "\nPort: " + ZenboHttpServer.PORT);
    }

    private String getWifiIpAddress() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext()
                    .getSystemService(WIFI_SERVICE);
            if (wm == null) return "unknown";
            WifiInfo info = wm.getConnectionInfo();
            int ipInt = info.getIpAddress();
            // 處理 byte order
            if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                ipInt = Integer.reverseBytes(ipInt);
            }
            byte[] ipBytes = new byte[]{
                    (byte)(ipInt >> 24 & 0xff),
                    (byte)(ipInt >> 16 & 0xff),
                    (byte)(ipInt >>  8 & 0xff),
                    (byte)(ipInt       & 0xff)
            };
            return InetAddress.getByAddress(ipBytes).getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
