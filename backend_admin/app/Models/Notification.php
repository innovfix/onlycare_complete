<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Notification extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;
    
    const UPDATED_AT = null;

    protected $fillable = [
        'id', 'user_id', 'title', 'message', 'notification_type',
        'reference_id', 'is_read'
    ];

    protected $casts = [
        'is_read' => 'boolean',
        'created_at' => 'datetime',
    ];

    // Relationships
    public function user()
    {
        return $this->belongsTo(User::class);
    }
}

