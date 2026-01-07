@extends('layouts.app')

@section('title', 'Edit Scheduled Notification')

@section('content')
<div class="space-y-6">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Edit Scheduled Notification</h1>

    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <form method="POST" action="{{ route('screen_notifications.update', $notification->id) }}" enctype="multipart/form-data">
            @csrf
            @method('PUT')

            <div class="space-y-6">
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Title</label>
                    <input type="text" name="title" required maxlength="255" value="{{ old('title', $notification->title) }}"
                           class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                    @error('title') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                </div>

                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Description</label>
                    <textarea name="description" required maxlength="5000" rows="5"
                              class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">{{ old('description', $notification->description) }}</textarea>
                    @error('description') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Time</label>
                        <input type="time" name="time" required value="{{ old('time', date('H:i', strtotime($notification->time))) }}"
                               class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                        @error('time') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                    </div>

                    <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Day</label>
                        <select name="day" required class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                            <option value="all" {{ $notification->day == 'all' ? 'selected' : '' }}>All Days</option>
                            <option value="Monday" {{ $notification->day == 'Monday' ? 'selected' : '' }}>Monday</option>
                            <option value="Tuesday" {{ $notification->day == 'Tuesday' ? 'selected' : '' }}>Tuesday</option>
                            <option value="Wednesday" {{ $notification->day == 'Wednesday' ? 'selected' : '' }}>Wednesday</option>
                            <option value="Thursday" {{ $notification->day == 'Thursday' ? 'selected' : '' }}>Thursday</option>
                            <option value="Friday" {{ $notification->day == 'Friday' ? 'selected' : '' }}>Friday</option>
                            <option value="Saturday" {{ $notification->day == 'Saturday' ? 'selected' : '' }}>Saturday</option>
                            <option value="Sunday" {{ $notification->day == 'Sunday' ? 'selected' : '' }}>Sunday</option>
                        </select>
                        @error('day') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                    </div>
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Gender</label>
                        <select name="gender" required class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                            <option value="all" {{ $notification->gender == 'all' ? 'selected' : '' }}>All</option>
                            <option value="male" {{ $notification->gender == 'male' ? 'selected' : '' }}>Male</option>
                            <option value="female" {{ $notification->gender == 'female' ? 'selected' : '' }}>Female</option>
                        </select>
                        @error('gender') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                    </div>

                    <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Language</label>
                        <select name="language" required class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                            <option value="all" {{ $notification->language == 'all' ? 'selected' : '' }}>All</option>
                            <option value="Hindi" {{ $notification->language == 'Hindi' ? 'selected' : '' }}>Hindi</option>
                            <option value="English" {{ $notification->language == 'English' ? 'selected' : '' }}>English</option>
                            <option value="Tamil" {{ $notification->language == 'Tamil' ? 'selected' : '' }}>Tamil</option>
                            <option value="Telugu" {{ $notification->language == 'Telugu' ? 'selected' : '' }}>Telugu</option>
                            <option value="Malayalam" {{ $notification->language == 'Malayalam' ? 'selected' : '' }}>Malayalam</option>
                            <option value="Kannada" {{ $notification->language == 'Kannada' ? 'selected' : '' }}>Kannada</option>
                            <option value="Bengali" {{ $notification->language == 'Bengali' ? 'selected' : '' }}>Bengali</option>
                            <option value="Marathi" {{ $notification->language == 'Marathi' ? 'selected' : '' }}>Marathi</option>
                        </select>
                        @error('language') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                    </div>
                </div>

                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Logo (Optional)</label>
                    @if($notification->logo)
                        <p class="text-sm text-gray-500 dark:text-gray-400 mb-2">Current: <a href="{{ asset('storage/' . $notification->logo) }}" target="_blank" class="text-blue-500">View</a></p>
                    @endif
                    <input type="file" name="logo" accept="image/*" 
                           class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                    @error('logo') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                </div>

                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Image (Optional)</label>
                    @if($notification->image)
                        <p class="text-sm text-gray-500 dark:text-gray-400 mb-2">Current: <a href="{{ asset('storage/' . $notification->image) }}" target="_blank" class="text-blue-500">View</a></p>
                    @endif
                    <input type="file" name="image" accept="image/*" 
                           class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                    @error('image') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                </div>

                <div class="flex gap-4">
                    <button type="submit" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
                        Update Scheduled Notification
                    </button>
                    <a href="{{ route('screen_notifications.index') }}" class="font-medium rounded-lg px-6 py-2 bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-white">
                        Cancel
                    </a>
                </div>
            </div>
        </form>
    </div>
</div>
@endsection

