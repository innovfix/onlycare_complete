# 🚨 Quick Fix: Agora Token Issue

## The Problem

Receiver accepts call successfully, but gets **Agora Error 110** immediately. Call doesn't connect.

---

## Root Cause

**Token/UID Mismatch**

The UID you use when **generating** the Agora token must **match exactly** when the user **joins** the channel.

```
❌ Generate token with UID 1002, but app joins with UID 0 → ERROR 110
✅ Generate token with UID 0, and app joins with UID 0 → SUCCESS
```

---

## Quick Questions for Backend Team

### 1. What UID are you using when generating tokens?

```php
// Check your code - what value is $uid?
$token = $agoraService->generateToken($channelName, $uid);
```

**Is it:**
- `0` ?
- `null` ?
- `$userId` (actual user ID)?
- Random number?

### 2. Are caller and receiver getting different tokens?

- **Same token** for both? (Current token is copied from caller to receiver)
- **Different tokens**? (New token generated for receiver)

---

## ✅ Simplest Fix (5 Minutes)

### Option 1: Use UID = 0 for Everyone

Change your token generation to **always use UID = 0**:

```php
// In POST /calls/initiate
$token = $agoraService->generateToken(
    $channelName, 
    0,  // ✅ Use 0 here
    RtcTokenBuilder::ROLE_PUBLISHER
);

// In GET /calls/incoming (or when call is accepted)
$token = $agoraService->generateToken(
    $channelName, 
    0,  // ✅ Use 0 here too
    RtcTokenBuilder::ROLE_PUBLISHER
);
```

**Important:**
- Both caller and receiver tokens must use `uid = 0`
- Role must be `ROLE_PUBLISHER` (not `ROLE_SUBSCRIBER`)

---

## 🔧 Better Fix (10 Minutes)

### Option 2: Use Unique UIDs + Return UID in API

1. **Generate unique UID for each user:**

```php
// Helper function
private function getUserAgoraUid($userId) {
    // Convert user ID to integer UID
    // Example: "USR_12345" → 12345
    return (int) str_replace('USR_', '', $userId);
}
```

2. **Update POST /calls/initiate:**

```php
$callerUid = $this->getUserAgoraUid($callerId);
$callerToken = $agoraService->generateToken($channelName, $callerUid);

return [
    'call_id' => $callId,
    'agora_token' => $callerToken,
    'agora_uid' => $callerUid,  // ✅ ADD THIS
    'channel_name' => $channelName
];
```

3. **Update GET /calls/incoming:**

```php
$receiverUid = $this->getUserAgoraUid($receiverId);
$receiverToken = $agoraService->generateToken($channelName, $receiverUid);

return [
    'id' => $callId,
    'agora_token' => $receiverToken,  // Different from caller's token
    'agora_uid' => $receiverUid,      // ✅ ADD THIS
    'channel_name' => $channelName
];
```

---

## 📝 Database Changes (Optional but Recommended)

```sql
ALTER TABLE calls 
ADD COLUMN caller_agora_uid INTEGER,
ADD COLUMN receiver_agora_uid INTEGER;
```

Store UIDs so you can debug issues later.

---

## 🧪 How to Test

### 1. Add Debug Logs:

```php
Log::debug('Agora Token Generated', [
    'channel' => $channelName,
    'uid' => $uid,
    'token_length' => strlen($token)
]);
```

### 2. Make a Test Call:

1. User A calls User B
2. Check backend logs:
   - What UID was used for caller token?
   - What UID was used for receiver token?
3. Share logs with Android team

---

## 🎯 Expected Results

After fix, you should see in Android logs:

```
✅ Receiver joins channel (result: 0)
✅ Remote user joined (UID: 1001)
✅ Call connected
```

**No more Error 110! 🎉**

---

## 📞 Need Help?

If you're unsure about any of this, please share:
1. Your current token generation code
2. What UID values you're using
3. Backend logs from a test call

We'll help you fix it! 🚀




