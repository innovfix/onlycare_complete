@extends('layouts.app')

@section('title', 'Create Scheduled Notification')

@section('content')
<div class="space-y-6">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Create Scheduled Notification</h1>

    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <form method="POST" action="{{ route('screen_notifications.store') }}" enctype="multipart/form-data">
            @csrf

            <div class="space-y-6">
                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Title</label>
                    <input type="text" name="title" required maxlength="255" 
                           class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full"
                           placeholder="Notification title">
                    @error('title') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                </div>

                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Description</label>
                    <textarea name="description" required maxlength="5000" rows="5"
                              class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full"
                              placeholder="Notification message"></textarea>
                    @error('description') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Time</label>
                        <input type="time" name="time" required 
                               class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                        @error('time') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                    </div>

                    <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Day</label>
                        <select name="day" required class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                            <option value="all">All Days</option>
                            <option value="Monday">Monday</option>
                            <option value="Tuesday">Tuesday</option>
                            <option value="Wednesday">Wednesday</option>
                            <option value="Thursday">Thursday</option>
                            <option value="Friday">Friday</option>
                            <option value="Saturday">Saturday</option>
                            <option value="Sunday">Sunday</option>
                        </select>
                        @error('day') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                    </div>
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Gender</label>
                        <select name="gender" required class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                            <option value="all">All</option>
                            <option value="male">Male</option>
                            <option value="female">Female</option>
                        </select>
                        @error('gender') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                    </div>

                    <div>
                        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Language</label>
                        <select name="language" required class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                            <option value="all">All</option>
                            <option value="Hindi">Hindi</option>
                            <option value="English">English</option>
                            <option value="Tamil">Tamil</option>
                            <option value="Telugu">Telugu</option>
                            <option value="Malayalam">Malayalam</option>
                            <option value="Kannada">Kannada</option>
                            <option value="Bengali">Bengali</option>
                            <option value="Marathi">Marathi</option>
                        </select>
                        @error('language') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                    </div>
                </div>

                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Logo (Optional)</label>
                    <input type="file" name="logo" accept="image/*" 
                           class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                    @error('logo') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                </div>

                <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Image (Optional)</label>
                    <input type="file" name="image" accept="image/*" 
                           class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full">
                    @error('image') <p class="text-red-500 text-xs mt-1">{{ $message }}</p> @enderror
                </div>

                <div class="flex gap-4">
                    <button type="submit" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
                        Create Scheduled Notification
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

