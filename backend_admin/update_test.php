<?php
require __DIR__.'/vendor/autoload.php';
$app = require_once __DIR__.'/bootstrap/app.php';
$app->make(Illuminate\Contracts\Console\Kernel::class)->bootstrap();

use Illuminate\Support\Facades\DB;

if ($argc < 3) {
    echo "Usage: php update_test.php <test_number> <status> [notes]\n";
    echo "Example: php update_test.php 1.1 Pass 'Login page loads correctly'\n";
    exit(1);
}

$testNumber = $argv[1];
$status = $argv[2];
$notes = $argv[3] ?? '';

$updated = DB::table('testing_checklist')
    ->where('test_number', $testNumber)
    ->update([
        'status' => $status,
        'notes' => $notes,
        'tested_at' => now(),
        'updated_at' => now()
    ]);

if ($updated) {
    echo "✅ Test $testNumber updated to: $status\n";
    if ($notes) {
        echo "   Notes: $notes\n";
    }
} else {
    echo "❌ Test $testNumber not found\n";
}







