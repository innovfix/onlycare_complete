<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('notifications', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('user_id', 50);
            $table->string('title', 255);
            $table->text('message');
            $table->enum('notification_type', ['CALL', 'MESSAGE', 'PAYMENT', 'SYSTEM', 'PROMOTIONAL']);
            $table->string('reference_id', 50)->nullable(); // Related entity ID
            $table->boolean('is_read')->default(false);
            $table->timestamp('created_at')->useCurrent();
            
            $table->foreign('user_id')->references('id')->on('users')->onDelete('cascade');
            
            $table->index('user_id');
            $table->index('is_read');
            $table->index('created_at');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('notifications');
    }
};

