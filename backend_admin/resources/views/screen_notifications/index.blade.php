@extends('layouts.app')

@section('title', 'Scheduled Notifications')

@section('content')
<div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Scheduled Notifications</h1>
        <a href="{{ route('screen_notifications.create') }}" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
            Create Scheduled Notification
        </a>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <form method="GET" action="{{ route('screen_notifications.index') }}" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                <select name="day" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Days</option>
                    <option value="all" {{ request('day') == 'all' ? 'selected' : '' }}>All Days</option>
                    <option value="Monday" {{ request('day') == 'Monday' ? 'selected' : '' }}>Monday</option>
                    <option value="Tuesday" {{ request('day') == 'Tuesday' ? 'selected' : '' }}>Tuesday</option>
                    <option value="Wednesday" {{ request('day') == 'Wednesday' ? 'selected' : '' }}>Wednesday</option>
                    <option value="Thursday" {{ request('day') == 'Thursday' ? 'selected' : '' }}>Thursday</option>
                    <option value="Friday" {{ request('day') == 'Friday' ? 'selected' : '' }}>Friday</option>
                    <option value="Saturday" {{ request('day') == 'Saturday' ? 'selected' : '' }}>Saturday</option>
                    <option value="Sunday" {{ request('day') == 'Sunday' ? 'selected' : '' }}>Sunday</option>
                </select>
                
                <select name="gender" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Genders</option>
                    <option value="all" {{ request('gender') == 'all' ? 'selected' : '' }}>All</option>
                    <option value="male" {{ request('gender') == 'male' ? 'selected' : '' }}>Male</option>
                    <option value="female" {{ request('gender') == 'female' ? 'selected' : '' }}>Female</option>
                </select>

                <select name="language" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Languages</option>
                    <option value="all" {{ request('language') == 'all' ? 'selected' : '' }}>All</option>
                    <option value="Hindi" {{ request('language') == 'Hindi' ? 'selected' : '' }}>Hindi</option>
                    <option value="English" {{ request('language') == 'English' ? 'selected' : '' }}>English</option>
                    <option value="Tamil" {{ request('language') == 'Tamil' ? 'selected' : '' }}>Tamil</option>
                    <option value="Telugu" {{ request('language') == 'Telugu' ? 'selected' : '' }}>Telugu</option>
                    <option value="Malayalam" {{ request('language') == 'Malayalam' ? 'selected' : '' }}>Malayalam</option>
                    <option value="Kannada" {{ request('language') == 'Kannada' ? 'selected' : '' }}>Kannada</option>
                    <option value="Bengali" {{ request('language') == 'Bengali' ? 'selected' : '' }}>Bengali</option>
                    <option value="Marathi" {{ request('language') == 'Marathi' ? 'selected' : '' }}>Marathi</option>
                </select>

                <button type="submit" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
                    Filter
                </button>
            </div>
        </form>
    </div>

    <!-- Notifications Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm overflow-hidden">
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead class="bg-gray-50 dark:bg-gray-700">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">ID</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Title</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Day</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Time</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Gender</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Language</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    @forelse($notifications as $notification)
                    <tr class="hover:bg-gray-50 dark:hover:bg-gray-700">
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            {{ $notification->id }}
                        </td>
                        <td class="px-6 py-4 text-sm text-gray-900 dark:text-white">
                            {{ Str::limit($notification->title, 40) }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            {{ $notification->day }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            {{ date('H:i', strtotime($notification->time)) }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
                                {{ ucfirst($notification->gender) }}
                            </span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            {{ $notification->language }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                            <a href="{{ route('screen_notifications.edit', $notification->id) }}" class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 mr-3">
                                Edit
                            </a>
                            <form action="{{ route('screen_notifications.destroy', $notification->id) }}" method="POST" class="inline" onsubmit="return confirm('Are you sure?');">
                                @csrf
                                @method('DELETE')
                                <button type="submit" class="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300">
                                    Delete
                                </button>
                            </form>
                        </td>
                    </tr>
                    @empty
                    <tr>
                        <td colspan="7" class="px-6 py-4 text-center text-gray-500 dark:text-gray-400">
                            No scheduled notifications found.
                        </td>
                    </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        
        <!-- Pagination -->
        <div class="bg-white dark:bg-gray-800 px-4 py-3 border-t border-gray-200 dark:border-gray-700 sm:px-6">
            {{ $notifications->appends(request()->query())->links() }}
        </div>
    </div>
</div>
@endsection

