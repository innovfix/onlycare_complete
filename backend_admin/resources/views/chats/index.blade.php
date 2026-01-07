@extends('layouts.app')

@section('title', 'Chat Management')

@section('content')
<div class="space-y-6">
    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border">
            <h3 class="text-3xl font-bold text-white mb-1">{{ number_format($stats['total_messages']) }}</h3>
            <p class="text-dark-text-secondary text-sm font-medium">Total Messages</p>
        </div>
        <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border">
            <h3 class="text-3xl font-bold text-white mb-1">{{ number_format($stats['total_conversations']) }}</h3>
            <p class="text-dark-text-secondary text-sm font-medium">Conversations</p>
        </div>
        <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border">
            <h3 class="text-3xl font-bold text-white mb-1">{{ number_format($stats['unread_messages']) }}</h3>
            <p class="text-dark-text-secondary text-sm font-medium">Unread Messages</p>
        </div>
    </div>

    <!-- Conversations List -->
    <div class="bg-dark-surface rounded-2xl border border-dark-border overflow-hidden">
        <div class="px-6 py-4 border-b border-dark-border bg-black/20">
            <h3 class="text-lg font-semibold text-white">Conversations</h3>
        </div>
        <div class="overflow-x-auto">
            <table class="w-full">
                <thead class="bg-black/20">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">Users</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">Last Message</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">Time</th>
                        <th class="px-6 py-3 text-right text-xs font-medium text-dark-text-secondary uppercase">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-dark-border">
                    @forelse($conversationData as $conv)
                    <tr class="hover:bg-white/5 transition-colors">
                        <td class="px-6 py-4">
                            <div class="flex items-center space-x-2">
                                <span class="text-sm text-white">{{ $conv['sender']->name ?? 'N/A' }}</span>
                                <span class="text-dark-text-secondary">↔</span>
                                <span class="text-sm text-white">{{ $conv['receiver']->name ?? 'N/A' }}</span>
                                @if($conv['unread_count'] > 0)
                                    <span class="ml-2 px-2 py-1 text-xs font-bold rounded-full bg-primary/10 text-primary">{{ $conv['unread_count'] }}</span>
                                @endif
                            </div>
                        </td>
                        <td class="px-6 py-4 text-sm text-dark-text-secondary">
                            {{ Str::limit($conv['last_message']->content ?? 'No messages', 50) }}
                        </td>
                        <td class="px-6 py-4 text-sm text-dark-text-secondary">
                            {{ $conv['last_message']->created_at->diffForHumans() ?? 'N/A' }}
                        </td>
                        <td class="px-6 py-4 text-right">
                            <a href="{{ route('chats.show', $conv['sender']->id ?? $conv['receiver']->id) }}" class="text-primary hover:text-primary-dark text-sm font-medium">
                                View
                            </a>
                        </td>
                    </tr>
                    @empty
                    <tr>
                        <td colspan="4" class="px-6 py-8 text-center text-dark-text-secondary">No conversations found</td>
                    </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        
        @if(isset($conversations) && $conversations->hasPages())
        <div class="px-6 py-4 border-t border-dark-border">
            {{ $conversations->links() }}
        </div>
        @endif
    </div>
</div>
@endsection





