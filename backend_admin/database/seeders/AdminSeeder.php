<?php

namespace Database\Seeders;

use App\Models\Admin;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;

class AdminSeeder extends Seeder
{
    public function run(): void
    {
        // Create Super Admin
        Admin::create([
            'id' => 'ADM_' . Str::random(10),
            'username' => 'admin',
            'email' => 'admin@onlycare.app',
            'password' => Hash::make('admin123'),
            'role' => 'SUPER_ADMIN',
            'is_active' => true,
        ]);

        echo "✅ Admin user created successfully!\n";
        echo "Email: admin@onlycare.app\n";
        echo "Password: admin123\n";
    }
}







