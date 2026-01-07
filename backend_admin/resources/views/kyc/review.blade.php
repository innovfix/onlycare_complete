@extends('layouts.app')

@section('title', 'Review KYC - ' . $user->name)

@section('content')
<div class="max-w-4xl mx-auto">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <div class="flex items-center justify-between mb-6">
            <div>
                <h2 class="text-2xl font-bold text-gray-900 dark:text-white">KYC Verification</h2>
                <p class="text-gray-500 dark:text-gray-400 mt-1">{{ $user->name }}</p>
            </div>
            <a href="{{ route('kyc.index') }}" class="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">
                ← Back to KYC List
            </a>
        </div>

        <!-- User Information -->
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4 mb-6">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div>
                    <div class="text-sm text-gray-500 dark:text-gray-400">User ID</div>
                    <div class="text-sm font-medium text-gray-900 dark:text-white">{{ $user->id }}</div>
                </div>
                <div>
                    <div class="text-sm text-gray-500 dark:text-gray-400">Phone</div>
                    <div class="text-sm font-medium text-gray-900 dark:text-white">{{ $user->phone }}</div>
                </div>
                <div>
                    <div class="text-sm text-gray-500 dark:text-gray-400">KYC Status</div>
                    <div class="text-sm font-medium">
                        <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                            @if($user->kyc_status == 'VERIFIED') bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200
                            @elseif($user->kyc_status == 'PENDING') bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200
                            @else bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200
                            @endif">
                            {{ $user->kyc_status ?? 'PENDING' }}
                        </span>
                    </div>
                </div>
                <div>
                    <div class="text-sm text-gray-500 dark:text-gray-400">Submitted</div>
                    <div class="text-sm font-medium text-gray-900 dark:text-white">
                        {{ $user->kycDocuments->first()->submitted_at ? $user->kycDocuments->first()->submitted_at->format('M d, Y') : 'N/A' }}
                    </div>
                </div>
            </div>
        </div>

        <!-- KYC Documents -->
        <div class="space-y-6 mb-6">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Documents</h3>
            
            @forelse($user->kycDocuments as $document)
            <div class="border border-gray-200 dark:border-gray-700 rounded-lg p-4">
                <div class="flex items-center justify-between mb-3">
                    <div>
                        <h4 class="text-md font-medium text-gray-900 dark:text-white">{{ $document->document_type }}</h4>
                        @if($document->document_number)
                        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Number: {{ $document->document_number }}</p>
                        @endif
                    </div>
                    <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                        @if($document->status == 'APPROVED') bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200
                        @elseif($document->status == 'PENDING') bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200
                        @else bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200
                        @endif">
                        {{ $document->status }}
                    </span>
                </div>
                
                @if($document->document_url)
                <div class="mt-3">
                    <a href="{{ $document->document_url }}" target="_blank" 
                       class="inline-flex items-center text-black hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 text-sm">
                        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                        </svg>
                        View Document
                    </a>
                </div>
                @endif
                
                @if($document->rejected_reason)
                <div class="mt-3 p-3 bg-red-50 dark:bg-red-900/20 rounded">
                    <p class="text-sm text-red-800 dark:text-red-200">
                        <strong>Rejection Reason:</strong> {{ $document->rejected_reason }}
                    </p>
                </div>
                @endif
            </div>
            @empty
            <div class="text-center py-8 text-gray-500 dark:text-gray-400">
                No KYC documents found for this user.
            </div>
            @endforelse
        </div>

        <!-- Actions -->
        @if($user->kycDocuments->where('status', 'PENDING')->count() > 0)
        <div class="border-t border-gray-200 dark:border-gray-700 pt-6">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Verification Actions</h3>
            
            <div class="flex space-x-4">
                <form method="POST" action="{{ route('kyc.approve', $user->id) }}" class="flex-1">
                    @csrf
                    <button type="submit" 
                            class="w-full bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg px-6 py-3">
                        ✅ Approve KYC
                    </button>
                </form>
                
                <form method="POST" action="{{ route('kyc.reject', $user->id) }}" class="flex-1" id="rejectForm">
                    @csrf
                    <div class="mb-3">
                        <label for="rejected_reason" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                            Rejection Reason (Optional)
                        </label>
                        <textarea name="rejected_reason" id="rejected_reason" rows="3"
                                  class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2"
                                  placeholder="Specify reason for rejection..."></textarea>
                    </div>
                    <button type="submit" 
                            class="w-full bg-red-600 hover:bg-red-700 text-white font-medium rounded-lg px-6 py-3">
                        ❌ Reject KYC
                    </button>
                </form>
            </div>
        </div>
        @else
        <div class="border-t border-gray-200 dark:border-gray-700 pt-6">
            <p class="text-gray-500 dark:text-gray-400 text-center">All documents have been processed.</p>
        </div>
        @endif
    </div>
</div>
@endsection












