<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Referral extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;
    
    const UPDATED_AT = null;

    protected $fillable = [
        'id', 'referrer_id', 'referred_user_id', 'referral_code',
        'bonus_coins', 'is_claimed', 'claimed_at'
    ];

    protected $casts = [
        'bonus_coins' => 'integer',
        'is_claimed' => 'boolean',
        'created_at' => 'datetime',
        'claimed_at' => 'datetime',
    ];

    // Relationships
    public function referrer()
    {
        return $this->belongsTo(User::class, 'referrer_id');
    }

    public function referredUser()
    {
        return $this->belongsTo(User::class, 'referred_user_id');
    }
}

