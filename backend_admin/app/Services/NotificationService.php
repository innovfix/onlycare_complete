<?php

namespace App\Services;

use App\Models\User;
use App\Models\ScreenNotifications;
use Carbon\Carbon;
use Berkayk\OneSignal\OneSignalFacade as OneSignal;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class NotificationService
{
    /**
     * Check if user should receive notification based on smart filtering
     */
    public function shouldSendNotification($userId, $notificationType = 'scheduled')
    {
        // 1. Check if user exists
        $user = User::find($userId);
        if (!$user) {
            return false;
        }

        // 2. Check if user is blocked
        if ($user->is_blocked == 1 || $user->blocked == 1) {
            return false;
        }

        // 3. For scheduled notifications, check if user was recently active
        if ($notificationType === 'scheduled' && $user->last_seen) {
            $lastSeen = is_numeric($user->last_seen) 
                ? Carbon::createFromTimestamp($user->last_seen)
                : Carbon::parse($user->last_seen);
            
            $minutesSinceActive = Carbon::now()->diffInMinutes($lastSeen);
            
            // Don't send if user was active in last 2 hours
            if ($minutesSinceActive < 120) {
                return false;
            }
        }

        // 4. Rate limiting for scheduled notifications (max 1 per hour)
        if ($notificationType === 'scheduled') {
            // Since we don't have notification_logs, we'll skip rate limiting
            // This can be implemented later if needed
        }

        // 5. Daily limit check (max 8 notifications per day)
        // Since we don't have notification_logs, we'll skip daily limit
        // This can be implemented later if needed

        return true;
    }

    /**
     * Build OneSignal filters based on gender and language
     */
    public function buildOneSignalFilters($gender, $language)
    {
        $filters = [];

        // If both are "all", send to everyone
        if ($gender === 'all' && $language === 'all') {
            return []; // Empty filters means send to all
        }

        // If both gender and language are specified
        if ($gender !== 'all' && $language !== 'all') {
            $filters[] = [
                "field" => "tag",
                "key" => "gender_language",
                "relation" => "=",
                "value" => "{$gender}_{$language}"
            ];
        } 
        // If only gender is specified
        elseif ($gender !== 'all') {
            $filters[] = [
                "field" => "tag",
                "key" => "gender",
                "relation" => "=",
                "value" => $gender
            ];
        } 
        // If only language is specified
        elseif ($language !== 'all') {
            $filters[] = [
                "field" => "tag",
                "key" => "language",
                "relation" => "=",
                "value" => $language
            ];
        }

        return $filters;
    }

    /**
     * Send scheduled notification with smart filtering
     */
    public function sendScheduledNotification($notification)
    {
        try {
            // Build OneSignal filters
            $filters = $this->buildOneSignalFilters($notification->gender, $notification->language);

            // Build payload
            $payload = [
                "app_id" => config('onesignal.app_id'),
                "headings" => ["en" => $notification->title],
                "contents" => ["en" => $notification->description],
                "small_icon" => "notification_icon",
            ];

            // Add filters or send to all
            if (!empty($filters)) {
                $payload["filters"] = $filters;
            } else {
                $payload["included_segments"] = ["All"];
            }

            // Add logo if exists
            if ($notification->logo) {
                $logoUrl = asset('storage/' . $notification->logo);
                $payload["large_icon"] = $logoUrl;
            }

            // Add image if exists
            if ($notification->image) {
                $imageUrl = asset('storage/' . $notification->image);
                $payload["big_picture"] = $imageUrl;
            }

            // Send notification
            $response = OneSignal::sendNotificationCustom($payload);

            return $response;
        } catch (\Exception $e) {
            Log::error('Failed to send scheduled notification: ' . $e->getMessage());
            return false;
        }
    }

    /**
     * Send immediate notification
     */
    public function sendImmediateNotification($notification)
    {
        try {
            // Build OneSignal filters
            $filters = $this->buildOneSignalFilters($notification->gender, $notification->language);

            // Build payload
            $payload = [
                "app_id" => config('onesignal.app_id'),
                "headings" => ["en" => $notification->title],
                "contents" => ["en" => $notification->description],
                "small_icon" => "notification_icon",
            ];

            // Add filters or send to all
            if (!empty($filters)) {
                $payload["filters"] = $filters;
            } else {
                $payload["included_segments"] = ["All"];
            }

            // Add logo if exists
            if ($notification->logo) {
                $logoUrl = asset('storage/' . $notification->logo);
                $payload["large_icon"] = $logoUrl;
            }

            // Add image if exists
            if ($notification->image) {
                $imageUrl = asset('storage/' . $notification->image);
                $payload["big_picture"] = $imageUrl;
            }

            // Send notification
            $response = OneSignal::sendNotificationCustom($payload);

            return $response;
        } catch (\Exception $e) {
            Log::error('Failed to send immediate notification: ' . $e->getMessage());
            return false;
        }
    }

    /**
     * Send personalized notification to specific user
     */
    public function sendPersonalizedNotification($userId, $title, $description, $type = 'event', $data = [])
    {
        try {
            $user = User::find($userId);
            if (!$user || !$user->player_id) {
                return false;
            }

            $payload = [
                "app_id" => config('onesignal.app_id'),
                "include_player_ids" => [$user->player_id],
                "headings" => ["en" => $title],
                "contents" => ["en" => $description],
                "small_icon" => "notification_icon",
                "data" => array_merge(["type" => $type], $data),
            ];

            $response = OneSignal::sendNotificationCustom($payload);
            return $response;
        } catch (\Exception $e) {
            Log::error('Failed to send personalized notification: ' . $e->getMessage());
            return false;
        }
    }

    /**
     * Notify users when their favorite creator comes online
     */
    public function notifyFavoritesOnline($creatorId)
    {
        try {
            $creator = User::find($creatorId);
            if (!$creator) {
                return false;
            }

            // Get users who favorited this creator
            // Note: This assumes you have a favorites/followers relationship
            // Adjust based on your actual database structure
            $favorites = DB::table('friendships')
                ->where('friend_id', $creatorId)
                ->pluck('user_id');

            foreach ($favorites as $userId) {
                if ($this->shouldSendNotification($userId, 'event')) {
                    $this->sendPersonalizedNotification(
                        $userId,
                        "{$creator->name} is now online!",
                        "Your favorite creator is available for calls.",
                        'favorite_online',
                        ['creator_id' => $creatorId]
                    );
                }
            }

            return true;
        } catch (\Exception $e) {
            Log::error('Failed to notify favorites online: ' . $e->getMessage());
            return false;
        }
    }
}

