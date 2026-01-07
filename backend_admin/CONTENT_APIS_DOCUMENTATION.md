# Content & Policy APIs Documentation

## Overview

This document describes the newly implemented Content & Policy APIs for the Only Care application. These APIs provide access to important legal and policy documents that users need to review.

## Implementation Summary

### Files Created/Modified

1. **New Controller**: `app/Http/Controllers/Api/ContentController.php`
   - Contains methods for all content APIs
   - Returns structured JSON responses with comprehensive policy content

2. **Routes**: `routes/api.php`
   - Added Content API routes (public, no authentication required)
   - Updated section numbering to accommodate new APIs

3. **Web Documentation**: `resources/views/api-docs/index-dark.blade.php`
   - Added sidebar navigation for Content & Policies
   - Added documentation sections for all 4 APIs
   - Added code examples with request/response samples

---

## API Endpoints

All endpoints are public and do not require authentication.

### Base URL
```
Development: http://localhost/only_care_admin/public/api/v1
Production: https://api.onlycare.app/v1
```

---

## 1. Get Privacy Policy

**Endpoint**: `GET /content/privacy-policy`

**Description**: Retrieves the complete privacy policy of the application.

**Request Headers**:
```
Accept: application/json
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "title": "Privacy Policy",
    "last_updated": "2025-11-04",
    "content": [
      {
        "heading": "Introduction",
        "text": "Welcome to Only Care...",
        "points": []
      },
      {
        "heading": "1. Information We Collect",
        "text": "We collect information that you provide...",
        "points": [
          "Register for an account",
          "Create your profile",
          "Make voice or video calls",
          "..."
        ]
      }
    ]
  }
}
```

**Content Sections**:
1. Introduction
2. Information We Collect
3. Personal Information
4. How We Use Your Information
5. Information Sharing
6. Data Security
7. Data Retention
8. Your Rights
9. Children's Privacy
10. Changes to Privacy Policy
11. Contact Us

---

## 2. Get Terms & Conditions

**Endpoint**: `GET /content/terms-conditions`

**Description**: Retrieves the complete terms and conditions of the application.

**Request Headers**:
```
Accept: application/json
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "title": "Terms & Conditions",
    "last_updated": "2025-11-04",
    "content": [
      {
        "heading": "Introduction",
        "text": "Welcome to Only Care...",
        "points": []
      },
      {
        "heading": "1. Acceptance of Terms",
        "text": "By creating an account...",
        "points": []
      }
    ]
  }
}
```

**Content Sections**:
1. Introduction
2. Acceptance of Terms
3. Eligibility
4. Account Registration
5. User Conduct
6. Voice & Video Calls
7. Payments & Coins
8. Content Ownership
9. Earnings & Withdrawals (Female Creators)
10. Prohibited Content
11. Account Suspension & Termination
12. Disclaimer of Warranties
13. Limitation of Liability
14. Indemnification
15. Changes to Terms
16. Contact Information

---

## 3. Get Refund & Cancellation Policy

**Endpoint**: `GET /content/refund-policy`

**Description**: Retrieves the complete refund and cancellation policy.

**Request Headers**:
```
Accept: application/json
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "title": "Refund & Cancellation Policy",
    "last_updated": "2025-11-04",
    "content": [
      {
        "heading": "Introduction",
        "text": "This Refund & Cancellation Policy...",
        "points": []
      },
      {
        "heading": "1. General Policy",
        "text": "All coin purchases...",
        "points": []
      }
    ]
  }
}
```

**Content Sections**:
1. Introduction
2. General Policy
3. Refund Eligibility
4. Non-Refundable Situations
5. Refund Request Process
6. Cancellation Policy
7. Payment Processing
8. Unauthorized Transactions
9. Creator Earnings
10. Dispute Resolution
11. Changes to Refund Policy
12. Contact Information
13. Legal Rights

---

## 4. Get Community Guidelines & Moderation Policy

**Endpoint**: `GET /content/community-guidelines`

**Description**: Retrieves the complete community guidelines and moderation policy.

**Request Headers**:
```
Accept: application/json
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "title": "Community Guidelines & Moderation Policy",
    "last_updated": "2025-11-04",
    "content": [
      {
        "heading": "Introduction",
        "text": "Only Care is committed to...",
        "points": []
      },
      {
        "heading": "1. Be Respectful",
        "text": "Treat everyone with dignity...",
        "points": [
          "Be polite and courteous",
          "Respect different opinions",
          "Use appropriate language"
        ]
      }
    ]
  }
}
```

**Content Sections**:
1. Introduction
2. Be Respectful
3. No Harassment or Abuse
4. Appropriate Content
5. Safety First
6. No Illegal Activities
7. Privacy & Consent
8. Authentic Profiles
9. For Female Creators
10. Reporting & Moderation
11. Consequences of Violations
12. Appeals Process
13. Age Verification
14. Platform Integrity
15. Updates to Guidelines
16. Contact & Support
17. Conclusion

---

## Response Structure

All APIs return a consistent JSON structure:

```json
{
  "success": boolean,
  "data": {
    "title": "string",
    "last_updated": "YYYY-MM-DD",
    "content": [
      {
        "heading": "string",
        "text": "string",
        "points": ["string", "string", ...]
      }
    ]
  }
}
```

### Content Array Structure

Each content object contains:
- **heading**: Section heading or title
- **text**: Main content text for the section
- **points**: Array of bullet points (optional, can be empty array)

---

## Error Responses

**500 Internal Server Error**:
```json
{
  "success": false,
  "message": "Failed to fetch [content type]"
}
```

---

## Testing the APIs

### Using cURL

**Privacy Policy**:
```bash
curl -X GET http://localhost/only_care_admin/public/api/v1/content/privacy-policy \
  -H "Accept: application/json"
```

**Terms & Conditions**:
```bash
curl -X GET http://localhost/only_care_admin/public/api/v1/content/terms-conditions \
  -H "Accept: application/json"
```

**Refund Policy**:
```bash
curl -X GET http://localhost/only_care_admin/public/api/v1/content/refund-policy \
  -H "Accept: application/json"
```

**Community Guidelines**:
```bash
curl -X GET http://localhost/only_care_admin/public/api/v1/content/community-guidelines \
  -H "Accept: application/json"
```

### Using JavaScript (React Native)

```javascript
const getPrivacyPolicy = async () => {
  try {
    const response = await fetch(
      'https://api.onlycare.app/v1/content/privacy-policy',
      {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      }
    );
    const data = await response.json();
    if (data.success) {
      console.log('Privacy Policy:', data.data);
      // Display the content in your UI
      renderPolicyContent(data.data);
    }
  } catch (error) {
    console.error('Error fetching privacy policy:', error);
  }
};
```

---

## UI Implementation Suggestions

### Rendering the Content

```jsx
const PolicyScreen = ({ content }) => {
  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>{content.title}</Text>
      <Text style={styles.lastUpdated}>
        Last Updated: {content.last_updated}
      </Text>
      
      {content.content.map((section, index) => (
        <View key={index} style={styles.section}>
          <Text style={styles.heading}>{section.heading}</Text>
          <Text style={styles.text}>{section.text}</Text>
          
          {section.points && section.points.length > 0 && (
            <View style={styles.pointsContainer}>
              {section.points.map((point, idx) => (
                <Text key={idx} style={styles.point}>
                  • {point}
                </Text>
              ))}
            </View>
          )}
        </View>
      ))}
    </ScrollView>
  );
};
```

---

## Integration with Profile Screen

Based on the profile screen screenshot showing:
- Privacy (card)
- Terms & Condition (link)
- Refund & Cancellation (link)
- Community Guidelines & Moderation Policy (link)

Each should navigate to a screen that:
1. Calls the respective API endpoint
2. Displays the content in a scrollable view
3. Shows the last updated date
4. Allows users to scroll through all sections

---

## Web-Based API Documentation

The complete interactive API documentation is available at:
```
http://localhost/only_care_admin/public/api-docs
```

Features:
- Dark theme UI
- Sidebar navigation
- Request/response examples
- Interactive code snippets
- Copy-to-clipboard functionality

---

## Key Features

### 1. No Authentication Required
- All content APIs are public
- Can be accessed without login
- Useful for showing policies during registration

### 2. Comprehensive Content
- Privacy Policy: 10 sections covering all privacy aspects
- Terms & Conditions: 15 sections covering usage terms
- Refund Policy: 12 sections covering refund conditions
- Community Guidelines: 16 sections covering community standards

### 3. Structured Data
- Easy to parse and display in mobile apps
- Consistent format across all APIs
- Supports both text and bullet points

### 4. Last Updated Date
- Each policy includes a `last_updated` field
- Apps can cache and check for updates
- Users always see the current version

---

## Contact Information

For questions or support regarding these APIs:
- Email: himaapp000@gmail.com
- In-app support section

---

## Testing Results

All APIs have been tested and verified:

✅ **Privacy Policy API**: Working correctly
✅ **Terms & Conditions API**: Working correctly
✅ **Refund Policy API**: Working correctly
✅ **Community Guidelines API**: Working correctly

Test command results:
```bash
# Privacy Policy
curl http://localhost/only_care_admin/public/api/v1/content/privacy-policy | jq -r '.data.title'
Output: Privacy Policy

# Terms & Conditions
curl http://localhost/only_care_admin/public/api/v1/content/terms-conditions | jq -r '.data.title'
Output: Terms & Conditions

# Refund Policy
curl http://localhost/only_care_admin/public/api/v1/content/refund-policy | jq -r '.data.title'
Output: Refund & Cancellation Policy

# Community Guidelines
curl http://localhost/only_care_admin/public/api/v1/content/community-guidelines | jq -r '.data.title'
Output: Community Guidelines & Moderation Policy
```

---

## Future Enhancements

Potential improvements for future versions:

1. **Database Storage**: Store content in database for easier updates
2. **Admin Panel**: Add admin UI to edit policies without code changes
3. **Version History**: Track policy changes over time
4. **Multi-language**: Support multiple languages
5. **User Acceptance**: Track which users have accepted which version
6. **PDF Export**: Generate PDF versions of policies
7. **Email Notifications**: Notify users when policies are updated

---

## Compliance Notes

These policies cover:
- **GDPR Compliance**: Data collection, usage, and user rights
- **Indian IT Act**: Privacy and data protection requirements
- **Consumer Protection**: Refund and cancellation rights
- **Content Moderation**: Community standards and reporting

---

## Implementation Date

- **Created**: November 4, 2025
- **Last Updated**: November 4, 2025
- **Version**: 1.0.0

---

## Support

For technical support or questions about implementation:
1. Check the web-based API documentation
2. Review this documentation
3. Contact: himaapp000@gmail.com







