<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     * 
     * This migration adds a unique constraint to prevent duplicate transactions
     * for the same call. It ensures that for each call (reference_id), each user
     * can only have ONE transaction of each type (CALL_SPENT or CALL_EARNED).
     * 
     * This prevents the bug where duplicate endCall API requests create
     * duplicate transactions and charge users twice.
     */
    public function up(): void
    {
        // Add unique index to prevent duplicate call transactions
        try {
            Schema::table('transactions', function (Blueprint $table) {
                $table->unique(
                    ['reference_id', 'user_id', 'type'], 
                    'unique_call_transaction'
                );
            });
        } catch (\Exception $e) {
            // Index might already exist, which is fine
            if (strpos($e->getMessage(), 'Duplicate key name') === false) {
                throw $e;
            }
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('transactions', function (Blueprint $table) {
            $table->dropUnique('unique_call_transaction');
        });
    }
};
