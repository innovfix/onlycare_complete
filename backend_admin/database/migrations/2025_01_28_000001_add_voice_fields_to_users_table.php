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
        Schema::table('users', function (Blueprint $table) {
            if (!Schema::hasColumn('users', 'voice')) {
                $table->string('voice', 255)->nullable()->after('profile_image');
            }
            if (!Schema::hasColumn('users', 'voice_gender')) {
                $table->string('voice_gender', 10)->nullable()->after('voice');
            }
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            if (Schema::hasColumn('users', 'voice')) {
                $table->dropColumn('voice');
            }
            if (Schema::hasColumn('users', 'voice_gender')) {
                $table->dropColumn('voice_gender');
            }
        });
    }
};









