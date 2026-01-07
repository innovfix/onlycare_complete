<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('coin_packages', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->integer('coins');
            $table->decimal('price', 10, 2);
            $table->decimal('original_price', 10, 2);
            $table->integer('discount')->default(0); // percentage
            $table->boolean('is_popular')->default(false);
            $table->boolean('is_best_value')->default(false);
            $table->boolean('is_active')->default(true);
            $table->integer('sort_order')->default(0);
            $table->timestamps();
            
            $table->index('is_active');
            $table->index('sort_order');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('coin_packages');
    }
};

