@extends('layouts.app')

@section('title', 'Edit ' . $title)

@section('content')
<div class="max-w-6xl mx-auto">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <a href="{{ route('policies.index') }}" class="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 text-sm mb-2 inline-block">
                    &larr; Back to Policies
                </a>
                <h2 class="text-2xl font-bold text-gray-900 dark:text-white">Edit {{ $title }}</h2>
                <p class="text-gray-500 dark:text-gray-400 text-sm mt-1">Last updated: {{ $updated_at }}</p>
            </div>
        </div>

        @if(session('success'))
        <div class="bg-green-100 dark:bg-green-900 border border-green-400 dark:border-green-700 text-green-700 dark:text-green-200 px-4 py-3 rounded mb-6">
            {{ session('success') }}
        </div>
        @endif

        @if(session('error'))
        <div class="bg-red-100 dark:bg-red-900 border border-red-400 dark:border-red-700 text-red-700 dark:text-red-200 px-4 py-3 rounded mb-6">
            {{ session('error') }}
        </div>
        @endif

        <form method="POST" action="{{ route('policies.update', $key) }}">
            @csrf
            @method('PUT')

            <!-- Editor Toolbar Info -->
            <div class="mb-4 p-3 bg-blue-50 dark:bg-blue-900/30 rounded-lg text-sm text-blue-700 dark:text-blue-300">
                <strong>HTML Editor:</strong> Use HTML tags for formatting. Common tags:
                <code class="mx-1">&lt;h2&gt;</code> for headings,
                <code class="mx-1">&lt;p&gt;</code> for paragraphs,
                <code class="mx-1">&lt;ul&gt;&lt;li&gt;</code> for lists,
                <code class="mx-1">&lt;strong&gt;</code> for bold.
            </div>

            <!-- Content Editor -->
            <div class="mb-6">
                <label for="content" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Content (HTML)
                </label>
                <textarea name="content" id="content" rows="25"
                          class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-3 font-mono text-sm"
                          placeholder="Enter policy content in HTML format...">{{ old('content', $content) }}</textarea>
                @error('content')
                    <p class="mt-1 text-sm text-red-500">{{ $message }}</p>
                @enderror
            </div>

            <!-- Preview Section -->
            <div class="mb-6">
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Preview
                </label>
                <div id="preview" class="w-full bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg p-6 prose dark:prose-invert max-w-none min-h-[200px]">
                    {!! $content !!}
                </div>
            </div>

            <!-- Actions -->
            <div class="flex justify-between items-center pt-6 border-t border-gray-200 dark:border-gray-700">
                <a href="{{ route('policies.index') }}"
                   class="border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300 font-medium rounded-lg px-6 py-2">
                    Cancel
                </a>
                <button type="submit"
                        class="bg-black hover:bg-gray-800 text-white font-medium rounded-lg px-6 py-2">
                    Save Changes
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    // Live preview
    document.getElementById('content').addEventListener('input', function() {
        document.getElementById('preview').innerHTML = this.value;
    });
</script>

<style>
    .prose h2 { font-size: 1.5rem; font-weight: 600; margin-top: 1.5rem; margin-bottom: 0.75rem; }
    .prose h3 { font-size: 1.25rem; font-weight: 600; margin-top: 1.25rem; margin-bottom: 0.5rem; }
    .prose p { margin-bottom: 1rem; }
    .prose ul { list-style-type: disc; margin-left: 1.5rem; margin-bottom: 1rem; }
    .prose li { margin-bottom: 0.25rem; }
</style>
@endsection
