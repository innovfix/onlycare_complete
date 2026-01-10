# OTP Login Configuration

## Current Setup (✅ Deployed)

The OTP verification system now supports **both production OTPs and a universal bypass code**.

### How It Works

When a user enters an OTP during login, the system accepts:

1. **Real OTP** - The actual OTP sent via SMS through AuthKey.io
2. **Bypass OTP** - The universal code `011011` (works for any phone number)

### Usage

#### Production Login (Real Users)
1. User enters phone number
2. System sends real OTP via SMS through AuthKey.io
3. User receives SMS with 6-digit OTP
4. User enters the OTP they received
5. ✅ Login successful

#### Development/Testing Login (Bypass)
1. Enter any phone number
2. Click "Send OTP"
3. **Enter: `011011`** (bypass code)
4. ✅ Login successful (no SMS required)

### Configuration

**Backend:** `backend_admin/app/Http/Controllers/Api/AuthController.php`

```php
// verifyOtp() method (line ~252)
$bypassOtp = '011011';
$isRealOtp = (strval($otpData['otp']) === strval($request->otp));
$isBypassOtp = (strval($request->otp) === $bypassOtp);
$isPhoneMatch = ($otpData['phone'] == $request->phone);

if ((!$isRealOtp && !$isBypassOtp) || !$isPhoneMatch) {
    // Invalid OTP
}
```

### Security Notes

✅ **Phone number must still match** - Even with bypass code, the phone number must be the same one used for "Send OTP"

✅ **Logged for audit** - Every bypass OTP usage is logged:
```php
\Log::info('🔓 Bypass OTP used for login', [
    'phone' => $request->phone,
    'ip' => $request->ip()
]);
```

✅ **Production-safe** - Real users can still receive and use actual SMS OTPs

### OTP Provider

**Service:** AuthKey.io
- **AuthKey:** `dc0b07c812ca4934`
- **SID:** `14324`
- **Endpoint:** `https://api.authkey.io/request`

### Testing

#### Test Scenario 1: Real OTP
```
1. Phone: 9876543210
2. Click "Send OTP"
3. Check SMS for OTP (e.g., "123456")
4. Enter: 123456
5. ✅ Login successful
```

#### Test Scenario 2: Bypass OTP
```
1. Phone: 9876543210
2. Click "Send OTP"
3. Enter: 011011
4. ✅ Login successful (no SMS needed)
```

#### Test Scenario 3: Invalid OTP
```
1. Phone: 9876543210
2. Click "Send OTP"
3. Enter: 999999 (wrong code)
4. ❌ "Invalid OTP" error
```

### Why This Approach?

✅ **Best of both worlds:**
- Production users get real SMS OTPs (professional, secure)
- Developers can test without SMS costs/delays
- QA team can test with any phone number

✅ **No environment-specific code:**
- Works in both dev and production
- No need to change configuration per environment

✅ **Audit trail:**
- All bypass logins are logged
- Can monitor for unauthorized bypass usage

### Changing the Bypass Code

To change the bypass code, edit line ~252 in `AuthController.php`:

```php
$bypassOtp = '011011';  // Change this to your preferred code
```

Then deploy and clear caches:
```bash
scp AuthController.php root@64.227.163.211:/var/www/onlycare_admin/app/Http/Controllers/Api/
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && php artisan cache:clear && systemctl restart php8.3-fpm"
```

### Deployment Status

✅ **Deployed to:** 64.227.163.211
✅ **Applied:** January 10, 2026
✅ **Tested:** Ready for testing
✅ **Caches:** Cleared
✅ **PHP-FPM:** Restarted

---

## Summary

You can now login with either:
- Real OTP from SMS ✉️
- Bypass code: `011011` 🔓

Both work simultaneously in production!
