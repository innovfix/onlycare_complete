<!DOCTYPE html>
<html lang="en" class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Only Care Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    @vite(['resources/css/app.css', 'resources/js/app.js'])
    <style>
        body {
            background: #000000;
            font-family: 'Outfit', sans-serif;
            background-image: radial-gradient(circle at 50% 0%, #1a1a1a 0%, #000000 70%);
        }
        
        .premium-card {
            background: rgba(18, 18, 18, 0.6);
            backdrop-filter: blur(20px);
            border: 1px solid rgba(255, 255, 255, 0.05);
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
        }
        
        .premium-input {
            background: rgba(255, 255, 255, 0.03) !important;
            color: #ffffff !important;
            border: 1px solid rgba(255, 255, 255, 0.1) !important;
            transition: all 0.3s ease;
            border-radius: 1rem !important;
        }
        
        .premium-input:focus {
            background: rgba(255, 255, 255, 0.05) !important;
            border-color: #FF1744 !important;
            box-shadow: 0 0 0 4px rgba(255, 23, 68, 0.1) !important;
        }
        
        .premium-button {
            background: linear-gradient(45deg, #FF1744, #D50000) !important;
            color: #ffffff !important;
            border: none !important;
            font-weight: 700 !important;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(255, 23, 68, 0.3) !important;
        }
        
        .premium-button:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(255, 23, 68, 0.4) !important;
        }
        
        .premium-button:active {
            transform: translateY(0);
        }
        
        .logo-gradient {
            background: linear-gradient(to right, #FF1744, #2196F3);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
    </style>
</head>
<body class="min-h-screen flex items-center justify-center p-6">
    <div class="w-full max-w-md">
        <!-- Main Card -->
        <div class="premium-card rounded-2xl overflow-hidden">
            <!-- Header -->
            <div class="px-10 pt-12 pb-8 text-center">
                <h1 class="logo-gradient text-5xl font-bold mb-3 tracking-tight">OnlyCare</h1>
                <p class="text-gray-400 text-sm uppercase tracking-widest font-medium">Admin Dashboard</p>
            </div>

            <!-- Form -->
            <div class="px-10 py-10">
                <form action="{{ route('login.post') }}" method="POST" class="space-y-8">
                    @if ($errors->any())
                        <div class="bg-red-900 bg-opacity-20 text-red-400 px-5 py-4 rounded-2xl backdrop-blur-sm">
                            @foreach ($errors->all() as $error)
                                <p class="text-sm font-medium">{{ $error }}</p>
                            @endforeach
                        </div>
                    @endif
                    @csrf

                    <!-- Email -->
                    <div>
                        <label for="email" class="premium-label block text-sm text-gray-300 mb-6">
                            Email Address
                        </label>
                        <input 
                            type="email" 
                            id="email" 
                            name="email" 
                            value="{{ old('email') }}"
                            class="premium-input w-full px-5 py-4 text-black rounded-2xl focus:outline-none font-medium text-sm"
                            placeholder="admin@onlycare.app"
                            required
                            autofocus
                        >
                        @error('email')
                            <p class="text-red-400 text-xs mt-2 font-medium">{{ $message }}</p>
                        @enderror
                    </div>

                    <!-- Password -->
                    <div class="mt-8">
                        <label for="password" class="premium-label block text-sm text-gray-300 mb-6">
                            Password
                        </label>
                        <input 
                            type="password" 
                            id="password" 
                            name="password" 
                            class="premium-input w-full px-5 py-4 text-black rounded-2xl focus:outline-none font-medium text-sm"
                            placeholder="Enter your password"
                            required
                        >
                        @error('password')
                            <p class="text-red-400 text-xs mt-2 font-medium">{{ $message }}</p>
                        @enderror
                    </div>

                    <!-- Remember Me -->
                    <div class="flex items-center pt-1">
                        <input 
                            type="checkbox" 
                            id="remember" 
                            name="remember" 
                            class="w-4 h-4 rounded bg-gray-800 text-white focus:ring-0 focus:ring-offset-0 cursor-pointer border-0"
                        >
                        <label for="remember" class="ml-3 text-sm text-gray-400 cursor-pointer select-none">
                            Keep me signed in for 30 days
                        </label>
                    </div>

                    <!-- Submit Button -->
                    <div class="pt-4">
                        <button 
                            type="submit" 
                            class="premium-button w-full py-4 text-black font-bold rounded-full focus:outline-none uppercase tracking-wider text-sm"
                        >
                            Sign In to Dashboard
                        </button>
                    </div>
                </form>
            </div>

            <!-- Footer -->
            <div class="px-10 pb-10">
                <div class="pt-8">
                    <p class="text-gray-500 text-xs uppercase tracking-wider text-center mb-4 font-semibold">
                        Default Credentials
                    </p>
                    <div class="bg-black bg-opacity-40 backdrop-blur-sm rounded-xl px-6 py-4 space-y-2">
                        <div class="flex items-center justify-between">
                            <span class="text-gray-500 text-xs font-medium">Email:</span>
                            <span class="font-mono text-white text-sm">admin@onlycare.app</span>
                        </div>
                        <div class="flex items-center justify-between">
                            <span class="text-gray-500 text-xs font-medium">Password:</span>
                            <span class="font-mono text-white text-sm">admin123</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Copyright -->
        <div class="text-center mt-8">
            <p class="text-gray-600 text-xs">&copy; {{ date('Y') }} Only Care. All rights reserved.</p>
        </div>
    </div>
</body>
</html>
