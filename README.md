<div align="center">

# 📱 SMS Gateway for Android™

**Turn your Android phone into a programmable SMS gateway.**

Expose a local REST API to send SMS, receive inbound messages via webhooks, and optionally relay through a cloud server — all running on-device.

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026)-brightgreen.svg)](https://developer.android.com/about/versions/oreo)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Ktor](https://img.shields.io/badge/Ktor-2.3-087CFA.svg)](https://ktor.io)
[![Build](https://img.shields.io/github/actions/workflow/status/your-org/android-sms-gateway/build.yml?branch=main)](https://github.com/your-org/android-sms-gateway/actions)

</div>

---

## Overview

SMS Gateway for Android™ runs an embedded HTTP server directly on your device. Any application on your local network — or remotely via the cloud relay — can send SMS messages, query delivery status, and receive inbound SMS forwarded as signed webhook payloads.

No third-party SMS service required. No SIM card locked to a cloud provider. Your phone, your number, your infrastructure.

---

## Features

| | |
|---|---|
| 🌐 **Local HTTP API** | Embedded Ktor server binds to your LAN on a configurable port |
| 📤 **Send SMS** | Single and multipart messages with per-request SIM selection |
| 📥 **Receive SMS** | Inbound messages forwarded to your webhook endpoint in real time |
| 🔔 **Delivery Tracking** | `SENT` and `DELIVERED` states tracked via Android broadcast receivers |
| 🔗 **Webhook Events** | HMAC-SHA256 signed payloads for `incoming_sms`, `message_sent`, `message_delivered` |
| ☁️ **Cloud Relay** | WebSocket relay mode for remote access without port forwarding |
| 🔑 **API Key Auth** | Bearer token authentication auto-generated on first run |
| 📶 **Multi-SIM** | List subscriptions, set a default, or override per request |
| 📋 **Message Logs** | Persistent log of all inbound/outbound messages with full state history |
| 🔋 **Background Stable** | Foreground service + WorkManager keeps the gateway alive |

---

## Requirements

- Android **8.0+** (API 26)
- A physical device with an active SIM card
- Android Studio **Hedgehog** or later (to build from source)

---

## Getting Started

### Build from source

```bash
git clone https://github.com/your-org/android-sms-gateway.git
cd android-sms-gateway
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

Install via ADB:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### First run

1. Open the app and grant all requested permissions
2. Tap **Start Server** on the Home screen
3. Note the displayed IP address and port (default: `8080`)
4. Copy your API token from the **Auth** screen

> **Tip:** Disable battery optimization for the app to ensure reliable background operation.  
> Settings → Apps → SMS Gateway → Battery → **Unrestricted**

---

## API Usage

Base URL: `http://<device-ip>:8080/api/v1`

All endpoints except `/health` require:

```
Authorization: Bearer <your_api_token>
```

### Send an SMS

```bash
curl -X POST http://192.168.1.100:8080/api/v1/messages \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "+1234567890",
    "text": "Hello from SMS Gateway!",
    "simSlot": 0
  }'
```

```json
{
  "messageId": "550e8400-e29b-41d4-a716-446655440000",
  "state": "SENDING"
}
```

### Check message status

```bash
curl http://192.168.1.100:8080/api/v1/messages/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "direction": "OUTBOUND",
  "phoneNumber": "+1234567890",
  "body": "Hello from SMS Gateway!",
  "state": "DELIVERED",
  "partsTotal": 1,
  "partsSent": 1,
  "partsDelivered": 1,
  "createdAt": 1700000000000,
  "updatedAt": 1700000005000
}
```

Message states: `QUEUED` → `SENDING` → `SENT` → `DELIVERED` | `FAILED`

### List SIM cards

```bash
curl http://192.168.1.100:8080/api/v1/sims \
  -H "Authorization: Bearer YOUR_TOKEN"
```

```json
[
  {
    "subscriptionId": 1,
    "slotIndex": 0,
    "displayName": "Personal",
    "carrierName": "Carrier Name",
    "number": "+1234567890"
  }
]
```

### Health check (no auth)

```bash
curl http://192.168.1.100:8080/api/v1/health
```

```json
{ "status": "ok", "version": "1.0.0", "deviceId": "uuid" }
```

### Full API reference → [`docs/api.md`](docs/api.md)

---

## Webhooks

Configure a webhook endpoint to receive real-time events for inbound and outbound messages.

### Configure via API

```bash
curl -X PUT http://192.168.1.100:8080/api/v1/webhooks \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://your-server.com/webhook",
    "secret": "your-webhook-secret",
    "enabled": true,
    "onIncomingSms": true,
    "onMessageSent": true,
    "onMessageDelivered": true
  }'
```

### Incoming SMS payload

```json
{
  "event": "incoming_sms",
  "messageId": "uuid",
  "direction": "INBOUND",
  "phoneNumber": "+1234567890",
  "body": "Hello!",
  "state": "DELIVERED",
  "subscriptionId": 1,
  "timestamp": 1700000000000
}
```

### Signature verification

Every webhook request includes these headers:

| Header | Description |
|---|---|
| `X-SMSGateway-Event` | Event type (`incoming_sms`, `message_sent`, `message_delivered`) |
| `X-SMSGateway-Timestamp` | Unix timestamp in seconds |
| `X-SMSGateway-Signature` | `HMAC-SHA256(secret, "<timestamp>.<body>")` base64-encoded |

Verify in Python:

```python
import hmac, hashlib, base64

def verify_webhook(secret: str, timestamp: str, body: str, signature: str) -> bool:
    payload = f"{timestamp}.{body}"
    expected = base64.b64encode(
        hmac.new(secret.encode(), payload.encode(), hashlib.sha256).digest()
    ).decode()
    return hmac.compare_digest(expected, signature)
```

Failed deliveries are retried up to **5 times** with exponential backoff (30s base) via WorkManager.

### Full webhook reference → [`docs/webhooks.md`](docs/webhooks.md)

---

## Cloud Relay

The cloud relay lets you access the gateway remotely without opening ports or configuring a VPN.

```
External Client ──► Relay Server ──► WebSocket ──► Android Device
```

The device maintains an outbound WebSocket connection to your relay server. External API calls are forwarded as signed JSON envelopes; the device processes them identically to local API requests and returns a signed response.

**Security properties:**
- Bearer token authentication on WebSocket connect
- HMAC-SHA256 signed response envelopes
- Per-request TTL (default 30s) to prevent replay attacks

Configure in the app under the **Cloud** tab: enter your relay URL and toggle **Enable Cloud Relay**.

### Relay protocol reference → [`docs/relay.md`](docs/relay.md)

---

## Permissions

| Permission | Required for |
|---|---|
| `SEND_SMS` | Sending outbound messages |
| `RECEIVE_SMS` | Receiving inbound messages |
| `READ_SMS` | Reading message state |
| `READ_PHONE_STATE` | Listing SIM subscriptions |
| `READ_PHONE_NUMBERS` | Displaying SIM phone numbers |
| `INTERNET` | HTTP server, webhook delivery, relay |
| `FOREGROUND_SERVICE` | Persistent background operation |
| `RECEIVE_BOOT_COMPLETED` | Auto-start server after reboot |
| `POST_NOTIFICATIONS` | Foreground service notification |

---

## Project Structure

```
android-sms-gateway/
├── app/
│   └── src/main/java/com/example/smsgateway/
│       ├── core/           # Constants, Crypto (HMAC), NetworkUtils
│       ├── data/           # Room DB, DAOs, entities, SettingsDataStore
│       ├── sms/            # SmsSender, SmsReceiver, delivery receivers, SimManager
│       ├── webhook/        # WebhookPayload, WebhookWorker (WorkManager)
│       ├── net/
│       │   ├── local/      # Ktor server, routing, API models
│       │   └── relay/      # WebSocket relay client, protocol envelopes
│       ├── service/        # GatewayService (foreground), BootReceiver
│       └── ui/             # Compose screens, MainViewModel, NavHost
├── docs/                   # API reference, webhook schema, relay protocol
├── dev-docs/               # Architecture notes, build instructions
├── user-docs/              # Setup guides, battery optimization per OEM
└── .github/workflows/      # CI: build APK + run unit tests
```

---

## Tech Stack

| Layer | Library |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM, StateFlow, ViewModel |
| HTTP Server | Ktor 2.3 (CIO engine) |
| HTTP Client | Ktor + OkHttp |
| WebSocket | Ktor WebSocket client |
| Database | Room 2.6 |
| Settings | DataStore Preferences |
| Background | WorkManager 2.9 |
| Serialization | Kotlinx Serialization |

---

## Battery Optimization

For reliable 24/7 operation, exclude the app from battery optimization.

**Stock Android / Pixel:**  
Settings → Apps → SMS Gateway → Battery → **Unrestricted**

OEM-specific instructions (Samsung, Xiaomi, Huawei, OnePlus) → [`user-docs/battery.md`](user-docs/battery.md)

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m 'Add my feature'`
4. Push and open a Pull Request

Please run `./gradlew test` before submitting.

---

## License

```
Copyright 2024 SMS Gateway for Android Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

See [LICENSE](LICENSE) for the full text.
