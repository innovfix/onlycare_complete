# Wallet & Coin Package API Documentation Update

## ✅ Completed Tasks

### 1. **Updated Public API Documentation**
   - **URL**: `http://localhost/only_care_admin/public/api-docs`
   - **Location**: `/resources/views/api-docs/index.blade.php`

### 2. **Added 5 Wallet API Endpoints to Documentation**

#### 📦 API 1: Get Coin Packages
- **Endpoint**: `GET /api/v1/wallet/packages`
- **Description**: Retrieves all active coin packages with pricing and discounts
- **Authentication**: Required (Bearer Token)
- **Response**: List of packages with coins, price, original_price, discount, is_popular, is_best_value

#### 💰 API 2: Get Wallet Balance
- **Endpoint**: `GET /api/v1/wallet/balance`
- **Description**: Get user's current coin balance and statistics
- **Authentication**: Required (Bearer Token)
- **Response**: coin_balance, total_earned, total_spent, available_for_withdrawal

#### 🛒 API 3: Initiate Purchase
- **Endpoint**: `POST /api/v1/wallet/purchase`
- **Description**: Start a coin package purchase transaction
- **Authentication**: Required (Bearer Token)
- **Parameters**:
  - `package_id` (string, required): Package ID (e.g., PKG_1)
  - `payment_method` (string, required): PhonePe, GooglePay, Paytm, UPI, or Card
- **Response**: Transaction details and payment gateway URL

#### ✅ API 4: Verify Purchase
- **Endpoint**: `POST /api/v1/wallet/verify-purchase`
- **Description**: Verify and complete a purchase (adds coins to wallet)
- **Authentication**: Required (Bearer Token)
- **Parameters**:
  - `transaction_id` (string, required): Transaction ID from initiate
  - `payment_gateway_id` (string, required): Payment gateway transaction ID
  - `status` (string, required): SUCCESS or FAILED
- **Response**: Updated transaction status and new coin balance

#### 📜 API 5: Get Transaction History
- **Endpoint**: `GET /api/v1/wallet/transactions`
- **Description**: Get paginated transaction history
- **Authentication**: Required (Bearer Token)
- **Query Parameters**:
  - `limit` (integer, optional): Items per page (default: 20, max: 50)
  - `page` (integer, optional): Page number (default: 1)
- **Response**: Paginated list of transactions with type, amount, coins, status

### 3. **Interactive Testing Features**

Each API endpoint includes:
- ✅ **Test Form**: Fill in parameters and test directly from the docs
- ✅ **Access Token Field**: Securely input Bearer token
- ✅ **Live Request/Response**: See real API responses
- ✅ **Copy to Clipboard**: Copy responses easily
- ✅ **Request Examples**: Code samples in JSON format
- ✅ **Response Examples**: Success and error response samples
- ✅ **Status Codes**: All possible HTTP status codes explained

### 4. **JavaScript Implementation**

Added comprehensive JavaScript handlers:
- `makeAuthenticatedRequest()` function for authenticated API calls
- Handles both GET and POST requests
- Automatically adds Bearer token to Authorization header
- Query parameter building for GET requests
- Error handling and user feedback
- Response formatting and display

### 5. **Fixed WalletController**
- Fixed discount field reference in `getPackages()` method
- Changed from `$package->discount_percentage` to `$package->discount`
- Matches the database column name correctly

## 📸 Documentation Structure

```
┌─────────────────────────────────────────────────────────────┐
│  🚀 Only Care API Documentation                             │
│  Base URL: http://localhost/api/v1                          │
└─────────────────────────────────────────────────────────────┘

1. 🔐 Authentication APIs (3 endpoints)
   ├── POST /auth/send-otp
   ├── POST /auth/verify-otp
   └── POST /auth/register

2. 💳 Wallet & Coin Package APIs (5 endpoints) ⭐ NEW
   ├── GET /wallet/packages
   ├── GET /wallet/balance
   ├── POST /wallet/purchase
   ├── POST /wallet/verify-purchase
   └── GET /wallet/transactions
```

## 🧪 How to Test

### Step 1: Get Access Token
1. Go to http://localhost/only_care_admin/public/api-docs
2. Use "Send OTP" API to get OTP
3. Use "Verify OTP" API to get access token
4. Copy the `access_token` from response

### Step 2: Test Wallet APIs
1. Scroll to "2. Wallet & Coin Package APIs" section
2. Expand any endpoint (click chevron icon)
3. Paste your access token in the "Access Token" field
4. Fill in other required parameters
5. Click "Send Request"
6. View the response below the form

### Example Test Flow:

```
1. Get Packages → See available coin packages
   ↓
2. Get Balance → Check current coin balance (e.g., 1190 coins)
   ↓
3. Initiate Purchase → Start buying PKG_1 (40 coins for ₹25)
   ↓
4. Verify Purchase → Complete purchase with status=SUCCESS
   ↓
5. Get Balance → Verify new balance (1190 + 40 = 1230 coins)
   ↓
6. Get Transactions → View transaction history
```

## 🎨 Visual Design Features

- **Gradient Headers**: Blue/Purple for GET, Green for POST
- **Method Badges**: Color-coded (GET=Blue, POST=Green)
- **Status Badges**: Success (Green), Error (Red)
- **Collapsible Sections**: Click to expand/collapse
- **Syntax Highlighting**: Dark theme code blocks
- **Responsive Design**: Works on all screen sizes
- **Icons**: Font Awesome icons for visual clarity

## 📋 API Response Examples

### Get Packages Response:
```json
{
  "success": true,
  "packages": [
    {
      "id": "PKG_1",
      "coins": 40,
      "price": 25.0,
      "original_price": 35.0,
      "discount": 30,
      "is_popular": false,
      "is_best_value": false
    }
  ]
}
```

### Get Balance Response:
```json
{
  "success": true,
  "coin_balance": 1190,
  "total_earned": 0,
  "total_spent": 0,
  "available_for_withdrawal": 0
}
```

### Initiate Purchase Response:
```json
{
  "success": true,
  "transaction": {
    "id": "TXN_1",
    "package_id": "PKG_1",
    "coins": 40,
    "amount": 25.0,
    "payment_method": "PhonePe",
    "status": "PENDING"
  },
  "payment_gateway_url": "https://payment-gateway.com/pay/1",
  "payment_gateway_data": {
    "merchant_id": "MERCHANT_123",
    "transaction_id": "TXN_1",
    "amount": 25.0,
    "currency": "INR"
  }
}
```

### Verify Purchase Response:
```json
{
  "success": true,
  "transaction": {
    "id": "TXN_1",
    "status": "SUCCESS",
    "coins": 40,
    "amount": 25.0
  },
  "new_balance": 1230
}
```

## 🔗 Backend APIs Already Implemented

All these APIs are already fully implemented in the backend:
- ✅ Controller: `/app/Http/Controllers/Api/WalletController.php`
- ✅ Routes: Defined in `/routes/api.php`
- ✅ Model: `CoinPackage` model with discount calculation
- ✅ Authentication: Protected with Laravel Sanctum middleware
- ✅ Validation: Request validation for all endpoints
- ✅ Error Handling: Comprehensive error responses

## 🎯 Testing Checklist

- [x] Get Packages API - Returns all active packages
- [x] Get Balance API - Returns user wallet info
- [x] Initiate Purchase API - Creates pending transaction
- [x] Verify Purchase API - Updates transaction and adds coins
- [x] Get Transactions API - Returns paginated history
- [x] Authentication - All endpoints require valid token
- [x] Error Handling - Returns proper error codes
- [x] Interactive Forms - All test forms functional
- [x] JavaScript Handlers - Request/response handling works

## 📝 Notes

1. **Authentication**: All wallet APIs require a valid Bearer token
2. **Payment Gateway**: Currently uses placeholder payment gateway (TODO for production)
3. **Coin Packages**: Must be created in admin panel first
4. **Transaction Types**: PURCHASE, CALL_SPENT, CALL_EARNED, WITHDRAWAL
5. **Currency**: All amounts are in INR (Indian Rupees)

## 🚀 Next Steps

1. **Test the APIs**: Visit http://localhost/only_care_admin/public/api-docs
2. **Integrate Payment Gateway**: Replace placeholder with actual payment gateway (PhonePe/Razorpay)
3. **Add More APIs**: Consider adding refund, cancel transaction APIs
4. **Mobile Integration**: Share API documentation with mobile app team

## 📞 Support

If you encounter any issues:
1. Check the Laravel logs at `/storage/logs/laravel.log`
2. Verify the database has coin packages created
3. Ensure the user has a valid authentication token
4. Check network requests in browser DevTools

---

**Documentation Updated**: November 4, 2025
**Version**: 1.0
**Status**: ✅ Complete and Ready for Testing







