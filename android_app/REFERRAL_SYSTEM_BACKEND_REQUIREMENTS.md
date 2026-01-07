# Referral System Backend Requirements

## Overview
The referral system has been updated to support different reward types based on user gender:
- **Female to Female**: Referrer gets 50 Rs when referred person completes KYC
- **Male to Male**: Referrer gets 10 coins when referred person registers

## API Changes Required

### 1. Register API (`POST /api/v1/auth/register`)

**Request Body:**
```json
{
  "phone": "1234567890",
  "gender": "MALE" | "FEMALE",
  "avatar_id": "avatar123",
  "language": "ENGLISH",
  "age": 25,  // Optional, for female users
  "interests": ["Music", "Movies"],  // Optional, for female users
  "description": "Bio text",  // Optional, for female users
  "referral_code": "ABC12345"  // NEW: Optional referral code
}
```

**Backend Logic:**
- If `referral_code` is provided, validate it exists and belongs to a user
- Store the referral relationship (referrer_id, referred_user_id)
- **For Male to Male referrals:**
  - Give referrer 10 coins immediately upon registration
  - Update referrer's coin balance
- **For Female to Female referrals:**
  - Store referral relationship but DO NOT give reward yet
  - Reward (50 Rs) will be given when referred person completes KYC

### 2. Referral Code API (`GET /api/v1/referral/code`)

**Response should include reward type based on user gender:**

```json
{
  "success": true,
  "referral_code": "ABC12345",
  "referral_url": "https://onlycare.app/ref/ABC12345",
  "my_invites": 5,
  "per_invite_coins": 10,  // For male users
  "per_invite_rupees": 50,  // For female users (NEW)
  "total_coins_earned": 50,  // For male users
  "total_rupees_earned": 250,  // For female users (NEW)
  "reward_type": "COINS" | "RUPEES",  // NEW: Based on user gender
  "share_message": "Join me on OnlyCare! Use my referral code ABC12345...",
  "whatsapp_share_url": "https://wa.me/?text=..."
}
```

**Backend Logic:**
- If user is MALE: return `per_invite_coins: 10`, `reward_type: "COINS"`
- If user is FEMALE: return `per_invite_rupees: 50`, `reward_type: "RUPEES"`

### 3. Referral History API (`GET /api/v1/referral/history`)

**Response should include reward type:**

```json
{
  "success": true,
  "referrals": [
    {
      "id": "ref123",
      "referred_user": {
        "id": "user456",
        "name": "John Doe",
        "phone": "1234567890",
        "profile_image": "https://..."
      },
      "bonus_coins": 10,  // For male referrers
      "bonus_rupees": 50,  // For female referrers (NEW)
      "reward_type": "COINS" | "RUPEES",  // NEW
      "is_claimed": true,
      "created_at": "2024-01-01T00:00:00Z",
      "created_at_formatted": "Jan 1, 2024",
      "claimed_at": "2024-01-01T00:00:00Z"
    }
  ],
  "pagination": { ... }
}
```

### 4. KYC Completion API (`POST /api/v1/kyc/submit` or similar)

**When a female user completes KYC:**

1. Check if this user was referred by another female user
2. If yes, give the referrer 50 Rs
3. Update the referral record to mark reward as given
4. Update referrer's earnings balance

**Backend Logic:**
```php
// Pseudo-code
if ($user->gender == 'FEMALE' && $user->referred_by) {
    $referrer = User::find($user->referred_by);
    if ($referrer->gender == 'FEMALE') {
        // Give 50 Rs to referrer
        $referrer->earnings_balance += 50;
        $referrer->save();
        
        // Update referral record
        $referral = Referral::where('referred_user_id', $user->id)
            ->where('referrer_id', $referrer->id)
            ->first();
        if ($referral && !$referral->is_claimed) {
            $referral->bonus_rupees = 50;
            $referral->is_claimed = true;
            $referral->claimed_at = now();
            $referral->save();
        }
    }
}
```

## Database Schema Updates

### Referrals Table
```sql
CREATE TABLE referrals (
    id VARCHAR(255) PRIMARY KEY,
    referrer_id VARCHAR(255) NOT NULL,
    referred_user_id VARCHAR(255) NOT NULL,
    referral_code VARCHAR(8) NOT NULL,
    bonus_coins INT DEFAULT NULL,  -- For male referrers
    bonus_rupees INT DEFAULT NULL,  -- NEW: For female referrers
    reward_type ENUM('COINS', 'RUPEES') DEFAULT NULL,  -- NEW
    is_claimed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    claimed_at TIMESTAMP NULL,
    FOREIGN KEY (referrer_id) REFERENCES users(id),
    FOREIGN KEY (referred_user_id) REFERENCES users(id),
    INDEX idx_referrer (referrer_id),
    INDEX idx_referred (referred_user_id)
);
```

### Users Table
Add/Update:
- `referred_by` VARCHAR(255) NULL - ID of user who referred this user
- `earnings_balance` INT DEFAULT 0 - For female users (Rupees earned)

## Flow Summary

### Male to Male Referral Flow:
1. User A (male) shares referral code
2. User B (male) registers with User A's referral code
3. **Immediately**: User A gets 10 coins added to coin_balance
4. Referral record created with `bonus_coins: 10`, `reward_type: "COINS"`, `is_claimed: true`

### Female to Female Referral Flow:
1. User A (female) shares referral code
2. User B (female) registers with User A's referral code
3. Referral record created with `bonus_rupees: NULL`, `reward_type: "RUPEES"`, `is_claimed: false`
4. **When User B completes KYC**: User A gets 50 Rs added to earnings_balance
5. Referral record updated with `bonus_rupees: 50`, `is_claimed: true`, `claimed_at: now()`

### Important: Apply timing (client behavior)
- **Male users**: client should call `POST /api/v1/referral/apply` immediately after successful registration (to finalize crediting).
- **Female users**: client should call `POST /api/v1/referral/apply` only after KYC success (to finalize crediting).

### Referred user bonus
- **Not required**: in the current product rule, **only the referrer gets the reward** (referred user gets no bonus).

## Important Notes

1. **KYC Requirement**: Female referrers only get reward when referred person completes KYC, not on registration
2. **Reward Types**: 
   - Male users earn COINS (added to coin_balance)
   - Female users earn RUPEES (added to earnings_balance)
3. **Validation**: Ensure referral code belongs to a user of the same gender (optional but recommended)
4. **Prevent Self-Referral**: Users cannot use their own referral code

## Testing Checklist

- [ ] Male user registers with male referral code → Referrer gets 10 coins immediately
- [ ] Female user registers with female referral code → No reward yet
- [ ] Female user completes KYC → Referrer gets 50 Rs
- [ ] Referral code API returns correct reward_type based on user gender
- [ ] Referral history shows correct reward type and amounts
- [ ] Self-referral is prevented
- [ ] Cross-gender referrals are handled (optional: allow or reject)
