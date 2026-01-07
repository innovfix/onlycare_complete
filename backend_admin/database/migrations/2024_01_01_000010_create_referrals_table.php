<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('referrals', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('referrer_id', 50);
            $table->string('referred_user_id', 50);
            $table->string('referral_code', 20);
            $table->integer('bonus_coins')->default(0);
            $table->boolean('is_claimed')->default(false);
            $table->timestamp('created_at')->useCurrent();
            $table->timestamp('claimed_at')->nullable();
            
            $table->foreign('referrer_id')->references('id')->on('users')->onDelete('cascade');
            $table->foreign('referred_user_id')->references('id')->on('users')->onDelete('cascade');
            
            $table->index('referrer_id');
            $table->index('referral_code');
            $table->index('created_at');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('referrals');
    }
};

