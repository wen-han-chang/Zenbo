# Zenbo Remote Control

透過 WiFi 用手機／平板遠端控制 ASUS Zenbo 機器人，包含語音、表情與移動控制。

## 架構

```
ZenboClient (手機/平板)
        │  HTTP over WiFi
        ▼
ZenboServer (Zenbo 機器人)
        │  Zenbo SDK
        ▼
    Zenbo 硬體
```

| 專案 | 裝置 | 說明 |
|------|------|------|
| `ZenboServer` | Zenbo 機器人 | 跑 HTTP Server（port 8080），接收指令並呼叫 Zenbo SDK |
| `ZenboClient` | 手機 / 平板 | 控制介面並透過 HTTP 送出指令 |

## 功能

- **語音**：讓 Zenbo 說任意文字（搭配 HAPPY 表情）
- **表情**：開心 / 自信 / 擔心 / 生氣 / 睏 / 預設
- **移動**：前進 / 後退 / 左轉 / 右轉 / 停止
- **Ping**：測試連線狀態

## 環境需求

- Android Studio Hedgehog 以上
- Zenbo SDK（`ZenboSDK.jar` 已內含於 `ZenboServer/app/libs/`）
- Zenbo 與控制端需在同一 WiFi 網段

## 安裝方式

### ZenboServer（燒入 Zenbo）

1. 開啟 Zenbo 的開發者模式與 USB 偵錯
2. USB 接上電腦
3. Android Studio 開啟 `ZenboServer` 專案，選擇 asus Zenbo 裝置後按 Run

### ZenboClient（燒入手機）

1. Android Studio 開啟 `ZenboClient` 專案，選擇目標裝置後按 Run

## 使用方式

1. 啟動 ZenboServer，Zenbo 螢幕會顯示 IP 與 Port（預設 8080）
2. 在 ZenboClient 的 IP 欄輸入 Zenbo 顯示的 IP，按「連線」
3. 連線成功後即可控制語音、表情與移動

## HTTP API

| 路徑 | 參數 | 說明 |
|------|------|------|
| `GET /ping` | — | 測試連線，回傳 `pong` |
| `GET /speak` | `text=你好` | 讓 Zenbo 說話 |
| `GET /expression` | `face=happy` | 設定表情 |
| `GET /move` | `dir=forward` | 移動（forward/backward/left/right） |
| `GET /stop` | — | 停止移動 |

表情可選值：`happy` / `confident` / `worried` / `angry` / `sleepy` / `default`

## 專案結構

```
Zenbo/
├── ZenboServer/          # Zenbo 機器人端 App
│   └── app/src/main/java/com/example/zenboserver/
│       ├── MainActivity.java          # 顯示 IP
│       ├── ZenboServerService.java    # Foreground Service
│       └── ZenboHttpServer.java       # HTTP Server 與指令處理
└── ZenboClient/          # 控制端 App
    └── app/src/main/java/com/example/zenboclient/
        ├── MainActivity.java          # 控制介面
        └── ZenboApi.java              # HTTP 呼叫封裝
```
