<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Call extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;
    
    const UPDATED_AT = null; // Calls table doesn't have updated_at

    protected $fillable = [
        'id', 'caller_id', 'receiver_id', 'call_type', 'status', 'duration',
        'coins_spent', 'coins_earned', 'coin_rate_per_minute', 'agora_token',
        'channel_name', 'started_at', 'receiver_joined_at', 'ended_at', 'rating', 'feedback'
    ];

    protected $casts = [
        'duration' => 'integer',
        'coins_spent' => 'integer',
        'coins_earned' => 'integer',
        'coin_rate_per_minute' => 'integer',
        'rating' => 'decimal:1',
        'started_at' => 'datetime',
        'receiver_joined_at' => 'datetime',
        'ended_at' => 'datetime',
        'created_at' => 'datetime',
    ];

    // Relationships
    public function caller()
    {
        return $this->belongsTo(User::class, 'caller_id');
    }

    public function receiver()
    {
        return $this->belongsTo(User::class, 'receiver_id');
    }

    // Helper methods
    public function getDurationFormatted()
    {
        $minutes = floor($this->duration / 60);
        $seconds = $this->duration % 60;
        return sprintf('%dm %ds', $minutes, $seconds);
    }
}

