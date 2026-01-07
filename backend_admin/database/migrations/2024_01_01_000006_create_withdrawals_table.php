<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('withdrawals', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('user_id', 50);
            $table->decimal('amount', 10, 2); // INR
            $table->integer('coins');
            $table->enum('status', ['PENDING', 'APPROVED', 'REJECTED', 'COMPLETED'])->default('PENDING');
            $table->string('bank_account_id', 50);
            $table->text('admin_notes')->nullable();
            $table->text('rejected_reason')->nullable();
            $table->timestamp('requested_at')->useCurrent();
            $table->timestamp('processed_at')->nullable();
            $table->timestamp('completed_at')->nullable();
            
            $table->foreign('user_id')->references('id')->on('users')->onDelete('cascade');
            $table->foreign('bank_account_id')->references('id')->on('bank_accounts');
            
            $table->index('user_id');
            $table->index('status');
            $table->index('requested_at');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('withdrawals');
    }
};

