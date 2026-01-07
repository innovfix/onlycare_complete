<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('transactions', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('user_id', 50);
            $table->enum('type', ['PURCHASE', 'CALL', 'GIFT', 'WITHDRAWAL', 'BONUS', 'REFUND']);
            $table->decimal('amount', 10, 2)->default(0); // INR amount
            $table->integer('coins')->default(0); // Positive for credit, negative for debit
            $table->enum('status', ['PENDING', 'SUCCESS', 'FAILED', 'CANCELLED']);
            $table->string('payment_method', 50)->nullable();
            $table->string('payment_gateway_id', 100)->nullable(); // External payment ID
            $table->string('reference_id', 50)->nullable(); // Related call_id or withdrawal_id
            $table->text('description')->nullable();
            $table->timestamps();
            
            $table->foreign('user_id')->references('id')->on('users')->onDelete('cascade');
            
            $table->index('user_id');
            $table->index('type');
            $table->index('status');
            $table->index('created_at');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('transactions');
    }
};

