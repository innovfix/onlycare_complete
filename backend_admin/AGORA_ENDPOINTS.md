# Agora App ID - API Endpoints

## Base URL
```
https://your-domain.com/api/v1
```

---

## 1. Initiate Call

**Endpoint:** `POST /calls/initiate`

**Request:**
```json
{
  "receiver_id": 456,
  "call_type": "AUDIO"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Call initiated successfully",
  "call": {
    "id": "CALL_17324567891234",
    "agora_app_id": "63783c2ad2724b839b1e58714bfc2629",  ✅
    "agora_token": "",
    "channel_name": "call_CALL_17324567891234",
    "balance_time": "15:00",
    ...
  },
  "agora_app_id": "63783c2ad2724b839b1e58714bfc2629",  ✅
  "agora_token": "",
  "channel_name": "call_CALL_17324567891234"
}
```

---

## 2. Get Incoming Calls

**Endpoint:** `GET /calls/incoming`

**Request:** No body (just GET request)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "CALL_17324567891234",
      "caller_id": 123,
      "caller_name": "John Doe",
      "caller_image": "https://...",
      "call_type": "AUDIO",
      "status": "CONNECTING",
      "agora_app_id": "63783c2ad2724b839b1e58714bfc2629",  ✅
      "agora_token": "",
      "channel_name": "call_CALL_17324567891234",
      "created_at": "2025-11-22 14:30:00"
    }
  ]
}
```

---

## 3. Accept Call

**Endpoint:** `POST /calls/{callId}/accept`

**Request:** No body

**Response:**
```json
{
  "success": true,
  "message": "Call accepted",
  "call": {
    "id": "CALL_17324567891234",
    "status": "ONGOING",
    "started_at": "2025-11-22T14:30:00Z",
    "agora_app_id": "63783c2ad2724b839b1e58714bfc2629",  ✅
    "agora_token": "",
    "channel_name": "call_CALL_17324567891234"
  }
}
```

---

## 4. Get Call Status

**Endpoint:** `GET /calls/{callId}`

**Request:** No body (just GET request)

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "CALL_17324567891234",
    "caller_id": 123,
    "receiver_id": 456,
    "call_type": "AUDIO",
    "status": "ONGOING",
    "agora_app_id": "63783c2ad2724b839b1e58714bfc2629",  ✅
    "agora_token": "",
    "channel_name": "call_CALL_17324567891234",
    "duration": 120,
    "coins_spent": 40
  }
}
```

---

## Summary

**All 4 endpoints now return:**
- `agora_app_id`: `63783c2ad2724b839b1e58714bfc2629`
- `agora_token`: `""` (empty - unsecure mode)
- `channel_name`: `call_CALL_xxxxx`

**Use these in your app to initialize Agora SDK**







