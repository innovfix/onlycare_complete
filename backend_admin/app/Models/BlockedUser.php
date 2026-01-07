<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class BlockedUser extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;
    
    const UPDATED_AT = null;
    const CREATED_AT = 'blocked_at';

    protected $fillable = [
        'id', 'user_id', 'blocked_user_id'
    ];

    protected $casts = [
        'blocked_at' => 'datetime',
    ];

    // Relationships
    public function user()
    {
        return $this->belongsTo(User::class, 'user_id');
    }

    public function blockedUser()
    {
        return $this->belongsTo(User::class, 'blocked_user_id');
    }
}

