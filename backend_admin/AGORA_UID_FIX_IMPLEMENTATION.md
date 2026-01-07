# 🛠️ Agora UID Mismatch - Implementation Guide

**Date:** November 22, 2025  
**Status:** READY TO IMPLEMENT  
**Estimated Time:** Option A: 30 minutes | Option B: 2 hours

---

## 🎯 Choose Your Implementation

### Option A: Quick Fix (Recommended for Immediate Resolution)
- **Time:** 30 minutes
- **Changes:** Minimal (API responses only)
- **Risk:** Low
- **Complexity:** Simple

### Option B: Proper Implementation (Recommended for Long-term)
- **Time:** 2 hours
- **Changes:** Database migration + API updates
- **Risk:** Medium
- **Complexity:** Moderate

---

# 🚀 OPTION A: Quick Fix (UID = 0 for Everyone)

## Step 1: Update CallController.php

**File:** `/app/Http/Controllers/Api/CallController.php`

### Change 1: Update `initiateCall()` Response (Line 289)

**Find this code:**

```php
return response()->json([
    'success' => true,
    'message' => 'Call initiated successfully',
    'call' => [
        'id' => $call->id,
        'caller_id' => $caller->id,
        'caller_name' => $caller->name,
        'caller_image' => $caller->profile_image,
        'receiver_id' => $receiver->id,
        'receiver_name' => $receiver->name,
        'receiver_image' => $receiver->profile_image,
        'call_type' => $request->call_type,
        'status' => 'CONNECTING',
        'duration' => 0,
        'coins_spent' => 0,
        'coins_earned' => 0,
        'started_at' => null,
        'ended_at' => null,
        'rating' => null,
        'timestamp' => $call->created_at->timestamp,
        'agora_token' => $agoraToken,
        'channel_name' => $channelName,
        'balance_time' => $balanceTime
    ],
    'agora_token' => $agoraToken,
    'channel_name' => $channelName,
    'balance_time' => $balanceTime
]);
```

**Replace with:**

```php
return response()->json([
    'success' => true,
    'message' => 'Call initiated successfully',
    'call' => [
        'id' => $call->id,
        'caller_id' => $caller->id,
        'caller_name' => $caller->name,
        'caller_image' => $caller->profile_image,
        'receiver_id' => $receiver->id,
        'receiver_name' => $receiver->name,
        'receiver_image' => $receiver->profile_image,
        'call_type' => $request->call_type,
        'status' => 'CONNECTING',
        'duration' => 0,
        'coins_spent' => 0,
        'coins_earned' => 0,
        'started_at' => null,
        'ended_at' => null,
        'rating' => null,
        'timestamp' => $call->created_at->timestamp,
        'agora_token' => $agoraToken,
        'channel_name' => $channelName,
        'agora_uid' => 0,  // ✅ ADD THIS LINE
        'balance_time' => $balanceTime
    ],
    'agora_token' => $agoraToken,
    'channel_name' => $channelName,
    'agora_uid' => 0,  // ✅ ADD THIS LINE
    'balance_time' => $balanceTime
]);
```

---

### Change 2: Update `getIncomingCalls()` Response (Line 365)

**Find this code:**

```php
return [
    'id' => $call->id,
    'caller_id' => $call->caller_id,
    'caller_name' => $callerName,
    'caller_image' => $call->caller->profile_image ?? null,
    'call_type' => $call->call_type,
    'status' => $call->status,
    'created_at' => $call->created_at->toDateTimeString(),
    'agora_token' => $agoraToken,
    'channel_name' => $channelName,
];
```

**Replace with:**

```php
return [
    'id' => $call->id,
    'caller_id' => $call->caller_id,
    'caller_name' => $callerName,
    'caller_image' => $call->caller->profile_image ?? null,
    'call_type' => $call->call_type,
    'status' => $call->status,
    'created_at' => $call->created_at->toDateTimeString(),
    'agora_token' => $agoraToken,
    'channel_name' => $channelName,
    'agora_uid' => 0,  // ✅ ADD THIS LINE
];
```

---

### Change 3: Update `acceptCall()` Response (Line 453)

**Find this code:**

```php
return response()->json([
    'success' => true,
    'message' => 'Call accepted',
    'call' => [
        'id' => $call->id,
        'status' => $call->status,
        'started_at' => $call->started_at->toIso8601String(),
        'agora_token' => $agoraToken,
        'channel_name' => $channelName
    ]
]);
```

**Replace with:**

```php
return response()->json([
    'success' => true,
    'message' => 'Call accepted',
    'call' => [
        'id' => $call->id,
        'status' => $call->status,
        'started_at' => $call->started_at->toIso8601String(),
        'agora_token' => $agoraToken,
        'channel_name' => $channelName,
        'agora_uid' => 0  // ✅ ADD THIS LINE
    ]
]);
```

---

### Change 4: Add Debug Logging (Optional but Recommended)

**Find this code (Line 878):**

```php
Log::debug("Token Generation Debug for call {$callId}:");
Log::debug("  - App ID: {$appId}");
Log::debug("  - Certificate: " . substr($appCertificate, 0, 20) . "...");
Log::debug("  - Channel: {$channelName}");
```

**Add after it:**

```php
Log::debug("  - UID: {$uid}");  // ✅ ADD THIS LINE
Log::debug("  - Token will be valid for UID: 0");  // ✅ ADD THIS LINE
```

---

## Step 2: Update Android App

**File:** Your Android Kotlin file (wherever you join Agora channel)

### Before (Current - WRONG):

```kotlin
// ❌ WRONG - Using random UID or user ID
val response = apiService.initiateCall(receiverId, callType)
rtcEngine.joinChannel(
    response.agora_token,
    response.channel_name,
    uid = userId.toInt()  // ❌ Wrong UID
)
```

### After (Correct - FIXED):

```kotlin
// ✅ CORRECT - Using UID from API
val response = apiService.initiateCall(receiverId, callType)
rtcEngine.joinChannel(
    response.agora_token,
    response.channel_name,
    uid = response.agora_uid  // ✅ Use UID from API (will be 0)
)
```

---

## Step 3: Update Android API Response Models

**File:** Your Kotlin data classes

### Update CallResponse model:

```kotlin
data class CallResponse(
    val success: Boolean,
    val call: CallData,
    val agora_token: String,
    val channel_name: String,
    val agora_uid: Int,  // ✅ ADD THIS FIELD
    val balance_time: String
)

data class CallData(
    val id: String,
    val agora_token: String,
    val channel_name: String,
    val agora_uid: Int,  // ✅ ADD THIS FIELD
    // ... other fields
)

data class IncomingCall(
    val id: String,
    val caller_id: String,
    val caller_name: String,
    val caller_image: String?,
    val call_type: String,
    val status: String,
    val created_at: String,
    val agora_token: String,
    val channel_name: String,
    val agora_uid: Int  // ✅ ADD THIS FIELD
)
```

---

## Step 4: Test the Fix

### Test Flow:

1. **User A calls User B**
   - Check backend logs for: `UID: 0`
   - Check Android logs for: `Joining with UID: 0`

2. **User B sees incoming call**
   - Check API response has: `"agora_uid": 0`
   - Check Android uses UID: `0`

3. **User B accepts call**
   - Check API response has: `"agora_uid": 0`
   - Check Android uses UID: `0`

4. **Both users should connect successfully**
   - No Error 110
   - Audio/Video streaming works

---

## ✅ Testing Checklist

- [ ] Backend changes deployed
- [ ] Android app updated
- [ ] Test call from User A to User B
- [ ] User B receives call
- [ ] User B accepts call
- [ ] Both users connect without Error 110
- [ ] Audio/Video works properly
- [ ] Call ends successfully

---

# 🏗️ OPTION B: Proper Implementation (Unique UID per User)

## Step 1: Database Migration

**Create migration file:**

```bash
php artisan make:migration add_agora_uid_fields_to_calls_table
```

**File:** `database/migrations/YYYY_MM_DD_HHMMSS_add_agora_uid_fields_to_calls_table.php`

```php
<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('calls', function (Blueprint $table) {
            $table->integer('caller_agora_uid')->after('channel_name')->nullable();
            $table->integer('receiver_agora_uid')->after('caller_agora_uid')->nullable();
            $table->text('caller_agora_token')->after('receiver_agora_uid')->nullable();
            $table->text('receiver_agora_token')->after('caller_agora_token')->nullable();
            
            // Add index for performance
            $table->index(['caller_agora_uid', 'receiver_agora_uid'], 'idx_calls_agora_uids');
        });
    }

    public function down(): void
    {
        Schema::table('calls', function (Blueprint $table) {
            $table->dropIndex('idx_calls_agora_uids');
            $table->dropColumn([
                'caller_agora_uid',
                'receiver_agora_uid',
                'caller_agora_token',
                'receiver_agora_token'
            ]);
        });
    }
};
```

**Run migration:**

```bash
php artisan migrate
```

---

## Step 2: Update Call Model

**File:** `/app/Models/Call.php`

**Add to `$fillable` array:**

```php
protected $fillable = [
    'id',
    'caller_id',
    'receiver_id',
    'call_type',
    'status',
    'duration',
    'coins_spent',
    'coins_earned',
    'coin_rate_per_minute',
    'started_at',
    'ended_at',
    'rating',
    'feedback',
    'agora_token',
    'channel_name',
    'caller_agora_uid',      // ✅ ADD
    'receiver_agora_uid',    // ✅ ADD
    'caller_agora_token',    // ✅ ADD
    'receiver_agora_token',  // ✅ ADD
];
```

---

## Step 3: Update CallController.php

### Change 1: Add Helper Method for UID Generation

**Add this method at the bottom of CallController class (before closing brace):**

```php
/**
 * Get Agora UID for a user
 * 
 * Converts user ID string to integer UID for Agora
 * Example: USR_17637424324851 → 17637424324851
 * 
 * Note: Agora UID must be:
 * - 32-bit unsigned integer (max: 2,147,483,647)
 * - Unique per user in the channel
 */
private function getUserAgoraUid(string $userId): int
{
    // Remove USR_ prefix
    $numericId = str_replace('USR_', '', $userId);
    
    // Convert to integer
    $uid = (int) $numericId;
    
    // Ensure it fits in 32-bit signed integer range
    // If too large, use hash approach
    if ($uid > 2147483647) {
        // Use CRC32 hash for large IDs
        $uid = abs(crc32($userId));
    }
    
    return $uid;
}
```

---

### Change 2: Update `generateAgoraToken()` Method

**Find this method (Line 871):**

```php
private function generateAgoraToken($callId)
{
    $appId = config('services.agora.app_id', env('AGORA_APP_ID'));
    $appCertificate = config('services.agora.app_certificate', env('AGORA_APP_CERTIFICATE'));
    $channelName = 'call_' . $callId;
    $uid = 0; // 0 means any user can join
    
    // ... rest
}
```

**Replace with:**

```php
private function generateAgoraToken($callId, $uid = 0)
{
    $appId = config('services.agora.app_id', env('AGORA_APP_ID'));
    $appCertificate = config('services.agora.app_certificate', env('AGORA_APP_CERTIFICATE'));
    $channelName = 'call_' . $callId;
    // $uid now comes from parameter
    
    // DEBUG: Log exact credentials being used
    Log::debug("Token Generation Debug for call {$callId}:");
    Log::debug("  - App ID: {$appId}");
    Log::debug("  - Certificate: " . substr($appCertificate, 0, 20) . "...");
    Log::debug("  - Channel: {$channelName}");
    Log::debug("  - UID: {$uid}");  // ✅ UPDATED
    
    // ... rest of method stays the same
}
```

---

### Change 3: Update `initiateCall()` Method

**Find this code (Line 236-242):**

```php
// Generate unique call ID first
$callId = 'CALL_' . time() . rand(1000, 9999);

Log::info('🔑 Generating Agora token for call:', ['call_id' => $callId]);
$agoraToken = $this->generateAgoraToken($callId);
$channelName = 'call_' . $callId;
Log::info('✅ Agora credentials generated:', ['channel' => $channelName, 'token_length' => strlen($agoraToken)]);
```

**Replace with:**

```php
// Generate unique call ID first
$callId = 'CALL_' . time() . rand(1000, 9999);

Log::info('🔑 Generating Agora credentials for call:', ['call_id' => $callId]);

// Generate caller UID and token
$callerUid = $this->getUserAgoraUid($caller->id);
$callerToken = $this->generateAgoraToken($callId, $callerUid);

// Generate receiver UID and token
$receiverUid = $this->getUserAgoraUid($receiverId);
$receiverToken = $this->generateAgoraToken($callId, $receiverUid);

$channelName = 'call_' . $callId;

Log::info('✅ Agora credentials generated:', [
    'channel' => $channelName,
    'caller_uid' => $callerUid,
    'receiver_uid' => $receiverUid,
    'caller_token_length' => strlen($callerToken),
    'receiver_token_length' => strlen($receiverToken)
]);
```

---

### Change 4: Update Call Creation

**Find this code (Line 251-260):**

```php
$call = Call::create([
    'id' => $callId,
    'caller_id' => $caller->id,
    'receiver_id' => $receiverId,
    'call_type' => $request->call_type,
    'status' => 'CONNECTING',
    'coin_rate_per_minute' => $requiredCoins,
    'agora_token' => $agoraToken,
    'channel_name' => $channelName
]);
```

**Replace with:**

```php
$call = Call::create([
    'id' => $callId,
    'caller_id' => $caller->id,
    'receiver_id' => $receiverId,
    'call_type' => $request->call_type,
    'status' => 'CONNECTING',
    'coin_rate_per_minute' => $requiredCoins,
    'channel_name' => $channelName,
    'caller_agora_uid' => $callerUid,          // ✅ ADD
    'receiver_agora_uid' => $receiverUid,      // ✅ ADD
    'caller_agora_token' => $callerToken,      // ✅ ADD
    'receiver_agora_token' => $receiverToken,  // ✅ ADD
    // Keep old field for backward compatibility
    'agora_token' => $callerToken
]);
```

---

### Change 5: Update `initiateCall()` Response

**Find the return statement (Line 289):**

```php
return response()->json([
    'success' => true,
    'message' => 'Call initiated successfully',
    'call' => [
        'id' => $call->id,
        // ... other fields ...
        'agora_token' => $agoraToken,
        'channel_name' => $channelName,
        'balance_time' => $balanceTime
    ],
    'agora_token' => $agoraToken,
    'channel_name' => $channelName,
    'balance_time' => $balanceTime
]);
```

**Replace with:**

```php
return response()->json([
    'success' => true,
    'message' => 'Call initiated successfully',
    'call' => [
        'id' => $call->id,
        // ... other fields ...
        'agora_token' => $callerToken,         // ✅ UPDATED
        'channel_name' => $channelName,
        'agora_uid' => $callerUid,             // ✅ ADD
        'balance_time' => $balanceTime
    ],
    'agora_token' => $callerToken,             // ✅ UPDATED
    'channel_name' => $channelName,
    'agora_uid' => $callerUid,                 // ✅ ADD
    'balance_time' => $balanceTime
]);
```

---

### Change 6: Update `getIncomingCalls()` Method

**Find this code (Line 334-353):**

```php
->map(function ($call) {
    $callerName = $call->caller->name ?? 'Unknown';
    
    // ✅ CRITICAL FIX: Use saved Agora credentials from database
    $agoraToken = $call->agora_token;
    $channelName = $call->channel_name;
    
    // Fallback: If credentials are missing (old calls), regenerate them
    if (empty($agoraToken) || empty($channelName)) {
        Log::warning('⚠️ Missing Agora credentials for call ' . $call->id . ', regenerating...');
        $agoraToken = $this->generateAgoraToken($call->id);
        $channelName = 'call_' . $call->id;
        
        // Save the regenerated credentials
        $call->update([
            'agora_token' => $agoraToken,
            'channel_name' => $channelName
        ]);
    }
```

**Replace with:**

```php
->map(function ($call) use ($user) {
    $callerName = $call->caller->name ?? 'Unknown';
    
    // ✅ Use receiver's token (not caller's token)
    $agoraToken = $call->receiver_agora_token;
    $agoraUid = $call->receiver_agora_uid;
    $channelName = $call->channel_name;
    
    // Fallback: If credentials are missing (old calls), regenerate them
    if (empty($agoraToken) || empty($agoraUid) || empty($channelName)) {
        Log::warning('⚠️ Missing Agora credentials for call ' . $call->id . ', regenerating...');
        
        $receiverUid = $this->getUserAgoraUid($call->receiver_id);
        $agoraToken = $this->generateAgoraToken($call->id, $receiverUid);
        $agoraUid = $receiverUid;
        $channelName = 'call_' . $call->id;
        
        // Save the regenerated credentials
        $call->update([
            'receiver_agora_token' => $agoraToken,
            'receiver_agora_uid' => $agoraUid,
            'channel_name' => $channelName
        ]);
    }
```

---

### Change 7: Update `getIncomingCalls()` Response

**Find the return array (Line 365):**

```php
return [
    'id' => $call->id,
    'caller_id' => $call->caller_id,
    'caller_name' => $callerName,
    'caller_image' => $call->caller->profile_image ?? null,
    'call_type' => $call->call_type,
    'status' => $call->status,
    'created_at' => $call->created_at->toDateTimeString(),
    'agora_token' => $agoraToken,
    'channel_name' => $channelName,
];
```

**Replace with:**

```php
return [
    'id' => $call->id,
    'caller_id' => $call->caller_id,
    'caller_name' => $callerName,
    'caller_image' => $call->caller->profile_image ?? null,
    'call_type' => $call->call_type,
    'status' => $call->status,
    'created_at' => $call->created_at->toDateTimeString(),
    'agora_token' => $agoraToken,      // Receiver's token
    'channel_name' => $channelName,
    'agora_uid' => $agoraUid,          // ✅ ADD (Receiver's UID)
];
```

---

### Change 8: Update `acceptCall()` Method

**Find this code (Line 443-451):**

```php
// ✅ Use saved Agora credentials from database (same as caller received)
$agoraToken = $call->agora_token;
$channelName = $call->channel_name;

// Fallback: If credentials are missing, regenerate them
if (empty($agoraToken) || empty($channelName)) {
    Log::warning('⚠️ Missing Agora credentials in acceptCall for ' . $call->id . ', regenerating...');
    $agoraToken = $this->generateAgoraToken($call->id);
    $channelName = 'call_' . $call->id;
}
```

**Replace with:**

```php
// ✅ Use receiver's token (not caller's token)
$agoraToken = $call->receiver_agora_token;
$agoraUid = $call->receiver_agora_uid;
$channelName = $call->channel_name;

// Fallback: If credentials are missing, regenerate them
if (empty($agoraToken) || empty($agoraUid) || empty($channelName)) {
    Log::warning('⚠️ Missing Agora credentials in acceptCall for ' . $call->id . ', regenerating...');
    
    $receiverUid = $this->getUserAgoraUid($call->receiver_id);
    $agoraToken = $this->generateAgoraToken($call->id, $receiverUid);
    $agoraUid = $receiverUid;
    $channelName = 'call_' . $call->id;
}
```

---

### Change 9: Update `acceptCall()` Response

**Find this code (Line 453):**

```php
return response()->json([
    'success' => true,
    'message' => 'Call accepted',
    'call' => [
        'id' => $call->id,
        'status' => $call->status,
        'started_at' => $call->started_at->toIso8601String(),
        'agora_token' => $agoraToken,
        'channel_name' => $channelName
    ]
]);
```

**Replace with:**

```php
return response()->json([
    'success' => true,
    'message' => 'Call accepted',
    'call' => [
        'id' => $call->id,
        'status' => $call->status,
        'started_at' => $call->started_at->toIso8601String(),
        'agora_token' => $agoraToken,      // Receiver's token
        'channel_name' => $channelName,
        'agora_uid' => $agoraUid           // ✅ ADD (Receiver's UID)
    ]
]);
```

---

## Step 4: Update Android App (Same as Option A)

See Option A Step 2 above.

---

## Step 5: Testing

### Test Flow:

1. **User A calls User B**
   - Backend generates: Caller UID (e.g., 1001) + Receiver UID (e.g., 1002)
   - Caller gets token for UID 1001
   - Check logs: `Caller UID: 1001, Receiver UID: 1002`

2. **User B sees incoming call**
   - API returns token for UID 1002
   - Check response: `"agora_uid": 1002`

3. **User B accepts call**
   - API returns token for UID 1002
   - Check response: `"agora_uid": 1002`

4. **Both users connect**
   - Caller joins with UID 1001
   - Receiver joins with UID 1002
   - Both connect successfully

---

## ✅ Testing Checklist (Option B)

- [ ] Migration run successfully
- [ ] Call model updated
- [ ] CallController updated
- [ ] Backend changes deployed
- [ ] Android app updated
- [ ] Test call: User A → User B
- [ ] Verify caller gets unique UID
- [ ] Verify receiver gets different UID
- [ ] Both users connect without errors
- [ ] Audio/Video works
- [ ] Test multiple simultaneous calls

---

## 📊 Comparison: Option A vs Option B

| Feature | Option A | Option B |
|---------|----------|----------|
| Implementation Time | 30 min | 2 hours |
| Code Changes | Minimal | Moderate |
| Database Changes | None | Migration required |
| UID per user | No (UID=0 for all) | Yes (unique) |
| Security | Basic | Better |
| Debugging | Limited | Excellent |
| Agora Analytics | Basic | Detailed |
| Token Revocation | All users | Per user |
| Backward Compatibility | ✅ Yes | ✅ Yes |

---

## 🎯 Recommendation

**For Immediate Fix:** Use **Option A**
- Gets your app working today
- Minimal risk
- Easy to implement

**For Production:** Migrate to **Option B**
- Better security
- Better tracking
- Better debugging
- Industry best practice

---

## 📞 Need Help?

If you encounter any issues during implementation, check:

1. **Error 110 still happening?**
   - Verify Android app is using `agora_uid` from API
   - Check backend logs for UID value
   - Ensure UID matches between token and join call

2. **Migration fails?**
   - Check if columns already exist
   - Verify database permissions
   - Check Laravel logs

3. **Token generation fails?**
   - Verify AGORA_APP_ID is set
   - Verify AGORA_APP_CERTIFICATE is set
   - Check token builder service

Let me know which option you choose and I can help with implementation!

