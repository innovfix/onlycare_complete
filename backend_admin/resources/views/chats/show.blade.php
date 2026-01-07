@extends('layouts.app')

@section('title', 'Chat Details')

@section('content')
<div class="space-y-6">
    <!-- User Info -->
    <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border">
        <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
                <div class="w-12 h-12 rounded-full bg-gradient-to-br from-gray-700 to-gray-900 flex items-center justify-center text-white font-bold text-lg border border-dark-border">
                    {{ substr($user->name, 0, 1) }}
                </div>
                <div>
                    <h3 class="text-lg font-semibold text-white">{{ $user->name }}</h3>
                    <p class="text-sm text-dark-text-secondary">{{ $user->email ?? 'N/A' }}</p>
                </div>
            </div>
            <a href="{{ route('chats.index') }}" class="text-primary hover:text-primary-dark text-sm font-medium">
                ← Back to Conversations
            </a>
        </div>
    </div>

    <!-- Messages List -->
    <div class="bg-dark-surface rounded-2xl border border-dark-border overflow-hidden">
        <div class="px-6 py-4 border-b border-dark-border bg-black/20">
            <h3 class="text-lg font-semibold text-white">Messages</h3>
        </div>
        <div class="overflow-x-auto max-h-96 overflow-y-auto">
            <table class="w-full">
                <thead class="bg-black/20 sticky top-0">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">From</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">To</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">Message</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">Status</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">Time</th>
                        <th class="px-6 py-3 text-right text-xs font-medium text-dark-text-secondary uppercase">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-dark-border">
                    @forelse($messages as $message)
                    <tr class="hover:bg-white/5 transition-colors">
                        <td class="px-6 py-4">
                            <span class="text-sm text-white">{{ $message->sender->name ?? 'N/A' }}</span>
                        </td>
                        <td class="px-6 py-4">
                            <span class="text-sm text-white">{{ $message->receiver->name ?? 'N/A' }}</span>
                        </td>
                        <td class="px-6 py-4 text-sm text-dark-text-secondary">
                            {{ Str::limit($message->content, 100) }}
                        </td>
                        <td class="px-6 py-4">
                            @if($message->is_read)
                                <span class="px-2 py-1 text-xs font-bold rounded-full bg-success/10 text-success">Read</span>
                            @else
                                <span class="px-2 py-1 text-xs font-bold rounded-full bg-warning/10 text-warning">Unread</span>
                            @endif
                        </td>
                        <td class="px-6 py-4 text-sm text-dark-text-secondary">
                            {{ $message->created_at->diffForHumans() }}
                        </td>
                        <td class="px-6 py-4 text-right">
                            <form action="{{ route('chats.destroy', $message->id) }}" method="POST" class="inline" onsubmit="return confirm('Are you sure you want to delete this message?');">
                                @csrf
                                @method('DELETE')
                                <button type="submit" class="text-danger hover:text-danger-dark text-sm">Delete</button>
                            </form>
                        </td>
                    </tr>
                    @empty
                    <tr>
                        <td colspan="6" class="px-6 py-8 text-center text-dark-text-secondary">No messages found</td>
                    </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        
        <div class="px-6 py-4 border-t border-dark-border">
            {{ $messages->links() }}
        </div>
    </div>
</div>
@endsection





