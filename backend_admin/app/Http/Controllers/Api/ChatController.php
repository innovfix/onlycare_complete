<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Message;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;

class ChatController extends Controller
{
    /**
     * Get conversations list
     */
    public function getConversations(Request $request)
    {
        $perPage = min($request->get('limit', 20), 50);
        
        // Get all users with whom current user has messages
        $conversations = Message::where(function($query) use ($request) {
                            $query->where('sender_id', $request->user()->id)
                                  ->orWhere('receiver_id', $request->user()->id);
                        })
                        ->select(DB::raw('
                            CASE 
                                WHEN sender_id = ' . $request->user()->id . ' THEN receiver_id
                                ELSE sender_id
                            END as other_user_id
                        '))
                        ->selectRaw('MAX(created_at) as last_message_time')
                        ->groupBy('other_user_id')
                        ->orderBy('last_message_time', 'desc')
                        ->paginate($perPage);

        $conversationData = $conversations->map(function($conv) use ($request) {
            $otherUser = User::find($conv->other_user_id);
            
            // Get last message
            $lastMessage = Message::where(function($query) use ($request, $otherUser) {
                                $query->where('sender_id', $request->user()->id)
                                      ->where('receiver_id', $otherUser->id);
                            })
                            ->orWhere(function($query) use ($request, $otherUser) {
                                $query->where('sender_id', $otherUser->id)
                                      ->where('receiver_id', $request->user()->id);
                            })
                            ->orderBy('created_at', 'desc')
                            ->first();

            // Count unread messages
            $unreadCount = Message::where('sender_id', $otherUser->id)
                                 ->where('receiver_id', $request->user()->id)
                                 ->where('is_read', false)
                                 ->count();

            return [
                'user' => [
                    'id' => 'USR_' . $otherUser->id,
                    'name' => $otherUser->name,
                    'profile_image' => $otherUser->profile_image,
                    'is_online' => $otherUser->is_online
                ],
                'last_message' => [
                    'content' => $lastMessage->message,
                    'created_at' => $lastMessage->created_at->toIso8601String(),
                    'is_from_me' => $lastMessage->sender_id === $request->user()->id
                ],
                'unread_count' => $unreadCount
            ];
        });

        return response()->json([
            'success' => true,
            'conversations' => $conversationData,
            'pagination' => [
                'current_page' => $conversations->currentPage(),
                'total_pages' => $conversations->lastPage(),
                'total_items' => $conversations->total(),
                'per_page' => $conversations->perPage()
            ]
        ]);
    }

    /**
     * Get messages with a specific user
     */
    public function getMessages(Request $request, $userId)
    {
        $id = str_replace('USR_', '', $userId);
        $otherUser = User::find($id);

        if (!$otherUser) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'User not found'
                ]
            ], 404);
        }

        $perPage = min($request->get('limit', 50), 100);
        
        $messages = Message::where(function($query) use ($request, $id) {
                        $query->where('sender_id', $request->user()->id)
                              ->where('receiver_id', $id);
                    })
                    ->orWhere(function($query) use ($request, $id) {
                        $query->where('sender_id', $id)
                              ->where('receiver_id', $request->user()->id);
                    })
                    ->orderBy('created_at', 'desc')
                    ->paginate($perPage);

        return response()->json([
            'success' => true,
            'messages' => $messages->map(function($message) {
                return [
                    'id' => 'MSG_' . $message->id,
                    'sender_id' => 'USR_' . $message->sender_id,
                    'receiver_id' => 'USR_' . $message->receiver_id,
                    'content' => $message->message,
                    'is_read' => $message->is_read,
                    'created_at' => $message->created_at->toIso8601String()
                ];
            }),
            'pagination' => [
                'current_page' => $messages->currentPage(),
                'total_pages' => $messages->lastPage(),
                'total_items' => $messages->total(),
                'per_page' => $messages->perPage()
            ]
        ]);
    }

    /**
     * Send message
     */
    public function sendMessage(Request $request, $userId)
    {
        $validator = Validator::make($request->all(), [
            'content' => 'required|string|max:1000'
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

        $receiverId = str_replace('USR_', '', $userId);
        $receiver = User::find($receiverId);

        if (!$receiver) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'User not found'
                ]
            ], 404);
        }

        $message = Message::create([
            'sender_id' => $request->user()->id,
            'receiver_id' => $receiverId,
            'message' => $request->content,
            'is_read' => false
        ]);

        // TODO: Send push notification to receiver via WebSocket or Firebase

        return response()->json([
            'success' => true,
            'message' => [
                'id' => 'MSG_' . $message->id,
                'sender_id' => 'USR_' . $message->sender_id,
                'receiver_id' => 'USR_' . $message->receiver_id,
                'content' => $message->message,
                'is_read' => $message->is_read,
                'created_at' => $message->created_at->toIso8601String()
            ]
        ]);
    }

    /**
     * Mark messages as read
     */
    public function markAsRead(Request $request, $userId)
    {
        $id = str_replace('USR_', '', $userId);

        Message::where('sender_id', $id)
               ->where('receiver_id', $request->user()->id)
               ->where('is_read', false)
               ->update(['is_read' => true]);

        return response()->json([
            'success' => true,
            'message' => 'Messages marked as read'
        ]);
    }
}







