<?php

return [
    /*
    |--------------------------------------------------------------------------
    | Firebase Service Account Credentials
    |--------------------------------------------------------------------------
    |
    | Path to the Firebase service account JSON file.
    | Download this from: Firebase Console → Project Settings → Service Accounts
    | → Generate New Private Key
    |
    | IMPORTANT:
    | - Do NOT commit the JSON to git.
    | - Put the JSON on the server: storage/app/firebase-credentials.json
    | - Or set FIREBASE_CREDENTIALS to an absolute path in .env
    |
    */

    'credentials' => env(
        'FIREBASE_CREDENTIALS',
        storage_path('app/firebase-credentials.json')
    ),

    /*
    |--------------------------------------------------------------------------
    | Firebase Project ID
    |--------------------------------------------------------------------------
    |
    | Your Firebase project ID (optional, mainly for reference)
    |
    */

    'project_id' => env('FIREBASE_PROJECT_ID', ''),

    /*
    |--------------------------------------------------------------------------
    | Firebase Database URL
    |--------------------------------------------------------------------------
    |
    | Your Firebase Realtime Database URL (if using Firebase Database)
    |
    */

    'database_url' => env('FIREBASE_DATABASE_URL', ''),
];
