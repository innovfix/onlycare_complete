<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class BankAccount extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;

    protected $fillable = [
        'id', 'user_id', 'account_holder_name', 'account_number',
        'ifsc_code', 'bank_name', 'branch_name', 'upi_id',
        'is_primary', 'is_verified',
        'pancard_name', 'pancard_number'
    ];

    protected $casts = [
        'is_primary' => 'boolean',
        'is_verified' => 'boolean',
    ];

    // Relationships
    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function withdrawals()
    {
        return $this->hasMany(Withdrawal::class);
    }

    // Helper to mask account number
    public function getMaskedAccountNumber()
    {
        $length = strlen($this->account_number);
        if ($length <= 4) return $this->account_number;
        return str_repeat('X', $length - 4) . substr($this->account_number, -4);
    }
}

