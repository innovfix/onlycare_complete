<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     * 
     * Adds receiver_joined_at column to track when receiver actually accepts/picks up the call.
     * This ensures billing is based on actual talk time, not ringing time.
     */
    public function up(): void
    {
        Schema::table('calls', function (Blueprint $table) {
            // Add receiver_joined_at timestamp column (nullable for backward compatibility)
            $table->timestamp('receiver_joined_at')->nullable()->after('started_at');
            
            // Add index for performance
            $table->index('receiver_joined_at');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('calls', function (Blueprint $table) {
            $table->dropIndex(['receiver_joined_at']);
            $table->dropColumn('receiver_joined_at');
        });
    }
};




