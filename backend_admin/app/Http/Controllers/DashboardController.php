<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Models\Call;
use App\Models\Transaction;
use App\Models\Withdrawal;
use App\Models\KycDocument;
use App\Models\Report;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class DashboardController extends Controller
{
    public function index()
    {
        // User Statistics
        $totalUsers = User::count();
        $maleUsers = User::where('gender', 'MALE')->count();
        $femaleUsers = User::where('gender', 'FEMALE')->count();
        $activeToday = User::whereDate('updated_at', today())->count();
        $onlineNow = User::where('is_online', true)->count();
        
        // Call Statistics
        $totalCalls = Call::count();
        $callsToday = Call::whereDate('created_at', today())->count();
        $audioCalls = Call::where('call_type', 'AUDIO')->count();
        $videoCalls = Call::where('call_type', 'VIDEO')->count();
        $avgDuration = Call::where('status', 'ENDED')->avg('duration');
        
        // Revenue Statistics
        $totalRevenue = Transaction::where('type', 'PURCHASE')
            ->where('status', 'SUCCESS')
            ->sum('amount');
        $revenueToday = Transaction::where('type', 'PURCHASE')
            ->where('status', 'SUCCESS')
            ->whereDate('created_at', today())
            ->sum('amount');
        $revenueThisMonth = Transaction::where('type', 'PURCHASE')
            ->where('status', 'SUCCESS')
            ->whereMonth('created_at', now()->month)
            ->sum('amount');
        
        // Pending Actions
        $pendingWithdrawals = Withdrawal::where('status', 'PENDING')->count();
        $pendingKyc = KycDocument::where('status', 'PENDING')->count();
        $pendingReports = Report::where('status', 'PENDING')->count();
        
        // Recent Users
        $recentUsers = User::latest()->take(5)->get();
        
        // Recent Calls
        $recentCalls = Call::with(['caller', 'receiver'])
            ->latest()
            ->take(5)
            ->get();
        
        // Chart Data - User growth (last 7 days)
        $userGrowthData = User::select(
                DB::raw('DATE(created_at) as date'),
                DB::raw('count(*) as count')
            )
            ->where('created_at', '>=', now()->subDays(7))
            ->groupBy('date')
            ->orderBy('date')
            ->get();
        
        // Chart Data - Revenue (last 7 days)
        $revenueData = Transaction::select(
                DB::raw('DATE(created_at) as date'),
                DB::raw('sum(amount) as total')
            )
            ->where('type', 'PURCHASE')
            ->where('status', 'SUCCESS')
            ->where('created_at', '>=', now()->subDays(7))
            ->groupBy('date')
            ->orderBy('date')
            ->get();
        
        return view('dashboard.index', compact(
            'totalUsers', 'maleUsers', 'femaleUsers', 'activeToday', 'onlineNow',
            'totalCalls', 'callsToday', 'audioCalls', 'videoCalls', 'avgDuration',
            'totalRevenue', 'revenueToday', 'revenueThisMonth',
            'pendingWithdrawals', 'pendingKyc', 'pendingReports',
            'recentUsers', 'recentCalls', 'userGrowthData', 'revenueData'
        ));
    }
}

