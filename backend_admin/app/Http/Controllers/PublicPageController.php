<?php

namespace App\Http\Controllers;

use App\Models\AppSetting;
use Illuminate\Http\Request;

class PublicPageController extends Controller
{
    /**
     * Safe helper: fetch AppSetting value without crashing if DB/config is broken.
     */
    private function safeSettingValue(string $key): ?string
    {
        try {
            $row = AppSetting::where('setting_key', $key)->first();
            return $row ? $row->setting_value : null;
        } catch (\Throwable $e) {
            // If DB connection/migrations are not ready, fall back to defaults.
            return null;
        }
    }

    /**
     * Show Privacy Policy page
     */
    public function privacyPolicy()
    {
        $content = $this->safeSettingValue('privacy_policy') ?: $this->getDefaultPrivacyPolicy();
        $updatedAt = $this->safeSettingValue('privacy_policy_updated') ?: date('Y-m-d');

        return view('public.policy', [
            'title' => 'Privacy Policy',
            'content' => $content,
            'updated_at' => $updatedAt
        ]);
    }

    /**
     * Show Terms & Conditions page
     */
    public function termsConditions()
    {
        $content = $this->safeSettingValue('terms_conditions') ?: $this->getDefaultTermsConditions();
        $updatedAt = $this->safeSettingValue('terms_conditions_updated') ?: date('Y-m-d');

        return view('public.policy', [
            'title' => 'Terms & Conditions',
            'content' => $content,
            'updated_at' => $updatedAt
        ]);
    }

    /**
     * Show Refund Policy page
     */
    public function refundPolicy()
    {
        $content = $this->safeSettingValue('refund_policy') ?: $this->getDefaultRefundPolicy();
        $updatedAt = $this->safeSettingValue('refund_policy_updated') ?: date('Y-m-d');

        return view('public.policy', [
            'title' => 'Refund & Cancellation Policy',
            'content' => $content,
            'updated_at' => $updatedAt
        ]);
    }

    /**
     * Show Community Guidelines page
     */
    public function communityGuidelines()
    {
        $content = $this->safeSettingValue('community_guidelines') ?: $this->getDefaultCommunityGuidelines();
        $updatedAt = $this->safeSettingValue('community_guidelines_updated') ?: date('Y-m-d');

        return view('public.policy', [
            'title' => 'Community Guidelines',
            'content' => $content,
            'updated_at' => $updatedAt
        ]);
    }

    /**
     * Default Privacy Policy HTML
     */
    private function getDefaultPrivacyPolicy()
    {
        return '<h2>Introduction</h2>
<p>Welcome to Only Care ("we", "our", "us"). We are committed to protecting your personal information and your right to privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our mobile application and services.</p>

<h2>1. Information We Collect</h2>
<p>We collect information that you provide directly to us when you:</p>
<ul>
    <li>Register for an account</li>
    <li>Create your profile</li>
    <li>Make voice or video calls</li>
    <li>Send messages</li>
    <li>Make purchases</li>
    <li>Contact customer support</li>
</ul>

<h2>2. Personal Information</h2>
<p>The personal information we collect may include:</p>
<ul>
    <li>Phone number</li>
    <li>Name, age, and gender</li>
    <li>Profile photos and videos</li>
    <li>Bio and interests</li>
    <li>Location data</li>
    <li>Device information</li>
    <li>Payment information</li>
    <li>Call and message history</li>
</ul>

<h2>3. How We Use Your Information</h2>
<p>We use the information we collect to:</p>
<ul>
    <li>Provide, maintain, and improve our services</li>
    <li>Process transactions and send related information</li>
    <li>Send you technical notices and support messages</li>
    <li>Respond to your comments and questions</li>
    <li>Monitor and analyze trends, usage, and activities</li>
    <li>Detect, prevent, and address fraud and security issues</li>
</ul>

<h2>4. Contact Us</h2>
<p>If you have questions about this Privacy Policy, please contact us at:</p>
<ul>
    <li>Email: onlycareapp000@gmail.com</li>
    <li>Through the app\'s support section</li>
</ul>';
    }

    /**
     * Default Terms & Conditions HTML
     */
    private function getDefaultTermsConditions()
    {
        return '<h2>Introduction</h2>
<p>Welcome to Only Care. These Terms and Conditions ("Terms") govern your use of our mobile application and services. By accessing or using Only Care, you agree to be bound by these Terms.</p>

<h2>1. Acceptance of Terms</h2>
<p>By creating an account and using our services, you acknowledge that you have read, understood, and agree to be bound by these Terms and our Privacy Policy.</p>

<h2>2. Eligibility</h2>
<p>To use Only Care, you must:</p>
<ul>
    <li>Be at least 18 years of age</li>
    <li>Have the legal capacity to enter into a binding agreement</li>
    <li>Not be prohibited from using the service under applicable laws</li>
    <li>Provide accurate and complete registration information</li>
</ul>

<h2>3. User Conduct</h2>
<p>You agree not to:</p>
<ul>
    <li>Use the service for any illegal purpose</li>
    <li>Harass, abuse, or harm other users</li>
    <li>Post inappropriate, offensive, or explicit content</li>
    <li>Engage in fraudulent activities</li>
</ul>

<h2>4. Contact Information</h2>
<p>For questions about these Terms, contact us at:</p>
<ul>
    <li>Email: onlycareapp000@gmail.com</li>
</ul>';
    }

    /**
     * Default Refund Policy HTML
     */
    private function getDefaultRefundPolicy()
    {
        return '<h2>Introduction</h2>
<p>This Refund & Cancellation Policy outlines the conditions under which refunds may be issued for coin purchases and other transactions on Only Care.</p>

<h2>1. General Policy</h2>
<p>All coin purchases on Only Care are generally non-refundable. However, we may consider refunds in exceptional circumstances as outlined below.</p>

<h2>2. Refund Eligibility</h2>
<p>Refunds may be issued in the following cases:</p>
<ul>
    <li>Payment was charged but coins were not credited to your account</li>
    <li>You were charged multiple times for the same purchase</li>
    <li>Technical errors resulted in incorrect charges</li>
    <li>Unauthorized transactions on your account</li>
</ul>

<h2>3. Contact Information</h2>
<p>For refund requests, contact us at:</p>
<ul>
    <li>Email: onlycareapp000@gmail.com</li>
</ul>';
    }

    /**
     * Default Community Guidelines HTML
     */
    private function getDefaultCommunityGuidelines()
    {
        return '<h2>Introduction</h2>
<p>Only Care is committed to providing a safe, respectful, and inclusive environment for all users. These Community Guidelines outline the standards of behavior expected from all members of our community.</p>

<h2>1. Be Respectful</h2>
<p>Treat everyone with dignity and respect:</p>
<ul>
    <li>Be polite and courteous in all interactions</li>
    <li>Respect different opinions, cultures, and backgrounds</li>
    <li>Use appropriate language</li>
</ul>

<h2>2. No Harassment or Abuse</h2>
<p>Harassment of any kind is strictly prohibited:</p>
<ul>
    <li>No bullying, intimidation, or threats</li>
    <li>No sexual harassment or unwanted advances</li>
    <li>No stalking or persistent unwanted contact</li>
</ul>

<h2>3. Contact Information</h2>
<p>Report violations at:</p>
<ul>
    <li>Email: onlycareapp000@gmail.com</li>
</ul>';
    }
}
