# API Reference

Base URL: `http://<device-ip>:<port>/api/v1`

All endpoints except `/health` require:
```
Authorization: Bearer <api_token>
```

---

## GET /health

Public. Returns server status.

**Response 200:**
```json
{"status": "ok", "version": "1.0.0", "deviceId": "uuid"}
```

---

## POST /messages

Send an SMS.

**Request:**
```json
{
  "to": "+1234567890",
  "text": "Your message here",
  "simSlot": 0,
  "subscriptionId": null,
  "clientRef": "optional-ref"
}
```

**Response 202:**
```json
{"messageId": "uuid", "state": "SENDING"}
```

---

## GET /messages/{id}

Get message status by ID.

**Response 200:**
```json
{
  "id": "uuid",
  "direction": "OUTBOUND",
  "phoneNumber": "+1234567890",
  "body": "Your message",
  "state": "DELIVERED",
  "partsTotal": 1,
  "partsSent": 1,
  "partsDelivered": 1,
  "createdAt": 1700000000000,
  "updatedAt": 1700000001000
}
```

States: `QUEUED` → `SENDING` → `SENT` → `DELIVERED` | `FAILED`

---

## GET /messages?limit=50

List recent messages (max 200).

---

## GET /sims

List active SIM subscriptions.

**Response 200:**
```json
[
  {
    "subscriptionId": 1,
    "slotIndex": 0,
    "displayName": "SIM 1",
    "carrierName": "Carrier Name",
    "number": "+1234567890"
  }
]
```

---

## GET /webhooks

Get current webhook configuration.

---

## PUT /webhooks

Update webhook configuration.

**Request:**
```json
{
  "url": "https://your-server.com/webhook",
  "secret": "hmac-secret",
  "enabled": true,
  "onIncomingSms": true,
  "onMessageSent": true,
  "onMessageDelivered": true
}
```
