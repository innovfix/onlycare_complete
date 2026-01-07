<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('pay_sprint', function (Blueprint $table) {
            $table->id();
            $table->string('user_id', 50);
            $table->string('type', 255); // 'pan_verification', 'upi_verification', etc.
            $table->decimal('amount', 10, 2)->default(1.00);
            $table->dateTime('datetime');
            $table->timestamps();
            
            $table->index('user_id');
            $table->index('type');
            $table->index('datetime');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('pay_sprint');
    }
};












