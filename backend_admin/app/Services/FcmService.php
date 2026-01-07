<?php

namespace App\Services;

use Illuminate\Support\Facades\Log;

class FcmService
{
    /**
     * Send an "incoming_call" push notification to a single device token.
     *
     * Uses Kreait Firebase PHP SDK (FCM HTTP v1 via service account).
     *
     * @return bool true if sent, false if skipped/failed
     */
    public function sendIncomingCall(array $payload): bool
    {
        $token = $payload['token'] ?? null;
        if (empty($token)) {
            Log::info('FCM: no token provided, skipping push');
            return false;
        }

        // Soft-fail if SDK not installed yet
        if (!class_exists(\Kreait\Firebase\Factory::class)) {
            Log::warning('FCM: kreait/firebase-php not installed. Run: composer require kreait/firebase-php');
            return false;
        }

        $credentialsPath = config('firebase.credentials');
        if (empty($credentialsPath) || !file_exists($credentialsPath)) {
            Log::error("FCM: credentials JSON not found at: {$credentialsPath}");
            return false;
        }

        try {
            $firebase = (new \Kreait\Firebase\Factory())->withServiceAccount($credentialsPath);
            $messaging = $firebase->createMessaging();

            $notificationTitle = $payload['title'] ?? 'Incoming call';
            $notificationBody = $payload['body'] ?? 'Tap to answer';

            $notification = \Kreait\Firebase\Messaging\Notification::create($notificationTitle, $notificationBody);

            // Data keys must match Android app expectations (see CallNotificationService)
            $data = [
                'type' => 'incoming_call',
                'callId' => (string)($payload['callId'] ?? ''),
                'callerId' => (string)($payload['callerId'] ?? ''),
                'callerName' => (string)($payload['callerName'] ?? ''),
                'callerPhoto' => (string)($payload['callerPhoto'] ?? ''),
                'channelId' => (string)($payload['channelId'] ?? ''),
                'agoraToken' => (string)($payload['agoraToken'] ?? ''),
                'agoraAppId' => (string)($payload['agoraAppId'] ?? ''),
                'callType' => (string)($payload['callType'] ?? ''),
                'balanceTime' => (string)($payload['balanceTime'] ?? ''),
            ];

            // Remove null-ish values (FCM requires string map)
            $data = array_filter($data, static fn($v) => $v !== null);

            $androidConfig = \Kreait\Firebase\Messaging\AndroidConfig::fromArray([
                'priority' => 'high',
            ]);

            $message = \Kreait\Firebase\Messaging\CloudMessage::withTarget('token', $token)
                ->withNotification($notification)
                ->withData($data)
                ->withAndroidConfig($androidConfig);

            $messaging->send($message);

            Log::info('FCM: incoming_call push sent', [
                'callId' => $data['callId'] ?? null,
                'to' => substr($token, 0, 12) . '...',
            ]);

            return true;
        } catch (\Throwable $e) {
            Log::error('FCM: push failed', [
                'message' => $e->getMessage(),
            ]);
            return false;
        }
    }
}







