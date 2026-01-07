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
        Schema::create('push_notifications', function (Blueprint $table) {
            $table->id();
            $table->string('title', 5000);
            $table->text('description');
            $table->timestamp('datetime');
            $table->string('gender'); // 'all', 'male', 'female'
            $table->string('language');
            $table->string('logo')->nullable();
            $table->string('image')->nullable();
            $table->timestamps();
            
            $table->index('datetime');
            $table->index(['gender', 'language']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('push_notifications');
    }
};
