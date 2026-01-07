# Only Care - Business Logic & Features Documentation

## Business Logic & Rules

### 1. User Registration & Authentication

#### Male User Flow
1. Enter phone number
2. Verify OTP (hardcoded: 123456 for testing)
3. Select gender (MALE)
4. Select preferred language (8 options)
5. Setup profile (name, age, bio, interests)
6. Grant permissions (camera, mic, storage, notifications)
7. Receive 100 welcome bonus coins
8. Redirected to Male Home Screen

#### Female User Flow
1. Enter phone number
2. Verify OTP
3. Select gender (FEMALE)
4. Select preferred language
5. Setup profile
6. Voice identification recording (for verification)
7. Grant permissions
8. Redirected to Female Home Screen

**Validation Rules:**
- Phone: 10 digits
- OTP: 6 digits (123456 for testing)
- Name: 3-100 characters
- Age: 18-99 years
- Bio: Optional, max 500 characters
- Interests: Select 1-6 from 12 available options

---

### 2. Call System

#### Call Types
- **Audio Call**: 10 coins/minute
- **Video Call**: 15 coins/minute

#### Call Flow (Male Initiates)
1. Male user browses female users
2. Clicks Audio/Video call button
3. System checks:
   - Male has sufficient coins (minimum 10 for audio, 15 for video)
   - Female is online
   - Female has enabled call type
   - Neither user has blocked the other
4. Show "Connecting..." screen
5. Send notification to female
6. Female accepts/rejects
7. If accepted:
   - Start call
   - Start coin deduction (real-time)
   - Start duration timer
8. During call:
   - Deduct coins per minute from male
   - Add coins per minute to female
   - Show live coin counter
9. Call ends when:
   - Either party hangs up
   - Male runs out of coins
   - Connection lost
10. Show call summary
11. Request rating from male (1-5 stars)

#### Call Pricing
```
Audio Call: 10 coins/minute
- Per second rate: 0.167 coins/second
- Minimum charge: 1 minute

Video Call: 15 coins/minute
- Per second rate: 0.25 coins/second
- Minimum charge: 1 minute
```

#### Auto-Disconnect Rules
- Male coin balance < 10 (audio) or < 15 (video)
- Call duration > 60 minutes (requires confirmation to continue)
- Either party loses internet for > 30 seconds

---

### 3. Coin Economy

#### Male Users - Coin Purchase

**Coin Packages:**
| Package ID | Coins | Original Price | Selling Price | Discount |
|------------|-------|----------------|---------------|----------|
| 1 | 100 | ₹150 | ₹99 | 34% |
| 2 | 500 | ₹750 | ₹399 | 47% (Popular) |
| 3 | 1000 | ₹1500 | ₹699 | 53% (Best Value) |
| 4 | 2500 | ₹3750 | ₹1499 | 60% |
| 5 | 5000 | ₹7500 | ₹2499 | 67% |
| 6 | 10000 | ₹15000 | ₹3999 | 73% |

**Payment Methods:**
- PhonePe
- Google Pay
- Paytm
- UPI
- Credit/Debit Card
- Net Banking

**Purchase Flow:**
1. User selects package
2. Redirected to payment screen
3. Selects payment method
4. Completes payment via gateway
5. On success:
   - Coins added to balance
   - Transaction recorded
   - User notified
6. On failure:
   - Show error
   - Allow retry

**Transaction Record:**
```json
{
  "type": "PURCHASE",
  "coins": 500,
  "amount": 399.0,
  "payment_method": "PhonePe",
  "status": "SUCCESS"
}
```

#### Female Users - Earnings

**Earning Rules:**
- Audio call: 10 coins/minute
- Video call: 15 coins/minute
- Minimum call duration for earnings: 10 seconds
- Coins credited immediately after call ends

**Conversion Rate:**
- 1 Coin = ₹1

**Withdrawal Rules:**
- Minimum withdrawal: ₹500 (500 coins)
- Maximum withdrawal per day: ₹50,000
- Processing time: 3-5 business days
- Requires verified bank account
- KYC must be approved

**Withdrawal Flow:**
1. Female user goes to Earnings screen
2. Clicks "Withdraw Earnings"
3. Enter amount (≥ ₹500)
4. Select bank account
5. Confirm withdrawal
6. Admin reviews and approves
7. Amount transferred to bank
8. User notified on completion

---

### 4. User Matching & Discovery

#### Female User Display (Male Home)
- Shows female users in 2-column grid
- Filters:
  - Online users shown first
  - Verified users prioritized
  - Users with enabled call types
  - Exclude blocked users
- Sort by:
  - Online status (online first)
  - Rating (highest first)
  - Last active (recent first)

#### User Card Information
- Profile picture
- Name, Age
- Online/Offline status
- Rating (stars)
- Call enabled indicators
- Quick call buttons (Audio/Video)

#### Random Call Feature
- Male clicks "Random Call" FAB
- System finds random online female user:
  - Is online
  - Has audio calls enabled
  - Has not blocked the male user
  - Has good rating (>3.5)
- Initiates call immediately

---

### 5. Chat System

#### Chat Features
- Real-time text messaging
- Message status (sent, delivered, read)
- Online status indicator
- Unread count badges
- Last message preview
- Conversation list sorted by recent activity

#### Chat Rules
- Can only chat with:
  - Friends
  - Users you've called
  - Users who've called you
- Blocked users cannot send messages
- Maximum message length: 1000 characters
- Image/video sharing (planned feature)

#### Message Types
- TEXT: Regular text messages
- IMAGE: Image attachments
- AUDIO: Voice messages
- VIDEO: Video messages

---

### 6. Friends System

#### Add Friend
1. View user profile
2. Click "Add Friend"
3. Friend request sent
4. Recipient accepts/rejects
5. If accepted, both become friends

#### Friend Benefits
- Direct messaging
- See online status
- Priority in random calls
- Reduced call rates (planned)

#### Friend List
- Shows all accepted friends
- Quick call/chat buttons
- Online status
- Last seen timestamp

---

### 7. Rating System

#### Rating Flow
1. Call ends
2. Show rating screen to caller
3. Select 1-5 stars
4. Optional text feedback
5. Submit or skip
6. Rating saved

#### Rating Calculation
```
New Average = ((Old Average × Total Ratings) + New Rating) / (Total Ratings + 1)
```

#### Rating Display
- Shown on user profiles
- Shown on user cards
- Affects user visibility (low ratings = less discovery)

#### Low Rating Actions
- Rating < 2.0: User shown warning
- Rating < 1.5: User profile review required
- Rating < 1.0: Temporary suspension

---

### 8. Female-Specific Features

#### Call Availability Toggle
- Audio Call: ON/OFF switch
- Video Call: ON/OFF switch
- When OFF:
  - User not shown to male users for that call type
  - Cannot receive calls of that type
  - User shows as "unavailable"

#### Earnings Dashboard
**Displays:**
- Total Earnings: All-time coins earned
- Today's Earnings: Coins earned today
- This Week: Coins earned this week
- This Month: Coins earned this month
- Total Calls: Number of calls received
- Average per Call: Average earnings per call
- Total Duration: Total call time

**Earnings Breakdown:**
```
Total Earnings = Sum of (coins_earned from all calls)
Available Balance = Total Earnings - Withdrawn - Pending Withdrawals
```

#### Recent Callers
- List of recent male callers
- Caller info: name, age, rating
- Call details: duration, earnings, timestamp
- Quick actions: Call back, Chat, Block

---

### 9. KYC Verification (Female Users)

#### Required Documents
1. **Aadhaar Card**
   - Front and back photo
   - 12-digit number
2. **PAN Card**
   - Card photo
   - 10-character PAN number
3. **Selfie**
   - Live photo for verification

#### Verification Flow
1. User submits documents
2. Status: PENDING
3. Admin reviews documents
4. Admin approves or rejects
5. If approved:
   - KYC status: APPROVED
   - User can withdraw earnings
6. If rejected:
   - Show rejection reason
   - Allow resubmission

#### Verification Criteria
- Name matches on all documents
- Documents are clear and readable
- Aadhaar number is valid
- PAN number is valid
- Selfie matches document photos

---

### 10. Bank Account Management

#### Add Bank Account
**Required Fields:**
- Account Holder Name (must match KYC)
- Account Number (10-18 digits)
- Confirm Account Number
- IFSC Code (11 characters)
- Bank Name (auto-filled from IFSC)
- Branch Name (auto-filled from IFSC)

**Optional:**
- UPI ID (for faster transfers)

#### Validation
- Account number confirmed matches original
- IFSC code format: XXXX0YYYYYY
- Account holder name matches KYC name
- One primary account required for withdrawals

---

### 11. Referral System

#### How It Works
1. User has unique referral code (e.g., "USER123ABC")
2. User shares code with friends
3. Friend signs up using code
4. Friend completes first call/purchase
5. Rewards distributed:
   - Referrer: 100 coins
   - Referred user: 50 coins

#### Referral Tracking
```sql
referrer_id: User who shared code
referred_user_id: New user who used code
bonus_coins: Reward amount
is_claimed: TRUE when conditions met
```

#### Referral Limits
- Maximum referrals per user: Unlimited
- Maximum bonus per month: 5000 coins
- Minimum activity for referred user: 1 completed call or 1 purchase

---

### 12. Blocking & Reporting

#### User Blocking
**User can block:**
- Any user they've interacted with
- Users from search/discovery

**Effects of blocking:**
- Cannot call each other
- Cannot message each other
- Cannot see each other's profiles
- Removed from discovery

**Unblock:**
- User can unblock anytime from Blocked Users screen

#### Reporting Users
**Report Types:**
- Inappropriate Behavior
- Harassment
- Spam
- Fake Profile
- Other (with description)

**Report Flow:**
1. User reports another user
2. Provides reason and description
3. Report submitted to admin
4. Admin reviews report
5. Admin takes action:
   - Warning to reported user
   - Temporary suspension
   - Permanent ban
   - No action (dismiss report)

---

### 13. Notifications

#### Notification Types
1. **Call Notifications**
   - Incoming call alert
   - Missed call
   - Call ended

2. **Message Notifications**
   - New message received
   - Unread message count

3. **Payment Notifications**
   - Coins purchased successfully
   - Payment failed
   - Withdrawal approved
   - Withdrawal completed

4. **System Notifications**
   - Profile verification approved/rejected
   - KYC approved/rejected
   - Account warning
   - Account suspended

5. **Promotional Notifications**
   - New coin offers
   - Referral bonuses
   - Special events

#### Notification Delivery
- Push notifications (OneSignal)
- In-app notification center
- Badge counts on tabs

---

### 14. Settings & Privacy

#### Account Settings
- Edit Profile
- Change Language
- Manage Phone Number
- Delete Account (with confirmation)

#### Privacy Settings
- Block Users
- Report Users
- Call Preferences (Female only)
- Online Status Visibility

#### App Settings
- Notifications ON/OFF
- Sound Effects ON/OFF
- Vibration ON/OFF
- Data Saver Mode

---

### 15. Content Moderation

#### Auto-Moderation
- Profanity filter in messages
- Inappropriate image detection
- Spam detection (multiple same messages)

#### Admin Moderation
- Review reported users
- Review flagged content
- Review low-rated users
- Manual account suspension

#### Suspension Types
1. **Warning**: Notification sent, no restrictions
2. **Temporary Suspension**: 7/15/30 days
3. **Permanent Ban**: Account disabled

**Reasons for suspension:**
- Multiple reports (>5 in 30 days)
- Rating < 1.0
- Inappropriate content
- Payment fraud
- Multiple account violations

---

## Feature Permissions by User Type

### Male Users Can:
✅ Browse female users
✅ Make audio/video calls
✅ Purchase coins
✅ Send messages
✅ Add friends
✅ View call history
✅ View transaction history
✅ Rate female users
✅ Refer friends
✅ Edit profile
✅ Block users
✅ Report users

### Male Users Cannot:
❌ Earn coins from calls
❌ Withdraw money
❌ Toggle call availability
❌ Submit KYC
❌ Add bank accounts
❌ View earnings dashboard

### Female Users Can:
✅ Receive audio/video calls
✅ Earn coins from calls
✅ Withdraw earnings
✅ Toggle call availability
✅ Send messages
✅ Add friends
✅ View recent callers
✅ View earnings dashboard
✅ Submit KYC
✅ Add bank accounts
✅ Edit profile
✅ Block users
✅ Report users
✅ Refer friends

### Female Users Cannot:
❌ Initiate calls (planned feature)
❌ Purchase coins
❌ Browse other female users
❌ View transaction history (only earnings)

---

## Interest Categories

Available interests for profile setup:
1. Music
2. Movies
3. Sports
4. Gaming
5. Travel
6. Food
7. Photography
8. Art
9. Books
10. Fitness
11. Technology
12. Fashion

---

## Language Options

Supported languages:
1. English
2. हिंदी (Hindi)
3. தமிழ் (Tamil)
4. తెలుగు (Telugu)
5. ಕನ್ನಡ (Kannada)
6. മലയാളം (Malayalam)
7. বাংলা (Bengali)
8. मराठी (Marathi)

---

## Call Status Lifecycle

```
PENDING → User initiating call
    ↓
CONNECTING → Waiting for receiver to accept
    ↓
ONGOING → Active call in progress
    ↓
ENDED → Call completed normally
```

**Alternative paths:**
- PENDING → CANCELLED (caller cancels before receiver responds)
- CONNECTING → MISSED (receiver doesn't respond)
- CONNECTING → REJECTED (receiver rejects)
- ONGOING → ENDED (normal completion)

---

## Transaction Status Lifecycle

```
PENDING → Payment initiated
    ↓
SUCCESS → Payment confirmed, coins added
```

**Alternative path:**
- PENDING → FAILED → Payment gateway error
- PENDING → CANCELLED → User cancelled payment

---

This documentation provides the business logic required to build backend APIs and admin panel features.

