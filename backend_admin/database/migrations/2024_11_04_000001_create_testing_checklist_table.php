<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('testing_checklist', function (Blueprint $table) {
            $table->id();
            $table->string('test_number', 20); // e.g., "1.1", "2.3"
            $table->string('category', 100); // e.g., "Authentication", "Dashboard"
            $table->text('test_case'); // Description of test
            $table->enum('status', ['Not Tested', 'In Progress', 'Pass', 'Fail'])->default('Not Tested');
            $table->text('notes')->nullable(); // Notes about the test
            $table->timestamp('tested_at')->nullable();
            $table->timestamps();
            
            $table->index('category');
            $table->index('status');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('testing_checklist');
    }
};







