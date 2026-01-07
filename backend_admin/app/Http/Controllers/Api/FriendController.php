<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Friendship;
use App\Models\User;
use Illuminate\Http\Request;

class FriendController extends Controller
{
    /**
     * Get friends list
     */
    public function getFriends(Request $request)
    {
        $perPage = min($request->get('limit', 20), 50);
        
        $friendships = Friendship::where(function($query) use ($request) {
                            $query->where('user_id', $request->user()->id)
                                  ->orWhere('friend_id', $request->user()->id);
                        })
                        ->where('status', 'ACCEPTED')
                        ->paginate($perPage);

        $friends = $friendships->map(function($friendship) use ($request) {
            $friendId = $friendship->user_id === $request->user()->id ? 
                       $friendship->friend_id : 
                       $friendship->user_id;
            
            $friend = User::find($friendId);
            
            return [
                'id' => 'USR_' . $friend->id,
                'name' => $friend->name,
                'age' => $friend->age,
                'profile_image' => $friend->profile_image,
                'is_online' => $friend->is_online,
                'last_seen' => $friend->last_seen ? $friend->last_seen->timestamp * 1000 : null
            ];
        });

        return response()->json([
            'success' => true,
            'friends' => $friends,
            'pagination' => [
                'current_page' => $friendships->currentPage(),
                'total_pages' => $friendships->lastPage(),
                'total_items' => $friendships->total(),
                'per_page' => $friendships->perPage()
            ]
        ]);
    }

    /**
     * Send friend request
     */
    public function sendRequest(Request $request, $userId)
    {
        $friendId = str_replace('USR_', '', $userId);
        
        $friend = User::find($friendId);
        if (!$friend) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'User not found'
                ]
            ], 404);
        }

        // Check if already friends or request pending
        $existing = Friendship::where(function($query) use ($request, $friendId) {
                        $query->where('user_id', $request->user()->id)
                              ->where('friend_id', $friendId);
                    })
                    ->orWhere(function($query) use ($request, $friendId) {
                        $query->where('user_id', $friendId)
                              ->where('friend_id', $request->user()->id);
                    })
                    ->first();

        if ($existing) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'ALREADY_EXISTS',
                    'message' => 'Friend request already sent or already friends'
                ]
            ], 400);
        }

        Friendship::create([
            'user_id' => $request->user()->id,
            'friend_id' => $friendId,
            'status' => 'PENDING'
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Friend request sent'
        ]);
    }

    /**
     * Accept friend request
     */
    public function acceptRequest(Request $request, $userId)
    {
        $friendId = str_replace('USR_', '', $userId);
        
        $friendship = Friendship::where('user_id', $friendId)
                                ->where('friend_id', $request->user()->id)
                                ->where('status', 'PENDING')
                                ->first();

        if (!$friendship) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Friend request not found'
                ]
            ], 404);
        }

        $friendship->update(['status' => 'ACCEPTED']);

        return response()->json([
            'success' => true,
            'message' => 'Friend request accepted'
        ]);
    }

    /**
     * Reject friend request
     */
    public function rejectRequest(Request $request, $userId)
    {
        $friendId = str_replace('USR_', '', $userId);
        
        $friendship = Friendship::where('user_id', $friendId)
                                ->where('friend_id', $request->user()->id)
                                ->where('status', 'PENDING')
                                ->first();

        if (!$friendship) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Friend request not found'
                ]
            ], 404);
        }

        $friendship->update(['status' => 'REJECTED']);

        return response()->json([
            'success' => true,
            'message' => 'Friend request rejected'
        ]);
    }

    /**
     * Remove friend
     */
    public function removeFriend(Request $request, $userId)
    {
        $friendId = str_replace('USR_', '', $userId);
        
        Friendship::where(function($query) use ($request, $friendId) {
                    $query->where('user_id', $request->user()->id)
                          ->where('friend_id', $friendId);
                })
                ->orWhere(function($query) use ($request, $friendId) {
                    $query->where('user_id', $friendId)
                          ->where('friend_id', $request->user()->id);
                })
                ->delete();

        return response()->json([
            'success' => true,
            'message' => 'Friend removed'
        ]);
    }
}







