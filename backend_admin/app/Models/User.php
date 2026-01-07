<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Database\Eloquent\SoftDeletes;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    use HasFactory, SoftDeletes, HasApiTokens;

    protected $keyType = 'string';
    public $incrementing = false;
    
    protected $fillable = [
        'id', 'phone', 'country_code', 'name', 'username', 'age', 'gender', 'user_type', 'profile_image', 'bio', 
        'language', 'interests', 'is_online', 'is_busy', 'last_seen', 'online_datetime', 'rating', 
        'total_ratings', 'coin_balance', 'total_earnings', 'audio_call_enabled', 
        'video_call_enabled', 'is_verified', 'verified_datetime', 'kyc_status', 'is_blocked', 'blocked_reason',
        'referral_code', 'is_active', 'api_token', 'fcm_token', 'voice', 'voice_gender'
    ];

    protected $casts = [
        'interests' => 'array',
        'is_online' => 'boolean',
        'is_busy' => 'boolean',
        'last_seen' => 'integer',
        'online_datetime' => 'datetime',
        'audio_call_enabled' => 'boolean',
        'video_call_enabled' => 'boolean',
        'is_verified' => 'boolean',
        'is_blocked' => 'boolean',
        'is_active' => 'boolean',
        'rating' => 'decimal:1',
        'verified_datetime' => 'datetime',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
        'deleted_at' => 'datetime',
    ];

    // Relationships
    public function callsAsCaller()
    {
        return $this->hasMany(Call::class, 'caller_id');
    }

    public function callsAsReceiver()
    {
        return $this->hasMany(Call::class, 'receiver_id');
    }

    public function transactions()
    {
        return $this->hasMany(Transaction::class);
    }

    public function bankAccounts()
    {
        return $this->hasMany(BankAccount::class);
    }

    public function withdrawals()
    {
        return $this->hasMany(Withdrawal::class);
    }

    public function kycDocuments()
    {
        return $this->hasMany(KycDocument::class);
    }

    public function sentMessages()
    {
        return $this->hasMany(Message::class, 'sender_id');
    }

    public function receivedMessages()
    {
        return $this->hasMany(Message::class, 'receiver_id');
    }

    public function friendships()
    {
        return $this->hasMany(Friendship::class, 'user_id');
    }

    public function referralsGiven()
    {
        return $this->hasMany(Referral::class, 'referrer_id');
    }

    public function referralsReceived()
    {
        return $this->hasMany(Referral::class, 'referred_user_id');
    }

    public function reportsMade()
    {
        return $this->hasMany(Report::class, 'reporter_id');
    }

    public function reportsReceived()
    {
        return $this->hasMany(Report::class, 'reported_user_id');
    }

    public function notifications()
    {
        return $this->hasMany(Notification::class);
    }

    public function blockedUsers()
    {
        return $this->hasMany(BlockedUser::class, 'user_id');
    }
}

