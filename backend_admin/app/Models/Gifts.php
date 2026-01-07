<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Gifts extends Model
{
    use HasFactory;

    protected $table = 'gifts';

    protected $fillable = [
        'gift_icon',
        'coins'
    ];

    protected $casts = [
        'coins' => 'integer',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
    ];
}
