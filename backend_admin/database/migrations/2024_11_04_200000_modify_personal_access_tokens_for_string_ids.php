<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // Drop the personal_access_tokens table if it exists
        Schema::dropIfExists('personal_access_tokens');
        
        // Recreate with string tokenable_id
        Schema::create('personal_access_tokens', function (Blueprint $table) {
            $table->id();
            $table->string('tokenable_type'); // Model type
            $table->string('tokenable_id', 50); // Changed to string to support User string IDs
            $table->string('name');
            $table->string('token', 64)->unique();
            $table->text('abilities')->nullable();
            $table->timestamp('last_used_at')->nullable();
            $table->timestamp('expires_at')->nullable();
            $table->timestamps();

            $table->index(['tokenable_type', 'tokenable_id']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('personal_access_tokens');
    }
};







