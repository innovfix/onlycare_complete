<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('admins', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('username', 50)->unique();
            $table->string('email', 100)->unique();
            $table->string('password', 255);
            $table->enum('role', ['SUPER_ADMIN', 'ADMIN', 'MODERATOR', 'FINANCE', 'SUPPORT']);
            $table->boolean('is_active')->default(true);
            $table->timestamp('last_login')->nullable();
            $table->rememberToken();
            $table->timestamps();
            
            $table->index('username');
            $table->index('email');
            $table->index('role');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('admins');
    }
};

