<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('app_settings', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('setting_key', 100)->unique();
            $table->text('setting_value');
            $table->enum('setting_type', ['STRING', 'INTEGER', 'BOOLEAN', 'JSON'])->default('STRING');
            $table->text('description')->nullable();
            $table->timestamp('updated_at')->useCurrent()->useCurrentOnUpdate();
            
            $table->index('setting_key');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('app_settings');
    }
};

