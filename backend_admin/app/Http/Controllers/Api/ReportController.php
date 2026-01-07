<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Report;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class ReportController extends Controller
{
    /**
     * Report user
     */
    public function reportUser(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'reported_user_id' => 'required|string',
            'report_type' => 'required|in:HARASSMENT,SPAM,INAPPROPRIATE_CONTENT,FAKE_PROFILE,OTHER',
            'description' => 'required|string|max:1000'
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

        $reportedUserId = str_replace('USR_', '', $request->reported_user_id);
        
        $reportedUser = User::find($reportedUserId);
        if (!$reportedUser) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'User not found'
                ]
            ], 404);
        }

        // Can't report self
        if ($reportedUserId == $request->user()->id) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INVALID_ACTION',
                    'message' => 'Cannot report yourself'
                ]
            ], 400);
        }

        $report = Report::create([
            'reporter_id' => $request->user()->id,
            'reported_user_id' => $reportedUserId,
            'report_type' => $request->report_type,
            'description' => $request->description,
            'status' => 'PENDING'
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Report submitted successfully',
            'report_id' => 'REP_' . $report->id
        ]);
    }
}







