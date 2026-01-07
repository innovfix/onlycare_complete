<?php
require 'vendor/autoload.php';
$app = require_once 'bootstrap/app.php';
$app->make('Illuminate\Contracts\Console\Kernel')->bootstrap();

$females = DB::table('users')
    ->where('gender', 'female')
    ->select('id', 'name', 'phone', 'is_verified', 'is_online')
    ->limit(10)
    ->get();

echo "=== FEMALE TEST ACCOUNTS ===\n\n";
foreach($females as $f) {
    echo "ID: {$f->id}\n";
    echo "Name: {$f->name}\n";
    echo "Phone: {$f->phone}\n";
    echo "Verified: " . ($f->is_verified ? 'Yes' : 'No') . "\n";
    echo "Online: " . ($f->is_online ? 'Yes' : 'No') . "\n";
    echo "---\n";
}
echo "\nTo login: Use phone number with OTP 011011\n";





