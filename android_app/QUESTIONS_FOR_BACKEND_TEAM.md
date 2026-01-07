# ❓ Questions for Backend Team - FCM Incoming Calls

## 🎯 Context
Incoming calls work when app is OPEN, but NOT when app is CLOSED. Mobile app is ready and FCM service is implemented. Need to verify backend FCM configuration.

---

## ✅ Question 1: Is FCM Token Being Saved?

**Question to ask:**
> "When the mobile app sends FCM token to `POST /api/v1/users/update-fcm-token`, are you saving it in the user's database record? Can you check if my user has an fcm_token value in the database?"

**What to check:**
- Look in `users` table for the `fcm_token` column
- My user ID: `USR_USR_17637560616692`
- Should have a long token like: `eifKhal2QvyKtCCkyofk4w:APA91bGo-xd9f7qux02p...`

**Expected answer:**
- "Yes, we are saving it" ✅
- "Your fcm_token is: eifKhal2QvyKtCCkyofk4w..." ✅

**If they say NO:**
- They need to implement the endpoint properly

---

## ✅ Question 2: What is the EXACT FCM Payload Format?

**Question to ask:**
> "Can you show me the exact FCM payload you're sending in CallController.php when a call is initiated? I need to see the data structure."

**What you need to see:**
```php
$message = CloudMessage::withTarget('token', $receiverToken)
    ->withData([
        'type' => 'incoming_call',      // ⬅️ Must be exactly this
        'callerId' => $caller->id,      // ⬅️ camelCase, not snake_case
        'callerName' => $caller->name,  // ⬅️ camelCase
        'callerPhoto' => $caller->profile_image ?? '',
        'channelId' => $call->channel_name,
        'agoraToken' => $call->agora_token ?? ''
    ])
    ->withAndroidConfig(
        AndroidConfig::fromArray(['priority' => 'high'])  // ⬅️ MUST have this
    );
```

**⚠️ Check for these mistakes:**

❌ **Wrong field names (snake_case):**
```php
'caller_id' => ...    // ❌ Should be 'callerId'
'caller_name' => ...  // ❌ Should be 'callerName'
'channel_id' => ...   // ❌ Should be 'channelId'
'agora_token' => ...  // ❌ Should be 'agoraToken'
```

❌ **Using notification instead of data:**
```php
->withNotification([...])  // ❌ Wrong! Must use withData()
```

❌ **Missing Android priority:**
```php
// ❌ Missing ->withAndroidConfig(...)
```

---

## ✅ Question 3: Are FCM Notifications Actually Being Sent?

**Question to ask:**
> "When someone initiates a call, are you successfully sending FCM notifications? Can you check your Laravel logs for FCM send success/error messages?"

**What to look for in logs:**
```
✅ "FCM notification sent successfully"
or
❌ "FCM error: [some error message]"
```

**If they see errors, ask:**
> "What is the exact FCM error message?"

**Common FCM errors:**
- `"Invalid registration token"` → Token is wrong/expired
- `"Authentication error"` → Firebase credentials issue
- `"Invalid argument"` → Payload format is wrong

---

## ✅ Question 4: Is Priority Set to HIGH?

**Question to ask:**
> "Is the FCM notification priority set to 'high' in the Android config? This is critical for background delivery."

**Must have:**
```php
->withAndroidConfig(
    AndroidConfig::fromArray([
        'priority' => 'high'  // ⬅️ MUST be 'high', not 'normal'
    ])
)
```

---

## ✅ Question 5: Can You Send a Test FCM Notification?

**Question to ask:**
> "Can you manually send a test FCM notification to my device using this exact payload format? My FCM token is: `eifKhal2QvyKtCCkyofk4w:APA91bGo-xd9f7qux02pfCXdzLi__haK78PDsJkAa7U1nZ6uviUU_ZhpCnS90kpNnnjt-81UDgh7kKyJntJg9DcEpg8nW1fHWb45C0ZjcwQG1xt9LWDnR80`"

**Test payload they should send:**
```php
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\AndroidConfig;

$testMessage = CloudMessage::withTarget('token', 'eifKhal2QvyKtCCkyofk4w:APA91bGo-xd9f7qux02pfCXdzLi__haK78PDsJkAa7U1nZ6uviUU_ZhpCnS90kpNnnjt-81UDgh7kKyJntJg9DcEpg8nW1fHWb45C0ZjcwQG1xt9LWDnR80')
    ->withData([
        'type' => 'incoming_call',
        'callerId' => 'TEST_123',
        'callerName' => 'Test Caller',
        'callerPhoto' => '',
        'channelId' => 'test_channel_123',
        'agoraToken' => ''
    ])
    ->withAndroidConfig(
        AndroidConfig::fromArray(['priority' => 'high'])
    );

try {
    $messaging->send($testMessage);
    echo "✅ Test FCM sent successfully\n";
} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
}
```

**Expected result on my device:**
- Full-screen incoming call appears
- Phone rings
- Even if app is completely closed!

---

## ✅ Question 6: Are You Sending to the Correct User?

**Question to ask:**
> "When User A calls User B, are you sending the FCM notification to User B's fcm_token (the receiver), not User A's token?"

**Correct:**
```php
// Get RECEIVER's token
$receiverToken = $receiver->fcm_token;  // ✅ Receiver!

// Send notification to RECEIVER
$message = CloudMessage::withTarget('token', $receiverToken);
```

**Wrong:**
```php
// ❌ Sending to caller instead of receiver
$callerToken = $caller->fcm_token;
```

---

## ✅ Question 7: Is Firebase Credentials File Valid?

**Question to ask:**
> "Is your Firebase service account JSON file valid and does it have the correct permissions? Can you verify the Firebase project ID matches our app's google-services.json?"

**What to check:**
- Firebase Console → Project Settings
- Project ID should match
- Service account should have "Firebase Cloud Messaging Admin" role

---

## ✅ Question 8: What Happens When FCM Send Fails?

**Question to ask:**
> "If FCM notification sending fails, are you logging the error? What does the error say?"

**They should have try-catch:**
```php
try {
    $messaging->send($message);
    Log::info('✅ FCM sent to: ' . $receiver->id);
} catch (\Exception $e) {
    Log::error('❌ FCM error: ' . $e->getMessage());
    Log::error('Token: ' . $receiverToken);
    Log::error('User: ' . $receiver->id);
}
```

---

## ✅ Question 9: Can You Share the CallController Code?

**Question to ask:**
> "Can you share the exact code from CallController.php where you send the FCM notification? I need to verify the payload format matches what the mobile app expects."

**What to look for:**
- Is it using `withData()`? ✅
- Are field names in camelCase? ✅
- Is priority set to 'high'? ✅
- Is it sending to receiver's token? ✅

---

## 📊 Summary Checklist for Backend Team

Ask them to verify:

- [ ] FCM token is being saved in database (`fcm_token` column)
- [ ] FCM payload uses `withData()` not `withNotification()`
- [ ] Field names are camelCase: `callerId`, `callerName`, `channelId`, `agoraToken`
- [ ] Android priority is set to `'high'`
- [ ] Notification is sent to RECEIVER's token, not caller's
- [ ] Firebase credentials are valid
- [ ] They can send a manual test FCM notification
- [ ] They log FCM send errors

---

## 🎯 The Most Important Question

**If you can only ask ONE question, ask this:**

> "Can you send a manual test FCM notification to this token with the exact payload format I provided? Just run it as a quick test script. If my phone shows a full-screen incoming call (even with app closed), then the system works and we just need to debug why it's not being sent during actual calls."

**My FCM Token:**
```
eifKhal2QvyKtCCkyofk4w:APA91bGo-xd9f7qux02pfCXdzLi__haK78PDsJkAa7U1nZ6uviUU_ZhpCnS90kpNnnjt-81UDgh7kKyJntJg9DcEpg8nW1fHWb45C0ZjcwQG1xt9LWDnR80
```

---

## 📱 What I'll Test on Mobile Side

While backend checks their side, I can test:

**Test 1:** Kill the app and wait for a call
```bash
# I'll run this command and share logs:
adb logcat -c && adb logcat | grep -E "FCM|CallNotification|FirebaseMessaging"
```

**Test 2:** Check if app receives FCM when closed
- I'll tell you if FCM notification arrives or not

**Test 3:** Check FCM payload received
- I'll show you what data the app receives (if any)

---

## ✅ Expected Working Flow

**This is what SHOULD happen:**

```
1. User A calls User B
2. Backend sends FCM to User B's token
3. User B's phone receives FCM (even if app closed)
4. CallNotificationService wakes up
5. IncomingCallService starts
6. Full-screen UI appears + ringtone plays
7. User B sees call and can answer!
```

**Currently happening:**
```
1. User A calls User B
2. ??? (Need to verify if FCM is being sent)
3. ??? (Need to verify if phone receives it)
4. ❌ Nothing happens when app is closed
```

---

## 🎯 Copy-Paste This to Your Backend Team

**Subject:** FCM Incoming Call - Need Help Debugging

**Message:**

> Hi Backend Team,
> 
> The mobile app's full-screen incoming call feature is ready, but calls only work when the app is OPEN. When the app is closed, users don't receive calls.
> 
> The mobile app is 100% ready for FCM. I need to verify the backend FCM implementation is sending notifications correctly.
> 
> Please answer these questions (in QUESTIONS_FOR_BACKEND_TEAM.md):
> 
> 1. Is my FCM token being saved in the database?
> 2. What is the exact FCM payload format you're sending?
> 3. Are FCM field names in camelCase (callerId, callerName, etc.)?
> 4. Is Android priority set to 'high'?
> 5. Can you send a manual test FCM to my token?
> 
> My FCM Token: `eifKhal2QvyKtCCkyofk4w:APA91bGo-xd9f7qux02pfCXdzLi__haK78PDsJkAa7U1nZ6uviUU_ZhpCnS90kpNnnjt-81UDgh7kKyJntJg9DcEpg8nW1fHWb45C0ZjcwQG1xt9LWDnR80`
> 
> Please check the document for full details!
> 
> Thanks!

---

**Status:** ⏳ Waiting for backend answers to debug FCM configuration



