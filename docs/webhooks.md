# Webhook Events

## Headers

Every webhook POST includes:

| Header | Value |
|---|---|
| `X-SMSGateway-Event` | Event type (see below) |
| `X-SMSGateway-Timestamp` | Unix timestamp (seconds) |
| `X-SMSGateway-Signature` | HMAC-SHA256 signature |

## Signature Verification

```
signature = HMAC-SHA256(secret, "<timestamp>.<body>")
```

Python example:
```python
import hmac, hashlib, base64

def verify(secret, timestamp, body, signature):
    payload = f"{timestamp}.{body}"
    expected = base64.b64encode(
        hmac.new(secret.encode(), payload.encode(), hashlib.sha256).digest()
    ).decode()
    return hmac.compare_digest(expected, signature)
```

## Event: incoming_sms

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

## Event: message_sent

```json
{
  "event": "message_sent",
  "messageId": "uuid",
  "direction": "OUTBOUND",
  "phoneNumber": "+1234567890",
  "body": "Your message",
  "state": "SENT",
  "subscriptionId": 1,
  "simSlot": 0,
  "clientRef": "optional-ref",
  "timestamp": 1700000001000
}
```

## Event: message_delivered

Same as `message_sent` but with `"event": "message_delivered"` and `"state": "DELIVERED"`.

## Retry Policy

Failed webhook deliveries are retried up to 5 times with exponential backoff (30s base) via WorkManager.
