@extends('layouts.app')

@section('title', 'Edit Gift')

@section('content')
<div class="max-w-3xl mx-auto">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">Edit Gift</h2>
        
        <form method="POST" action="{{ route('gifts.update', $gift->id) }}" enctype="multipart/form-data" class="space-y-6">
            @csrf
            @method('PUT')
            
            @if($errors->any())
            <div class="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded-lg">
                <ul class="list-disc list-inside">
                    @foreach($errors->all() as $error)
                    <li>{{ $error }}</li>
                    @endforeach
                </ul>
            </div>
            @endif
            
            <div>
                <label for="gift_icon" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Gift Icon
                </label>
                <input type="file" name="gift_icon" id="gift_icon" accept="image/jpeg,image/png,image/jpg,image/gif,image/avif"
                       class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-black file:text-white hover:file:bg-black">
                <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">Upload a new image to replace the current one (JPEG, PNG, JPG, GIF, AVIF - Max 2MB). Leave empty to keep current image.</p>
                
                @if($gift->gift_icon)
                <div class="mt-3">
                    <p class="text-sm text-gray-500 dark:text-gray-400 mb-2">Current Image:</p>
                    <img src="{{ asset('storage/' . $gift->gift_icon) }}" alt="Current Gift Icon" id="currentImg" class="w-32 h-32 object-cover rounded-lg border-2 border-gray-300 dark:border-gray-600">
                </div>
                @endif
                
                <div id="imagePreview" class="mt-3 hidden">
                    <p class="text-sm text-gray-500 dark:text-gray-400 mb-2">New Image Preview:</p>
                    <img id="previewImg" src="" alt="Preview" class="w-32 h-32 object-cover rounded-lg border-2 border-blue-500">
                </div>
            </div>
            
            <div>
                <label for="coins" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Coins Required *
                </label>
                <input type="number" name="coins" id="coins" value="{{ old('coins', $gift->coins) }}" min="1" step="1" required
                       class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">Number of coins required to send this gift</p>
            </div>
            
            <div class="flex justify-end space-x-3 pt-6 border-t border-gray-200 dark:border-gray-700">
                <a href="{{ route('gifts.index') }}" 
                   class="bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 font-medium rounded-lg px-6 py-2 hover:bg-gray-300 dark:hover:bg-gray-600">
                    Cancel
                </a>
                <button type="submit" 
                        class="bg-black hover:bg-black text-white font-medium rounded-lg px-6 py-2">
                    Update Gift
                </button>
            </div>
        </form>
    </div>
</div>

<script>
document.getElementById('gift_icon').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('previewImg').src = e.target.result;
            document.getElementById('imagePreview').classList.remove('hidden');
            // Hide current image when new one is selected
            const currentImg = document.getElementById('currentImg');
            if (currentImg) {
                currentImg.style.opacity = '0.5';
            }
        }
        reader.readAsDataURL(file);
    } else {
        document.getElementById('imagePreview').classList.add('hidden');
        const currentImg = document.getElementById('currentImg');
        if (currentImg) {
            currentImg.style.opacity = '1';
        }
    }
});
</script>
@endsection








