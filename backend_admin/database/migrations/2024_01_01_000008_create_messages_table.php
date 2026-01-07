<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('messages', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('sender_id', 50);
            $table->string('receiver_id', 50);
            $table->text('content');
            $table->enum('message_type', ['TEXT', 'IMAGE', 'AUDIO', 'VIDEO'])->default('TEXT');
            $table->text('media_url')->nullable();
            $table->boolean('is_read')->default(false);
            $table->timestamp('created_at')->useCurrent();
            
            $table->foreign('sender_id')->references('id')->on('users')->onDelete('cascade');
            $table->foreign('receiver_id')->references('id')->on('users')->onDelete('cascade');
            
            $table->index('sender_id');
            $table->index('receiver_id');
            $table->index(['sender_id', 'receiver_id']);
            $table->index('created_at');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('messages');
    }
};

