<?php

namespace App\Http\Controllers;

use App\Models\Message;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class ChatController extends Controller
{
    public function index(Request $request)
    {
        // Get unique conversations
        $conversations = Message::select('sender_id', 'receiver_id', DB::raw('MAX(created_at) as last_message_time'))
            ->groupBy('sender_id', 'receiver_id')
            ->orderBy('last_message_time', 'desc')
            ->paginate(50);
        
        // Get conversation details
        $conversationData = [];
        foreach ($conversations as $conv) {
            $lastMessage = Message::where(function($q) use ($conv) {
                $q->where('sender_id', $conv->sender_id)
                  ->where('receiver_id', $conv->receiver_id);
            })->orWhere(function($q) use ($conv) {
                $q->where('sender_id', $conv->receiver_id)
                  ->where('receiver_id', $conv->sender_id);
            })->latest()->first();
            
            $sender = User::find($conv->sender_id);
            $receiver = User::find($conv->receiver_id);
            
            $unreadCount = Message::where(function($q) use ($conv) {
                $q->where('sender_id', $conv->sender_id)
                  ->where('receiver_id', $conv->receiver_id);
            })->orWhere(function($q) use ($conv) {
                $q->where('sender_id', $conv->receiver_id)
                  ->where('receiver_id', $conv->sender_id);
            })->where('is_read', false)->count();
            
            $conversationData[] = [
                'id' => $conv->sender_id . '_' . $conv->receiver_id,
                'sender' => $sender,
                'receiver' => $receiver,
                'last_message' => $lastMessage,
                'unread_count' => $unreadCount
            ];
        }
        
        // Stats
        $stats = [
            'total_messages' => Message::count(),
            'total_conversations' => Message::select('sender_id', 'receiver_id')
                ->groupBy('sender_id', 'receiver_id')
                ->get()
                ->count(),
            'unread_messages' => Message::where('is_read', false)->count(),
        ];
        
        return view('chats.index', compact('conversationData', 'stats', 'conversations'));
    }
    
    public function show($userId)
    {
        $user = User::findOrFail($userId);
        
        // Get all messages for this user (both sent and received)
        $messages = Message::where('sender_id', $userId)
            ->orWhere('receiver_id', $userId)
            ->with(['sender', 'receiver'])
            ->latest()
            ->paginate(100);
        
        return view('chats.show', compact('user', 'messages'));
    }
    
    public function destroy($id)
    {
        $message = Message::findOrFail($id);
        $message->delete();
        
        return redirect()->back()
            ->with('success', 'Message deleted successfully');
    }
}





