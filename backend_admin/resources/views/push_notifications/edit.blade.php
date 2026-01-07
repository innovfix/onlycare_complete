@extends('layouts.app')

@section('title', 'Edit Push Notification')

@section('content')
<div class="space-y-6">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Edit Push Notification</h1>

    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <form method="POST" action="{{ route('push_notifications.update', $notification->id) }}">
            @csrf
            @method('PUT')

            <div class="space-y-6">
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Title</label>
                    <input type="text" name="title" required maxlength="5000" value="{{ old('title', $notification->title) }}"
                           class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                    @error('title') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                </div>

                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Description</label>
                    <textarea name="description" required maxlength="5000" rows="5"
                              class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">{{ old('description', $notification->description) }}</textarea>
                    @error('description') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                </div>

                <div class="flex gap-4">
                    <button type="submit" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
                        Update Notification
                    </button>
                    <a href="{{ route('push_notifications.index') }}" class="font-medium rounded-lg px-6 py-2 bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-white">
                        Cancel
                    </a>
                </div>
            </div>
        </form>
    </div>
</div>
@endsection

