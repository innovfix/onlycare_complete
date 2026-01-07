<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class ScreenNotifications extends Model
{
    use HasFactory;

    protected $fillable = [
        'title',
        'description',
        'time',
        'day',
        'gender',
        'language',
        'logo',
        'image',
    ];
}
