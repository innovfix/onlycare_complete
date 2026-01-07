@extends('layouts.app')

@section('title', 'Edit Avatar')

@section('content')
<div class="max-w-3xl mx-auto">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">Edit Avatar</h2>
        
        <form method="POST" action="{{ route('avatars.update', $avatar->id) }}" enctype="multipart/form-data" class="space-y-6">
            @csrf
            @method('PUT')
            
            <div>
                <label for="gender" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Gender *
                </label>
                <select name="gender" id="gender" required
                        class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="MALE" {{ $avatar->gender == 'MALE' ? 'selected' : '' }}>Male</option>
                    <option value="FEMALE" {{ $avatar->gender == 'FEMALE' ? 'selected' : '' }}>Female</option>
                </select>
            </div>
            
            <div>
                <label for="image" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Avatar Image
                </label>
                <input type="file" name="image" id="image" accept="image/jpeg,image/png,image/jpg,image/gif"
                       class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-black file:text-white hover:file:bg-black">
                <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">Upload a new image to replace the current one (JPEG, PNG, JPG, GIF - Max 5MB). Leave empty to keep current image.</p>
                
                @if($avatar->image_url)
                <div class="mt-3">
                    <p class="text-sm text-gray-500 dark:text-gray-400 mb-2">Current Image:</p>
                    <img src="{{ $avatar->image_url }}" alt="Current Avatar" id="currentImg" class="w-32 h-32 rounded-full object-cover border-2 border-gray-300 dark:border-gray-600">
                </div>
                @endif
                
                <div id="imagePreview" class="mt-3 hidden">
                    <p class="text-sm text-gray-500 dark:text-gray-400 mb-2">New Image Preview:</p>
                    <img id="previewImg" src="" alt="Preview" class="w-32 h-32 rounded-full object-cover border-2 border-blue-500">
                </div>
            </div>
            
            <div class="flex justify-end space-x-3 pt-6 border-t border-gray-200 dark:border-gray-700">
                <a href="{{ route('avatars.index') }}" 
                   class="bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 font-medium rounded-lg px-6 py-2 hover:bg-gray-300 dark:hover:bg-gray-600">
                    Cancel
                </a>
                <button type="submit" 
                        class="bg-black hover:bg-black text-white font-medium rounded-lg px-6 py-2">
                    Update Avatar
                </button>
            </div>
        </form>
    </div>
</div>

<script>
document.getElementById('image').addEventListener('change', function(e) {
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

