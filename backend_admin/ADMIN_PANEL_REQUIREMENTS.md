# Admin Panel Requirements - Only Care App

## Overview

The admin panel should provide comprehensive tools to manage users, monitor app activity, handle financial transactions, moderate content, and view analytics.

---

## 1. Dashboard (Home Screen)

### Key Metrics (Overview Cards)
```
┌─────────────────────────────────────────────────────────┐
│  Total Users: 10,234                                    │
│  ├─ Male: 8,156 (79.7%)                                 │
│  └─ Female: 2,078 (20.3%)                               │
│                                                          │
│  Active Users (Last 7 days): 5,432 (53.1%)              │
│  New Users (Today): 45                                   │
│  Online Now: 234                                         │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Total Calls: 45,678                                     │
│  ├─ Audio: 32,145 (70.4%)                                │
│  └─ Video: 13,533 (29.6%)                                │
│                                                          │
│  Calls Today: 567                                        │
│  Average Call Duration: 5m 23s                           │
│  Total Call Minutes: 3,45,678                            │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Revenue                                                 │
│  ├─ Today: ₹45,678                                       │
│  ├─ This Week: ₹2,34,567                                 │
│  ├─ This Month: ₹8,45,678                                │
│  └─ Total: ₹45,67,890                                    │
│                                                          │
│  Coins Sold: 4,56,789                                    │
│  Coins in Circulation: 2,34,567                          │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Pending Actions                                         │
│  ├─ Withdrawal Requests: 23                              │
│  ├─ KYC Verifications: 15                                │
│  ├─ User Reports: 8                                      │
│  └─ Support Tickets: 12                                  │
└─────────────────────────────────────────────────────────┘
```

### Charts & Graphs
1. **User Growth Chart** (Line graph - Last 30 days)
   - X-axis: Date
   - Y-axis: New users count
   - Show male/female breakdown

2. **Revenue Chart** (Bar graph - Last 12 months)
   - X-axis: Month
   - Y-axis: Revenue in INR
   - Show coin purchases vs withdrawals

3. **Call Activity Chart** (Line graph - Last 7 days)
   - X-axis: Date/Time
   - Y-axis: Number of calls
   - Show audio vs video calls

4. **User Distribution** (Pie chart)
   - Male vs Female ratio
   - Active vs Inactive users

---

## 2. User Management

### 2.1 User List View

**Filters:**
- Gender: All / Male / Female
- Status: All / Active / Inactive / Blocked / Deleted
- Verification: All / Verified / Unverified
- KYC: All / Approved / Pending / Rejected / Not Submitted
- Date Range: Registration date
- Search: By name, phone, user ID

**Table Columns:**
```
| User ID | Profile | Name | Age | Gender | Phone | Status | Verified | Rating | Coins | Registered | Actions |
```

**Actions per row:**
- 👁️ View Details
- ✏️ Edit
- 🚫 Block/Unblock
- 🗑️ Delete
- 💬 Send Notification

**Bulk Actions:**
- Block selected users
- Delete selected users
- Export to CSV
- Send notification to selected

### 2.2 User Detail View

**Profile Section:**
```
┌────────────────────────────────────────────────┐
│  [Profile Image]                               │
│                                                 │
│  Name: Priya Sharma                             │
│  Age: 24                                        │
│  Gender: Female                                 │
│  Phone: +91 9876543210                          │
│  User ID: USR_1234567890                        │
│  Language: Hindi                                │
│  Status: Active ✅                              │
│  Verified: Yes ✅                               │
│  KYC Status: Approved ✅                        │
│  Rating: 4.5 ⭐ (127 ratings)                   │
│                                                 │
│  Bio: Love music and travel...                  │
│  Interests: Music, Travel, Movies               │
│                                                 │
│  Registered: Jan 15, 2024                       │
│  Last Active: 2 hours ago                       │
└────────────────────────────────────────────────┘
```

**Statistics Tab:**
- Total Calls: 127
  - Audio: 89
  - Video: 38
- Total Call Duration: 42 hours
- Average Call Duration: 19 minutes
- Total Earnings: ₹5,430 (Female)
- Total Spent: ₹12,340 (Male)
- Friends: 23
- Messages Sent: 456
- Reports Received: 2
- Reports Made: 0

**Activity Tab:**
- Recent calls (last 20)
- Recent messages (last 50)
- Recent transactions
- Login history

**Financial Tab (Female):**
- Total Earnings: ₹5,430
- Available Balance: ₹2,340
- Withdrawn: ₹3,090
- Pending Withdrawals: ₹0
- Recent Withdrawal History

**Financial Tab (Male):**
- Total Spent: ₹12,340
- Current Balance: 234 coins
- Total Purchased: 15,000 coins
- Total Used: 14,766 coins
- Recent Purchases

**Actions:**
- Edit Profile
- Block/Unblock User
- Verify/Unverify User
- Adjust Coin Balance
- Send Notification
- View KYC Documents
- View Bank Details
- Delete Account

---

## 3. Call Management

### 3.1 Call List View

**Filters:**
- Call Type: All / Audio / Video
- Status: All / Ended / Missed / Rejected / Cancelled
- Date Range
- User: Filter by specific user ID
- Duration: < 1min / 1-5min / 5-15min / 15+min

**Table Columns:**
```
| Call ID | Caller | Receiver | Type | Duration | Coins Spent | Coins Earned | Status | Rating | Date | Actions |
```

**Actions:**
- 👁️ View Details
- 📊 View Analytics

### 3.2 Call Detail View

```
┌────────────────────────────────────────────────┐
│  Call Details                                   │
│                                                 │
│  Call ID: CALL_1234567890                       │
│  Type: Video Call 📹                            │
│  Status: Ended                                  │
│                                                 │
│  Caller (Male):                                 │
│  ├─ Name: Rahul Kumar                           │
│  ├─ User ID: USR_9876543210                     │
│  └─ Coins Spent: 75                             │
│                                                 │
│  Receiver (Female):                             │
│  ├─ Name: Priya Sharma                          │
│  ├─ User ID: USR_1234567890                     │
│  └─ Coins Earned: 75                            │
│                                                 │
│  Duration: 5 minutes 12 seconds                 │
│  Rate: 15 coins/minute (Video)                  │
│                                                 │
│  Started At: Jan 20, 2024 10:30 AM              │
│  Ended At: Jan 20, 2024 10:35 AM                │
│                                                 │
│  Rating: 4 stars ⭐⭐⭐⭐                        │
│  Feedback: "Good conversation"                  │
└────────────────────────────────────────────────┘
```

### 3.3 Call Analytics

**Charts:**
1. Calls per day (Last 30 days)
2. Call duration distribution
3. Peak calling hours (Heatmap)
4. Audio vs Video ratio
5. Average call ratings

**Statistics:**
- Total Calls: 45,678
- Average Duration: 5m 23s
- Success Rate: 78.5%
- Missed Call Rate: 15.2%
- Rejection Rate: 6.3%

---

## 4. Financial Management

### 4.1 Transactions

**Filters:**
- Type: All / Purchase / Call / Withdrawal / Bonus / Refund
- Status: All / Success / Pending / Failed
- User: Filter by user ID
- Date Range
- Amount Range

**Table Columns:**
```
| Transaction ID | User | Type | Amount (INR) | Coins | Payment Method | Status | Date | Actions |
```

**Actions:**
- 👁️ View Details
- 🔄 Refund (for purchases)
- 📄 Download Invoice

### 4.2 Coin Packages Management

**List View:**
```
| Package ID | Coins | Price | Original Price | Discount | Popular | Best Value | Active | Sort Order | Actions |
```

**Actions:**
- ➕ Add New Package
- ✏️ Edit Package
- 🗑️ Delete Package
- ⬆️⬇️ Reorder

**Add/Edit Package Form:**
- Coins: Number input
- Current Price: INR
- Original Price: INR
- Discount %: Auto-calculated or manual
- Is Popular: Checkbox
- Is Best Value: Checkbox
- Is Active: Checkbox
- Sort Order: Number

### 4.3 Withdrawal Management

**Status Tabs:**
- Pending (23)
- Approved (156)
- Rejected (12)
- Completed (3,456)

**Table Columns:**
```
| Withdrawal ID | User | Amount | Coins | Bank Account | Status | Requested Date | Actions |
```

**Actions:**
- 👁️ View Details
- ✅ Approve
- ❌ Reject
- ✓ Mark Completed

**Withdrawal Detail View:**
```
┌────────────────────────────────────────────────┐
│  Withdrawal Request Details                     │
│                                                 │
│  Request ID: WD_1234567890                      │
│  Status: Pending Review                         │
│                                                 │
│  User Details:                                  │
│  ├─ Name: Priya Sharma                          │
│  ├─ User ID: USR_1234567890                     │
│  ├─ Phone: +91 9876543210                       │
│  ├─ KYC Status: Approved ✅                     │
│  └─ Verification: Verified ✅                   │
│                                                 │
│  Withdrawal Details:                            │
│  ├─ Amount: ₹1,500                              │
│  ├─ Coins: 1,500                                │
│  └─ Requested On: Jan 20, 2024 10:30 AM         │
│                                                 │
│  Bank Account:                                  │
│  ├─ Account Holder: Priya Sharma                │
│  ├─ Account Number: XXXX-XXXX-1234              │
│  ├─ IFSC Code: SBIN0001234                      │
│  ├─ Bank: State Bank of India                   │
│  └─ UPI ID: priya@paytm                         │
│                                                 │
│  Admin Notes:                                   │
│  [Text area for notes]                          │
│                                                 │
│  Actions:                                       │
│  [Approve] [Reject] [Request More Info]         │
└────────────────────────────────────────────────┘
```

**Approve Flow:**
1. Review user details
2. Verify KYC approved
3. Check available balance
4. Add admin notes
5. Click Approve
6. Withdrawal status → APPROVED
7. Process bank transfer
8. Mark as COMPLETED
9. Notify user

**Reject Flow:**
1. Select rejection reason
2. Add explanation
3. Click Reject
4. Coins returned to user
5. Notify user

### 4.4 Revenue Reports

**Report Types:**
1. **Daily Revenue Report**
   - Date, Total Sales, Coin Packages Sold, Total Withdrawals, Net Revenue

2. **Monthly Revenue Report**
   - Month-wise breakdown
   - Revenue trends
   - Growth percentage

3. **Payment Method Report**
   - Revenue by payment method
   - Success/failure rates

4. **User Spending Report**
   - Top spenders (Male users)
   - Average spending per user
   - Retention analysis

**Export Options:**
- PDF
- CSV
- Excel
- Print

---

## 5. KYC Management

### 5.1 KYC Verification Queue

**Status Tabs:**
- Pending (15)
- Approved (234)
- Rejected (23)

**Table Columns:**
```
| User | Name | Phone | Submitted Date | Status | Actions |
```

**Actions:**
- 🔍 Review Documents

### 5.2 KYC Review Screen

```
┌────────────────────────────────────────────────┐
│  KYC Verification - Priya Sharma                │
│                                                 │
│  User ID: USR_1234567890                        │
│  Phone: +91 9876543210                          │
│  Submitted: Jan 20, 2024 10:30 AM               │
│                                                 │
│  Documents:                                     │
│                                                 │
│  1. Aadhaar Card:                               │
│     Number: 1234-5678-9012                      │
│     [View Image 📄]                             │
│                                                 │
│  2. PAN Card:                                   │
│     Number: ABCDE1234F                          │
│     [View Image 📄]                             │
│                                                 │
│  3. Selfie:                                     │
│     [View Image 📄]                             │
│                                                 │
│  Verification Checklist:                        │
│  ☐ Name matches on all documents                │
│  ☐ Documents are clear and readable             │
│  ☐ Aadhaar number is valid                      │
│  ☐ PAN number is valid                          │
│  ☐ Selfie matches document photos               │
│  ☐ Age is 18+ years                             │
│                                                 │
│  Admin Notes:                                   │
│  [Text area]                                    │
│                                                 │
│  Actions:                                       │
│  [✅ Approve] [❌ Reject]                       │
│                                                 │
│  Rejection Reasons (if rejecting):              │
│  ☐ Documents unclear                            │
│  ☐ Name mismatch                                │
│  ☐ Invalid document numbers                     │
│  ☐ Selfie doesn't match                         │
│  ☐ Age below 18                                 │
│  ☐ Other: [Specify]                             │
└────────────────────────────────────────────────┘
```

**Approve:**
- KYC status → APPROVED
- User can now withdraw
- Send approval notification

**Reject:**
- Select rejection reason
- Add explanation
- KYC status → REJECTED
- User can resubmit
- Send rejection notification

---

## 6. Content Moderation

### 6.1 Reports Management

**Status Tabs:**
- Pending (8)
- Reviewing (5)
- Resolved (1,234)
- Dismissed (456)

**Table Columns:**
```
| Report ID | Reporter | Reported User | Type | Description | Status | Date | Actions |
```

**Report Types:**
- Inappropriate Behavior
- Harassment
- Spam
- Fake Profile
- Other

**Actions:**
- 🔍 Review Report

### 6.2 Report Review Screen

```
┌────────────────────────────────────────────────┐
│  Report Details                                 │
│                                                 │
│  Report ID: REP_1234567890                      │
│  Status: Pending Review                         │
│  Report Type: Harassment                        │
│                                                 │
│  Reporter:                                      │
│  ├─ Name: Priya Sharma                          │
│  ├─ User ID: USR_1234567890                     │
│  └─ Previous Reports: 0                         │
│                                                 │
│  Reported User:                                 │
│  ├─ Name: Rahul Kumar                           │
│  ├─ User ID: USR_9876543210                     │
│  ├─ Rating: 2.3 ⭐                              │
│  ├─ Reports Received: 5                         │
│  └─ Status: Active                              │
│                                                 │
│  Description:                                   │
│  "User was sending inappropriate messages       │
│   during the call..."                           │
│                                                 │
│  Related Information:                           │
│  ├─ Call ID: CALL_1234567890                    │
│  ├─ Call Date: Jan 20, 2024                     │
│  └─ Call Duration: 5 minutes                    │
│                                                 │
│  Previous Actions on Reported User:             │
│  - Warning sent on Jan 10, 2024                 │
│  - 7-day suspension on Dec 15, 2023             │
│                                                 │
│  Admin Notes:                                   │
│  [Text area]                                    │
│                                                 │
│  Actions:                                       │
│  ├─ [Send Warning]                              │
│  ├─ [Suspend 7 Days]                            │
│  ├─ [Suspend 15 Days]                           │
│  ├─ [Suspend 30 Days]                           │
│  ├─ [Permanent Ban]                             │
│  ├─ [Dismiss Report]                            │
│  └─ [Request More Info from Reporter]           │
└────────────────────────────────────────────────┘
```

### 6.3 Suspended Users

**Table Columns:**
```
| User ID | Name | Suspension Type | Reason | Start Date | End Date | Status | Actions |
```

**Actions:**
- View Details
- Lift Suspension Early
- Extend Suspension
- Convert to Permanent Ban

---

## 7. Settings Management

### 7.1 App Settings

**Call Rates:**
```
Audio Call Rate: [10] coins/minute
Video Call Rate: [15] coins/minute
```

**Withdrawal Settings:**
```
Minimum Withdrawal: ₹ [500]
Maximum Withdrawal per Day: ₹ [50,000]
Withdrawal Processing Days: [3-5] days
Coin to INR Rate: [1] INR per coin
```

**Referral Settings:**
```
Referrer Bonus: [100] coins
Referred User Bonus: [50] coins
Max Referrals per Month: [Unlimited]
```

**User Settings:**
```
Minimum Age: [18] years
Maximum Age: [99] years
Minimum Name Length: [3] characters
OTP Expiry: [10] minutes
```

**Auto-Moderation:**
```
☑ Enable Profanity Filter
☑ Enable Spam Detection
☐ Auto-ban on 10+ reports
Warning Threshold: [3] reports
Suspension Threshold: [5] reports
```

### 7.2 Notification Templates

**Template Types:**
- Welcome Message
- OTP Message
- Payment Success
- Payment Failed
- Withdrawal Approved
- Withdrawal Completed
- KYC Approved
- KYC Rejected
- Account Warning
- Account Suspended

**Edit Template:**
```
Template: Payment Success

Title: Payment Successful! 🎉

Message:
Your payment of ₹{amount} is successful.
{coins} coins have been added to your wallet.
Transaction ID: {transaction_id}

Variables:
- {amount}: Payment amount
- {coins}: Coins purchased
- {transaction_id}: Transaction ID

[Save Template]
```

### 7.3 Admin Users

**Table Columns:**
```
| Admin ID | Username | Email | Role | Status | Last Login | Actions |
```

**Roles:**
- Super Admin (Full access)
- Admin (User management)
- Moderator (Content moderation)
- Finance (Withdrawals, reports)
- Support (View-only, support tickets)

**Actions:**
- ➕ Add New Admin
- ✏️ Edit Admin
- 🚫 Deactivate
- 🗑️ Delete

---

## 8. Analytics & Reports

### 8.1 User Analytics

**Charts:**
1. User Growth Over Time
2. Male vs Female Ratio
3. User Activity Heatmap
4. Retention Rate (Daily/Weekly/Monthly)
5. Churn Rate
6. User Lifetime Value

**Statistics:**
- Total Users: 10,234
- Active Users (Last 7 days): 5,432
- New Users (Last 30 days): 1,234
- Average Session Duration: 15 minutes
- Daily Active Users (DAU): 2,345
- Monthly Active Users (MAU): 7,890
- DAU/MAU Ratio: 29.7%

### 8.2 Call Analytics

**Charts:**
1. Calls per Day
2. Audio vs Video Distribution
3. Peak Calling Hours
4. Average Call Duration Trend
5. Call Success Rate

**Statistics:**
- Total Calls: 45,678
- Calls Today: 567
- Average Duration: 5m 23s
- Audio Calls: 32,145 (70.4%)
- Video Calls: 13,533 (29.6%)
- Success Rate: 78.5%
- Missed Rate: 15.2%
- Rejection Rate: 6.3%

### 8.3 Revenue Analytics

**Charts:**
1. Revenue Over Time
2. Revenue by Package
3. Revenue by Payment Method
4. Withdrawals vs Revenue

**Statistics:**
- Total Revenue: ₹45,67,890
- Revenue Today: ₹45,678
- Revenue This Month: ₹8,45,678
- Average Order Value: ₹567
- Total Withdrawals: ₹12,34,567
- Net Revenue: ₹33,33,323

### 8.4 Export Reports

**Report Types:**
1. User Report
2. Call Report
3. Transaction Report
4. Revenue Report
5. Withdrawal Report
6. KYC Report

**Filters:**
- Date Range
- User Type
- Status
- Custom Filters

**Export Formats:**
- PDF
- CSV
- Excel
- JSON

---

## 9. Support Management

### 9.1 Support Tickets

**Status Tabs:**
- Open (12)
- In Progress (8)
- Resolved (456)
- Closed (1,234)

**Table Columns:**
```
| Ticket ID | User | Subject | Category | Priority | Status | Created | Last Updated | Assigned To | Actions |
```

**Categories:**
- Account Issues
- Payment Issues
- Call Issues
- Technical Issues
- KYC Issues
- Withdrawal Issues
- Other

**Priority:**
- Low
- Medium
- High
- Urgent

**Actions:**
- View Details
- Reply
- Assign
- Change Status
- Close

### 9.2 Ticket Detail View

```
┌────────────────────────────────────────────────┐
│  Support Ticket #TICK_1234                      │
│                                                 │
│  User: Priya Sharma (USR_1234567890)            │
│  Subject: Unable to withdraw earnings           │
│  Category: Withdrawal Issues                    │
│  Priority: High                                 │
│  Status: Open                                   │
│  Created: Jan 20, 2024 10:30 AM                 │
│  Last Updated: Jan 20, 2024 11:45 AM            │
│  Assigned To: Admin User 1                      │
│                                                 │
│  Conversation:                                  │
│  ───────────────────────────────────────        │
│  [User] Jan 20, 10:30 AM:                       │
│  "I'm trying to withdraw ₹1500 but getting      │
│   an error..."                                  │
│                                                 │
│  [Admin] Jan 20, 11:45 AM:                      │
│  "We're looking into this issue..."             │
│  ───────────────────────────────────────        │
│                                                 │
│  Reply:                                         │
│  [Text editor]                                  │
│                                                 │
│  [Send Reply] [Change Status] [Close Ticket]    │
└────────────────────────────────────────────────┘
```

---

## 10. Logs & Activity

### 10.1 Admin Activity Log

**Table Columns:**
```
| Timestamp | Admin | Action | Entity | Details | IP Address |
```

**Logged Actions:**
- User blocked/unblocked
- User deleted
- KYC approved/rejected
- Withdrawal approved/rejected
- Settings changed
- Admin user created/modified
- Report resolved

### 10.2 System Logs

**Log Types:**
- Error Logs
- API Logs
- Payment Gateway Logs
- Authentication Logs

**Filters:**
- Date Range
- Log Level (Info/Warning/Error)
- Component
- Search

---

## Technical Requirements

### Authentication
- Admin login with email/password
- 2FA optional
- Session timeout: 30 minutes
- Remember me option

### Permissions
```
Super Admin: All permissions
Admin: Users, Calls, Reports, KYC
Moderator: Reports only
Finance: Withdrawals, Revenue reports
Support: Read-only + Support tickets
```

### Performance
- Load time < 2 seconds
- Handle 10,000+ users
- Real-time updates for dashboard metrics
- Pagination: 50 items per page
- Export limits: 10,000 records max

### Security
- HTTPS only
- CSRF protection
- SQL injection prevention
- XSS prevention
- Rate limiting on APIs
- Activity logging

### Tech Stack Suggestions
- **Backend**: Node.js/Python/PHP/Java
- **Database**: MySQL/PostgreSQL
- **Frontend**: React/Vue/Angular
- **Charts**: Chart.js/D3.js
- **UI Framework**: Material UI/Ant Design/Bootstrap

---

This comprehensive admin panel will provide full control and visibility over the Only Care app operations.

