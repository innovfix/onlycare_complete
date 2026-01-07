# Paysprint UPI Verification Documentation

## Overview

This documentation covers the UPI ID verification functionality using Paysprint API. It includes the verification function implementation and its usage in the `update_upi` API, focusing solely on verification without payout/payment gateway integration.

---

## Table of Contents

1. [validateUpiIdWithPaysprint() Function](#1-validateupiidwithpaysprint-function)

2. [Usage in update_upi() API](#2-usage-in-update_upi-api)

3. [Helper Function: generatePaysprintToken()](#3-helper-function-generatepaysprinttoken)

4. [Environment Variables](#4-environment-variables-required)

5. [Flow Diagram](#5-complete-flow-diagram)

6. [Example Usage](#6-example-usage)

7. [Response Data Structure](#7-response-data-structure)

8. [Important Notes](#8-important-notes)

9. [Dependencies](#9-dependencies)

---

## 1. validateUpiIdWithPaysprint() Function

### Purpose

Validates a UPI ID using Paysprint's verification API to ensure the UPI ID exists and is valid.

### Location

```php
app/Http/Controllers/Api/AuthController.php
```

### Function Signature

```php
public function validateUpiIdWithPaysprint($upi_id)
```

### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `$upi_id` | string | The UPI ID to verify (e.g., "user@paytm", "user@phonepe") |

### Implementation Flow

#### Step 1: Generate JWT Token

- Calls `generatePaysprintToken()` to create a JWT token

- Token includes:

  - Issuer: `PSPRINT`

  - Timestamp: Current UNIX time

  - Partner ID: From environment variable

  - Product: `WALLET`

  - Request ID: Random 9-digit number

#### Step 2: Prepare API Request

- **API Endpoint:** `https://api.verifya2z.com/api/v1/verification/upi_verify`

- **Method:** `POST`

- **Headers:**

  ```php
  [
      'Token' => $token,                    // JWT token from Step 1
      'Authorisedkey' => env('PAYSPRINT_AUTH_KEY'),
      'Content-Type' => 'application/json'
  ]
  ```

- **Payload:**

  ```json
  {
      "refid": "<UUID>",        // Unique reference ID (generated using Str::uuid())
      "id_number": "<upi_id>"   // The UPI ID to verify
  }
  ```

#### Step 3: Make API Call

- Uses Laravel's `Http` facade to make POST request

- Sends headers and payload to Paysprint API

#### Step 4: Validate Response

The function checks for successful verification by validating:

1. HTTP response is successful (`$response->successful()`)

2. Response has `status` field set to `true`

3. Response has `data.account_exists` field set to `true`

**Success Condition:**

```php
$response->successful()
    && isset($responseData['status'])
    && $responseData['status'] === true
    && isset($responseData['data']['account_exists'])
    && $responseData['data']['account_exists'] === true
```

#### Step 5: Return Result

**Success Response:**

```php
[
    'valid' => true,
    'response' => $responseData,  // Full API response from Paysprint
    'token' => $token              // JWT token used for the request
]
```

**Failure Response:**

```php
[
    'valid' => false,
    'response' => $responseData,  // Full API response (may contain error details)
    'token' => $token
]
```

**Exception Response:**

```php
[
    'valid' => false,
    'response' => ['exception' => $e->getMessage()],
    'token' => $token
]
```

### Logging

- **Info Log:** Full request/response details for debugging

- **Info Log:** Successful verification confirmation

- **Warning Log:** Failed verification attempts

- **Error Log:** Exceptions during API call

### Expected Paysprint API Response Structure

**Success Response:**

```json
{
    "status": true,
    "data": {
        "account_exists": true,
        "full_name": "USER FULL NAME",
        // ... other fields
    },
    "message": "Verification successful"
}
```

**Failure Response:**

```json
{
    "status": false,
    "data": {
        "account_exists": false
        // or missing account_exists field
    },
    "message": "Error message"
}
```

---

## 2. Usage in update_upi() API

### Integration Point

The `validateUpiIdWithPaysprint()` function is called in the `update_upi()` API.

### Code Flow

```php
// Step 1: Validate UPI using Paysprint
$upiValidation = $this->validateUpiIdWithPaysprint($upi_id);

// Step 2: Check validation result
if (!$upiValidation['valid']) {
    return response()->json([
        'success' => false,
        'message' => 'Invalid or unverified UPI ID.'
    ], 422);
}

// Step 3: Extract verified name from response
$res = $upiValidation['response'];
$verifiedNameRaw = strtoupper(trim($res['data']['full_name'] ?? ''));
```

### Name Matching Logic

After successful UPI verification, the API compares the verified UPI holder name with the user's PAN card name:

```php
// Get verified name from Paysprint response
$verifiedNameRaw = strtoupper(trim($res['data']['full_name'] ?? ''));

// Get user's PAN card name
$providedNameRaw = strtoupper(trim($user->pancard_name ?? ''));

// Split names into words
$verifiedWords = preg_split('/\s+/', $verifiedNameRaw);
$providedWords = preg_split('/\s+/', $providedNameRaw);

// Check if any word from PAN name matches verified UPI name
$matchFound = false;
foreach ($providedWords as $providedWord) {
    if (in_array($providedWord, $verifiedWords)) {
        $matchFound = true;
        break;
    }
}

// If no match found, return error
if (!$matchFound) {
    return response()->json([
        'success' => false,
        'message' => "PAN name and UPI holder name do not match. Please provide valid details.",
    ], 200);
}
```

### Error Handling

If UPI verification fails:

- **HTTP Status:** `422 Unprocessable Entity`

- **Response:**

  ```json
  {
      "success": false,
      "message": "Invalid or unverified UPI ID."
  }
  ```

---

## 3. Helper Function: generatePaysprintToken()

### Purpose

Generates JWT token required for Paysprint API authentication.

### Location

```php
app/Http/Controllers/Api/AuthController.php
```

### Implementation

```php
public function generatePaysprintToken()
{
    $partnerId = env('PAYSPRINT_PARTNER_ID');
    $rawSecretKey = env('PAYSPRINT_JWT_SECRET_RAW');
    $reqid = random_int(100000000, 999999999); // 9-digit random ID
    $timestamp = time();

    $payload = [
        'iss' => 'PSPRINT',             // Issuer
        'timestamp' => $timestamp,      // Current UNIX time
        'partnerId' => $partnerId,      // Your partner ID
        'product' => 'WALLET',          // Product type
        'reqid' => $reqid               // Random request ID
    ];

    $header = [
        'typ' => 'JWT',
        'alg' => 'HS256'
    ];

    // Sign the token with HS256 using the raw secret
    $token = JWT::encode($payload, $rawSecretKey, 'HS256', null, $header);

    return $token;
}
```

### JWT Token Structure

- **Algorithm:** `HS256`

- **Header:**

  - `typ`: `JWT`

  - `alg`: `HS256`

- **Payload:**

  - `iss`: `PSPRINT`

  - `timestamp`: Current UNIX timestamp

  - `partnerId`: From environment variable

  - `product`: `WALLET`

  - `reqid`: Random 9-digit number

---

## 4. Environment Variables Required

Add these to your `.env` file:

```env
# Paysprint Configuration
PAYSPRINT_PARTNER_ID=your_partner_id_here
PAYSPRINT_JWT_SECRET_RAW=your_jwt_secret_key_here
PAYSPRINT_AUTH_KEY=your_authorised_key_here
```

### How to Get These Values

1. Contact Paysprint support or check your Paysprint dashboard

2. Partner ID: Usually provided during account setup

3. JWT Secret Raw: Secret key for signing JWT tokens

4. Auth Key: Authorization key for API requests

---

## 5. Complete Flow Diagram

```
User Request (UPI ID)
    ↓
validateUpiIdWithPaysprint($upi_id)
    ↓
generatePaysprintToken()
    ↓
Create JWT Token (HS256)
    ↓
POST to Paysprint API
    ├─→ https://api.verifya2z.com/api/v1/verification/upi_verify
    ├─→ Headers: Token, Authorisedkey, Content-Type
    └─→ Payload: refid (UUID), id_number (UPI ID)
    ↓
    ├─→ Success Response?
    │       ↓
    │   Check: status === true && account_exists === true
    │       ↓
    │   Return: ['valid' => true, 'response' => ...]
    │
    └─→ Failure/Exception?
            ↓
        Return: ['valid' => false, 'response' => ...]
            ↓
        Log Error/Warning
```

---

## 6. Example Usage

### Basic Verification Example

```php
// In your controller
$upi_id = "user@paytm";

// Call verification function
$result = $this->validateUpiIdWithPaysprint($upi_id);

if ($result['valid']) {
    // UPI is valid and verified
    $verifiedName = $result['response']['data']['full_name'];
    echo "UPI verified! Holder name: " . $verifiedName;
    
    // Access full response data
    $responseData = $result['response'];
    // Process response data...
} else {
    // UPI verification failed
    $errorMessage = $result['response']['message'] ?? 'Verification failed';
    echo "UPI verification failed: " . $errorMessage;
}
```

### In API Context (update_upi)

```php
public function update_upi(Request $request)
{
    // Authentication check...
    
    $upi_id = $request->input('upi_id');
    
    // Validate UPI using Paysprint
    $upiValidation = $this->validateUpiIdWithPaysprint($upi_id);
    
    if (!$upiValidation['valid']) {
        return response()->json([
            'success' => false,
            'message' => 'Invalid or unverified UPI ID.'
        ], 422);
    }
    
    // Extract verified name
    $res = $upiValidation['response'];
    $verifiedName = $res['data']['full_name'] ?? '';
    
    // Continue with update logic...
}
```

### Standalone Verification Endpoint Example

```php
public function verifyUpi(Request $request)
{
    $request->validate([
        'upi_id' => 'required|string'
    ]);
    
    $result = $this->validateUpiIdWithPaysprint($request->upi_id);
    
    return response()->json([
        'success' => $result['valid'],
        'message' => $result['valid'] 
            ? 'UPI ID verified successfully' 
            : 'UPI ID verification failed',
        'data' => [
            'upi_id' => $request->upi_id,
            'verified' => $result['valid'],
            'holder_name' => $result['valid'] 
                ? ($result['response']['data']['full_name'] ?? null)
                : null
        ]
    ], $result['valid'] ? 200 : 422);
}
```

---

## 7. Response Data Structure

### Successful Verification Response

```php
[
    'valid' => true,
    'response' => [
        'status' => true,
        'data' => [
            'account_exists' => true,
            'full_name' => 'JOHN DOE KUMAR',
            // ... other fields from Paysprint API
        ],
        'message' => 'Verification successful'
    ],
    'token' => 'eyJ0eXAiOiJKV1QiLCJhbGc...'
]
```

### Failed Verification Response

```php
[
    'valid' => false,
    'response' => [
        'status' => false,
        'data' => [
            'account_exists' => false
        ],
        'message' => 'UPI ID not found'
    ],
    'token' => 'eyJ0eXAiOiJKV1QiLCJhbGc...'
]
```

### Exception Response

```php
[
    'valid' => false,
    'response' => [
        'exception' => 'Connection timeout'
    ],
    'token' => 'eyJ0eXAiOiJKV1QiLCJhbGc...'
]
```

---

## 8. Important Notes

1. **Token Generation:** Each verification request generates a new JWT token

2. **Reference ID:** Uses UUID for each request (`Str::uuid()`)

3. **Name Extraction:** Verified name is available in `response['data']['full_name']`

4. **Case Sensitivity:** Name matching is case-insensitive (converted to uppercase)

5. **Word Matching:** Uses word-by-word comparison, not exact string match

6. **Logging:** All requests/responses are logged for debugging purposes

7. **Error Handling:** Exceptions are caught and logged, but don't crash the application

8. **Rate Limiting:** Be aware of Paysprint API rate limits

9. **Network Timeout:** Consider adding timeout configuration if needed

10. **Data Privacy:** UPI IDs and user data should be handled securely

---

## 9. Dependencies

### Required Packages

- Laravel Framework

- JWT Library (for token generation) - `firebase/php-jwt` (already installed)

- Laravel HTTP Client (`Illuminate\Support\Facades\Http`)

- Laravel Logging (`Illuminate\Support\Facades\Log`)

- Laravel Str Helper (`Illuminate\Support\Str`)

### Installation

JWT library is already installed via `firebase/php-jwt` package:

```bash
composer show firebase/php-jwt
# Output: firebase/php-jwt v6.11.1
```

### Required Imports

```php
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Str;
use Firebase\JWT\JWT;
```

---

## 10. Testing

### Test Cases to Consider

1. **Valid UPI ID**

   - Input: `"user@paytm"`

   - Expected: `['valid' => true]`

2. **Invalid UPI ID**

   - Input: `"invalid@upi"`

   - Expected: `['valid' => false]`

3. **Empty UPI ID**

   - Input: `""`

   - Expected: Handle gracefully

4. **Network Error**

   - Simulate connection failure

   - Expected: Exception caught and logged

5. **Invalid API Response**

   - Mock malformed response

   - Expected: `['valid' => false]`

### Sample Test Code

```php
// In your test file
public function test_valid_upi_verification()
{
    $controller = new AuthController();
    $result = $controller->validateUpiIdWithPaysprint('valid@paytm');
    
    $this->assertTrue($result['valid']);
    $this->assertArrayHasKey('response', $result);
}

public function test_invalid_upi_verification()
{
    $controller = new AuthController();
    $result = $controller->validateUpiIdWithPaysprint('invalid@upi');
    
    $this->assertFalse($result['valid']);
}
```

---

## 11. Troubleshooting

### Common Issues

1. **JWT Token Generation Fails**

   - Check: `PAYSPRINT_JWT_SECRET_RAW` is set correctly

   - Check: JWT library is installed

2. **API Returns 401 Unauthorized**

   - Check: `PAYSPRINT_AUTH_KEY` is correct

   - Check: JWT token is being generated properly

3. **API Returns 500 Error**

   - Check: Paysprint API status

   - Check: Network connectivity

   - Review logs for detailed error

4. **Verification Always Fails**

   - Check: Response structure matches expected format

   - Check: `account_exists` field exists in response

   - Review logs for actual API response

### Debug Steps

1. Enable detailed logging

2. Check Laravel logs: `storage/logs/laravel.log`

3. Review Paysprint API documentation

4. Verify environment variables are loaded correctly

---

## 12. API Reference Summary

### Function: validateUpiIdWithPaysprint()

- **Purpose:** Verify UPI ID using Paysprint API

- **Input:** UPI ID string

- **Output:** Array with `valid`, `response`, and `token` keys

- **Throws:** Catches exceptions, returns error array

### Function: generatePaysprintToken()

- **Purpose:** Generate JWT token for Paysprint authentication

- **Input:** None (uses environment variables)

- **Output:** JWT token string

- **Algorithm:** HS256

### API Endpoint: POST /api/v1/auth/update-upi

- **Purpose:** Update user's UPI ID with verification

- **Authentication:** Required (Bearer Token)

- **Request Body:**

  ```json
  {
      "upi_id": "user@paytm"
  }
  ```

- **Success Response (200):**

  ```json
  {
      "success": true,
      "message": "UPI ID updated and verified successfully",
      "data": {
          "upi_id": "user@paytm",
          "verified_name": "JOHN DOE"
      }
  }
  ```

- **Error Response (422):**

  ```json
  {
      "success": false,
      "message": "Invalid or unverified UPI ID."
  }
  ```

---

## Contact & Support

For Paysprint API related issues:

- Check Paysprint API documentation

- Contact Paysprint support team

- Review API status page

For implementation issues:

- Review Laravel logs

- Check environment configuration

- Verify network connectivity

---

**Last Updated:** January 2025

**Version:** 1.0












