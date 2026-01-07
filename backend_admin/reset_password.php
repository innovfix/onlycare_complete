<?php

require __DIR__.'/vendor/autoload.php';

$app = require_once __DIR__.'/bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use App\Models\Admin;
use Illuminate\Support\Facades\Hash;

$admin = Admin::where('email', 'admin@onlycare.app')->first();

if ($admin) {
    $admin->password = Hash::make('password');
    $admin->save();
    echo "Password reset successfully for admin@onlycare.app\n";
} else {
    echo "Admin not found!\n";
}







