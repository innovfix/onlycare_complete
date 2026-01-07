<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('friendships', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('user_id', 50);
            $table->string('friend_id', 50);
            $table->enum('status', ['PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED'])->default('PENDING');
            $table->timestamps();
            
            $table->foreign('user_id')->references('id')->on('users')->onDelete('cascade');
            $table->foreign('friend_id')->references('id')->on('users')->onDelete('cascade');
            
            $table->unique(['user_id', 'friend_id'], 'unique_friendship');
            $table->index('user_id');
            $table->index('friend_id');
            $table->index('status');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('friendships');
    }
};

