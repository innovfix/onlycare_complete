<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        $prefix = DB::getTablePrefix();
        DB::statement("ALTER TABLE {$prefix}transactions MODIFY COLUMN type ENUM('PURCHASE', 'CALL', 'CALL_SPENT', 'CALL_EARNED', 'GIFT', 'WITHDRAWAL', 'BONUS', 'REFUND') NOT NULL");
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        $prefix = DB::getTablePrefix();
        DB::statement("ALTER TABLE {$prefix}transactions MODIFY COLUMN type ENUM('PURCHASE', 'CALL', 'GIFT', 'WITHDRAWAL', 'BONUS', 'REFUND') NOT NULL");
    }
};
