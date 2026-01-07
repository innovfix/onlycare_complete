<?php

namespace App\Http\Controllers;

use App\Models\AppSetting;
use Illuminate\Http\Request;

class SettingController extends Controller
{
    public function index()
    {
        $settingsCollection = AppSetting::all()->keyBy('setting_key');
        $settings = [];
        
        foreach ($settingsCollection as $key => $setting) {
            $settings[$key] = $setting->setting_value;
        }
        
        return view('settings.index', compact('settings'));
    }
    
    public function update(Request $request)
    {
        foreach ($request->except('_token') as $key => $value) {
            AppSetting::updateOrCreate(
                ['setting_key' => $key],
                ['setting_value' => $value]
            );
        }
        
        return back()->with('success', 'Settings updated successfully');
    }
}

