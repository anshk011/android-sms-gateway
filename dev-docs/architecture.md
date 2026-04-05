# Architecture Notes

## Package Structure

```
com.example.smsgateway
├── core/           Constants, Crypto, NetworkUtils
├── data/           Room DB, DAOs, entities, SettingsDataStore
├── sms/            SmsSender, SmsReceiver, delivery receivers, SimManager
├── webhook/        WebhookPayload, WebhookWorker
├── net/
│   ├── local/      Ktor server, routing, API models
│   └── relay/      WebSocket relay client, protocol envelopes
├── service/        GatewayService (foreground), BootReceiver
└── ui/             Compose screens, MainViewModel, NavHost, theme
```

## Data Flow — Outbound SMS

```
API POST /messages
  → SmsSender.send()
  → SmsManager (Android)
  → SmsSentReceiver (broadcast)
  → DB update → WebhookWorker (SENT event)
  → SmsDeliveredReceiver (broadcast)
  → DB update → WebhookWorker (DELIVERED event)
```

## Data Flow — Inbound SMS

```
SmsReceiver (broadcast)
  → Parse PDUs
  → DB insert
  → WebhookWorker (INCOMING_SMS event)
```

## Data Flow — Cloud Relay

```
RelayClient (WebSocket)
  → Receive request envelope
  → TTL check + signature verify
  → Route to same handlers as local API
  → Send response envelope (signed)
```

## Building

```bash
./gradlew assembleDebug      # debug APK
./gradlew assembleRelease    # release APK (needs signing config)
./gradlew test               # unit tests
```

## Key Dependencies

- **Ktor 2.3** — embedded HTTP server (CIO engine) + WebSocket client
- **Room 2.6** — message log + webhook config persistence
- **DataStore** — lightweight settings (token, port, relay URL)
- **WorkManager** — reliable webhook retry with backoff
- **Kotlinx Serialization** — JSON for API + webhook payloads
