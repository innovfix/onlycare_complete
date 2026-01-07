<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Withdrawal extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;
    
    const CREATED_AT = 'requested_at';
    const UPDATED_AT = null;

    protected $fillable = [
        'id', 'user_id', 'amount', 'coins', 'status',
        'bank_account_id', 'admin_notes', 'rejected_reason',
        'processed_at', 'completed_at'
    ];

    protected $casts = [
        'amount' => 'decimal:2',
        'coins' => 'integer',
        'requested_at' => 'datetime',
        'processed_at' => 'datetime',
        'completed_at' => 'datetime',
    ];

    // Relationships
    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function bankAccount()
    {
        return $this->belongsTo(BankAccount::class);
    }
}

