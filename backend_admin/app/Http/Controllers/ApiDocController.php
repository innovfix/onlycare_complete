<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;

class ApiDocController extends Controller
{
    /**
     * Show API documentation overview page
     */
    public function index()
    {
        return view('api-docs.index');
    }

    /**
     * Show Authentication APIs documentation
     */
    public function auth()
    {
        return view('api-docs.auth');
    }

    /**
     * Show Users/Creators APIs documentation
     */
    public function creators()
    {
        return view('api-docs.creators');
    }

    /**
     * Show Wallet & Payments APIs documentation
     */
    public function wallet()
    {
        return view('api-docs.wallet');
    }

    /**
     * Show Call APIs documentation
     */
    public function calls()
    {
        return view('api-docs.calls');
    }

    /**
     * Show Referral & Rewards APIs documentation
     */
    public function referrals()
    {
        return view('api-docs.referrals');
    }

    /**
     * Show Content & Policies APIs documentation
     */
    public function content()
    {
        return view('api-docs.content');
    }
}

