# Cloud Relay Protocol

The relay allows remote access to the SMS Gateway without port forwarding.

## Architecture

```
External Client → Relay Server → WebSocket → Android Device
```

## WebSocket Connection

Device connects to:
```
wss://relay.example.com/ws?deviceId=<device-uuid>
```

## Message Envelope

```json
{
  "type": "request|response|event|auth|ping|pong",
  "id": "uuid",
  "ttl": 1700000030,
  "method": "POST",
  "path": "/api/v1/messages",
  "statusCode": 202,
  "headers": {"Authorization": "Bearer token"},
  "body": "{...json...}",
  "signature": "hmac-sha256-base64"
}
```

## Auth Flow

1. Device connects via WebSocket
2. Device sends `auth` envelope with `Authorization: Bearer <token>` header
3. Relay validates and registers the device

## TTL Enforcement

Requests include a `ttl` field (Unix epoch seconds). The device drops any request where `now > ttl`. Default TTL window: 30 seconds.

## Signature

Response envelopes are signed:
```
signature = HMAC-SHA256(apiToken, "<timestamp>.<body>")
```

## Relay Server (Stub)

A minimal relay server needs to:
1. Accept WebSocket connections from devices (`/ws?deviceId=...`)
2. Expose an HTTP API for external clients
3. Forward HTTP requests as `request` envelopes to the connected device
4. Wait for the `response` envelope and return it to the HTTP client

Reference implementation is out of scope for this app but the protocol above is sufficient to build one.
