<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('avatars', function (Blueprint $table) {
            // Check if columns exist before dropping
            $columns = Schema::getColumnListing('avatars');
            
            if (in_array('name', $columns)) {
                $table->dropColumn('name');
            }
            
            if (in_array('sort_order', $columns)) {
                $table->dropColumn('sort_order');
            }
            
            if (in_array('is_active', $columns)) {
                // Try to drop index first if it exists
                try {
                    $table->dropIndex(['is_active']);
                } catch (\Exception $e) {
                    // Index doesn't exist, continue
                }
                $table->dropColumn('is_active');
            }
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('avatars', function (Blueprint $table) {
            $table->string('name')->nullable()->after('image_url');
            $table->integer('sort_order')->default(0)->after('name');
            $table->boolean('is_active')->default(true)->after('sort_order');
            $table->index('is_active');
        });
    }
};
