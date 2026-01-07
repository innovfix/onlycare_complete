<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('reports', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('reporter_id', 50);
            $table->string('reported_user_id', 50);
            $table->enum('report_type', ['INAPPROPRIATE_BEHAVIOR', 'HARASSMENT', 'SPAM', 'FAKE_PROFILE', 'OTHER']);
            $table->text('description')->nullable();
            $table->enum('status', ['PENDING', 'REVIEWING', 'RESOLVED', 'DISMISSED'])->default('PENDING');
            $table->text('admin_notes')->nullable();
            $table->timestamp('created_at')->useCurrent();
            $table->timestamp('resolved_at')->nullable();
            $table->string('resolved_by', 50)->nullable(); // admin_id
            
            $table->foreign('reporter_id')->references('id')->on('users')->onDelete('cascade');
            $table->foreign('reported_user_id')->references('id')->on('users')->onDelete('cascade');
            
            $table->index('reporter_id');
            $table->index('reported_user_id');
            $table->index('status');
            $table->index('created_at');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('reports');
    }
};

