<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            // Add referral code column
            $table->string('referral_code', 20)->nullable()->unique()->after('phone');
            
            // Add country code column
            $table->string('country_code', 5)->default('+91')->after('phone');
            
            // Add user_type column (alias for gender)
            $table->string('user_type', 10)->default('MALE')->after('gender');
            
            // Add is_active column
            $table->boolean('is_active')->default(true)->after('is_blocked');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['referral_code', 'country_code', 'user_type', 'is_active']);
        });
    }
};







