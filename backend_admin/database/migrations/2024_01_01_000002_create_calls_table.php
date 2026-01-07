<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('calls', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('caller_id', 50);
            $table->string('receiver_id', 50);
            $table->enum('call_type', ['AUDIO', 'VIDEO']);
            $table->enum('status', ['PENDING', 'CONNECTING', 'ONGOING', 'ENDED', 'MISSED', 'REJECTED', 'CANCELLED']);
            $table->integer('duration')->default(0); // in seconds
            $table->integer('coins_spent')->default(0);
            $table->integer('coins_earned')->default(0);
            $table->integer('coin_rate_per_minute')->default(10);
            $table->timestamp('started_at')->nullable();
            $table->timestamp('ended_at')->nullable();
            $table->timestamp('created_at')->useCurrent();
            $table->decimal('rating', 2, 1)->default(0.0);
            $table->text('feedback')->nullable();
            
            $table->foreign('caller_id')->references('id')->on('users')->onDelete('cascade');
            $table->foreign('receiver_id')->references('id')->on('users')->onDelete('cascade');
            
            $table->index('caller_id');
            $table->index('receiver_id');
            $table->index('status');
            $table->index('created_at');
            $table->index('call_type');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('calls');
    }
};

