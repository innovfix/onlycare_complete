<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;

class Admin extends Authenticatable
{
    use HasFactory, Notifiable;

    protected $keyType = 'string';
    public $incrementing = false;

    protected $fillable = [
        'id', 'username', 'email', 'password', 'role', 'is_active', 'last_login'
    ];

    protected $hidden = [
        'password',
        'remember_token',
    ];

    protected $casts = [
        'is_active' => 'boolean',
        'last_login' => 'datetime',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
        'password' => 'hashed',
    ];

    // Role permissions helper
    public function hasRole($role)
    {
        return $this->role === $role;
    }

    public function isSuperAdmin()
    {
        return $this->role === 'SUPER_ADMIN';
    }

    public function canManageUsers()
    {
        return in_array($this->role, ['SUPER_ADMIN', 'ADMIN']);
    }

    public function canManageFinance()
    {
        return in_array($this->role, ['SUPER_ADMIN', 'FINANCE']);
    }

    public function canModerateContent()
    {
        return in_array($this->role, ['SUPER_ADMIN', 'ADMIN', 'MODERATOR']);
    }
}

