<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class KycDocument extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;
    
    const CREATED_AT = 'submitted_at';
    const UPDATED_AT = null;

    protected $fillable = [
        'id', 'user_id', 'document_type', 'document_number',
        'document_url', 'status', 'rejected_reason',
        'verified_at', 'verified_by'
    ];

    protected $casts = [
        'submitted_at' => 'datetime',
        'verified_at' => 'datetime',
    ];

    // Relationships
    public function user()
    {
        return $this->belongsTo(User::class);
    }
}

