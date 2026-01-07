<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Message extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;
    
    const UPDATED_AT = null;

    protected $fillable = [
        'id', 'sender_id', 'receiver_id', 'content',
        'message_type', 'media_url', 'is_read'
    ];

    protected $casts = [
        'is_read' => 'boolean',
        'created_at' => 'datetime',
    ];

    // Relationships
    public function sender()
    {
        return $this->belongsTo(User::class, 'sender_id');
    }

    public function receiver()
    {
        return $this->belongsTo(User::class, 'receiver_id');
    }
}

