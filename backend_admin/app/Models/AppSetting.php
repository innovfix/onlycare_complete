<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class AppSetting extends Model
{
    use HasFactory;

    protected $keyType = 'string';
    public $incrementing = false;
    
    const CREATED_AT = null;

    protected $fillable = [
        'id', 'setting_key', 'setting_value', 'setting_type', 'description'
    ];

    protected $casts = [
        'updated_at' => 'datetime',
    ];

    // Helper to get typed value
    public function getTypedValue()
    {
        switch ($this->setting_type) {
            case 'INTEGER':
                return (int) $this->setting_value;
            case 'BOOLEAN':
                return (bool) $this->setting_value;
            case 'JSON':
                return json_decode($this->setting_value, true);
            default:
                return $this->setting_value;
        }
    }
}

