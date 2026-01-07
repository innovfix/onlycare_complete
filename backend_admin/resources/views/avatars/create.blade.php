@extends('layouts.app')

@section('title', 'Create Avatar')

@section('content')
<div class="max-w-3xl mx-auto">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">Create New Avatar</h2>
        
        <form method="POST" action="{{ route('avatars.store') }}" enctype="multipart/form-data" class="space-y-6">
            @csrf
            
            <div>
                <label for="gender" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Gender *
                </label>
                <select name="gender" id="gender" required
                        class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">Select Gender</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                </select>
            </div>
            
            <div>
                <label for="image" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Avatar Image *
                </label>
                <input type="file" name="image" id="image" accept="image/jpeg,image/png,image/jpg,image/gif" required
                       class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-black file:text-white hover:file:bg-black">
                <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">Upload an image file (JPEG, PNG, JPG, GIF - Max 5MB)</p>
                <div id="imagePreview" class="mt-3 hidden">
                    <img id="previewImg" src="" alt="Preview" class="w-32 h-32 rounded-full object-cover border-2 border-gray-300 dark:border-gray-600">
                </div>
            </div>
            
            <div class="flex justify-end space-x-3 pt-6 border-t border-gray-200 dark:border-gray-700">
                <a href="{{ route('avatars.index') }}" 
                   class="bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 font-medium rounded-lg px-6 py-2 hover:bg-gray-300 dark:hover:bg-gray-600">
                    Cancel
                </a>
                <button type="submit" 
                        class="bg-black hover:bg-black text-white font-medium rounded-lg px-6 py-2">
                    Create Avatar
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
        }
        reader.readAsDataURL(file);
    } else {
        document.getElementById('imagePreview').classList.add('hidden');
    }
});
</script>
@endsection

