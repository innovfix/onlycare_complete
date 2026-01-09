# Direct Call Token Logging - Android App

## For Direct Calls (Not Random Calls)

When male user makes a **direct call** (calling a specific user), use this TAG:

---

## **TAG for Direct Calls:**

```
CallConnectingVM
```

---

## **Search Terms:**

### **In Logcat Search Box:**
```
DIRECT CALL INITIATED
```
or
```
CallConnectingVM
```

### **Alternative Search:**
```
AGORA CREDENTIALS FROM API (MALE - DIRECT CALL)
```

---

## **Logcat Filter:**

```
tag:CallConnectingVM
```

---

## **ADB Command:**

```bash
adb logcat CallConnectingVM:I *:S
```

### **Save to File:**
```bash
adb logcat CallConnectingVM:I *:S > direct_call_logs.txt
```

---

## **What You'll See:**

```
CallConnectingVM: ========================================
CallConnectingVM: 📞 MALE USER - DIRECT CALL INITIATED
CallConnectingVM: ========================================
CallConnectingVM: CALL_ID: CALL_17679608113449
CallConnectingVM: RECEIVER_ID: USR_17677720438040
CallConnectingVM: CALL_TYPE: AUDIO
CallConnectingVM: BALANCE_TIME: 90:00
CallConnectingVM: 
CallConnectingVM: 🔑 AGORA CREDENTIALS FROM API (MALE - DIRECT CALL):
CallConnectingVM: ========================================
CallConnectingVM: ✅ Token received: 0078b5e9417f15a48...
CallConnectingVM: Full token: 0078b5e9417f15a48ae929783f32d3d33d4AAAAIPBL7RahUJeYc9QLxDJ8cDkDQkAIc5eoVmgaofiOyYZIAi4mVGlg8OtpYkJrABhjYWxsX0NBTExfMTc2Nzk2MDgxMTM0NDkAAAAA
CallConnectingVM: AGORA_APP_ID = 8b5e9417f15a48ae929783f32d3d33d4
CallConnectingVM: CHANNEL_NAME = call_CALL_17679608113449
CallConnectingVM: TOKEN_LENGTH = 139
CallConnectingVM: ========================================
```

---

## **Summary - All Male Call Types:**

| Call Type | TAG | Search Term |
|-----------|-----|-------------|
| **Direct Call** | `CallConnectingVM` | `DIRECT CALL INITIATED` |
| **Random Call** | `RandomCallViewModel` | `MALE USER - CALL INITIATED` |

---

## **Quick Reference:**

### **For Direct Calls (Calling Specific User):**
- **TAG:** `CallConnectingVM`
- **Search:** `DIRECT CALL INITIATED`
- **File:** `CallConnectingViewModel.kt`

### **For Random Calls (Calling Random User):**
- **TAG:** `RandomCallViewModel`
- **Search:** `MALE USER - CALL INITIATED`
- **File:** `RandomCallViewModel.kt`

### **For Female (Receiving Call):**
- **TAG:** `IncomingCallActivity`
- **Search:** `INCOMING CALL DATA RECEIVED`
- **File:** `IncomingCallActivity.kt`

---

**Updated:** 2026-01-09
**Log Level:** INFO (I)
**Purpose:** Track Agora token for direct calls
