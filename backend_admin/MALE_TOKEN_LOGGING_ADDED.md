# Male Token Logging - Backend

## Summary
Added comprehensive logging to show exactly what Agora credentials are being sent to both MALE and FEMALE users.

---

## Changes Made

### File: `backend_admin/app/Http/Controllers/Api/CallController.php`

#### **1. Male User - API Response Logging**

**Location:** `initiateCall()` method (Line ~392)

**When:** When male user initiates a call and receives API response

**Added Logging:**
```php
Log::info('========================================');
Log::info('📤 SENDING TO MALE USER (API RESPONSE)');
Log::info('========================================');
Log::info('Call ID: ' . $call->id);
Log::info('Caller: ' . $caller->name . ' (' . $caller->id . ')');
Log::info('Receiver: ' . $receiver->name . ' (' . $receiver->id . ')');
Log::info('Call Type: ' . $request->call_type);
Log::info('');
Log::info('🔑 AGORA CREDENTIALS SENT TO MALE:');
Log::info('========================================');
Log::info('AGORA_APP_ID: ' . config('services.agora.app_id'));
Log::info('CHANNEL_NAME: ' . $channelName);
Log::info('AGORA_TOKEN: ' . $agoraToken);
Log::info('TOKEN_LENGTH: ' . strlen($agoraToken));
Log::info('AGORA_UID: 0');
Log::info('BALANCE_TIME: ' . $balanceTime);
Log::info('========================================');
```

---

#### **2. Female User - FCM Notification Logging**

**Location:** `sendPushNotification()` method (Line ~1723)

**When:** When FCM notification is sent to female with incoming call data

**Added Logging:**
```php
Log::info('========================================');
Log::info('📤 SENDING TO FEMALE USER (FCM NOTIFICATION)');
Log::info('========================================');
Log::info('Caller: ' . $caller->name . ' (' . $caller->id . ')');
Log::info('Receiver: ' . $receiver->name . ' (' . $receiver->id . ')');
Log::info('Call ID: ' . $callId);
Log::info('Call Type: ' . $callType);
Log::info('');
Log::info('🔑 AGORA CREDENTIALS SENT TO FEMALE (VIA FCM):');
Log::info('========================================');
Log::info('AGORA_APP_ID: ' . config('services.agora.app_id'));
Log::info('CHANNEL_NAME: ' . $call->channel_name);
Log::info('AGORA_TOKEN: ' . ($call->agora_token ?? 'NULL'));
Log::info('TOKEN_LENGTH: ' . strlen($call->agora_token ?? ''));
Log::info('BALANCE_TIME: ' . $balanceTime);
Log::info('========================================');
```

---

## What Gets Logged

### **For Male User (Caller):**
- Call ID
- Caller & Receiver info
- Call Type
- **Agora App ID**
- **Channel Name**
- **Complete Agora Token**
- Token Length
- UID (always 0)
- Balance Time

### **For Female User (Receiver):**
- Call ID
- Caller & Receiver info
- Call Type
- **Agora App ID**
- **Channel Name**
- **Complete Agora Token**
- Token Length
- Balance Time

---

## How to View Logs

### **View All Token Logs:**
```bash
ssh root@64.227.163.211 "tail -f /var/www/onlycare_admin/storage/logs/laravel.log | grep -E 'SENDING TO|AGORA CREDENTIALS SENT'"
```

### **View Male Token (API Response):**
```bash
ssh root@64.227.163.211 "tail -f /var/www/onlycare_admin/storage/logs/laravel.log | grep 'SENDING TO MALE'"
```

### **View Female Token (FCM):**
```bash
ssh root@64.227.163.211 "tail -f /var/www/onlycare_admin/storage/logs/laravel.log | grep 'SENDING TO FEMALE'"
```

### **View Token Generation:**
```bash
ssh root@64.227.163.211 "tail -f /var/www/onlycare_admin/storage/logs/laravel.log | grep agora_token"
```

---

## Example Log Output

### **Male User Receives Token:**
```
[2026-01-09 12:30:45] production.INFO: ========================================
[2026-01-09 12:30:45] production.INFO: 📤 SENDING TO MALE USER (API RESPONSE)
[2026-01-09 12:30:45] production.INFO: ========================================
[2026-01-09 12:30:45] production.INFO: Call ID: CALL_17679608113449
[2026-01-09 12:30:45] production.INFO: Caller: User_4780 (USR_17677720014836)
[2026-01-09 12:30:45] production.INFO: Receiver: Creator_2222 (USR_17677720438040)
[2026-01-09 12:30:45] production.INFO: Call Type: AUDIO
[2026-01-09 12:30:45] production.INFO: 
[2026-01-09 12:30:45] production.INFO: 🔑 AGORA CREDENTIALS SENT TO MALE:
[2026-01-09 12:30:45] production.INFO: ========================================
[2026-01-09 12:30:45] production.INFO: AGORA_APP_ID: 8b5e9417f15a48ae929783f32d3d33d4
[2026-01-09 12:30:45] production.INFO: CHANNEL_NAME: call_CALL_17679608113449
[2026-01-09 12:30:45] production.INFO: AGORA_TOKEN: 0078b5e9417f15a48ae929783f32d3d33d4AAAAIPBL7RahUJeYc9QLxDJ8cDkDQkAIc5eoVmgaofiOyYZIAi4mVGlg8OtpYkJrABhjYWxsX0NBTExfMTc2Nzk2MDgxMTM0NDkAAAAA
[2026-01-09 12:30:45] production.INFO: TOKEN_LENGTH: 139
[2026-01-09 12:30:45] production.INFO: AGORA_UID: 0
[2026-01-09 12:30:45] production.INFO: BALANCE_TIME: 90:00
[2026-01-09 12:30:45] production.INFO: ========================================
```

### **Female User Receives Token:**
```
[2026-01-09 12:30:45] production.INFO: ========================================
[2026-01-09 12:30:45] production.INFO: 📤 SENDING TO FEMALE USER (FCM NOTIFICATION)
[2026-01-09 12:30:45] production.INFO: ========================================
[2026-01-09 12:30:45] production.INFO: Caller: User_4780 (USR_17677720014836)
[2026-01-09 12:30:45] production.INFO: Receiver: Creator_2222 (USR_17677720438040)
[2026-01-09 12:30:45] production.INFO: Call ID: CALL_17679608113449
[2026-01-09 12:30:45] production.INFO: Call Type: AUDIO
[2026-01-09 12:30:45] production.INFO: 
[2026-01-09 12:30:45] production.INFO: 🔑 AGORA CREDENTIALS SENT TO FEMALE (VIA FCM):
[2026-01-09 12:30:45] production.INFO: ========================================
[2026-01-09 12:30:45] production.INFO: AGORA_APP_ID: 8b5e9417f15a48ae929783f32d3d33d4
[2026-01-09 12:30:45] production.INFO: CHANNEL_NAME: call_CALL_17679608113449
[2026-01-09 12:30:45] production.INFO: AGORA_TOKEN: 0078b5e9417f15a48ae929783f32d3d33d4AAAAIPBL7RahUJeYc9QLxDJ8cDkDQkAIc5eoVmgaofiOyYZIAi4mVGlg8OtpYkJrABhjYWxsX0NBTExfMTc2Nzk2MDgxMTM0NDkAAAAA
[2026-01-09 12:30:45] production.INFO: TOKEN_LENGTH: 139
[2026-01-09 12:30:45] production.INFO: BALANCE_TIME: 90:00
[2026-01-09 12:30:45] production.INFO: ========================================
```

---

## Complete Token Flow Logging

Now you can see the complete token flow:

1. **Token Generation:**
   - Search for: `[agora_token]`
   - Shows: Token created with method, expiration, etc.

2. **Token Sent to Male:**
   - Search for: `SENDING TO MALE USER`
   - Shows: Complete token in API response

3. **Token Sent to Female:**
   - Search for: `SENDING TO FEMALE USER`
   - Shows: Complete token in FCM notification

---

**Generated:** 2026-01-09
**Purpose:** Track Agora token distribution to both male and female users
**Deployment:** Live server (root@64.227.163.211)
