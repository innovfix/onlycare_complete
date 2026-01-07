<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\DB;

/**
 * Migration to fix Issue #2: Female user not recognized
 * 
 * This migration syncs the user_type field with the gender field for all users
 * to ensure the isFemaleUser() check works correctly.
 */
return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        // Get the table prefix from config
        $prefix = config('database.connections.mysql.prefix', '');
        $tableName = $prefix . 'users';
        
        // Sync user_type with gender for all users
        // This ensures both fields have the same value
        DB::statement("UPDATE `{$tableName}` SET user_type = gender WHERE user_type != gender OR user_type IS NULL");
        
        // Log the fix
        \Log::info('✅ User type synced with gender for all users in ' . $tableName);
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        // No need to reverse - this is a data fix migration
    }
};

