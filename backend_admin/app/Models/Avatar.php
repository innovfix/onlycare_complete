<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Avatar extends Model
{
    use HasFactory;

    protected $fillable = [
        'gender',
        'image_url'
    ];

    /**
     * Scope to filter by gender
     */
    public function scopeForGender($query, $gender)
    {
        return $query->where('gender', strtoupper($gender));
    }
}
