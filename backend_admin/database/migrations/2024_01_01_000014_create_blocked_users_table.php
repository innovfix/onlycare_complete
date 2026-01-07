<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('blocked_users', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('user_id', 50);
            $table->string('blocked_user_id', 50);
            $table->timestamp('blocked_at')->useCurrent();
            
            $table->foreign('user_id')->references('id')->on('users')->onDelete('cascade');
            $table->foreign('blocked_user_id')->references('id')->on('users')->onDelete('cascade');
            
            $table->unique(['user_id', 'blocked_user_id'], 'unique_block');
            $table->index('user_id');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('blocked_users');
    }
};

