<?php

namespace App\Services;

/**
 * Agora RTC Token Builder
 * Based on Agora's official token generation algorithm
 */
class AgoraTokenBuilder
{
    const ROLE_PUBLISHER = 1;
    const ROLE_SUBSCRIBER = 2;
    
    /**
     * Generate Agora RTC Token (simplified version)
     * 
     * @param string $appId - Agora App ID
     * @param string $appCertificate - Agora App Certificate  
     * @param string $channelName - Channel name for the call
     * @param int $uid - User ID (0 for any user)
     * @param int $role - 1 = Publisher, 2 = Subscriber
     * @param int $privilegeExpireTs - Token expiration (seconds from now)
     * @return string - Generated token
     */
    public static function buildToken(
        string $appId,
        string $appCertificate,
        string $channelName,
        int $uid = 0,
        int $role = 1,
        int $privilegeExpireTs = 3600
    ): string {
        // Calculate expiration timestamp
        $expireTimestamp = time() + $privilegeExpireTs;
        
        // Build the token using simplified algorithm
        $version = '007';
        
        // Create message for signing
        $salt = rand(1, 99999999);
        $ts = time();
        
        // Pack data
        $packContent = self::packUint32($salt) 
                     . self::packUint32($ts)
                     . self::packUint32($expireTimestamp);
        
        // Add channel name
        $packContent .= self::packString($channelName);
        
        // Add uid
        $packContent .= self::packUint32($uid);
        
        // Generate signature using HMAC SHA256
        $signature = hash_hmac('sha256', $packContent, $appCertificate, true);
        
        // Combine all parts
        $content = self::packUint32(strlen($signature)) . $signature . $packContent;
        
        // Encode to base64
        $token = $version . $appId . base64_encode($content);
        
        return $token;
    }

    /**
     * Build token with default role (Publisher) and 24-hour expiration
     */
    public static function buildTokenWithDefault(
        string $appId,
        string $appCertificate,
        string $channelName,
        int $uid = 0
    ): string {
        // Token expires in 24 hours (86400 seconds)
        return self::buildToken($appId, $appCertificate, $channelName, $uid, self::ROLE_PUBLISHER, 86400);
    }

    /**
     * Pack unsigned 32-bit integer
     */
    private static function packUint32(int $value): string
    {
        return pack('N', $value);
    }

    /**
     * Pack unsigned 16-bit integer
     */
    private static function packUint16(int $value): string
    {
        return pack('n', $value);
    }

    /**
     * Pack a string with 16-bit length prefix
     */
    private static function packString(string $value): string
    {
        return self::packUint16(strlen($value)) . $value;
    }
    
    /**
     * Pack map/array (for privileges)
     */
    private static function packMap(array $map): string
    {
        $result = self::packUint16(count($map));
        foreach ($map as $key => $value) {
            $result .= self::packUint16($key);
            $result .= self::packUint32($value);
        }
        return $result;
    }
}
