<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('kyc_documents', function (Blueprint $table) {
            $table->string('id', 50)->primary();
            $table->string('user_id', 50);
            $table->enum('document_type', ['AADHAAR', 'PAN', 'SELFIE']);
            $table->string('document_number', 50)->nullable();
            $table->text('document_url');
            $table->enum('status', ['PENDING', 'APPROVED', 'REJECTED'])->default('PENDING');
            $table->text('rejected_reason')->nullable();
            $table->timestamp('submitted_at')->useCurrent();
            $table->timestamp('verified_at')->nullable();
            $table->string('verified_by', 50)->nullable(); // admin_id
            
            $table->foreign('user_id')->references('id')->on('users')->onDelete('cascade');
            
            $table->index('user_id');
            $table->index('status');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('kyc_documents');
    }
};

