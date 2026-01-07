<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('gifts', function (Blueprint $table) {
            $table->id();
            $table->string('gift_icon'); // Path to gift icon image
            $table->integer('coins'); // Number of coins required
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('gifts');
    }
};
