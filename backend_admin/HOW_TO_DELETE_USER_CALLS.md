# 🗑️ How to Delete User Calls from Admin Panel

## For User with Phone: 6203224780

---

## 🚀 Quick Start

### Step 1: Navigate to Backend Directory

```bash
cd /Users/rishabh/OnlyCareProject/backend_admin
```

### Step 2: Run the Deletion Script

```bash
php delete_user_calls_6203224780.php
```

---

## 📋 What This Script Does

The script will:

1. ✅ **Find the user** by phone number `6203224780`
2. ✅ **Show user details** (ID, name, username, etc.)
3. ✅ **Count all calls** (as caller and receiver)
4. ✅ **Display call details** (type, status, duration, date)
5. ⚠️ **Ask for confirmation** before deleting
6. ✅ **Delete all calls** for this user
7. ✅ **Delete related transactions**
8. ✅ **Reset user busy status**
9. ✅ **Verify deletion** was successful

---

## 📊 Example Output

```
==========================================================
  DELETE ALL CALLS FOR USER WITH PHONE: 6203224780
==========================================================

🔍 Searching for user with phone: 6203224780...
✅ Found user:
   ID:       USR_123
   Name:     John Doe
   Username: johndoe
   Phone:    6203224780
   Email:    john@example.com
   Gender:   MALE

📊 CALL STATISTICS:
   Calls as CALLER:   15
   Calls as RECEIVER: 8
   TOTAL CALLS:       23

📋 CALL DETAILS:
------------------------------------------------------------
1. Call ID: CALL_001
   Type:     AUDIO
   Role:     CALLER
   With:     Jane Smith (USR_456)
   Status:   COMPLETED
   Duration: 120 seconds
   Date:     2026-01-09 10:30:00
   ----------------------------------------------------------
...

⚠️  WARNING: This will DELETE ALL 23 calls for this user!
⚠️  This action CANNOT be undone!

Are you sure you want to delete all calls? (yes/no): yes

🗑️  Starting deletion process...

1️⃣  Deleting call-related transactions...
   ✅ Deleted 5 transaction(s)

2️⃣  Deleting calls where user was CALLER...
   ✅ Deleted 15 call(s)

3️⃣  Deleting calls where user was RECEIVER...
   ✅ Deleted 8 call(s)

4️⃣  Resetting user busy status...
   ✅ User busy status reset

==========================================================
  ✅ DELETION COMPLETED SUCCESSFULLY!
==========================================================

📊 SUMMARY:
   Transactions deleted: 5
   Calls deleted (as caller): 15
   Calls deleted (as receiver): 8
   TOTAL CALLS DELETED: 23

🔍 VERIFICATION:
   Remaining calls for this user: 0
   ✅ All calls successfully deleted!

==========================================================
  Process completed at 2026-01-09 14:25:30
==========================================================
```

---

## ⚠️ Important Safety Features

1. **Confirmation Required**: The script will ask "yes/no" before deleting
2. **Database Transaction**: Uses transactions - if anything fails, ALL changes are rolled back
3. **Detailed Logging**: Shows exactly what will be deleted before proceeding
4. **Verification**: Checks if deletion was successful
5. **Error Handling**: Catches errors and prevents partial deletion

---

## 🔄 Alternative: Delete for ANY User by Phone

If you need to delete calls for a different user, you can modify the phone number in the script:

1. Open the file: `delete_user_calls_6203224780.php`
2. Change line 12: `$phoneNumber = '6203224780';` to your desired phone number
3. Save and run the script

---

## 📝 Manual Deletion via MySQL (Alternative Method)

If you prefer to use direct SQL commands:

### Step 1: Connect to MySQL

```bash
mysql -u your_username -p your_database
```

### Step 2: Find the User ID

```sql
SELECT id, name, username, phone 
FROM users 
WHERE phone LIKE '%6203224780%';
```

### Step 3: Count Calls

```sql
SELECT COUNT(*) as total_calls 
FROM calls 
WHERE caller_id = 'USR_XXX' OR receiver_id = 'USR_XXX';
```

### Step 4: Delete Calls

```sql
-- Delete transactions first
DELETE FROM transactions 
WHERE reference_type = 'CALL' 
AND reference_id IN (
    SELECT id FROM calls 
    WHERE caller_id = 'USR_XXX' OR receiver_id = 'USR_XXX'
);

-- Delete calls
DELETE FROM calls 
WHERE caller_id = 'USR_XXX' OR receiver_id = 'USR_XXX';

-- Reset user busy status
UPDATE users SET is_busy = 0 WHERE id = 'USR_XXX';
```

**Note:** Replace `USR_XXX` with the actual user ID.

---

## 🛡️ Backup Before Deletion (Recommended)

Create a backup before deleting:

```bash
# Backup calls table
mysqldump -u username -p database_name calls > calls_backup_$(date +%Y%m%d).sql

# Backup transactions related to calls
mysqldump -u username -p database_name transactions --where="reference_type='CALL'" > call_transactions_backup_$(date +%Y%m%d).sql
```

---

## ❓ Troubleshooting

### Issue: "User not found"
**Solution:** The phone number might be stored with country code (+91) or without. The script tries both.

### Issue: "Permission denied"
**Solution:** Make sure the PHP script has execute permissions:
```bash
chmod +x delete_user_calls_6203224780.php
```

### Issue: "Database connection error"
**Solution:** Check your `.env` file has correct database credentials:
```
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=your_database
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

---

## 📞 Need Help?

If you encounter any issues:
1. Check Laravel logs: `storage/logs/laravel.log`
2. Check MySQL error log
3. Verify database connection works: `php artisan migrate:status`

---

**Created:** January 9, 2026  
**For:** User with phone 6203224780  
**Status:** Ready to use ✅
