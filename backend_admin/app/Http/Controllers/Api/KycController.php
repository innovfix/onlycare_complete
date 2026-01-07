<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\KycDocument;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;

class KycController extends Controller
{
    /**
     * Get KYC status
     */
    public function getStatus(Request $request)
    {
        $documents = KycDocument::where('user_id', $request->user()->id)->get();

        $kycStatus = $request->user()->kyc_status ?? 'PENDING';

        return response()->json([
            'success' => true,
            'kyc_status' => $kycStatus,
            'documents' => $documents->map(function($doc) {
                return [
                    'type' => $doc->document_type,
                    'status' => $doc->status,
                    'submitted_at' => $doc->created_at->toIso8601String(),
                    'verified_at' => $doc->verified_at ? $doc->verified_at->toIso8601String() : null
                ];
            })
        ]);
    }

    /**
     * Submit KYC documents
     */
    public function submitDocuments(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'aadhaar_number' => 'required|string|size:12',
            'aadhaar_front' => 'required|string', // base64 or file upload
            'aadhaar_back' => 'required|string',
            'pan_number' => 'required|string|size:10',
            'pan_image' => 'required|string',
            'selfie' => 'required|string'
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

        // Check if KYC already submitted
        $existingKyc = KycDocument::where('user_id', $request->user()->id)
                                  ->whereIn('status', ['PENDING', 'APPROVED'])
                                  ->exists();

        if ($existingKyc) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'KYC_ALREADY_SUBMITTED',
                    'message' => 'KYC documents already submitted'
                ]
            ], 400);
        }

        // Store Aadhaar documents
        $aadhaarFrontPath = $this->storeDocument($request->aadhaar_front, 'aadhaar_front');
        $aadhaarBackPath = $this->storeDocument($request->aadhaar_back, 'aadhaar_back');

        KycDocument::create([
            'user_id' => $request->user()->id,
            'document_type' => 'AADHAAR',
            'document_number' => $request->aadhaar_number,
            'front_image' => $aadhaarFrontPath,
            'back_image' => $aadhaarBackPath,
            'status' => 'PENDING'
        ]);

        // Store PAN document
        $panImagePath = $this->storeDocument($request->pan_image, 'pan');

        KycDocument::create([
            'user_id' => $request->user()->id,
            'document_type' => 'PAN',
            'document_number' => strtoupper($request->pan_number),
            'front_image' => $panImagePath,
            'status' => 'PENDING'
        ]);

        // Store Selfie
        $selfiePath = $this->storeDocument($request->selfie, 'selfie');

        KycDocument::create([
            'user_id' => $request->user()->id,
            'document_type' => 'SELFIE',
            'front_image' => $selfiePath,
            'status' => 'PENDING'
        ]);

        // Update user KYC status
        $request->user()->update(['kyc_status' => 'PENDING']);

        return response()->json([
            'success' => true,
            'message' => 'KYC documents submitted successfully',
            'kyc_status' => 'PENDING'
        ]);
    }

    /**
     * Store document (placeholder for actual file storage)
     */
    private function storeDocument($fileData, $prefix)
    {
        // TODO: Implement actual file upload to S3/CDN
        // For now, we'll just return a placeholder URL
        
        // If it's a base64 string, decode and save
        if (strpos($fileData, 'data:image') === 0) {
            // Extract base64 data
            $image = str_replace('data:image/png;base64,', '', $fileData);
            $image = str_replace('data:image/jpg;base64,', '', $image);
            $image = str_replace('data:image/jpeg;base64,', '', $image);
            $image = str_replace(' ', '+', $image);
            
            $filename = $prefix . '_' . time() . '_' . uniqid() . '.jpg';
            
            // In production, upload to S3
            // Storage::disk('s3')->put('kyc/' . $filename, base64_decode($image));
            
            return 'kyc/' . $filename;
        }
        
        return $fileData;
    }
}







