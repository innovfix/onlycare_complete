<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('screen_notifications', function (Blueprint $table) {
            $table->id();
            $table->string('title');
            $table->text('description');
            $table->time('time');
            $table->string('day'); // Monday, Tuesday, ..., Sunday, or 'all'
            $table->string('gender'); // 'all', 'male', 'female'
            $table->string('language');
            $table->string('logo')->nullable();
            $table->string('image')->nullable();
            $table->tinyInteger('coupon')->default(0);
            $table->timestamps();
            
            $table->index(['day', 'time']);
            $table->index(['gender', 'language']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('screen_notifications');
    }
};
