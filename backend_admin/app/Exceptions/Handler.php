<?php

namespace App\Exceptions;

use Illuminate\Foundation\Exceptions\Handler as ExceptionHandler;
use Throwable;

class Handler extends ExceptionHandler
{
    /**
     * The list of the inputs that are never flashed to the session on validation exceptions.
     *
     * @var array<int, string>
     */
    protected $dontFlash = [
        'current_password',
        'password',
        'password_confirmation',
    ];

    /**
     * Register the exception handling callbacks for the application.
     */
    public function register(): void
    {
        $this->reportable(function (Throwable $e) {
            //
        });
    }
    
    /**
     * Render an exception into an HTTP response.
     */
    public function render($request, Throwable $e)
    {
        // For API requests, always return JSON
        if ($request->is('api/*') || $request->expectsJson()) {
            if ($e instanceof \Illuminate\Auth\AuthenticationException) {
                return response()->json([
                    'success' => false,
                    'error' => [
                        'code' => 'UNAUTHENTICATED',
                        'message' => 'Authentication required. Please login.'
                    ]
                ], 401);
            }
        }
        
        return parent::render($request, $e);
    }
}







