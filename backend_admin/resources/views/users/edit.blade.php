@extends('layouts.app')

@section('title', 'Edit User')

@section('content')
<div class="max-w-3xl mx-auto">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">Edit User</h2>
        
        <form method="POST" action="{{ route('users.update', $user->id) }}" class="space-y-6">
            @csrf
            @method('PUT')
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label for="name" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Name *
                    </label>
                    <input type="text" name="name" id="name" required value="{{ $user->name }}"
                           class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                </div>
                
                <div>
                    <label for="phone" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Phone *
                    </label>
                    <input type="text" name="phone" id="phone" required value="{{ $user->phone }}"
                           class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                </div>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label for="age" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Age
                    </label>
                    <input type="number" name="age" id="age" value="{{ $user->age }}"
                           class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                </div>
                
                <div>
                    <label for="gender" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Gender *
                    </label>
                    <select name="gender" id="gender" required
                            class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                        <option value="MALE" {{ $user->gender == 'MALE' ? 'selected' : '' }}>Male</option>
                        <option value="FEMALE" {{ $user->gender == 'FEMALE' ? 'selected' : '' }}>Female</option>
                        <option value="OTHER" {{ $user->gender == 'OTHER' ? 'selected' : '' }}>Other</option>
                    </select>
                </div>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label for="language" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Language
                    </label>
                    <input type="text" name="language" id="language" value="{{ $user->language }}"
                           class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                </div>
                
                <div>
                    <label for="coin_balance" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Coin Balance
                    </label>
                    <input type="number" name="coin_balance" id="coin_balance" value="{{ $user->coin_balance }}"
                           class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                </div>
            </div>
            
            <div>
                <label for="bio" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Bio
                </label>
                <textarea name="bio" id="bio" rows="3"
                          class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">{{ $user->bio }}</textarea>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label for="online_status" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Online Status
                    </label>
                    @php
                        // DB uses booleans is_online/is_busy; the form uses derived values.
                        $derivedOnlineStatus = $user->is_busy ? 'BUSY' : ($user->is_online ? 'ONLINE' : 'OFFLINE');
                    @endphp
                    <select name="online_status" id="online_status"
                            class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                        <option value="ONLINE" {{ $derivedOnlineStatus == 'ONLINE' ? 'selected' : '' }}>Online</option>
                        <option value="OFFLINE" {{ $derivedOnlineStatus == 'OFFLINE' ? 'selected' : '' }}>Offline</option>
                        <option value="BUSY" {{ $derivedOnlineStatus == 'BUSY' ? 'selected' : '' }}>Busy</option>
                    </select>
                </div>
                
                <div>
                    <label for="call_availability" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Call Availability
                    </label>
                    @php
                        // DB uses audio_call_enabled/video_call_enabled + is_busy; derive a single select value.
                        $derivedCallAvailability = $user->is_busy
                            ? 'IN_CALL'
                            : (($user->audio_call_enabled || $user->video_call_enabled) ? 'AVAILABLE' : 'UNAVAILABLE');
                    @endphp
                    <select name="call_availability" id="call_availability"
                            class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                        <option value="AVAILABLE" {{ $derivedCallAvailability == 'AVAILABLE' ? 'selected' : '' }}>Available</option>
                        <option value="UNAVAILABLE" {{ $derivedCallAvailability == 'UNAVAILABLE' ? 'selected' : '' }}>Unavailable</option>
                        <option value="IN_CALL" {{ $derivedCallAvailability == 'IN_CALL' ? 'selected' : '' }}>In Call</option>
                    </select>
                </div>
            </div>
            
            <div class="space-y-3">
                <div class="flex items-center">
                    <input type="checkbox" name="is_verified" id="is_verified" value="1" {{ $user->is_verified ? 'checked' : '' }}
                           class="w-4 h-4 text-black bg-gray-100 border-gray-300 rounded focus:ring-blue-500">
                    <label for="is_verified" class="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                        Verified Account
                    </label>
                </div>
                
                <div class="flex items-center">
                    <input type="checkbox" name="is_blocked" id="is_blocked" value="1" {{ $user->is_blocked ? 'checked' : '' }}
                           class="w-4 h-4 text-black bg-gray-100 border-gray-300 rounded focus:ring-blue-500">
                    <label for="is_blocked" class="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                        Block User
                    </label>
                </div>
            </div>

            <!-- Bank Account Details Section -->
            <div class="pt-6 border-t border-gray-200 dark:border-gray-700">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Bank Account Details</h3>
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <label for="account_holder_name" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Account Holder Name
                        </label>
                        <input type="text" name="account_holder_name" id="account_holder_name" 
                               value="{{ $primaryBankAccount?->account_holder_name ?? '' }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                    
                    <div>
                        <label for="account_number" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Account Number
                        </label>
                        <input type="text" name="account_number" id="account_number" 
                               value="{{ $primaryBankAccount?->account_number ?? '' }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                </div>
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                    <div>
                        <label for="ifsc_code" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            IFSC Code
                        </label>
                        <input type="text" name="ifsc_code" id="ifsc_code" 
                               value="{{ $primaryBankAccount?->ifsc_code ?? '' }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                    
                    <div>
                        <label for="bank_name" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Bank Name
                        </label>
                        <input type="text" name="bank_name" id="bank_name" 
                               value="{{ $primaryBankAccount?->bank_name ?? '' }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                </div>
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                    <div>
                        <label for="branch_name" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Branch Name
                        </label>
                        <input type="text" name="branch_name" id="branch_name" 
                               value="{{ $primaryBankAccount?->branch_name ?? '' }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                    
                    <div>
                        <label for="upi_id" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            UPI ID
                        </label>
                        <input type="text" name="upi_id" id="upi_id" 
                               value="{{ $primaryBankAccount?->upi_id ?? '' }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                </div>
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                    <div>
                        <label for="pancard_name" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            PAN Card Name
                        </label>
                        <input type="text" name="pancard_name" id="pancard_name" 
                               value="{{ $primaryBankAccount?->pancard_name ?? '' }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                    
                    <div>
                        <label for="pancard_number" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            PAN Card Number
                        </label>
                        <input type="text" name="pancard_number" id="pancard_number" 
                               value="{{ $primaryBankAccount?->pancard_number ?? '' }}"
                               pattern="[A-Z]{5}[0-9]{4}[A-Z]{1}"
                               placeholder="ABCDE1234F"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                        <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">Format: ABCDE1234F</p>
                    </div>
                </div>
            </div>
            
            <div class="flex justify-end space-x-3 pt-6 border-t border-gray-200 dark:border-gray-700">
                <a href="{{ route('users.show', $user->id) }}" 
                   class="bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 font-medium rounded-lg px-6 py-2 hover:bg-gray-300 dark:hover:bg-gray-600">
                    Cancel
                </a>
                <button type="submit" 
                        class="bg-black hover:bg-black text-white font-medium rounded-lg px-6 py-2">
                    Update User
                </button>
            </div>
        </form>
    </div>
</div>
@endsection







