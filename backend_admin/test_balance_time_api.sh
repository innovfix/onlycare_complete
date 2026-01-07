#!/bin/bash

# ===================================================================
# BALANCE_TIME API TEST SCRIPT
# ===================================================================
# 
# Purpose: Test the balance_time field in API responses and FCM notifications
# 
# Prerequisites:
# 1. Test user must have coins: Run test_user_coins_update.sql first
# 2. Get valid auth tokens for both caller and receiver
# 3. Update the variables below with actual values
# ===================================================================

# Configuration
API_BASE_URL="https://api.onlycare.app/api/v1"
# Alternative for local testing:
# API_BASE_URL="http://localhost/api/v1"

# Replace these with actual tokens and user IDs
CALLER_TOKEN="YOUR_CALLER_TOKEN_HERE"
RECEIVER_TOKEN="YOUR_RECEIVER_TOKEN_HERE"
RECEIVER_ID="USR_RECEIVER_ID_HERE"

echo "======================================================================"
echo "🧪 Testing balance_time Field Implementation"
echo "======================================================================"
echo ""

# ===================================================================
# TEST 1: Check /api/v1/calls/initiate returns balance_time
# ===================================================================
echo "Test 1: POST /api/v1/calls/initiate"
echo "----------------------------------------------------------------------"
echo "📞 Initiating audio call..."
echo ""

INITIATE_RESPONSE=$(curl -s -X POST "${API_BASE_URL}/calls/initiate" \
  -H "Authorization: Bearer ${CALLER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"receiver_id\": \"${RECEIVER_ID}\",
    \"call_type\": \"AUDIO\"
  }")

echo "Response:"
echo "$INITIATE_RESPONSE" | jq '.'
echo ""

# Extract balance_time
BALANCE_TIME=$(echo "$INITIATE_RESPONSE" | jq -r '.balance_time // "NOT_FOUND"')
CALL_BALANCE_TIME=$(echo "$INITIATE_RESPONSE" | jq -r '.call.balance_time // "NOT_FOUND"')

if [[ "$BALANCE_TIME" != "NOT_FOUND" ]] || [[ "$CALL_BALANCE_TIME" != "NOT_FOUND" ]]; then
    echo "✅ PASS: balance_time field exists"
    echo "   - Top level: ${BALANCE_TIME}"
    echo "   - In call object: ${CALL_BALANCE_TIME}"
else
    echo "❌ FAIL: balance_time field missing"
fi
echo ""

# Extract call_id for next test
CALL_ID=$(echo "$INITIATE_RESPONSE" | jq -r '.call.id // "NOT_FOUND"')

echo "----------------------------------------------------------------------"
echo ""

# ===================================================================
# TEST 2: Check /api/v1/calls/incoming returns balance_time
# ===================================================================
echo "Test 2: GET /api/v1/calls/incoming"
echo "----------------------------------------------------------------------"
echo "📥 Getting incoming calls for receiver..."
echo ""

INCOMING_RESPONSE=$(curl -s -X GET "${API_BASE_URL}/calls/incoming" \
  -H "Authorization: Bearer ${RECEIVER_TOKEN}" \
  -H "Content-Type: application/json")

echo "Response:"
echo "$INCOMING_RESPONSE" | jq '.'
echo ""

# Check if balance_time exists in first call
INCOMING_BALANCE_TIME=$(echo "$INCOMING_RESPONSE" | jq -r '.data[0].balance_time // "NOT_FOUND"')

if [[ "$INCOMING_BALANCE_TIME" != "NOT_FOUND" ]]; then
    echo "✅ PASS: balance_time field exists in incoming calls"
    echo "   - Balance time: ${INCOMING_BALANCE_TIME}"
else
    echo "❌ FAIL: balance_time field missing in incoming calls"
fi
echo ""

echo "----------------------------------------------------------------------"
echo ""

# ===================================================================
# TEST 3: Check Laravel logs for FCM notification
# ===================================================================
echo "Test 3: Check Laravel Logs for FCM balanceTime"
echo "----------------------------------------------------------------------"
echo "📧 Checking Laravel logs for FCM notification with balanceTime..."
echo ""

if [ -f "storage/logs/laravel.log" ]; then
    echo "Recent FCM notifications (last 20 lines with 'balanceTime'):"
    grep -A 10 "FCM notification sent" storage/logs/laravel.log | grep -i "balance" | tail -20
    
    if grep -q "balanceTime" storage/logs/laravel.log; then
        echo ""
        echo "✅ PASS: balanceTime found in FCM logs"
    else
        echo ""
        echo "❌ FAIL: balanceTime NOT found in FCM logs"
    fi
else
    echo "⚠️  WARNING: Laravel log file not found at storage/logs/laravel.log"
fi
echo ""

echo "----------------------------------------------------------------------"
echo ""

# ===================================================================
# TEST 4: Clean up - Reject the test call
# ===================================================================
if [[ "$CALL_ID" != "NOT_FOUND" ]] && [[ "$CALL_ID" != "null" ]]; then
    echo "Test 4: Cleanup - Rejecting test call"
    echo "----------------------------------------------------------------------"
    echo "🧹 Rejecting call ${CALL_ID}..."
    echo ""
    
    REJECT_RESPONSE=$(curl -s -X POST "${API_BASE_URL}/calls/${CALL_ID}/reject" \
      -H "Authorization: Bearer ${RECEIVER_TOKEN}" \
      -H "Content-Type: application/json")
    
    echo "Response:"
    echo "$REJECT_RESPONSE" | jq '.'
    echo ""
    echo "----------------------------------------------------------------------"
    echo ""
fi

# ===================================================================
# SUMMARY
# ===================================================================
echo "======================================================================"
echo "📊 Test Summary"
echo "======================================================================"
echo ""
echo "Test Results:"
echo "  1. /api/v1/calls/initiate balance_time:    ${BALANCE_TIME:-NOT_TESTED}"
echo "  2. /api/v1/calls/incoming balance_time:    ${INCOMING_BALANCE_TIME:-NOT_TESTED}"
echo "  3. FCM notification balanceTime:           Check logs above"
echo ""
echo "======================================================================"
echo "✅ Testing Complete!"
echo "======================================================================"
echo ""
echo "Note: To see full FCM payload, run:"
echo "  tail -f storage/logs/laravel.log | grep -A 30 'FCM notification'"
echo ""




