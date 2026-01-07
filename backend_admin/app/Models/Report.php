<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Report extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;
    
    const UPDATED_AT = null;

    protected $fillable = [
        'id', 'reporter_id', 'reported_user_id', 'report_type',
        'description', 'status', 'admin_notes', 'resolved_at', 'resolved_by'
    ];

    protected $casts = [
        'created_at' => 'datetime',
        'resolved_at' => 'datetime',
    ];

    // Relationships
    public function reporter()
    {
        return $this->belongsTo(User::class, 'reporter_id');
    }

    public function reportedUser()
    {
        return $this->belongsTo(User::class, 'reported_user_id');
    }
}

