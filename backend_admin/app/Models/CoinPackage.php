<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class CoinPackage extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;

    protected $fillable = [
        'id', 'coins', 'price', 'original_price', 'discount',
        'is_popular', 'is_best_value', 'is_active', 'sort_order'
    ];

    protected $casts = [
        'coins' => 'integer',
        'price' => 'decimal:2',
        'original_price' => 'decimal:2',
        'discount' => 'integer',
        'is_popular' => 'boolean',
        'is_best_value' => 'boolean',
        'is_active' => 'boolean',
        'sort_order' => 'integer',
    ];

    // Helper method to get discount percentage
    public function getDiscountPercentage()
    {
        if ($this->original_price > 0) {
            return round((($this->original_price - $this->price) / $this->original_price) * 100);
        }
        return 0;
    }
}

