# 🧪 Quick Test Guide - Incoming Calls API

## ✅ 3-Minute Verification

### Test 1: Check API Response
```bash
# Get a user token from the app and run:
curl -X GET 'https://onlycare.in/api/v1/calls/incoming' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE' \
  -H 'Accept: application/json' | jq
```

**✅ Success Indicators**:
- `agora_token`: Long string (130-150 chars), NOT null, NOT empty
- `channel_name`: Format is `call_CALL_xxxxxxxxxx`
- `status`: Either "CONNECTING" or "PENDING"

**❌ Failure Indicators**:
- `agora_token: null` or `agora_token: ""`
- `channel_name: null`
- Empty data array when there are active calls

---

### Test 2: Check Database
```sql
-- View recent calls
SELECT 
    id,
    status,
    CASE 
        WHEN agora_token IS NULL THEN '❌ NULL'
        WHEN agora_token = '' THEN '❌ EMPTY'
        ELSE CONCAT('✅ ', LENGTH(agora_token), ' chars')
    END as token_status,
    channel_name,
    created_at
FROM calls
WHERE created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY created_at DESC
LIMIT 10;
```

**✅ Success**:
- `token_status`: Shows "✅ 130 chars" or similar
- `channel_name`: Format is `call_CALL_xxxxxxxxxx`

**❌ Failure**:
- `token_status`: Shows "❌ NULL" or "❌ EMPTY"

---

### Test 3: End-to-End App Test

**Setup**:
- Device A: Male user with coins
- Device B: Verified female user (online)

**Steps**:
1. Device A: Initiate call to Device B
2. Device A: Should see "Ringing" screen ✅
3. Device B: Should see incoming call dialog ✅
4. Device B: Click "Accept"
5. **Expected Result**:
   - ✅ Device A: "Connected" screen with timer
   - ✅ Device B: "Connected" screen with timer
   - ✅ Audio works between devices
   - ✅ Mute/Speaker buttons work
   - ✅ Call timer counts up

**❌ If Both Stuck on "Ringing"**:
- Agora credentials are still missing
- Check logs: `tail -f storage/logs/laravel.log | grep -i agora`

---

## 🔍 Troubleshooting

### Issue: API returns empty data array
```json
{
  "success": true,
  "data": []
}
```

**Cause**: No active calls for this user  
**Fix**: This is normal if there are no incoming calls

---

### Issue: agora_token is null or empty
```json
{
  "agora_token": null,
  "channel_name": null
}
```

**Possible Causes**:
1. Agora credentials not configured in `.env`
2. Old database records without credentials
3. Code not deployed properly

**Fixes**:

1. **Check Agora Config**:
```bash
cd /var/www/onlycare_admin
php artisan tinker --execute="
echo 'App ID: ' . config('services.agora.app_id') . PHP_EOL;
echo 'Certificate: ' . (empty(config('services.agora.app_certificate')) ? 'NOT SET' : 'SET') . PHP_EOL;
"
```

Expected output:
```
App ID: 8b5e9417f15a48ae929783f32d3d33d4
Certificate: SET
```

2. **Check Database Schema**:
```bash
php artisan tinker --execute="
\$columns = \Illuminate\Support\Facades\Schema::getColumnListing('calls');
echo 'agora_token exists: ' . (in_array('agora_token', \$columns) ? 'YES' : 'NO') . PHP_EOL;
echo 'channel_name exists: ' . (in_array('channel_name', \$columns) ? 'YES' : 'NO') . PHP_EOL;
"
```

Expected output:
```
agora_token exists: YES
channel_name exists: YES
```

3. **Clear Cache**:
```bash
php artisan config:clear
php artisan cache:clear
php artisan route:clear
```

---

### Issue: Tokens don't match between users

**This should NOT happen with the new implementation!**

If it does:
1. Check that the database columns exist
2. Verify Call model has updated `$fillable` array
3. Check logs for any database errors

---

## 📊 Expected Values

### agora_token Format
```
0078b5e9417f15a48ae929783f32d3d33d4AAAAIPfihYG5sG8...
```
- Starts with: `007` or `006`
- Length: 130-150 characters
- Contains: App ID + encrypted data

### channel_name Format
```
call_CALL_173563029912345
```
- Starts with: `call_`
- Contains: Full call ID
- Length: ~30 characters

### status Values
Valid statuses in the database:
- ✅ `CONNECTING` (most common for incoming calls)
- ✅ `PENDING`
- ❌ NOT "ringing" (this doesn't exist in schema)

---

## ✅ Quick Verification Checklist

- [ ] Agora App ID configured in `.env`
- [ ] Agora Certificate configured in `.env`
- [ ] Database migration ran successfully
- [ ] `agora_token` column exists in `calls` table
- [ ] `channel_name` column exists in `calls` table
- [ ] API returns non-null `agora_token`
- [ ] API returns non-null `channel_name`
- [ ] Token length is 130-150 characters
- [ ] Channel name format is `call_CALL_xxxxx`
- [ ] Caller can see "Ringing" screen
- [ ] Receiver can see incoming call dialog
- [ ] Both users connect after "Accept"
- [ ] Audio/video works between users

---

## 🎉 Success Criteria

**All of these should be true**:
1. ✅ `GET /calls/incoming` returns `agora_token` (not null)
2. ✅ `GET /calls/incoming` returns `channel_name` (not null)
3. ✅ Caller joins Agora channel successfully
4. ✅ Receiver sees incoming call notification
5. ✅ Receiver can click "Accept"
6. ✅ Receiver joins the same Agora channel
7. ✅ Both users see "Connected" screen
8. ✅ Audio/video streams work
9. ✅ Call controls are functional
10. ✅ Call ends properly with coin deduction

---

**Need Help?** Check the full documentation: `INCOMING_CALLS_FIX_COMPLETE.md`

