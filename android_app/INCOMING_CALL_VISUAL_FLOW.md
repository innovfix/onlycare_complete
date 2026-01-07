# 📱 Full-Screen Incoming Call - Visual Flow Diagram

## 🔄 Complete System Flow

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          FULL-SCREEN INCOMING CALL FLOW                       │
└──────────────────────────────────────────────────────────────────────────────┘


1️⃣  USER A (CALLER) INITIATES CALL
════════════════════════════════════════
    
    📱 Mobile App
         ↓
    [Clicks "Call User B" button]
         ↓
    ┌─────────────────────────────┐
    │  POST /api/calls/initiate   │
    │  Body: {                    │
    │    callerId: "A",           │
    │    receiverId: "B",         │
    │    callType: "VIDEO"        │
    │  }                          │
    └─────────────┬───────────────┘
                  ↓


2️⃣  BACKEND PROCESSES CALL
════════════════════════════════════════
    
    🖥️  Backend Server
         ↓
    ┌─────────────────────────────┐
    │  1. Create call record      │
    │  2. Generate Agora token    │
    │  3. Get User B's FCM token  │
    └─────────────┬───────────────┘
                  ↓
    ┌─────────────────────────────┐
    │  Firebase Cloud Messaging   │
    │  Send notification to User B│
    │                             │
    │  Data Payload:              │
    │  {                          │
    │    type: "incoming_call",   │
    │    callerId: "A",           │
    │    callerName: "User A",    │
    │    callerPhoto: "...",      │
    │    channelId: "channel_123",│
    │    agoraToken: "token..."   │
    │  }                          │
    └─────────────┬───────────────┘
                  ↓
    
    🔥 FCM Push Notification
         ↓


3️⃣  USER B'S DEVICE RECEIVES NOTIFICATION (EVEN IF APP IS KILLED!)
════════════════════════════════════════════════════════════════════
    
    📱 User B's Device (App can be killed/closed)
         ↓
    ┌────────────────────────────────────┐
    │  CallNotificationService.kt        │
    │  (FCM Receiver)                    │
    │                                    │
    │  ✅ Receives notification          │
    │  ✅ Works even when app killed     │
    └────────────┬───────────────────────┘
                 ↓
    ┌────────────────────────────────────┐
    │  Starts IncomingCallService        │
    │  (Foreground Service)              │
    │                                    │
    │  ✅ Keeps app alive                │
    │  ✅ Shows notification             │
    └────────────┬───────────────────────┘
                 ↓
    
    ┌────────────────────────────────────┐
    │  1. Create notification            │
    │  2. Start foreground service       │
    │  3. Launch IncomingCallActivity    │
    │  4. Start ringtone & vibration     │
    └────────────┬───────────────────────┘
                 ↓


4️⃣  FULL-SCREEN INCOMING CALL UI APPEARS
════════════════════════════════════════════
    
    📱 IncomingCallActivity.kt
         ↓
    ┌────────────────────────────────────┐
    │  🔓 Unlocks screen                 │
    │  💡 Turns screen ON                │
    │  📱 Shows over lock screen         │
    │  🔔 Plays ringtone                 │
    │  📳 Vibrates                       │
    └────────────┬───────────────────────┘
                 ↓
    
    ┌─────────────────────────────────────┐
    │                                     │
    │         [Profile Picture]           │
    │                                     │
    │         User A is calling           │
    │                                     │
    │     Incoming video call...          │
    │                                     │
    │                                     │
    │    ┌─────────┐     ┌─────────┐    │
    │    │ REJECT  │     │ ACCEPT  │    │
    │    │   🔴    │     │   🟢    │    │
    │    └─────────┘     └─────────┘    │
    │                                     │
    └─────────────────────────────────────┘
                 ↓
    
    ⏳ User B must choose: Accept or Reject


5️⃣  USER B ACCEPTS CALL
════════════════════════════════════════
    
    [User taps ACCEPT button]
         ↓
    ┌────────────────────────────────────┐
    │  1. Stop ringtone                  │
    │  2. Stop vibration                 │
    │  3. Stop foreground service        │
    │  4. Close IncomingCallActivity     │
    └────────────┬───────────────────────┘
                 ↓
    ┌────────────────────────────────────┐
    │  Navigate to CallConnectingScreen  │
    │  Pass: channelId, agoraToken, etc  │
    └────────────┬───────────────────────┘
                 ↓
    ┌────────────────────────────────────┐
    │  Join Agora Channel                │
    │  Start video call                  │
    └────────────┬───────────────────────┘
                 ↓
    
    🎥 VIDEO CALL IN PROGRESS
    
    Both User A and User B now connected!


5️⃣  USER B REJECTS CALL (Alternative Path)
════════════════════════════════════════════
    
    [User taps REJECT button]
         ↓
    ┌────────────────────────────────────┐
    │  1. Stop ringtone                  │
    │  2. Stop vibration                 │
    │  3. Stop foreground service        │
    │  4. Close IncomingCallActivity     │
    └────────────┬───────────────────────┘
                 ↓
    ┌────────────────────────────────────┐
    │  Send rejection to backend         │
    │  POST /api/calls/reject            │
    └────────────┬───────────────────────┘
                 ↓
    
    Backend notifies User A:
    "Call was rejected"
         ↓
    
    ❌ CALL ENDED


6️⃣  CALLER CANCELS BEFORE ANSWER
════════════════════════════════════════
    
    [User A cancels call before B answers]
         ↓
    Backend sends FCM:
    {
      type: "call_cancelled",
      callerId: "A"
    }
         ↓
    ┌────────────────────────────────────┐
    │  CallNotificationService receives  │
    │  "call_cancelled" message          │
    └────────────┬───────────────────────┘
                 ↓
    ┌────────────────────────────────────┐
    │  1. Stop ringtone                  │
    │  2. Stop vibration                 │
    │  3. Stop foreground service        │
    │  4. Dismiss incoming call UI       │
    └────────────┬───────────────────────┘
                 ↓
    
    ❌ INCOMING CALL DISMISSED
```

---

## 🔄 State Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     INCOMING CALL STATES                     │
└─────────────────────────────────────────────────────────────┘


    [IDLE STATE]
         ↓
    FCM notification received
         ↓
    ┌──────────────────┐
    │   RINGING        │  ← Ringtone playing
    │   VIBRATING      │  ← Phone vibrating
    │   SCREEN ON      │  ← Screen woke up
    │   FULL-SCREEN UI │  ← UI visible
    └──────────────────┘
         ↓
    User interaction required
         ↓
    ┌─────────┬─────────┐
    │ ACCEPT  │ REJECT  │
    └────┬────┴────┬────┘
         ↓         ↓
    [ACCEPTED]  [REJECTED]
         ↓         ↓
    Join Call  Notify Backend
         ↓         ↓
    [IN CALL] [IDLE STATE]
```

---

## 📱 Device State Compatibility

```
┌──────────────────────────────────────────────────────────────┐
│           WORKS IN ALL THESE SCENARIOS                        │
└──────────────────────────────────────────────────────────────┘

✅ App in Foreground
   ├─ Full-screen UI appears
   ├─ Ringtone plays
   └─ User can accept/reject

✅ App in Background
   ├─ Full-screen UI appears over other apps
   ├─ Ringtone plays
   └─ User can accept/reject

✅ App Killed (Swiped Away)
   ├─ FCM wakes up app
   ├─ Full-screen UI appears
   ├─ Ringtone plays
   └─ User can accept/reject

✅ Screen Locked (Phone Sleeping)
   ├─ Screen turns ON automatically
   ├─ Full-screen UI appears over lock screen
   ├─ Ringtone plays
   └─ User can accept without unlocking

✅ Do Not Disturb Mode
   ├─ Notification appears (may not ring)
   ├─ Full-screen UI appears
   └─ User can accept/reject
```

---

## 🔧 Component Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                   SYSTEM COMPONENTS                           │
└──────────────────────────────────────────────────────────────┘


Firebase Cloud Messaging (FCM)
    ↓
    ├─→ CallNotificationService.kt
          ↓
          ├─→ Parses FCM payload
          ├─→ Extracts call data
          └─→ Starts IncomingCallService
                ↓
                ├─→ IncomingCallService.kt (Foreground)
                │     ↓
                │     ├─→ CallNotificationManager.kt
                │     │     ↓
                │     │     └─→ Creates notification
                │     │
                │     ├─→ Launches IncomingCallActivity
                │     │
                │     └─→ CallRingtoneManager.kt
                │           ↓
                │           ├─→ Plays ringtone
                │           └─→ Vibrates phone
                │
                └─→ IncomingCallActivity.kt
                      ↓
                      ├─→ Shows full-screen UI
                      ├─→ Wakes screen
                      ├─→ Shows over lock screen
                      └─→ Accept/Reject buttons
                            ↓
                            ├─→ [ACCEPT] → Navigate to CallConnectingScreen
                            └─→ [REJECT] → Notify backend + Dismiss
```

---

## 🎨 UI Component Hierarchy

```
IncomingCallActivity
├─ OnlyCareAppTheme
│  └─ IncomingCallScreen
│     ├─ Box (Gradient Background)
│     │  └─ Column
│     │     ├─ Column (Top Section)
│     │     │  ├─ AsyncImage (Profile Picture)
│     │     │  ├─ Spacer
│     │     │  ├─ Text (Caller Name)
│     │     │  ├─ Spacer
│     │     │  └─ Text (Subtitle)
│     │     │
│     │     └─ Row (Bottom Section)
│     │        ├─ FloatingActionButton (REJECT)
│     │        │  └─ Icon (CallEnd)
│     │        │
│     │        └─ FloatingActionButton (ACCEPT)
│     │           └─ Icon (Call)
```

---

## 🔐 Permission Flow

```
┌──────────────────────────────────────────────────────────────┐
│                    PERMISSION REQUESTS                        │
└──────────────────────────────────────────────────────────────┘


App First Launch
    ↓
FemaleHomeScreen loads
    ↓
RequestNotificationPermission composable
    ↓
    ┌────────────────────────────────┐
    │  Android 13+ (API 33)          │
    │  Request POST_NOTIFICATIONS    │
    └────────────┬───────────────────┘
                 ↓
    ┌────────────────────────────────┐
    │  User sees permission dialog   │
    │  "Allow notifications?"        │
    │                                │
    │  [ Allow ] [ Don't Allow ]     │
    └────────────┬───────────────────┘
                 ↓
        ┌────────┴────────┐
        ↓                 ↓
    [GRANTED]        [DENIED]
        ↓                 ↓
    ✅ Can receive   ❌ No notifications
    notifications    (but app still works)


Android 12 and below:
    → Notification permission granted by default
    → No runtime request needed
```

---

## 🔥 Firebase Data Flow

```
┌──────────────────────────────────────────────────────────────┐
│                   FCM TOKEN LIFECYCLE                         │
└──────────────────────────────────────────────────────────────┘


App Installation
    ↓
OnlyCareApplication.onCreate()
    ↓
FCMTokenManager.initializeFCM()
    ↓
Firebase generates token
    ↓
CallNotificationService.onNewToken()
    ↓
    ┌──────────────────────────┐
    │  Save token locally      │
    │  (SharedPreferences)     │
    └──────────┬───────────────┘
               ↓
    ┌──────────────────────────┐
    │  Send to backend         │
    │  POST /api/users/        │
    │  update-fcm-token        │
    └──────────┬───────────────┘
               ↓
    Backend stores token in database
               ↓
    Token ready for receiving notifications!


Token Refresh (automatic):
    → Firebase refreshes tokens periodically
    → onNewToken() called again
    → Update backend with new token
```

---

## ⚡ Performance & Battery

```
┌──────────────────────────────────────────────────────────────┐
│              BATTERY & PERFORMANCE OPTIMIZATION               │
└──────────────────────────────────────────────────────────────┘


App Killed State
    ↓
    [Zero battery drain]
    No services running
         ↓
    FCM notification arrives
         ↓
    System wakes app temporarily
         ↓
    ┌──────────────────────────┐
    │  IncomingCallService     │
    │  Runs as Foreground      │
    │  (Shows notification)    │
    │                          │
    │  Duration: ~30-60 sec    │
    │  Battery: Minimal        │
    └──────────┬───────────────┘
               ↓
    User accepts/rejects
         ↓
    Service stops
         ↓
    Back to zero battery drain


Battery Impact:
    ✅ No continuous background service
    ✅ Only active during incoming call
    ✅ Automatically stops after 60 seconds
    ✅ FCM handles wake-up efficiently
```

---

## 🎯 Success Indicators

```
IMPLEMENTATION IS SUCCESSFUL WHEN:

✅ Full-screen UI appears immediately
✅ Works with app completely killed
✅ Screen wakes up from sleep
✅ Shows over lock screen
✅ Ringtone plays continuously
✅ Phone vibrates with pattern
✅ Accept button joins call successfully
✅ Reject button dismisses cleanly
✅ No memory leaks
✅ No battery drain when idle
✅ Clean service lifecycle
✅ No crashes in any scenario
```

---

**This visual flow demonstrates the complete end-to-end system working together! 🎉**



