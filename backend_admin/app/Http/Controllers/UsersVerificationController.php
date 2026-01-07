<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Storage;

class UsersVerificationController extends Controller
{
    /**
     * Display user verification list page
     */
    public function index(Request $request)
    {
        $query = User::whereNotNull('voice')
            ->orderBy('created_at', 'desc');

        // Filters
        if ($request->filled('status')) {
            // Map status to is_verified
            if ($request->status == 'verified') {
                $query->where('is_verified', true);
            } elseif ($request->status == 'pending') {
                $query->where('is_verified', false)->whereNotNull('voice');
            } elseif ($request->status == 'rejected') {
                $query->where('voice_gender', 'male');
            }
        }

        if ($request->filled('language')) {
            $query->where('language', $request->language);
        }

        if ($request->filled('search')) {
            $search = $request->search;
            $query->where(function($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                  ->orWhere('phone', 'like', "%{$search}%")
                  ->orWhere('language', 'like', "%{$search}%");
            });
        }

        $users = $query->paginate(50);

        return view('users-verification.index', compact('users'));
    }

    /**
     * Detect gender from voice file
     */
    public function detectGender(Request $request)
    {
        try {
            $userId = $request->input('user_id');
            $voiceFile = $request->input('voice_file');

            // Validate inputs
            if (empty($userId) || empty($voiceFile)) {
                return response()->json([
                    'success' => false,
                    'error' => 'User ID and voice file are required.'
                ], 400);
            }

            // Get user
            $user = User::find($userId);
            if (!$user) {
                return response()->json([
                    'success' => false,
                    'error' => 'User not found.'
                ], 404);
            }

            // Build file path
            $voicePath = storage_path('app/public/voices/' . $voiceFile);

            // Check file exists
            if (!file_exists($voicePath)) {
                return response()->json([
                    'success' => false,
                    'error' => 'Voice file not found on server.'
                ], 404);
            }

            // Call Python API
            $pythonApiUrl = env('VOICE_GENDER_API_URL', 'http://localhost:5002/detect');
            
            $ch = curl_init($pythonApiUrl);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
                'audio_path' => $voicePath
            ]));
            curl_setopt($ch, CURLOPT_TIMEOUT, 30);
            curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 10);

            $response = curl_exec($ch);
            $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
            $curlError = curl_error($ch);
            curl_close($ch);

            // Check if Python API is available
            if ($httpCode === 0 || !empty($curlError)) {
                Log::error('Python API Connection Failed', [
                    'error' => $curlError,
                    'url' => $pythonApiUrl
                ]);

                return response()->json([
                    'success' => false,
                    'error' => 'Gender detection service is not available. Please ensure Python API is running.'
                ], 503);
            }

            // Parse response
            $result = json_decode($response, true);

            if ($httpCode === 200 && isset($result['success']) && $result['success']) {
                // Save to database
                $user->voice_gender = $result['gender'];
                
                // Set is_verified based on gender detection
                // Only female voices can be verified (true), male voices remain false
                $user->is_verified = ($result['gender'] === 'female') ? true : false;
                
                // Set verified_datetime if female (verified)
                if ($result['gender'] === 'female') {
                    $user->verified_datetime = now();
                } else {
                    $user->verified_datetime = null;
                }
                
                $user->save();

                Log::info('✅ Voice Gender Detected', [
                    'user_id' => $userId,
                    'gender' => $result['gender'],
                    'confidence' => $result['confidence'],
                    'is_verified' => $user->is_verified
                ]);

                // Return result
                return response()->json([
                    'success' => true,
                    'gender' => $result['gender'],
                    'confidence' => $result['confidence'],
                    'avg_pitch' => $result['avg_pitch'] ?? 'N/A',
                    'duration' => $result['duration'] ?? 0,
                    'is_verified' => $user->is_verified,
                    'model' => 'Hugging Face AI (98.46% accuracy)'
                ]);
            } else {
                return response()->json([
                    'success' => false,
                    'error' => $result['error'] ?? 'Unknown error during detection.'
                ], 400);
            }

        } catch (\Exception $e) {
            Log::error('❌ Detect Gender Exception', [
                'error' => $e->getMessage(),
                'file' => $e->getFile(),
                'line' => $e->getLine(),
                'trace' => $e->getTraceAsString()
            ]);

            return response()->json([
                'success' => false,
                'error' => 'An unexpected error occurred: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Bulk update verification status
     */
    public function bulkUpdateStatus(Request $request)
    {
        try {
            $userIds = $request->input('user_ids', []);
            $action = $request->input('action'); // 'verify' or 'cancel'

            if (empty($userIds) || !is_array($userIds)) {
                return response()->json([
                    'success' => false,
                    'error' => 'Please select at least one user.'
                ], 400);
            }

            if (!in_array($action, ['verify', 'cancel'])) {
                return response()->json([
                    'success' => false,
                    'error' => 'Invalid action. Use "verify" or "cancel".'
                ], 400);
            }

            // Update users
            $updateData = [
                'is_verified' => $action === 'verify' ? true : false
            ];
            
            // Set verified_datetime when verifying
            if ($action === 'verify') {
                $updateData['verified_datetime'] = now();
            } else {
                // Clear verified_datetime when canceling
                $updateData['verified_datetime'] = null;
            }
            
            $updated = User::whereIn('id', $userIds)
                ->update($updateData);

            Log::info('✅ Bulk Status Update', [
                'action' => $action,
                'user_ids' => $userIds,
                'updated_count' => $updated
            ]);

            return response()->json([
                'success' => true,
                'message' => "Successfully {$action}d {$updated} user(s).",
                'updated_count' => $updated
            ]);

        } catch (\Exception $e) {
            Log::error('❌ Bulk Update Exception', [
                'error' => $e->getMessage(),
                'file' => $e->getFile(),
                'line' => $e->getLine()
            ]);

            return response()->json([
                'success' => false,
                'error' => 'An unexpected error occurred: ' . $e->getMessage()
            ], 500);
        }
    }
}

