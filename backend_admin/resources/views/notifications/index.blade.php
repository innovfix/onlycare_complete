@extends('layouts.app')

@section('title', 'Notifications')

@section('content')
<div class="space-y-6">
    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border">
            <h3 class="text-3xl font-bold text-white mb-1">{{ number_format($stats['total']) }}</h3>
            <p class="text-dark-text-secondary text-sm font-medium">Total</p>
        </div>
        <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border">
            <h3 class="text-3xl font-bold text-warning mb-1">{{ number_format($stats['unread']) }}</h3>
            <p class="text-dark-text-secondary text-sm font-medium">Unread</p>
        </div>
        <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border">
            <h3 class="text-3xl font-bold text-success mb-1">{{ number_format($stats['read']) }}</h3>
            <p class="text-dark-text-secondary text-sm font-medium">Read</p>
        </div>
        <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border">
            <form action="{{ route('notifications.mark-all-read') }}" method="POST" class="h-full flex items-center">
                @csrf
                <button type="submit" class="w-full px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors">
                    Mark All Read
                </button>
            </form>
        </div>
    </div>

    <!-- Notifications List -->
    <div class="bg-dark-surface rounded-2xl border border-dark-border overflow-hidden">
        <div class="px-6 py-4 border-b border-dark-border bg-black/20">
            <h3 class="text-lg font-semibold text-white">All Notifications</h3>
        </div>
        <div class="overflow-x-auto">
            <table class="w-full">
                <thead class="bg-black/20">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">User</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">Title</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">Message</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">Type</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">Status</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase">Time</th>
                        <th class="px-6 py-3 text-right text-xs font-medium text-dark-text-secondary uppercase">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-dark-border">
                    @forelse($notifications as $notification)
                    <tr class="hover:bg-white/5 transition-colors {{ !$notification->is_read ? 'bg-primary/5' : '' }}">
                        <td class="px-6 py-4">
                            <span class="text-sm text-white">{{ $notification->user->name ?? 'N/A' }}</span>
                        </td>
                        <td class="px-6 py-4">
                            <span class="text-sm font-medium text-white">{{ $notification->title }}</span>
                        </td>
                        <td class="px-6 py-4 text-sm text-dark-text-secondary">
                            {{ Str::limit($notification->message, 50) }}
                        </td>
                        <td class="px-6 py-4">
                            <span class="px-2 py-1 text-xs font-bold rounded-full bg-primary/10 text-primary">
                                {{ $notification->notification_type }}
                            </span>
                        </td>
                        <td class="px-6 py-4">
                            @if($notification->is_read)
                                <span class="px-2 py-1 text-xs font-bold rounded-full bg-success/10 text-success">Read</span>
                            @else
                                <span class="px-2 py-1 text-xs font-bold rounded-full bg-warning/10 text-warning">Unread</span>
                            @endif
                        </td>
                        <td class="px-6 py-4 text-sm text-dark-text-secondary">
                            {{ $notification->created_at->diffForHumans() }}
                        </td>
                        <td class="px-6 py-4 text-right">
                            <div class="flex items-center justify-end space-x-2">
                                @if(!$notification->is_read)
                                    <form action="{{ route('notifications.mark-read', $notification->id) }}" method="POST" class="inline">
                                        @csrf
                                        <button type="submit" class="text-primary hover:text-primary-dark text-sm">Mark Read</button>
                                    </form>
                                @endif
                                <a href="{{ route('notifications.show', $notification->id) }}" class="text-primary hover:text-primary-dark text-sm font-medium">View</a>
                            </div>
                        </td>
                    </tr>
                    @empty
                    <tr>
                        <td colspan="7" class="px-6 py-8 text-center text-dark-text-secondary">No notifications found</td>
                    </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        
        <div class="px-6 py-4 border-t border-dark-border">
            {{ $notifications->links() }}
        </div>
    </div>
</div>
@endsection





