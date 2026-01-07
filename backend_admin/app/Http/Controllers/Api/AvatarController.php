<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Avatar;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class AvatarController extends Controller
{
    /**
     * Get avatar list by gender
     */
    public function index(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'gender' => 'required|in:MALE,FEMALE,male,female'
        ], [
            'gender.required' => 'Gender parameter is required',
            'gender.in' => 'Gender must be either MALE or FEMALE'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION_ERROR',
                    'message' => 'Invalid input data',
                    'details' => $validator->errors()
                ]
            ], 422);
        }

        $gender = strtoupper($request->gender);
        
        $avatars = Avatar::where('gender', $gender)
            ->orderBy('created_at', 'desc')
            ->get();

        return response()->json([
            'success' => true,
            'gender' => $gender,
            'count' => $avatars->count(),
            'avatars' => $avatars->map(function($avatar) {
                // Return full URL if image_url is relative, otherwise return as is
                $imageUrl = $avatar->image_url;
                if ($imageUrl && !filter_var($imageUrl, FILTER_VALIDATE_URL)) {
                    // If it's a relative path, make it absolute
                    $imageUrl = url($imageUrl);
                }
                
                return [
                    'id' => $avatar->id,
                    'image_url' => $imageUrl,
                    'gender' => $avatar->gender,
                    'created_at' => $avatar->created_at->toIso8601String(),
                    'updated_at' => $avatar->updated_at->toIso8601String()
                ];
            })
        ]);
    }
}