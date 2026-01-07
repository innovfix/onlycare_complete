<?php

namespace App\Services;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class WebSocketService
{
    protected $socketUrl;
    protected $timeout;

    public function __construct()
    {
        $this->socketUrl = config('websocket.url', 'http://localhost:3001');
        $this->timeout = config('websocket.timeout', 2);
    }

    /**
     * Check if user is connected to WebSocket
     *
     * @param string $userId
     * @return bool
     */
    public function isUserOnline(string $userId): bool
    {
        try {
            $response = Http::timeout($this->timeout)
                ->get("{$this->socketUrl}/api/users/{$userId}/online");
            
            if ($response->successful()) {
                return $response->json('isOnline', false);
            }
            
            return false;
        } catch (\Exception $e) {
            Log::warning("Failed to check WebSocket status for user {$userId}: " . $e->getMessage());
            return false;
        }
    }

    /**
     * Get all connected users
     *
     * @return array
     */
    public function getConnectedUsers(): array
    {
        try {
            $response = Http::timeout($this->timeout)
                ->get("{$this->socketUrl}/api/connected-users");
            
            if ($response->successful()) {
                return $response->json('users', []);
            }
            
            return [];
        } catch (\Exception $e) {
            Log::warning("Failed to get connected users: " . $e->getMessage());
            return [];
        }
    }

    /**
     * Get server health status
     *
     * @return array|null
     */
    public function getHealthStatus(): ?array
    {
        try {
            $response = Http::timeout($this->timeout)
                ->get("{$this->socketUrl}/health");
            
            if ($response->successful()) {
                return $response->json();
            }
            
            return null;
        } catch (\Exception $e) {
            Log::error("Failed to get WebSocket health status: " . $e->getMessage());
            return null;
        }
    }

    /**
     * Check if WebSocket server is available
     *
     * @return bool
     */
    public function isServerAvailable(): bool
    {
        $health = $this->getHealthStatus();
        return $health !== null && ($health['status'] ?? '') === 'OK';
    }
}









