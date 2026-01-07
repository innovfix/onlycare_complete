# Recent Sessions API - Complete Documentation

## Overview
The Recent Sessions API provides a comprehensive view of recent call activities for users, designed specifically for the "Recent" tab in the Only Care mobile app.

## Endpoint Details

### Base Information
- **Endpoint:** `GET /api/v1/calls/recent-sessions`
- **Authentication:** Required (Bearer Token)
- **Method:** GET
- **User Types:** All (Male & Female)

### Query Parameters

| Parameter | Type | Required | Default | Max | Description |
|-----------|------|----------|---------|-----|-------------|
| `page` | integer | No | 1 | - | Page number for pagination |
| `limit` | integer | No | 20 | 50 | Number of items per page |

### Example Request

```bash
curl -X GET "https://api.onlycare.app/v1/calls/recent-sessions?page=1&limit=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Accept: application/json"
```

## Response Structure

### Success Response (200 OK)

```json
{
  "success": true,
  "sessions": [
    {
      "id": "CALL_1234567890",
      "user": {
        "id": "USR_1234567890",
        "name": "Anushrma09",
        "username": "anushrma09",
        "age": 24,
        "profile_image": "https://cdn.onlycare.app/profiles/user_1234567890.jpg",
        "rating": 4.5,
        "is_online": true,
        "audio_call_enabled": true,
        "video_call_enabled": true
      },
      "call_type": "AUDIO",
      "status": "ENDED",
      "duration": 180,
      "duration_formatted": "3 min",
      "is_incoming": false,
      "is_outgoing": true,
      "coins_spent": 30,
      "coins_earned": null,
      "rating": 5,
      "created_at": "2024-01-20T21:48:00Z",
      "created_at_formatted": "Yesterday 09:48 PM"
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 5,
    "total_items": 98,
    "per_page": 20,
    "has_more": true
  }
}
```

### Error Responses

#### 401 Unauthorized
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Authentication required"
  }
}
```

#### 422 Validation Error
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid query parameters",
    "details": {
      "limit": ["The limit must not be greater than 50"]
    }
  }
}
```

## Response Field Details

### Session Object

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique call identifier (format: CALL_{id}) |
| `user` | object | Information about the other user in the call |
| `call_type` | string | Type of call: "AUDIO" or "VIDEO" |
| `status` | string | Call status: "ENDED", "REJECTED", or "MISSED" |
| `duration` | integer | Call duration in seconds (null if call wasn't connected) |
| `duration_formatted` | string | Human-readable duration (e.g., "3 min", "15 min") |
| `is_incoming` | boolean | True if current user received the call |
| `is_outgoing` | boolean | True if current user initiated the call |
| `coins_spent` | integer | Coins spent (only for callers, null for receivers) |
| `coins_earned` | integer | Coins earned (only for receivers, null for callers) |
| `rating` | decimal | Call rating (1-5), null if not rated |
| `created_at` | string | ISO 8601 timestamp |
| `created_at_formatted` | string | Human-readable timestamp |

### User Object

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | User identifier (format: USR_{id}) |
| `name` | string | Display name |
| `username` | string | Unique username |
| `age` | integer | User age |
| `profile_image` | string | Profile picture URL |
| `rating` | decimal | Average user rating (0-5) |
| `is_online` | boolean | Current online status |
| `audio_call_enabled` | boolean | Audio call availability |
| `video_call_enabled` | boolean | Video call availability |

### Pagination Object

| Field | Type | Description |
|-------|------|-------------|
| `current_page` | integer | Current page number |
| `total_pages` | integer | Total number of pages |
| `total_items` | integer | Total number of sessions |
| `per_page` | integer | Items per page |
| `has_more` | boolean | Whether more pages exist |

## Key Features

### 1. **Dual Perspective**
- Shows both incoming and outgoing calls
- `is_incoming` and `is_outgoing` flags help distinguish call direction
- Appropriate coins display (spent vs earned)

### 2. **Smart Timestamps**
- ISO 8601 format for programmatic use
- Human-readable format:
  - "Today {time}" for calls within last 24 hours
  - "Yesterday {time}" for calls 24-48 hours ago
  - "MMM DD, YYYY {time}" for older calls

### 3. **Duration Formatting**
- Raw seconds for calculations
- Formatted string for UI display
- Minimum shown as "1 min" even for shorter calls

### 4. **User Availability**
- Real-time online status
- Call type availability (audio/video)
- Enables/disables call action buttons in UI

### 5. **Efficient Pagination**
- `has_more` flag for infinite scroll implementation
- Maximum 50 items per page for performance
- Total counts for progress indicators

## Implementation Examples

### React Native Implementation

```javascript
import { useState, useEffect } from 'react';
import axios from 'axios';

const RecentSessionsScreen = () => {
  const [sessions, setSessions] = useState([]);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  const fetchSessions = async (pageNum = 1) => {
    if (loading) return;
    
    setLoading(true);
    try {
      const response = await axios.get('/api/v1/calls/recent-sessions', {
        params: { page: pageNum, limit: 20 }
      });
      
      if (pageNum === 1) {
        setSessions(response.data.sessions);
      } else {
        setSessions([...sessions, ...response.data.sessions]);
      }
      
      setHasMore(response.data.pagination.has_more);
    } catch (error) {
      console.error('Failed to fetch sessions:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSessions(1);
  }, []);

  const loadMore = () => {
    if (hasMore && !loading) {
      const nextPage = page + 1;
      setPage(nextPage);
      fetchSessions(nextPage);
    }
  };

  return (
    <FlatList
      data={sessions}
      renderItem={({ item }) => <SessionItem session={item} />}
      onEndReached={loadMore}
      onEndReachedThreshold={0.5}
      refreshing={loading}
      onRefresh={() => {
        setPage(1);
        fetchSessions(1);
      }}
    />
  );
};

const SessionItem = ({ session }) => (
  <View style={styles.sessionItem}>
    <Image 
      source={{ uri: session.user.profile_image }} 
      style={styles.avatar}
    />
    <View style={styles.details}>
      <Text style={styles.name}>{session.user.name}</Text>
      <Text style={styles.timestamp}>
        {session.created_at_formatted}
      </Text>
      <Text style={styles.duration}>
        {session.duration_formatted}
      </Text>
    </View>
    <View style={styles.actions}>
      <TouchableOpacity 
        disabled={!session.user.audio_call_enabled || !session.user.is_online}
        onPress={() => initiateCall(session.user.id, 'AUDIO')}
      >
        <Icon name="phone" />
      </TouchableOpacity>
      <TouchableOpacity 
        disabled={!session.user.video_call_enabled || !session.user.is_online}
        onPress={() => initiateCall(session.user.id, 'VIDEO')}
      >
        <Icon name="video" />
      </TouchableOpacity>
    </View>
  </View>
);
```

### Flutter Implementation

```dart
class RecentSessionsProvider extends ChangeNotifier {
  List<CallSession> _sessions = [];
  int _currentPage = 1;
  bool _hasMore = true;
  bool _loading = false;

  Future<void> fetchSessions({bool refresh = false}) async {
    if (_loading) return;
    
    _loading = true;
    if (refresh) {
      _currentPage = 1;
      _sessions.clear();
    }

    try {
      final response = await dio.get(
        '/api/v1/calls/recent-sessions',
        queryParameters: {
          'page': _currentPage,
          'limit': 20,
        },
      );

      final List<dynamic> sessionsList = response.data['sessions'];
      _sessions.addAll(
        sessionsList.map((json) => CallSession.fromJson(json)).toList()
      );
      
      _hasMore = response.data['pagination']['has_more'];
      _currentPage++;
    } catch (e) {
      print('Error fetching sessions: $e');
    } finally {
      _loading = false;
      notifyListeners();
    }
  }
}

class RecentSessionsScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Consumer<RecentSessionsProvider>(
      builder: (context, provider, child) {
        return RefreshIndicator(
          onRefresh: () => provider.fetchSessions(refresh: true),
          child: ListView.builder(
            itemCount: provider.sessions.length,
            itemBuilder: (context, index) {
              if (index == provider.sessions.length - 1 && provider.hasMore) {
                provider.fetchSessions();
              }
              return SessionCard(session: provider.sessions[index]);
            },
          ),
        );
      },
    );
  }
}
```

## Use Cases

### 1. **Recent Tab Display**
Primary use case for displaying recent call activity in the app's "Recent" tab.

### 2. **Quick Redial**
Enables users to quickly call recent contacts with appropriate call type buttons.

### 3. **Call Analytics**
Track call patterns, frequency, and engagement with specific users.

### 4. **Earnings Tracking (Female Users)**
Female users can see coins earned from recent calls.

### 5. **Spending Tracking (Male Users)**
Male users can see coins spent on recent calls.

## Best Practices

### 1. **Pagination**
- Implement infinite scroll for better UX
- Use `has_more` flag to hide/show loading indicators
- Load 20 items per page for optimal performance

### 2. **Caching**
- Cache first page for instant display
- Refresh on pull-to-refresh gesture
- Clear cache on logout

### 3. **Real-time Updates**
- Consider WebSocket integration for real-time session updates
- Update online status indicators periodically
- Refresh list when returning from call screen

### 4. **Error Handling**
- Show retry button on network errors
- Cache last successful response
- Provide offline indicators

### 5. **UI Optimization**
- Use `duration_formatted` and `created_at_formatted` for display
- Show appropriate icons based on `call_type`
- Disable call buttons based on availability flags

## Database Schema Reference

### Calls Table
```sql
CREATE TABLE calls (
  id BIGINT PRIMARY KEY,
  caller_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  call_type ENUM('AUDIO', 'VIDEO'),
  status ENUM('CONNECTING', 'ONGOING', 'ENDED', 'REJECTED', 'MISSED'),
  duration INT DEFAULT 0,
  coins_spent INT DEFAULT 0,
  coins_earned INT DEFAULT 0,
  rate_per_minute INT DEFAULT 10,
  rating DECIMAL(2,1),
  feedback TEXT,
  started_at TIMESTAMP,
  ended_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for optimal query performance
CREATE INDEX idx_calls_caller_created ON calls(caller_id, created_at DESC);
CREATE INDEX idx_calls_receiver_created ON calls(receiver_id, created_at DESC);
CREATE INDEX idx_calls_status ON calls(status);
```

## Performance Considerations

### Query Optimization
- Uses indexes on caller_id, receiver_id, and created_at
- Limits results to prevent memory issues
- Eager loads related user data to prevent N+1 queries

### Response Time
- Expected response time: 100-300ms
- Maximum response time: 1 second
- Cached user data reduces query time

### Scalability
- Handles millions of call records efficiently
- Pagination prevents large data transfers
- Indexed queries ensure consistent performance

## Change Log

### Version 1.0 (November 4, 2025)
- Initial release of Recent Sessions API
- Supports both male and female users
- Includes formatted timestamps and durations
- Provides user availability information
- Pagination with has_more flag

## Support & Issues

For API issues or questions:
1. Check API_DOCUMENTATION.md for general API guidelines
2. Review error codes and responses
3. Contact backend team with specific error messages
4. Include request/response logs for debugging

## Related APIs

- **Call History API** (`/calls/history`) - Historical call records only
- **Recent Callers API** (`/calls/recent-callers`) - Female-only, grouped by caller
- **Initiate Call API** (`/calls/initiate`) - Start a new call
- **User Details API** (`/users/{userId}`) - Get detailed user information

---

**Last Updated:** November 4, 2025  
**API Version:** v1  
**Status:** Production Ready ✅







