<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Transaction extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;

    protected $fillable = [
        'id', 'user_id', 'type', 'amount', 'coins', 'status',
        'payment_method', 'payment_gateway_id', 'reference_id', 'reference_type', 'description'
    ];

    protected $casts = [
        'amount' => 'decimal:2',
        'coins' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
    ];

    // Relationships
    public function user()
    {
        return $this->belongsTo(User::class);
    }

    // Get the call associated with this transaction (for CALL_SPENT type)
    public function call()
    {
        return $this->belongsTo(Call::class, 'reference_id');
    }

    // Get the gift associated with this transaction (for GIFT type)
    public function gift()
    {
        return $this->belongsTo(Gifts::class, 'reference_id');
    }

    // Get the polymorphic reference (call, package, gift, etc.)
    public function reference()
    {
        if ($this->reference_type === 'CALL') {
            return Call::find($this->reference_id);
        } elseif ($this->reference_type === 'PACKAGE') {
            return CoinPackage::find($this->reference_id);
        } elseif ($this->reference_type === 'GIFT') {
            return Gifts::find($this->reference_id);
        }
        return null;
    }
}

