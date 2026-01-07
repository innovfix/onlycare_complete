@extends('layouts.app')

@section('title', 'App Settings')

@section('content')
<div class="max-w-4xl mx-auto">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">App Settings</h2>
        
        @if(session('success'))
        <div class="bg-gray-100 dark:bg-black border border-green-400 dark:border-green-700 text-green-700 dark:text-white px-4 py-3 rounded mb-6">
            {{ session('success') }}
        </div>
        @endif
        
        <form method="POST" action="{{ route('settings.update') }}" class="space-y-8">
            @csrf
            
            <!-- Call Settings -->
            <div class="border-b border-gray-200 dark:border-gray-700 pb-8">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Call Settings</h3>
                
                <div class="space-y-4">
                    <div>
                        <label for="call_rate_per_minute" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Call Rate per Minute (Coins)
                        </label>
                        <input type="number" name="call_rate_per_minute" id="call_rate_per_minute" 
                               value="{{ $settings['call_rate_per_minute'] ?? 10 }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Number of coins deducted per minute of call</p>
                    </div>
                    
                    <div>
                        <label for="video_call_rate_per_minute" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Video Call Rate per Minute (Coins)
                        </label>
                        <input type="number" name="video_call_rate_per_minute" id="video_call_rate_per_minute" 
                               value="{{ $settings['video_call_rate_per_minute'] ?? 20 }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Number of coins deducted per minute of video call</p>
                    </div>
                </div>
            </div>
            
            <!-- Coin Settings -->
            <div class="border-b border-gray-200 dark:border-gray-700 pb-8">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Coin Economy</h3>
                
                <div class="space-y-4">
                    <div>
                        <label for="welcome_bonus" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Welcome Bonus (Coins)
                        </label>
                        <input type="number" name="welcome_bonus" id="welcome_bonus" 
                               value="{{ $settings['welcome_bonus'] ?? 100 }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Coins given to new users on signup</p>
                    </div>
                    
                    <div>
                        <label for="referral_bonus" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Referral Bonus (Coins)
                        </label>
                        <input type="number" name="referral_bonus" id="referral_bonus" 
                               value="{{ $settings['referral_bonus'] ?? 50 }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">Coins given to referrer when someone signs up using their code</p>
                    </div>
                    
                    <div>
                        <label for="coin_to_inr_rate" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Coins to INR Conversion Rate
                        </label>
                        <input type="number" step="0.01" name="coin_to_inr_rate" id="coin_to_inr_rate" 
                               value="{{ $settings['coin_to_inr_rate'] ?? 0.10 }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">1 Coin = ₹X (used for withdrawal calculations)</p>
                    </div>
                </div>
            </div>
            
            <!-- Withdrawal Settings -->
            <div class="border-b border-gray-200 dark:border-gray-700 pb-8">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Withdrawal Settings</h3>
                
                <div class="space-y-4">
                    <div>
                        <label for="min_withdrawal_amount" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Minimum Withdrawal Amount (₹)
                        </label>
                        <input type="number" step="0.01" name="min_withdrawal_amount" id="min_withdrawal_amount" 
                               value="{{ $settings['min_withdrawal_amount'] ?? 500 }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                    
                    <div>
                        <label for="max_withdrawal_amount" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Maximum Withdrawal Amount (₹)
                        </label>
                        <input type="number" step="0.01" name="max_withdrawal_amount" id="max_withdrawal_amount" 
                               value="{{ $settings['max_withdrawal_amount'] ?? 10000 }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                    
                    <div>
                        <label for="withdrawal_fee_percentage" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Withdrawal Fee (%)
                        </label>
                        <input type="number" step="0.01" name="withdrawal_fee_percentage" id="withdrawal_fee_percentage" 
                               value="{{ $settings['withdrawal_fee_percentage'] ?? 2.5 }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                </div>
            </div>
            
            <!-- General Settings -->
            <div class="pb-2">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">General Settings</h3>
                
                <div class="space-y-4">
                    <div>
                        <label for="app_name" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            App Name
                        </label>
                        <input type="text" name="app_name" id="app_name" 
                               value="{{ $settings['app_name'] ?? 'Only Care' }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                    
                    <div>
                        <label for="support_email" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Support Email
                        </label>
                        <input type="email" name="support_email" id="support_email" 
                               value="{{ $settings['support_email'] ?? 'support@onlycare.app' }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                    
                    <div>
                        <label for="support_phone" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Support Phone
                        </label>
                        <input type="text" name="support_phone" id="support_phone" 
                               value="{{ $settings['support_phone'] ?? '+91 1234567890' }}"
                               class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    </div>
                    
                    <div class="flex items-center">
                        <input type="checkbox" name="maintenance_mode" id="maintenance_mode" value="1" 
                               {{ ($settings['maintenance_mode'] ?? false) ? 'checked' : '' }}
                               class="w-4 h-4 text-black bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600">
                        <label for="maintenance_mode" class="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                            Enable Maintenance Mode
                        </label>
                    </div>
                </div>
            </div>
            
            <div class="flex justify-end pt-6 border-t border-gray-200 dark:border-gray-700">
                <button type="submit" 
                        class="bg-black hover:bg-black text-white font-medium rounded-lg px-6 py-2">
                    Save Settings
                </button>
            </div>
        </form>
    </div>
</div>
@endsection







