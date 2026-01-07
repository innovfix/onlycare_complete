<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('users', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('phone', 15)->unique();
            $table->string('name', 100);
            $table->integer('age')->nullable();
            $table->enum('gender', ['MALE', 'FEMALE']);
            $table->text('profile_image')->nullable();
            $table->text('bio')->nullable();
            $table->enum('language', ['ENGLISH', 'HINDI', 'TAMIL', 'TELUGU', 'KANNADA', 'MALAYALAM', 'BENGALI', 'MARATHI'])->nullable();
            $table->text('interests')->nullable(); // JSON array
            $table->boolean('is_online')->default(false);
            $table->bigInteger('last_seen')->nullable(); // Unix timestamp
            $table->decimal('rating', 2, 1)->default(0.0);
            $table->integer('total_ratings')->default(0);
            $table->integer('coin_balance')->default(0);
            $table->integer('total_earnings')->default(0);
            $table->boolean('audio_call_enabled')->default(true);
            $table->boolean('video_call_enabled')->default(true);
            $table->boolean('is_verified')->default(false);
            $table->enum('kyc_status', ['NOT_SUBMITTED', 'PENDING', 'APPROVED', 'REJECTED'])->default('NOT_SUBMITTED');
            $table->boolean('is_blocked')->default(false);
            $table->text('blocked_reason')->nullable();
            $table->timestamps();
            $table->softDeletes();
            
            $table->index('gender');
            $table->index('is_online');
            $table->index('phone');
            $table->index('created_at');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('users');
    }
};

