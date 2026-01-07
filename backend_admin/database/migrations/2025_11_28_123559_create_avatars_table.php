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
        Schema::create('avatars', function (Blueprint $table) {
            $table->id();
            $table->enum('gender', ['MALE', 'FEMALE']);
            $table->string('image_url');
            $table->timestamps();
            
            $table->index('gender');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('avatars');
    }
};
