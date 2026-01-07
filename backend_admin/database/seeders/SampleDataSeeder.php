<?php

namespace Database\Seeders;

use App\Models\User;
use App\Models\Call;
use App\Models\CoinPackage;
use App\Models\Transaction;
use App\Models\Withdrawal;
use App\Models\BankAccount;
use App\Models\KycDocument;
use App\Models\Report;
use App\Models\AppSetting;
use Illuminate\Database\Seeder;
use Illuminate\Support\Str;

class SampleDataSeeder extends Seeder
{
    public function run(): void
    {
        echo "🌱 Seeding sample data...\n";

        // Create Coin Packages
        $this->createCoinPackages();
        
        // Create Sample Users
        $users = $this->createUsers();
        
        // Create Sample Calls
        $this->createCalls($users);
        
        // Create Sample Transactions
        $this->createTransactions($users);
        
        // Create Sample Bank Accounts and Withdrawals
        $this->createWithdrawals($users);
        
        // Create Sample KYC Documents
        $this->createKYCDocuments($users);
        
        // Create Sample Reports
        $this->createReports($users);
        
        // Create App Settings
        $this->createAppSettings();

        echo "✅ Sample data seeded successfully!\n";
    }

    private function createCoinPackages()
    {
        $packages = [
            ['coins' => 100, 'price' => 99, 'original_price' => 150, 'discount' => 34, 'is_popular' => false, 'is_best_value' => false, 'sort_order' => 1],
            ['coins' => 500, 'price' => 399, 'original_price' => 750, 'discount' => 47, 'is_popular' => true, 'is_best_value' => false, 'sort_order' => 2],
            ['coins' => 1000, 'price' => 699, 'original_price' => 1500, 'discount' => 53, 'is_popular' => false, 'is_best_value' => true, 'sort_order' => 3],
            ['coins' => 2500, 'price' => 1499, 'original_price' => 3750, 'discount' => 60, 'is_popular' => false, 'is_best_value' => false, 'sort_order' => 4],
        ];

        foreach ($packages as $package) {
            CoinPackage::create(array_merge(['id' => 'PKG_' . Str::random(10)], $package));
        }

        echo "  ✓ Created coin packages\n";
    }

    private function createUsers()
    {
        $users = [];

        // Create 20 Male Users
        for ($i = 1; $i <= 20; $i++) {
            $users[] = User::create([
                'id' => 'USR_' . Str::random(10),
                'phone' => '98765432' . str_pad($i, 2, '0', STR_PAD_LEFT),
                'name' => 'Male User ' . $i,
                'age' => rand(20, 45),
                'gender' => 'MALE',
                'language' => 'ENGLISH',
                'coin_balance' => rand(100, 1000),
                'rating' => rand(30, 50) / 10,
                'total_ratings' => rand(5, 50),
                'is_online' => rand(0, 1) == 1,
                'created_at' => now()->subDays(rand(1, 30)),
            ]);
        }

        // Create 10 Female Users
        for ($i = 1; $i <= 10; $i++) {
            $users[] = User::create([
                'id' => 'USR_' . Str::random(10),
                'phone' => '87654321' . str_pad($i, 2, '0', STR_PAD_LEFT),
                'name' => 'Female User ' . $i,
                'age' => rand(20, 35),
                'gender' => 'FEMALE',
                'language' => 'ENGLISH',
                'total_earnings' => rand(1000, 10000),
                'coin_balance' => rand(500, 5000),
                'rating' => rand(35, 50) / 10,
                'total_ratings' => rand(10, 100),
                'is_online' => rand(0, 1) == 1,
                'is_verified' => rand(0, 1) == 1,
                'kyc_status' => ['NOT_SUBMITTED', 'PENDING', 'APPROVED'][rand(0, 2)],
                'created_at' => now()->subDays(rand(1, 60)),
            ]);
        }

        echo "  ✓ Created 30 sample users (20 male, 10 female)\n";
        return $users;
    }

    private function createCalls($users)
    {
        $maleUsers = array_filter($users, fn($u) => $u->gender === 'MALE');
        $femaleUsers = array_filter($users, fn($u) => $u->gender === 'FEMALE');

        // Create 50 sample calls
        for ($i = 0; $i < 50; $i++) {
            $caller = $maleUsers[array_rand($maleUsers)];
            $receiver = $femaleUsers[array_rand($femaleUsers)];
            $callType = rand(0, 1) ? 'AUDIO' : 'VIDEO';
            $duration = rand(60, 600); // 1-10 minutes
            $rate = $callType === 'AUDIO' ? 10 : 15;
            $coins = ceil($duration / 60) * $rate;

            Call::create([
                'id' => 'CALL_' . Str::random(10),
                'caller_id' => $caller->id,
                'receiver_id' => $receiver->id,
                'call_type' => $callType,
                'status' => 'ENDED',
                'duration' => $duration,
                'coins_spent' => $coins,
                'coins_earned' => $coins,
                'coin_rate_per_minute' => $rate,
                'started_at' => now()->subDays(rand(1, 30)),
                'ended_at' => now()->subDays(rand(1, 30))->addSeconds($duration),
                'rating' => rand(3, 5),
                'created_at' => now()->subDays(rand(1, 30)),
            ]);
        }

        echo "  ✓ Created 50 sample calls\n";
    }

    private function createTransactions($users)
    {
        foreach ($users as $user) {
            if ($user->gender === 'MALE' && rand(0, 1)) {
                Transaction::create([
                    'id' => 'TXN_' . Str::random(10),
                    'user_id' => $user->id,
                    'type' => 'PURCHASE',
                    'amount' => rand(99, 1499),
                    'coins' => rand(100, 2500),
                    'status' => 'SUCCESS',
                    'payment_method' => ['PhonePe', 'GPay', 'Paytm'][rand(0, 2)],
                    'created_at' => now()->subDays(rand(1, 30)),
                ]);
            }
        }

        echo "  ✓ Created sample transactions\n";
    }

    private function createWithdrawals($users)
    {
        $femaleUsers = array_filter($users, fn($u) => $u->gender === 'FEMALE');

        foreach ($femaleUsers as $user) {
            if (rand(0, 1)) {
                // Create bank account
                $bankAccount = BankAccount::create([
                    'id' => 'BANK_' . Str::random(10),
                    'user_id' => $user->id,
                    'account_holder_name' => $user->name,
                    'account_number' => '1234567890' . rand(1000, 9999),
                    'ifsc_code' => 'SBIN000' . rand(1000, 9999),
                    'bank_name' => 'State Bank of India',
                    'is_primary' => true,
                    'is_verified' => true,
                ]);

                // Create withdrawal
                Withdrawal::create([
                    'id' => 'WD_' . Str::random(10),
                    'user_id' => $user->id,
                    'amount' => rand(500, 5000),
                    'coins' => rand(500, 5000),
                    'status' => ['PENDING', 'APPROVED', 'COMPLETED'][rand(0, 2)],
                    'bank_account_id' => $bankAccount->id,
                    'requested_at' => now()->subDays(rand(1, 15)),
                ]);
            }
        }

        echo "  ✓ Created sample withdrawals and bank accounts\n";
    }

    private function createKYCDocuments($users)
    {
        $femaleUsers = array_filter($users, fn($u) => $u->gender === 'FEMALE');

        foreach ($femaleUsers as $user) {
            if (rand(0, 1)) {
                foreach (['AADHAAR', 'PAN', 'SELFIE'] as $type) {
                    KycDocument::create([
                        'id' => 'KYC_' . Str::random(10),
                        'user_id' => $user->id,
                        'document_type' => $type,
                        'document_number' => $type === 'SELFIE' ? null : Str::random(12),
                        'document_url' => 'https://example.com/kyc/' . Str::random(10) . '.jpg',
                        'status' => ['PENDING', 'APPROVED'][rand(0, 1)],
                        'submitted_at' => now()->subDays(rand(1, 20)),
                    ]);
                }
            }
        }

        echo "  ✓ Created sample KYC documents\n";
    }

    private function createReports($users)
    {
        for ($i = 0; $i < 10; $i++) {
            $reporter = $users[array_rand($users)];
            $reported = $users[array_rand($users)];

            if ($reporter->id !== $reported->id) {
                Report::create([
                    'id' => 'REP_' . Str::random(10),
                    'reporter_id' => $reporter->id,
                    'reported_user_id' => $reported->id,
                    'report_type' => ['INAPPROPRIATE_BEHAVIOR', 'HARASSMENT', 'SPAM', 'FAKE_PROFILE'][rand(0, 3)],
                    'description' => 'Sample report description',
                    'status' => ['PENDING', 'REVIEWING'][rand(0, 1)],
                    'created_at' => now()->subDays(rand(1, 10)),
                ]);
            }
        }

        echo "  ✓ Created sample reports\n";
    }

    private function createAppSettings()
    {
        $settings = [
            ['setting_key' => 'audio_call_rate', 'setting_value' => '10', 'setting_type' => 'INTEGER', 'description' => 'Audio call rate per minute in coins'],
            ['setting_key' => 'video_call_rate', 'setting_value' => '15', 'setting_type' => 'INTEGER', 'description' => 'Video call rate per minute in coins'],
            ['setting_key' => 'min_withdrawal_amount', 'setting_value' => '500', 'setting_type' => 'INTEGER', 'description' => 'Minimum withdrawal amount in INR'],
            ['setting_key' => 'coin_to_inr_rate', 'setting_value' => '1', 'setting_type' => 'INTEGER', 'description' => 'Coin to INR conversion rate'],
            ['setting_key' => 'referral_bonus_referrer', 'setting_value' => '100', 'setting_type' => 'INTEGER', 'description' => 'Referral bonus for referrer'],
            ['setting_key' => 'referral_bonus_referred', 'setting_value' => '50', 'setting_type' => 'INTEGER', 'description' => 'Referral bonus for referred user'],
        ];

        foreach ($settings as $setting) {
            AppSetting::create(array_merge(['id' => 'SET_' . Str::random(10)], $setting));
        }

        echo "  ✓ Created app settings\n";
    }
}







