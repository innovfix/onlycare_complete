<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('bank_accounts', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('user_id', 50);
            $table->string('account_holder_name', 100);
            $table->string('account_number', 30);
            $table->string('ifsc_code', 11);
            $table->string('bank_name', 100)->nullable();
            $table->string('branch_name', 100)->nullable();
            $table->string('upi_id', 100)->nullable();
            $table->boolean('is_primary')->default(false);
            $table->boolean('is_verified')->default(false);
            $table->timestamps();
            
            $table->foreign('user_id')->references('id')->on('users')->onDelete('cascade');
            
            $table->index('user_id');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('bank_accounts');
    }
};

