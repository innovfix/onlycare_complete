<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('bank_accounts', function (Blueprint $table) {
            $table->string('pancard_name', 100)->nullable()->after('account_holder_name');
            $table->string('pancard_number', 10)->nullable()->after('pancard_name');
        });
    }

    public function down(): void
    {
        Schema::table('bank_accounts', function (Blueprint $table) {
            $table->dropColumn(['pancard_name', 'pancard_number']);
        });
    }
};












