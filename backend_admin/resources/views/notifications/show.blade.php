@extends('layouts.app')

@section('title', 'Notification Details')

@section('content')
<div class="space-y-6">
    <!-- Notification Details -->
    <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border">
        <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-semibold text-white">Notification Details</h3>
            <a href="{{ route('notifications.index') }}" class="text-primary hover:text-primary-dark text-sm font-medium">
                ← Back to Notifications
            </a>
        </div>
        
        <div class="space-y-4">
            <div>
                <label class="text-sm font-medium text-dark-text-secondary">User</label>
                <p class="text-white mt-1">{{ $notification->user->name ?? 'N/A' }}</p>
            </div>
            
            <div>
                <label class="text-sm font-medium text-dark-text-secondary">Title</label>
                <p class="text-white mt-1">{{ $notification->title }}</p>
            </div>
            
            <div>
                <label class="text-sm font-medium text-dark-text-secondary">Message</label>
                <p class="text-white mt-1">{{ $notification->message }}</p>
            </div>
            
            <div>
                <label class="text-sm font-medium text-dark-text-secondary">Type</label>
                <p class="mt-1">
                    <span class="px-2 py-1 text-xs font-bold rounded-full bg-primary/10 text-primary">
                        {{ $notification->notification_type }}
                    </span>
                </p>
            </div>
            
            <div>
                <label class="text-sm font-medium text-dark-text-secondary">Status</label>
                <p class="mt-1">
                    @if($notification->is_read)
                        <span class="px-2 py-1 text-xs font-bold rounded-full bg-success/10 text-success">Read</span>
                    @else
                        <span class="px-2 py-1 text-xs font-bold rounded-full bg-warning/10 text-warning">Unread</span>
                    @endif
                </p>
            </div>
            
            @if($notification->reference_id)
            <div>
                <label class="text-sm font-medium text-dark-text-secondary">Reference ID</label>
                <p class="text-white mt-1">{{ $notification->reference_id }}</p>
            </div>
            @endif
            
            <div>
                <label class="text-sm font-medium text-dark-text-secondary">Created At</label>
                <p class="text-white mt-1">{{ $notification->created_at->format('M d, Y h:i A') }}</p>
            </div>
        </div>
        
        <div class="mt-6 flex items-center space-x-4">
            @if(!$notification->is_read)
                <form action="{{ route('notifications.mark-read', $notification->id) }}" method="POST" class="inline">
                    @csrf
                    <button type="submit" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors">
                        Mark as Read
                    </button>
                </form>
            @endif
            
            <form action="{{ route('notifications.destroy', $notification->id) }}" method="POST" class="inline" onsubmit="return confirm('Are you sure you want to delete this notification?');">
                @csrf
                @method('DELETE')
                <button type="submit" class="px-4 py-2 bg-danger text-white rounded-lg hover:bg-danger-dark transition-colors">
                    Delete
                </button>
            </form>
        </div>
    </div>
</div>
@endsection





