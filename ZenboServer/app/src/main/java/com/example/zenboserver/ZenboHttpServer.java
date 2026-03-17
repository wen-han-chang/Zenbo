package com.example.zenboserver;

import android.util.Log;

import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotFace;

import fi.iki.elonen.NanoHTTPD;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP Server 跑在 Zenbo 上，接收遠端控制指令並呼叫 ZenboSDK。
 *
 * API 端點：
 *   GET /speak?text=你好              → Zenbo 說話（搭配 HAPPY 表情）
 *   GET /expression?face=happy        → 設定表情（happy/confident/worried/serious/tired/default）
 *   GET /move?dir=forward             → 移動（forward/backward/left/right）
 *   GET /stop                         → 停止所有動作
 *   GET /ping                         → 測試連線
 */
public class ZenboHttpServer extends NanoHTTPD {

    private static final String TAG = "ZenboHttpServer";
    public static final int PORT = 8080;

    private final RobotAPI robotAPI;

    public ZenboHttpServer(RobotAPI robotAPI) {
        super(PORT);
        this.robotAPI = robotAPI;
    }

    @Override
    public Response serve(NanoHTTPD.IHTTPSession session) {
        String uri = session.getUri();
        Map<String, String> rawParams = session.getParms();
        Map<String, List<String>> params = new HashMap<>();
        for (Map.Entry<String, String> entry : rawParams.entrySet()) {
            params.put(entry.getKey(), java.util.Collections.singletonList(entry.getValue()));
        }
        Log.d(TAG, "Request: " + uri + " params=" + params);

        try {
            switch (uri) {
                case "/ping":
                    return ok("pong");

                case "/speak": {
                    String text = getParam(params, "text", "Hello");
                    robotAPI.robot.setExpression(RobotFace.HAPPY, text);
                    return ok("speak: " + text);
                }

                case "/expression": {
                    String face = getParam(params, "face", "happy");
                    RobotFace rf = parseFace(face);
                    robotAPI.robot.setExpression(rf, null);
                    return ok("expression: " + face);
                }

                case "/move": {
                    String dir = getParam(params, "dir", "forward");
                    handleMove(dir);
                    return ok("move: " + dir);
                }

                case "/stop": {
                    robotAPI.motion.stopMoving();
                    return ok("stop");
                }

                default:
                    return newFixedLengthResponse(
                            Response.Status.NOT_FOUND, "text/plain", "Unknown endpoint: " + uri);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling request", e);
            return newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void handleMove(String dir) {
        switch (dir) {
            case "forward":
                robotAPI.motion.moveBody(0, 0.3f, 1);
                break;
            case "backward":
                robotAPI.motion.moveBody(180, 0.3f, 1);
                break;
            case "left":
                robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.TURN_LEFT);
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .postDelayed(() -> robotAPI.motion.stopMoving(), 1000);
                break;
            case "right":
                robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.TURN_RIGHT);
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .postDelayed(() -> robotAPI.motion.stopMoving(), 1000);
                break;
        }
    }

    private RobotFace parseFace(String name) {
        switch (name.toLowerCase()) {
            case "confident": return RobotFace.CONFIDENT;
            case "worried":   return RobotFace.WORRIED;
            case "angry":     return RobotFace.SERIOUS;
            case "sleepy":    return RobotFace.TIRED;
            case "default":   return RobotFace.DEFAULT;
            default:          return RobotFace.HAPPY;
        }
    }

    private String getParam(Map<String, List<String>> params, String key, String def) {
        List<String> values = params.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : def;
    }

    private Response ok(String msg) {
        return newFixedLengthResponse(Response.Status.OK, "text/plain", msg);
    }
}
