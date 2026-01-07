<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\AppSetting;
use Illuminate\Http\Request;

class ContentController extends Controller
{
    /**
     * Get Privacy Policy
     */
    public function getPrivacyPolicy()
    {
        try {
            // Try to get from database first
            $dbContent = AppSetting::where('setting_key', 'privacy_policy')->first();
            $dbUpdated = AppSetting::where('setting_key', 'privacy_policy_updated')->first();

            if ($dbContent && !empty($dbContent->setting_value)) {
                $content = [
                    'title' => 'Privacy Policy',
                    'last_updated' => $dbUpdated ? $dbUpdated->setting_value : date('Y-m-d'),
                    'html_content' => $dbContent->setting_value
                ];
            } else {
                // Fallback to default content
                $content = [
                    'title' => 'Privacy Policy',
                    'last_updated' => '2025-11-04',
                    'content' => $this->getPrivacyPolicyContent()
                ];
            }

            return response()->json([
                'success' => true,
                'data' => $content
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to fetch privacy policy'
            ], 500);
        }
    }

    /**
     * Get Terms & Conditions
     */
    public function getTermsAndConditions()
    {
        try {
            // Try to get from database first
            $dbContent = AppSetting::where('setting_key', 'terms_conditions')->first();
            $dbUpdated = AppSetting::where('setting_key', 'terms_conditions_updated')->first();

            if ($dbContent && !empty($dbContent->setting_value)) {
                $content = [
                    'title' => 'Terms & Conditions',
                    'last_updated' => $dbUpdated ? $dbUpdated->setting_value : date('Y-m-d'),
                    'html_content' => $dbContent->setting_value
                ];
            } else {
                $content = [
                    'title' => 'Terms & Conditions',
                    'last_updated' => '2025-11-04',
                    'content' => $this->getTermsAndConditionsContent()
                ];
            }

            return response()->json([
                'success' => true,
                'data' => $content
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to fetch terms and conditions'
            ], 500);
        }
    }

    /**
     * Get Refund & Cancellation Policy
     */
    public function getRefundPolicy()
    {
        try {
            // Try to get from database first
            $dbContent = AppSetting::where('setting_key', 'refund_policy')->first();
            $dbUpdated = AppSetting::where('setting_key', 'refund_policy_updated')->first();

            if ($dbContent && !empty($dbContent->setting_value)) {
                $content = [
                    'title' => 'Refund & Cancellation Policy',
                    'last_updated' => $dbUpdated ? $dbUpdated->setting_value : date('Y-m-d'),
                    'html_content' => $dbContent->setting_value
                ];
            } else {
                $content = [
                    'title' => 'Refund & Cancellation Policy',
                    'last_updated' => '2025-11-04',
                    'content' => $this->getRefundPolicyContent()
                ];
            }

            return response()->json([
                'success' => true,
                'data' => $content
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to fetch refund policy'
            ], 500);
        }
    }

    /**
     * Get Community Guidelines & Moderation Policy
     */
    public function getCommunityGuidelines()
    {
        try {
            // Try to get from database first
            $dbContent = AppSetting::where('setting_key', 'community_guidelines')->first();
            $dbUpdated = AppSetting::where('setting_key', 'community_guidelines_updated')->first();

            if ($dbContent && !empty($dbContent->setting_value)) {
                $content = [
                    'title' => 'Community Guidelines & Moderation Policy',
                    'last_updated' => $dbUpdated ? $dbUpdated->setting_value : date('Y-m-d'),
                    'html_content' => $dbContent->setting_value
                ];
            } else {
                $content = [
                    'title' => 'Community Guidelines & Moderation Policy',
                    'last_updated' => '2025-11-04',
                    'content' => $this->getCommunityGuidelinesContent()
                ];
            }

            return response()->json([
                'success' => true,
                'data' => $content
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to fetch community guidelines'
            ], 500);
        }
    }

    /**
     * Privacy Policy Content
     */
    private function getPrivacyPolicyContent()
    {
        return [
            [
                'heading' => 'Introduction',
                'text' => 'Welcome to Only Care ("we", "our", "us"). We are committed to protecting your personal information and your right to privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our mobile application and services.'
            ],
            [
                'heading' => '1. Information We Collect',
                'text' => 'We collect information that you provide directly to us when you:',
                'points' => [
                    'Register for an account',
                    'Create your profile',
                    'Make voice or video calls',
                    'Send messages',
                    'Make purchases',
                    'Contact customer support'
                ]
            ],
            [
                'heading' => '2. Personal Information',
                'text' => 'The personal information we collect may include:',
                'points' => [
                    'Phone number',
                    'Name, age, and gender',
                    'Profile photos and videos',
                    'Bio and interests',
                    'Location data',
                    'Device information',
                    'Payment information',
                    'Call and message history'
                ]
            ],
            [
                'heading' => '3. How We Use Your Information',
                'text' => 'We use the information we collect to:',
                'points' => [
                    'Provide, maintain, and improve our services',
                    'Process transactions and send related information',
                    'Send you technical notices and support messages',
                    'Respond to your comments and questions',
                    'Monitor and analyze trends, usage, and activities',
                    'Detect, prevent, and address fraud and security issues',
                    'Personalize and improve your experience',
                    'Facilitate communication between users'
                ]
            ],
            [
                'heading' => '4. Information Sharing',
                'text' => 'We may share your information in the following situations:',
                'points' => [
                    'With other users (profile information, call history with them)',
                    'With service providers who perform services on our behalf',
                    'For legal purposes if required by law',
                    'To protect our rights and the safety of our users',
                    'With your consent or at your direction'
                ]
            ],
            [
                'heading' => '5. Data Security',
                'text' => 'We implement appropriate technical and organizational measures to protect your personal information. However, no method of transmission over the Internet is 100% secure, and we cannot guarantee absolute security.'
            ],
            [
                'heading' => '6. Data Retention',
                'text' => 'We retain your personal information for as long as necessary to provide our services and fulfill the purposes outlined in this Privacy Policy. You may request deletion of your account and data at any time.'
            ],
            [
                'heading' => '7. Your Rights',
                'text' => 'You have the right to:',
                'points' => [
                    'Access your personal information',
                    'Correct inaccurate information',
                    'Request deletion of your information',
                    'Object to processing of your information',
                    'Withdraw consent at any time',
                    'Lodge a complaint with authorities'
                ]
            ],
            [
                'heading' => '8. Children\'s Privacy',
                'text' => 'Our services are not intended for users under the age of 18. We do not knowingly collect information from children under 18. If you believe we have collected information from a child, please contact us immediately.'
            ],
            [
                'heading' => '9. Changes to Privacy Policy',
                'text' => 'We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the "Last Updated" date.'
            ],
            [
                'heading' => '10. Contact Us',
                'text' => 'If you have questions about this Privacy Policy, please contact us at:',
                'points' => [
                    'Email: onlycareapp000@gmail.com',
                    'Through the app\'s support section'
                ]
            ]
        ];
    }

    /**
     * Terms and Conditions Content
     */
    private function getTermsAndConditionsContent()
    {
        return [
            [
                'heading' => 'Introduction',
                'text' => 'Welcome to Only Care. These Terms and Conditions ("Terms") govern your use of our mobile application and services. By accessing or using Only Care, you agree to be bound by these Terms.'
            ],
            [
                'heading' => '1. Acceptance of Terms',
                'text' => 'By creating an account and using our services, you acknowledge that you have read, understood, and agree to be bound by these Terms and our Privacy Policy.'
            ],
            [
                'heading' => '2. Eligibility',
                'text' => 'To use Only Care, you must:',
                'points' => [
                    'Be at least 18 years of age',
                    'Have the legal capacity to enter into a binding agreement',
                    'Not be prohibited from using the service under applicable laws',
                    'Provide accurate and complete registration information'
                ]
            ],
            [
                'heading' => '3. Account Registration',
                'text' => 'When you register for an account, you agree to:',
                'points' => [
                    'Provide accurate, current, and complete information',
                    'Maintain the security of your account credentials',
                    'Notify us immediately of any unauthorized access',
                    'Be responsible for all activities under your account',
                    'Not create multiple accounts',
                    'Not impersonate any person or entity'
                ]
            ],
            [
                'heading' => '4. User Conduct',
                'text' => 'You agree not to:',
                'points' => [
                    'Use the service for any illegal purpose',
                    'Harass, abuse, or harm other users',
                    'Post inappropriate, offensive, or explicit content',
                    'Engage in fraudulent activities',
                    'Attempt to hack or disrupt the service',
                    'Collect user information without consent',
                    'Use automated systems (bots) to access the service',
                    'Violate any applicable laws or regulations'
                ]
            ],
            [
                'heading' => '5. Voice & Video Calls',
                'text' => 'Our platform facilitates voice and video calls between users:',
                'points' => [
                    'Calls are charged per minute based on coin packages',
                    'Coins are deducted automatically during calls',
                    'Call quality depends on your internet connection',
                    'We do not record or store call content',
                    'You must treat other users with respect',
                    'Inappropriate behavior may result in account suspension'
                ]
            ],
            [
                'heading' => '6. Payments & Coins',
                'text' => 'Regarding payments and virtual coins:',
                'points' => [
                    'Coins are virtual currency used within the app',
                    'Coins can be purchased through various payment methods',
                    'All purchases are non-refundable except as required by law',
                    'Coin prices are subject to change',
                    'Unused coins have no cash value',
                    'Earnings can be withdrawn as per withdrawal policy'
                ]
            ],
            [
                'heading' => '7. Content Ownership',
                'text' => 'You retain ownership of content you upload, but grant us a license to use it:',
                'points' => [
                    'You own your photos, videos, and other content',
                    'You grant us a worldwide license to use, display, and distribute your content',
                    'You represent that you have all rights to content you upload',
                    'We may remove content that violates our policies',
                    'You are responsible for backing up your content'
                ]
            ],
            [
                'heading' => '8. Earnings & Withdrawals (Female Creators)',
                'text' => 'For female creators earning through the platform:',
                'points' => [
                    'Earnings are based on call duration and rates',
                    'Minimum withdrawal amount applies',
                    'Valid KYC and bank account required for withdrawals',
                    'Withdrawals processed within 3-7 business days',
                    'We reserve the right to withhold earnings for violations'
                ]
            ],
            [
                'heading' => '9. Prohibited Content',
                'text' => 'The following content is strictly prohibited:',
                'points' => [
                    'Nudity or sexually explicit material',
                    'Violence or threats',
                    'Hate speech or discrimination',
                    'Illegal activities',
                    'Spam or misleading content',
                    'Copyrighted material without permission',
                    'Personal information of others'
                ]
            ],
            [
                'heading' => '10. Account Suspension & Termination',
                'text' => 'We reserve the right to:',
                'points' => [
                    'Suspend or terminate accounts that violate these Terms',
                    'Remove content that violates our policies',
                    'Refuse service to anyone at any time',
                    'Terminate accounts without notice for serious violations',
                    'Retain information as required by law after termination'
                ]
            ],
            [
                'heading' => '11. Disclaimer of Warranties',
                'text' => 'The service is provided "as is" without warranties of any kind. We do not guarantee uninterrupted, error-free, or secure service.'
            ],
            [
                'heading' => '12. Limitation of Liability',
                'text' => 'We are not liable for any indirect, incidental, special, or consequential damages arising from your use of the service.'
            ],
            [
                'heading' => '13. Indemnification',
                'text' => 'You agree to indemnify and hold us harmless from any claims, damages, or expenses arising from your use of the service or violation of these Terms.'
            ],
            [
                'heading' => '14. Changes to Terms',
                'text' => 'We may modify these Terms at any time. Continued use of the service after changes constitutes acceptance of the modified Terms.'
            ],
            [
                'heading' => '15. Contact Information',
                'text' => 'For questions about these Terms, contact us at:',
                'points' => [
                    'Email: onlycareapp000@gmail.com',
                    'Through the app\'s support section'
                ]
            ]
        ];
    }

    /**
     * Refund Policy Content
     */
    private function getRefundPolicyContent()
    {
        return [
            [
                'heading' => 'Introduction',
                'text' => 'This Refund & Cancellation Policy outlines the conditions under which refunds may be issued for coin purchases and other transactions on Only Care.'
            ],
            [
                'heading' => '1. General Policy',
                'text' => 'All coin purchases on Only Care are generally non-refundable. However, we may consider refunds in exceptional circumstances as outlined below.'
            ],
            [
                'heading' => '2. Refund Eligibility',
                'text' => 'Refunds may be issued in the following cases:',
                'points' => [
                    'Payment was charged but coins were not credited to your account',
                    'You were charged multiple times for the same purchase',
                    'Technical errors resulted in incorrect charges',
                    'Unauthorized transactions on your account',
                    'Service disruption prevented use of purchased coins'
                ]
            ],
            [
                'heading' => '3. Non-Refundable Situations',
                'text' => 'Refunds will NOT be issued in the following cases:',
                'points' => [
                    'Change of mind after purchase',
                    'Coins have been used for calls or services',
                    'Account suspension due to policy violations',
                    'Poor call quality due to user\'s internet connection',
                    'User dissatisfaction with other users',
                    'Failure to read terms before purchase'
                ]
            ],
            [
                'heading' => '4. Refund Request Process',
                'text' => 'To request a refund:',
                'points' => [
                    'Contact us within 48 hours of the transaction',
                    'Provide transaction ID and proof of payment',
                    'Explain the reason for the refund request',
                    'Wait for our team to review (3-5 business days)',
                    'Approved refunds processed within 7-10 business days'
                ]
            ],
            [
                'heading' => '5. Cancellation Policy',
                'text' => 'Regarding cancellation of services:',
                'points' => [
                    'Ongoing calls can be ended by either party at any time',
                    'Coins are deducted based on actual call duration',
                    'No cancellation fee applies',
                    'Unused coins remain in your account',
                    'Account closure does not entitle you to refund of unused coins'
                ]
            ],
            [
                'heading' => '6. Payment Processing',
                'text' => 'Important information about payments:',
                'points' => [
                    'All payments are processed through secure payment gateways',
                    'Payment processing fees are non-refundable',
                    'Refunds are issued to the original payment method',
                    'Currency exchange rates may affect refund amounts',
                    'We are not responsible for payment gateway delays'
                ]
            ],
            [
                'heading' => '7. Unauthorized Transactions',
                'text' => 'If you notice unauthorized transactions:',
                'points' => [
                    'Report immediately to onlycareapp000@gmail.com',
                    'Change your account password',
                    'Provide evidence of unauthorized access',
                    'We will investigate and take appropriate action',
                    'Verified unauthorized charges will be refunded'
                ]
            ],
            [
                'heading' => '8. Creator Earnings',
                'text' => 'For female creators:',
                'points' => [
                    'Earnings cannot be refunded once paid out',
                    'Pending withdrawals can be cancelled before processing',
                    'Earnings from completed calls are final',
                    'Disputed earnings will be investigated',
                    'Fraudulent earnings may be withheld or reversed'
                ]
            ],
            [
                'heading' => '9. Dispute Resolution',
                'text' => 'If you dispute a transaction:',
                'points' => [
                    'Contact us first before filing a chargeback',
                    'Provide detailed information about the dispute',
                    'Allow us 7 business days to investigate',
                    'Chargebacks may result in account suspension',
                    'We will work with you to resolve issues fairly'
                ]
            ],
            [
                'heading' => '10. Changes to Refund Policy',
                'text' => 'We reserve the right to modify this Refund Policy at any time. Changes will be effective immediately upon posting. Continued use of the service constitutes acceptance of the modified policy.'
            ],
            [
                'heading' => '11. Contact Information',
                'text' => 'For refund requests or questions, contact us:',
                'points' => [
                    'Email: onlycareapp000@gmail.com',
                    'Response time: Within 24-48 hours',
                    'Include: Transaction ID, Payment proof, Account details'
                ]
            ],
            [
                'heading' => '12. Legal Rights',
                'text' => 'This policy does not affect your statutory rights under applicable consumer protection laws in your jurisdiction.'
            ]
        ];
    }

    /**
     * Community Guidelines Content
     */
    private function getCommunityGuidelinesContent()
    {
        return [
            [
                'heading' => 'Introduction',
                'text' => 'Only Care is committed to providing a safe, respectful, and inclusive environment for all users. These Community Guidelines outline the standards of behavior expected from all members of our community.'
            ],
            [
                'heading' => '1. Be Respectful',
                'text' => 'Treat everyone with dignity and respect:',
                'points' => [
                    'Be polite and courteous in all interactions',
                    'Respect different opinions, cultures, and backgrounds',
                    'Use appropriate language',
                    'Listen actively and communicate clearly',
                    'Accept rejection gracefully',
                    'Report disrespectful behavior'
                ]
            ],
            [
                'heading' => '2. No Harassment or Abuse',
                'text' => 'Harassment of any kind is strictly prohibited:',
                'points' => [
                    'No bullying, intimidation, or threats',
                    'No sexual harassment or unwanted advances',
                    'No stalking or persistent unwanted contact',
                    'No doxxing or sharing personal information',
                    'No hate speech or discriminatory language',
                    'No revenge porn or non-consensual content sharing'
                ]
            ],
            [
                'heading' => '3. Appropriate Content',
                'text' => 'Keep all content appropriate and safe:',
                'points' => [
                    'No nudity or sexually explicit content',
                    'No violent or graphic content',
                    'No drug-related content',
                    'No weapons or dangerous items',
                    'Profile photos must show your face clearly',
                    'No misleading or fake profiles'
                ]
            ],
            [
                'heading' => '4. Safety First',
                'text' => 'Your safety is our priority:',
                'points' => [
                    'Never share personal information (address, financial details)',
                    'Do not arrange in-person meetings',
                    'Be cautious of requests for money or gifts',
                    'Report suspicious behavior immediately',
                    'Block users who make you uncomfortable',
                    'Trust your instincts'
                ]
            ],
            [
                'heading' => '5. No Illegal Activities',
                'text' => 'Illegal activities are strictly forbidden:',
                'points' => [
                    'No promotion of illegal services',
                    'No money laundering or fraud',
                    'No sale of illegal goods',
                    'No terrorism or extremist content',
                    'No intellectual property violations',
                    'Comply with all local laws'
                ]
            ],
            [
                'heading' => '6. Privacy & Consent',
                'text' => 'Respect the privacy of others:',
                'points' => [
                    'Do not record calls without consent',
                    'Do not screenshot or share private conversations',
                    'Do not share other users\' information',
                    'Respect blocking and reporting features',
                    'Obtain consent before sharing any content',
                    'Honor privacy preferences'
                ]
            ],
            [
                'heading' => '7. Authentic Profiles',
                'text' => 'Maintain honest and accurate profiles:',
                'points' => [
                    'Use real photos of yourself',
                    'Provide accurate age information',
                    'Do not impersonate others',
                    'Do not create fake accounts',
                    'Do not use celebrities\' photos',
                    'Keep profile information current'
                ]
            ],
            [
                'heading' => '8. For Female Creators',
                'text' => 'Additional guidelines for content creators:',
                'points' => [
                    'Maintain professional conduct during calls',
                    'Respect call time and user expectations',
                    'Do not solicit contact outside the app',
                    'Do not engage in explicit activities',
                    'Report inappropriate requests',
                    'Adhere to all platform policies'
                ]
            ],
            [
                'heading' => '9. Reporting & Moderation',
                'text' => 'Help us keep the community safe:',
                'points' => [
                    'Report violations through the app',
                    'Provide specific details when reporting',
                    'Do not abuse the reporting system',
                    'Our team reviews all reports promptly',
                    'False reports may result in penalties',
                    'We investigate all serious violations'
                ]
            ],
            [
                'heading' => '10. Consequences of Violations',
                'text' => 'Violations may result in:',
                'points' => [
                    'Warning message',
                    'Temporary account suspension (7-30 days)',
                    'Permanent account ban',
                    'Loss of earnings or coin balance',
                    'Reporting to law enforcement (serious violations)',
                    'Legal action if necessary'
                ]
            ],
            [
                'heading' => '11. Appeals Process',
                'text' => 'If your account is suspended:',
                'points' => [
                    'You will receive notification with reason',
                    'You may appeal within 7 days',
                    'Email onlycareapp000@gmail.com with details',
                    'Provide any relevant evidence',
                    'Our team will review and respond',
                    'Final decisions are at our discretion'
                ]
            ],
            [
                'heading' => '12. Age Verification',
                'text' => 'Only users 18+ are permitted:',
                'points' => [
                    'We may request age verification',
                    'Accounts of minors will be terminated immediately',
                    'Report suspected underage users',
                    'Parents should monitor device usage',
                    'We cooperate with authorities on minor safety'
                ]
            ],
            [
                'heading' => '13. Platform Integrity',
                'text' => 'Do not attempt to:',
                'points' => [
                    'Hack or exploit the platform',
                    'Use bots or automation',
                    'Manipulate the coin system',
                    'Reverse engineer the app',
                    'Create unfair advantages',
                    'Interfere with other users\' experience'
                ]
            ],
            [
                'heading' => '14. Updates to Guidelines',
                'text' => 'These guidelines may be updated periodically to reflect new policies, legal requirements, or community needs. Continued use of the platform indicates acceptance of updated guidelines.'
            ],
            [
                'heading' => '15. Contact & Support',
                'text' => 'Questions about these guidelines:',
                'points' => [
                    'Email: onlycareapp000@gmail.com',
                    'In-app support section',
                    'We respond within 24-48 hours',
                    'Serious violations addressed immediately'
                ]
            ],
            [
                'heading' => 'Conclusion',
                'text' => 'By following these guidelines, you help create a positive, safe, and enjoyable experience for everyone in the Only Care community. Thank you for being a responsible member!'
            ]
        ];
    }
}







