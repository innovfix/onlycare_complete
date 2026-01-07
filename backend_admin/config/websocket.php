<?php

return [

    /*
    |--------------------------------------------------------------------------
    | WebSocket Server URL
    |--------------------------------------------------------------------------
    |
    | The URL of your Socket.io server. This should be the internal URL
    | (localhost) when both Laravel and Socket.io are on the same server.
    |
    */

    'url' => env('WEBSOCKET_URL', 'http://localhost:3001'),

    /*
    |--------------------------------------------------------------------------
    | Connection Timeout
    |--------------------------------------------------------------------------
    |
    | Timeout in seconds for HTTP requests to the WebSocket server.
    | Keep this low to avoid blocking Laravel requests.
    |
    */

    'timeout' => env('WEBSOCKET_TIMEOUT', 2),

    /*
    |--------------------------------------------------------------------------
    | Public WebSocket URL
    |--------------------------------------------------------------------------
    |
    | The public URL that Android clients should use to connect.
    | This should use your domain with SSL in production.
    |
    */

    'public_url' => env('WEBSOCKET_PUBLIC_URL', 'https://onlycare.in'),

    /*
    |--------------------------------------------------------------------------
    | Enable WebSocket
    |--------------------------------------------------------------------------
    |
    | Toggle WebSocket functionality on/off. When disabled, the app
    | will fall back to FCM notifications only.
    |
    */

    'enabled' => env('WEBSOCKET_ENABLED', true),

    /*
    |--------------------------------------------------------------------------
    | WebSocket Secret
    |--------------------------------------------------------------------------
    |
    | Secret key for internal communication between Laravel and Socket.io
    | server. This should match LARAVEL_API_SECRET in socket server .env
    |
    */

    'secret' => env('WEBSOCKET_SECRET', 'your-secret-key-change-in-production'),

];









