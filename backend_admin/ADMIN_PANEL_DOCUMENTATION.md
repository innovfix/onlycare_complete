# Only Care App - Admin Panel Development Documentation

## Table of Contents
1. [App Overview](#app-overview)
2. [Database Schema](#database-schema)
3. [Features & Functionality](#features--functionality)
4. [Business Logic](#business-logic)
5. [Admin Panel Requirements](#admin-panel-requirements)
6. [Use Cases](#use-cases)
7. [API Endpoints Required](#api-endpoints-required)

---

## App Overview

### Application Type
**Only Care** is a random voice and video calling app that connects male and female users with a coin-based economy system.

### Business Model
- **Male Users**: Purchase coins to make calls (consumers)
- **Female Users**: Earn coins by answering calls, can withdraw earnings (providers)

### Technology Stack
- **Frontend (Mobile)**: Android - Kotlin, Jetpack Compose, MVVM Architecture
- **DI Framework**: Hilt (Dagger)
- **Navigation**: Navigation Compose
- **State Management**: ViewModel + StateFlow
- **Network**: Retrofit, OkHttp
- **Real-time Calls**: Agora SDK
- **Push Notifications**: OneSignal
- **Image Loading**: Coil, Glide

### App Package
- **Package Name**: `com.onlycare.app`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Version**: 1.0.0

---

## Database Schema

### 1. USERS Table

```sql
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    phone VARCHAR(15) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    age INT CHECK (age >= 18 AND age <= 99),
    gender ENUM('MALE', 'FEMALE') NOT NULL,
    profile_image TEXT,
    bio TEXT,
    language ENUM('ENGLISH', 'HINDI', 'TAMIL', 'TELUGU', 'KANNADA', 'MALAYALAM', 'BENGALI', 'MARATHI'),
    interests TEXT, -- JSON array of interests
    is_online BOOLEAN DEFAULT FALSE,
    last_seen BIGINT, -- Unix timestamp
    rating DECIMAL(2,1) DEFAULT 0.0,
    total_ratings INT DEFAULT 0,
    coin_balance INT DEFAULT 0,
    total_earnings INT DEFAULT 0, -- For female users
    audio_call_enabled BOOLEAN DEFAULT TRUE,
    video_call_enabled BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    kyc_status ENUM('NOT_SUBMITTED', 'PENDING', 'APPROVED', 'REJECTED') DEFAULT 'NOT_SUBMITTED',
    is_blocked BOOLEAN DEFAULT FALSE,
    blocked_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    INDEX idx_gender (gender),
    INDEX idx_online (is_online),
    INDEX idx_phone (phone),
    INDEX idx_created_at (created_at)
);
```

**Column Details:**
- `id`: Unique user identifier (UUID)
- `phone`: User's phone number (authentication)
- `name`: Display name (3-100 characters)
- `age`: User's age (18-99 years)
- `gender`: MALE or FEMALE (cannot be changed)
- `profile_image`: URL to profile picture
- `bio`: User description/about
- `language`: Preferred language for app interface
- `interests`: JSON array like `["Music", "Movies", "Gaming"]`
- `is_online`: Current online status
- `last_seen`: Last activity timestamp
- `rating`: Average rating (0.0-5.0)
- `total_ratings`: Number of ratings received
- `coin_balance`: Current coin balance
- `total_earnings`: Total coins earned (female users only)
- `audio_call_enabled`: Female can receive audio calls
- `video_call_enabled`: Female can receive video calls
- `is_verified`: Female user verification status
- `kyc_status`: KYC verification status
- `is_blocked`: Admin blocked status
- `blocked_reason`: Reason for blocking

---

### 2. CALLS Table

```sql
CREATE TABLE calls (
    id VARCHAR(50) PRIMARY KEY,
    caller_id VARCHAR(50) NOT NULL,
    receiver_id VARCHAR(50) NOT NULL,
    call_type ENUM('AUDIO', 'VIDEO') NOT NULL,
    status ENUM('PENDING', 'CONNECTING', 'ONGOING', 'ENDED', 'MISSED', 'REJECTED', 'CANCELLED') NOT NULL,
    duration INT DEFAULT 0, -- in seconds
    coins_spent INT DEFAULT 0,
    coins_earned INT DEFAULT 0,
    coin_rate_per_minute INT DEFAULT 10, -- Rate at time of call
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rating DECIMAL(2,1) DEFAULT 0.0,
    feedback TEXT,
    
    FOREIGN KEY (caller_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_caller (caller_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_call_type (call_type)
);
```

**Column Details:**
- `id`: Unique call identifier
- `caller_id`: Male user who initiated the call
- `receiver_id`: Female user who received the call
- `call_type`: AUDIO or VIDEO
- `status`: Current status of the call
- `duration`: Call duration in seconds
- `coins_spent`: Coins deducted from caller
- `coins_earned`: Coins earned by receiver
- `coin_rate_per_minute`: Rate per minute (AUDIO: 10, VIDEO: 15)
- `rating`: Rating given by caller (0-5)
- `feedback`: Optional text feedback

---

### 3. COIN_PACKAGES Table

```sql
CREATE TABLE coin_packages (
    id VARCHAR(50) PRIMARY KEY,
    coins INT NOT NULL,
    price DECIMAL(10,2) NOT NULL, -- in INR
    original_price DECIMAL(10,2) NOT NULL,
    discount INT DEFAULT 0, -- percentage
    is_popular BOOLEAN DEFAULT FALSE,
    is_best_value BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_active (is_active),
    INDEX idx_sort_order (sort_order)
);
```

**Column Details:**
- `id`: Package identifier
- `coins`: Number of coins in package
- `price`: Current selling price
- `original_price`: Original price (before discount)
- `discount`: Discount percentage
- `is_popular`: Show "Popular" badge
- `is_best_value`: Show "Best Value" badge
- `is_active`: Package available for purchase
- `sort_order`: Display order (lower first)

---

### 4. TRANSACTIONS Table

```sql
CREATE TABLE transactions (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    type ENUM('PURCHASE', 'CALL', 'GIFT', 'WITHDRAWAL', 'BONUS', 'REFUND') NOT NULL,
    amount DECIMAL(10,2) DEFAULT 0, -- INR amount
    coins INT DEFAULT 0, -- Positive for credit, negative for debit
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED') NOT NULL,
    payment_method VARCHAR(50),
    payment_gateway_id VARCHAR(100), -- External payment ID
    reference_id VARCHAR(50), -- Related call_id or withdrawal_id
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user (user_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

**Column Details:**
- `id`: Transaction identifier
- `user_id`: User associated with transaction
- `type`: Transaction type
  - PURCHASE: Male user buying coins
  - CALL: Coins spent/earned from call
  - GIFT: Promotional/gifted coins
  - WITHDRAWAL: Female user withdrawing money
  - BONUS: Referral or promotional bonus
  - REFUND: Refunded coins
- `amount`: Money amount (INR)
- `coins`: Coin amount (+ or -)
- `status`: Transaction status
- `payment_method`: PhonePe, GPay, UPI, etc.
- `payment_gateway_id`: External transaction ID
- `reference_id`: Related entity ID (call_id, etc.)

---

### 5. MESSAGES Table

```sql
CREATE TABLE messages (
    id VARCHAR(50) PRIMARY KEY,
    sender_id VARCHAR(50) NOT NULL,
    receiver_id VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    message_type ENUM('TEXT', 'IMAGE', 'AUDIO', 'VIDEO') DEFAULT 'TEXT',
    media_url TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_conversation (sender_id, receiver_id),
    INDEX idx_created_at (created_at)
);
```

---

### 6. FRIENDSHIPS Table

```sql
CREATE TABLE friendships (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    friend_id VARCHAR(50) NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_friendship (user_id, friend_id),
    INDEX idx_user (user_id),
    INDEX idx_friend (friend_id),
    INDEX idx_status (status)
);
```

---

### 7. WITHDRAWALS Table

```sql
CREATE TABLE withdrawals (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL, -- INR
    coins INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED') DEFAULT 'PENDING',
    bank_account_id VARCHAR(50) NOT NULL,
    admin_notes TEXT,
    rejected_reason TEXT,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(id),
    
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_requested_at (requested_at)
);
```

**Business Rules:**
- Minimum withdrawal: ₹500 (500 coins)
- Conversion rate: 1 Coin = ₹1
- Processing time: 3-5 business days

---

### 8. BANK_ACCOUNTS Table

```sql
CREATE TABLE bank_accounts (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    account_holder_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(30) NOT NULL,
    ifsc_code VARCHAR(11) NOT NULL,
    bank_name VARCHAR(100),
    branch_name VARCHAR(100),
    upi_id VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user (user_id)
);
```

---

### 9. KYC_DOCUMENTS Table

```sql
CREATE TABLE kyc_documents (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    document_type ENUM('AADHAAR', 'PAN', 'SELFIE') NOT NULL,
    document_number VARCHAR(50),
    document_url TEXT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    rejected_reason TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    verified_by VARCHAR(50), -- admin_id
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user (user_id),
    INDEX idx_status (status)
);
```

---

### 10. BLOCKED_USERS Table

```sql
CREATE TABLE blocked_users (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    blocked_user_id VARCHAR(50) NOT NULL,
    blocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_block (user_id, blocked_user_id),
    INDEX idx_user (user_id)
);
```

---

### 11. REFERRALS Table

```sql
CREATE TABLE referrals (
    id VARCHAR(50) PRIMARY KEY,
    referrer_id VARCHAR(50) NOT NULL,
    referred_user_id VARCHAR(50) NOT NULL,
    referral_code VARCHAR(20) NOT NULL,
    bonus_coins INT DEFAULT 0,
    is_claimed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    claimed_at TIMESTAMP NULL,
    
    FOREIGN KEY (referrer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (referred_user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_referrer (referrer_id),
    INDEX idx_code (referral_code),
    INDEX idx_created_at (created_at)
);
```

**Referral Rewards:**
- Referrer gets: 100 coins when referred user completes first call
- Referred user gets: 50 coins on signup

---

### 12. APP_SETTINGS Table

```sql
CREATE TABLE app_settings (
    id VARCHAR(50) PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT NOT NULL,
    setting_type ENUM('STRING', 'INTEGER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_key (setting_key)
);
```

**Key Settings:**
- `audio_call_rate`: 10 coins/minute
- `video_call_rate`: 15 coins/minute
- `min_withdrawal_amount`: 500
- `referral_bonus_referrer`: 100
- `referral_bonus_referred`: 50
- `coin_to_inr_rate`: 1
- `male_welcome_bonus`: 100
- `female_welcome_bonus`: 0

---

### 13. REPORTS Table

```sql
CREATE TABLE reports (
    id VARCHAR(50) PRIMARY KEY,
    reporter_id VARCHAR(50) NOT NULL,
    reported_user_id VARCHAR(50) NOT NULL,
    report_type ENUM('INAPPROPRIATE_BEHAVIOR', 'HARASSMENT', 'SPAM', 'FAKE_PROFILE', 'OTHER') NOT NULL,
    description TEXT,
    status ENUM('PENDING', 'REVIEWING', 'RESOLVED', 'DISMISSED') DEFAULT 'PENDING',
    admin_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    resolved_by VARCHAR(50), -- admin_id
    
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reported_user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_reporter (reporter_id),
    INDEX idx_reported (reported_user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

---

### 14. NOTIFICATIONS Table

```sql
CREATE TABLE notifications (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type ENUM('CALL', 'MESSAGE', 'PAYMENT', 'SYSTEM', 'PROMOTIONAL') NOT NULL,
    reference_id VARCHAR(50), -- Related entity ID
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user (user_id),
    INDEX idx_read (is_read),
    INDEX idx_created_at (created_at)
);
```

---

### 15. ADMINS Table

```sql
CREATE TABLE admins (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('SUPER_ADMIN', 'ADMIN', 'MODERATOR', 'FINANCE', 'SUPPORT') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);
```

**Admin Roles:**
- `SUPER_ADMIN`: Full access
- `ADMIN`: User management, content moderation
- `MODERATOR`: Content moderation only
- `FINANCE`: Withdrawal approvals, financial reports
- `SUPPORT`: User support, view-only

---

See **DATABASE_SCHEMA_EXTENDED.md** for additional tables and detailed relationships.

