# 🚨 BACKEND TEAM: Verify Agora Configuration NOW

## ❌ Current Issue: Error 110 on BOTH Devices

Both caller and receiver get **Agora Error 110** within 200ms of joining the channel.

---

## ✅ What We've Verified (Android App Side)

1. ✅ App is using correct **App ID**: `8b5e9417f15a48ae929783f32d3d33d4`
2. ✅ Agora free quota: **10,000 minutes remaining**
3. ✅ WebSocket connecting properly
4. ✅ Backend API calls working
5. ✅ Call acceptance flow working

---

## 🔴 What You Need to Check IMMEDIATELY (Backend)

### 1. **Verify Your App Certificate**

**Your Agora Console has:**
- **App ID**: `8b5e9417f15a48ae929783f32d3d33d4`
- **Primary Certificate**: `03e9b06b303e47a9b93e71aed9faac63`
- **Secondary Certificate**: `8b5a6bae5d87426b920a2758d2e398eb` (ENABLED)

**ACTION:** Find your token generation code and verify which certificate you're using.

```php
// Example - Check your code
$token = RtcTokenBuilder::buildTokenWithUid(
    '8b5e9417f15a48ae929783f32d3d33d4',  // App ID - MUST match
    '???',  // ⚠️ WHICH CERTIFICATE ARE YOU USING?
    $channelName,
    $uid,
    $role,
    $expireTime
);
```

**Requirements:**
- ✅ Must use **Primary**: `03e9b06b303e47a9b93e71aed9faac63`
- ✅ OR use **Secondary**: `8b5a6bae5d87426b920a2758d2e398eb`
- ❌ If you're using a different/old certificate → **THIS IS THE BUG**

---

### 2. **Verify Token UID = 0**

**ACTION:** Check what UID you pass when generating tokens.

```php
// Current code - what is $uid?
$token = RtcTokenBuilder::buildTokenWithUid(
    $appId,
    $appCertificate,
    $channelName,
    $uid,  // ⚠️ WHAT VALUE IS THIS?
    $role,
    time() + 3600
);
```

**Requirements:**
- ✅ `$uid` MUST be `0` (zero)
- ❌ If it's `null`, `$userId`, or any other value → **THIS CAUSES ERROR 110**

**Why?** The Android app joins with `uid = 0`. If your token is generated for a different UID, Agora rejects it immediately with Error 110.

---

### 3. **Verify Role is PUBLISHER**

```php
// MUST use PUBLISHER, not SUBSCRIBER
$role = RtcTokenBuilder::ROLE_PUBLISHER;  // ✅ Correct
// NOT RtcTokenBuilder::ROLE_SUBSCRIBER  // ❌ Wrong
```

**Why?** Both caller and receiver need to publish audio/video. `ROLE_SUBSCRIBER` can only listen, causing connection failures.

---

## 🔧 Quick Fix (5 Minutes)

**Update your token generation function:**

```php
class AgoraService 
{
    private $appId = '8b5e9417f15a48ae929783f32d3d33d4';
    private $appCertificate = '03e9b06b303e47a9b93e71aed9faac63';  // ✅ Use Primary
    
    public function generateToken($channelName) 
    {
        $uid = 0;  // ✅ Always use 0
        $role = RtcTokenBuilder::ROLE_PUBLISHER;  // ✅ Publisher role
        $expireTime = time() + 3600;  // 1 hour
        
        $token = RtcTokenBuilder::buildTokenWithUid(
            $this->appId,
            $this->appCertificate,
            $channelName,
            $uid,
            $role,
            $expireTime
        );
        
        // Add logging to verify
        Log::info('Agora token generated', [
            'channel' => $channelName,
            'uid' => $uid,
            'role' => 'PUBLISHER',
            'expires_in' => 3600,
            'token_prefix' => substr($token, 0, 20)
        ]);
        
        return $token;
    }
}
```

---

## 📝 Update ALL 3 Endpoints

### 1. POST /api/v1/calls/initiate

```php
public function initiateCall(Request $request) 
{
    $channelName = "call_" . $callId;
    $token = $this->agoraService->generateToken($channelName);  // UID = 0
    
    $call->agora_token = $token;
    $call->channel_name = $channelName;
    $call->save();
    
    return response()->json([
        'call_id' => $call->id,
        'agora_token' => $token,
        'channel_name' => $channelName
    ]);
}
```

### 2. GET /api/v1/calls/incoming

```php
public function getIncomingCall(Request $request) 
{
    $call = Call::where('receiver_id', $userId)
                ->where('status', 'PENDING')
                ->first();
    
    // Generate NEW token for receiver (not caller's token)
    $receiverToken = $this->agoraService->generateToken($call->channel_name);  // UID = 0
    
    return response()->json([
        'call_id' => $call->id,
        'caller_id' => $call->caller_id,
        'call_type' => $call->call_type,
        'agora_token' => $receiverToken,  // ✅ New token
        'channel_name' => $call->channel_name
    ]);
}
```

### 3. POST /api/v1/calls/accept

```php
public function acceptCall(Request $request) 
{
    $call = Call::find($request->call_id);
    $call->status = 'ONGOING';
    $call->save();
    
    // Generate NEW token for receiver
    $receiverToken = $this->agoraService->generateToken($call->channel_name);  // UID = 0
    
    // Send WebSocket notification to caller
    $this->webSocketService->sendCallAccepted($call->caller_id, $call->id);
    
    return response()->json([
        'message' => 'Call accepted',
        'agora_token' => $receiverToken,  // ✅ New token
        'channel_name' => $call->channel_name
    ]);
}
```

---

## 🧪 How to Test

1. **Update your code with the correct certificate**
2. **Restart your backend server**
3. **Test a call:**
   - Caller initiates call
   - Receiver accepts call
   - Both should connect within 2-3 seconds

4. **Check backend logs for:**
   ```
   Agora token generated: channel=call_XXX, uid=0, role=PUBLISHER
   ```

---

## 🎯 Summary of Required Values

| Configuration | Required Value |
|--------------|----------------|
| **App ID** | `8b5e9417f15a48ae929783f32d3d33d4` ✅ |
| **App Certificate** | `03e9b06b303e47a9b93e71aed9faac63` OR `8b5a6bae5d87426b920a2758d2e398eb` |
| **Token UID** | `0` (zero) |
| **Token Role** | `ROLE_PUBLISHER` |
| **Token Expiry** | `3600` seconds (1 hour) |

---

## ❓ Still Getting Error 110 After This?

If you've verified all the above and still get Error 110, check:

1. **Firewall/Network**: Agora needs to connect to their servers
2. **HTTPS Required**: Token generation must happen over HTTPS in production
3. **Correct SDK Version**: Ensure Agora SDK is up to date
4. **Token Expiry**: Token must not be expired before use

---

## 📞 Need Help?

If Error 110 persists after verifying configuration:

1. **Enable detailed logging** in your token generation
2. **Share backend logs** showing token generation
3. **Verify certificate** is copy-pasted correctly (no extra spaces)

The error is happening **instantly** (< 200ms), which means it's a **configuration mismatch**, not a network/timeout issue.



