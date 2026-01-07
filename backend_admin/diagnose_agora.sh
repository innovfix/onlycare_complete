#!/bin/bash

echo "============================================"
echo "  AGORA TOKEN GENERATION DIAGNOSTIC"
echo "============================================"
echo ""

cd /var/www/onlycare_admin

echo "1️⃣  Checking .env file..."
echo "-------------------------------------------"
if [ -f .env ]; then
    echo "✅ .env file exists"
    echo ""
    echo "AGORA credentials in .env:"
    grep -E "AGORA_APP_ID|AGORA_APP_CERTIFICATE" .env || echo "❌ No AGORA credentials found!"
else
    echo "❌ .env file not found!"
fi
echo ""

echo "2️⃣  Checking Laravel config..."
echo "-------------------------------------------"
php artisan tinker --execute="
    echo 'App ID from config: ' . config('services.agora.app_id') . PHP_EOL;
    echo 'App ID empty? ' . (empty(config('services.agora.app_id')) ? 'YES ❌' : 'NO ✅') . PHP_EOL;
    echo 'Certificate from config: ' . (empty(config('services.agora.app_certificate')) ? 'EMPTY ❌' : substr(config('services.agora.app_certificate'), 0, 10) . '... ✅') . PHP_EOL;
    echo 'Certificate length: ' . strlen(config('services.agora.app_certificate') ?? '') . PHP_EOL;
"
echo ""

echo "3️⃣  Testing AgoraTokenBuilder..."
echo "-------------------------------------------"
php artisan tinker --execute="
    try {
        \$token = \App\Services\AgoraTokenBuilder::buildTokenWithDefault(
            '8b5e9417f15a48ae929783f32d3d33d4',
            '03e9b06b303e47a9b93e71aed9faac63',
            'test_channel_12345',
            0
        );
        echo 'Token generated: ✅' . PHP_EOL;
        echo 'Token length: ' . strlen(\$token) . PHP_EOL;
        echo 'Token preview: ' . substr(\$token, 0, 50) . '...' . PHP_EOL;
    } catch (\Exception \$e) {
        echo 'Token generation FAILED: ❌' . PHP_EOL;
        echo 'Error: ' . \$e->getMessage() . PHP_EOL;
    }
"
echo ""

echo "4️⃣  Checking recent logs..."
echo "-------------------------------------------"
if [ -f storage/logs/laravel.log ]; then
    echo "Recent Agora-related log entries:"
    tail -200 storage/logs/laravel.log | grep -i "agora\|token" | tail -10 || echo "No recent Agora logs found"
else
    echo "❌ No log file found"
fi
echo ""

echo "============================================"
echo "  RECOMMENDATIONS"
echo "============================================"
echo ""
echo "If you see 'EMPTY' or 'NULL' values above, run:"
echo ""
echo "  php artisan config:clear"
echo "  php artisan cache:clear"
echo "  php artisan config:cache"
echo "  sudo systemctl restart php8.2-fpm"
echo "  sudo systemctl restart nginx"
echo ""
echo "Then run this script again!"
echo ""
echo "============================================"

