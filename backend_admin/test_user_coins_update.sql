-- ===================================================================
-- TEST USER COIN UPDATE - Add Balance for Testing
-- ===================================================================
-- 
-- Purpose: Add coins to test user for testing the countdown timer feature
-- Test User: USR_17637424324851 (User_5555)
-- 
-- After running this script:
-- - Audio calls: 50 minutes available (500 coins ÷ 10 coins/min)
-- - Video calls: 25 minutes available (500 coins ÷ 20 coins/min)
-- ===================================================================

-- Update test user's coin balance to 500 coins
UPDATE users 
SET coin_balance = 500 
WHERE id = 'USR_17637424324851';

-- Verify the update
SELECT 
    id,
    name,
    phone,
    coin_balance,
    user_type,
    is_verified,
    is_online,
    audio_call_enabled,
    video_call_enabled
FROM users 
WHERE id = 'USR_17637424324851';

-- ===================================================================
-- Expected Result:
-- ===================================================================
-- coin_balance: 500
-- 
-- This allows:
-- - Audio calls: 50 minutes (balance_time: "50:00")
-- - Video calls: 25 minutes (balance_time: "25:00")
-- ===================================================================




