# Home Screen Creator Listing API Documentation

## Overview
This API endpoint provides a list of female creators (users) for display on the male user's home screen. Each creator card includes their profile information, interests, call availability, and pricing.

---

## Endpoint

### Get Female Creators / Home Screen List

**GET** `/api/v1/users/females`

Lists all female creators with their profile information, availability, and call rates for the home screen display.

#### Authentication
Required: Yes (Bearer Token)

```http
Authorization: Bearer YOUR_ACCESS_TOKEN
```

#### Access Control
- Only **MALE** users can access this endpoint
- Female users will receive a `403 Forbidden` error

---

## Request Parameters

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `limit` | integer | No | 20 | Number of items per page (max: 50) |
| `page` | integer | No | 1 | Page number for pagination |
| `online` | boolean | No | false | Filter by online status (`true` or `false`) |
| `verified` | boolean | No | false | Filter by KYC verification status |
| `language` | string | No | - | Filter by language (e.g., `Hindi`, `Tamil`, `Malayalam`) |

### Supported Languages
- Hindi
- English
- Tamil
- Telugu
- Malayalam
- Kannada
- Bengali
- Marathi

---

## Request Examples

### Basic Request (All Creators)
```bash
curl -X GET "https://your-domain.com/api/v1/users/females" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json"
```

### Filter by Online Creators
```bash
curl -X GET "https://your-domain.com/api/v1/users/females?online=true" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Filter by Language
```bash
curl -X GET "https://your-domain.com/api/v1/users/females?language=Malayalam" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Filter by Verified Creators with Pagination
```bash
curl -X GET "https://your-domain.com/api/v1/users/females?verified=true&limit=10&page=1" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Multiple Filters
```bash
curl -X GET "https://your-domain.com/api/v1/users/females?online=true&language=Kannada&verified=true&limit=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## Response

### Success Response (200 OK)

```json
{
  "success": true,
  "data": [
    {
      "id": "USR_1730736000000",
      "name": "Ananya798",
      "age": 24,
      "gender": "FEMALE",
      "profile_image": "https://your-cdn.com/profiles/ananya.jpg",
      "bio": "D. boss all movies",
      "language": "Kannada",
      "interests": ["Travel", "Movies", "Music"],
      "is_online": true,
      "last_seen": 1730736123456,
      "rating": 4.5,
      "total_ratings": 127,
      "audio_call_enabled": true,
      "video_call_enabled": true,
      "is_verified": true,
      "audio_call_rate": 10,
      "video_call_rate": 60
    },
    {
      "id": "USR_1730736100000",
      "name": "Nandini043",
      "age": 22,
      "gender": "FEMALE",
      "profile_image": "https://your-cdn.com/profiles/nandini.jpg",
      "bio": "i like talking",
      "language": "Kannada",
      "interests": ["Music", "Dance"],
      "is_online": false,
      "last_seen": 1730735999999,
      "rating": 4.8,
      "total_ratings": 89,
      "audio_call_enabled": false,
      "video_call_enabled": true,
      "is_verified": true,
      "audio_call_rate": 10,
      "video_call_rate": 60
    },
    {
      "id": "USR_1730736200000",
      "name": "Jahnavi1107",
      "age": 26,
      "gender": "FEMALE",
      "profile_image": "https://your-cdn.com/profiles/jahnavi.jpg",
      "bio": "Art and creativity",
      "language": "Kannada",
      "interests": ["Art", "Photography", "Reading"],
      "is_online": true,
      "last_seen": 1730736120000,
      "rating": 4.2,
      "total_ratings": 45,
      "audio_call_enabled": true,
      "video_call_enabled": true,
      "is_verified": false,
      "audio_call_rate": 10,
      "video_call_rate": 60
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 5,
    "total_items": 94,
    "per_page": 20,
    "has_next": true,
    "has_prev": false
  }
}
```

### Response Fields

#### Main Response Object

| Field | Type | Description |
|-------|------|-------------|
| `success` | boolean | Indicates if the request was successful |
| `data` | array | Array of creator objects |
| `pagination` | object | Pagination information |

#### Creator Object (`data[]`)

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `id` | string | Unique user identifier | `"USR_1730736000000"` |
| `name` | string | Creator's display name | `"Ananya798"` |
| `age` | integer | Creator's age | `24` |
| `gender` | string | Always `"FEMALE"` for this endpoint | `"FEMALE"` |
| `profile_image` | string | URL to profile image/avatar | `"https://cdn.example.com/image.jpg"` |
| `bio` | string | Creator's bio/description | `"D. boss all movies"` |
| `language` | string | Creator's preferred language | `"Kannada"` |
| `interests` | array | Array of interest strings | `["Travel", "Movies"]` |
| `is_online` | boolean | Whether creator is currently online | `true` |
| `last_seen` | integer | Unix timestamp in milliseconds | `1730736123456` |
| `rating` | float | Average rating (0.0 - 5.0) | `4.5` |
| `total_ratings` | integer | Number of ratings received | `127` |
| `audio_call_enabled` | boolean | Whether audio calls are enabled | `true` |
| `video_call_enabled` | boolean | Whether video calls are enabled | `true` |
| `is_verified` | boolean | KYC verification status | `true` |
| `audio_call_rate` | integer | Cost per minute for audio calls (in coins) | `10` |
| `video_call_rate` | integer | Cost per minute for video calls (in coins) | `60` |

#### Available Interests
- Travel
- Music
- Movies
- Dance
- Art
- Photography
- Reading
- Cooking
- Sports
- Gaming
- Fashion
- Technology

#### Pagination Object

| Field | Type | Description |
|-------|------|-------------|
| `current_page` | integer | Current page number |
| `total_pages` | integer | Total number of pages |
| `total_items` | integer | Total number of creators |
| `per_page` | integer | Items per page |
| `has_next` | boolean | Whether there are more pages |
| `has_prev` | boolean | Whether there are previous pages |

---

## Error Responses

### 403 Forbidden - Not a Male User
```json
{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "Only male users can access this endpoint"
  }
}
```

### 401 Unauthorized - Missing or Invalid Token
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Unauthenticated"
  }
}
```

### 422 Validation Error - Invalid Parameters
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": {
      "limit": ["The limit must be less than or equal to 50"]
    }
  }
}
```

---

## Implementation Notes

### Filtering Logic
- **Blocked Users**: The API automatically excludes users that the requesting user has blocked
- **Active Only**: Only active female users are returned (`is_active = true`)
- **Real-time Data**: Online status and call availability are updated in real-time

### Call Rates
- `audio_call_rate` and `video_call_rate` are fetched from the app settings table
- Default values: Audio = 10 coins/min, Video = 60 coins/min (updated from 15 to 60 based on screenshot)
- Rates are consistent across all creators
- Admin can update rates from the admin panel settings

### Performance
- Results are paginated for optimal performance
- Maximum limit: 50 items per request
- Default limit: 20 items per request

### Online Status
- `is_online`: Boolean indicating current status
- `last_seen`: Timestamp in milliseconds when user was last active
- When `is_online = true`, `last_seen` reflects the most recent activity

---

## UI Display Guidelines

Based on the provided screenshot, each creator card should display:

### Card Layout
```
┌─────────────────────────────────────────┐
│  [Avatar]    CreatorName                │
│              📍 Language                 │
│              🎯 Interest                 │
│                                          │
│              Bio/Description             │
│                                          │
│  [📞 audio_call_rate/min]  [🎥 video_call_rate/min] │
└─────────────────────────────────────────┘
```

### Visual Elements
1. **Avatar**: Display `profile_image` with "Click to view" overlay
2. **Name**: Display `name` as the main heading
3. **Language**: Show with location icon + `language`
4. **Interest**: Show primary interest (first item from `interests` array)
5. **Description**: Display `bio` text
6. **Call Buttons**:
   - Audio call button: Show `audio_call_rate` coins/min
   - Video call button: Show `video_call_rate` coins/min
   - Disable button if corresponding `*_call_enabled` is `false`
   - Gray out button when `is_online = false`

### Status Indicators
- **Online**: Show green indicator when `is_online = true`
- **Offline**: Show gray indicator when `is_online = false`
- **Verified**: Show verification badge when `is_verified = true`

---

## Testing with Postman

### Step 1: Import Collection
Create a new Postman collection with the following requests:

### Step 2: Set Environment Variables
```
BASE_URL: https://your-domain.com
TOKEN: Your_Bearer_Token
```

### Step 3: Test Scenarios

#### Test 1: Get All Creators
```
GET {{BASE_URL}}/api/v1/users/females
Headers:
  Authorization: Bearer {{TOKEN}}
  Content-Type: application/json
```

#### Test 2: Get Online Creators Only
```
GET {{BASE_URL}}/api/v1/users/females?online=true
```

#### Test 3: Filter by Language (Malayalam)
```
GET {{BASE_URL}}/api/v1/users/females?language=Malayalam
```

#### Test 4: Pagination Test
```
GET {{BASE_URL}}/api/v1/users/females?limit=5&page=1
GET {{BASE_URL}}/api/v1/users/females?limit=5&page=2
```

#### Test 5: Multiple Filters
```
GET {{BASE_URL}}/api/v1/users/females?online=true&language=Kannada&verified=true
```

---

## Code Integration Examples

### JavaScript (React/React Native)
```javascript
// Fetch creators for home screen
const fetchCreators = async (page = 1, filters = {}) => {
  try {
    const params = new URLSearchParams({
      limit: 20,
      page,
      ...filters
    });
    
    const response = await fetch(
      `${API_BASE_URL}/api/v1/users/females?${params}`,
      {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    const data = await response.json();
    
    if (data.success) {
      return {
        creators: data.data,
        pagination: data.pagination
      };
    } else {
      throw new Error(data.error.message);
    }
  } catch (error) {
    console.error('Error fetching creators:', error);
    throw error;
  }
};

// Usage
fetchCreators(1, { online: true, language: 'Malayalam' })
  .then(({ creators, pagination }) => {
    console.log('Creators:', creators);
    console.log('Total pages:', pagination.total_pages);
  });
```

### React Native Component Example
```jsx
import React, { useEffect, useState } from 'react';
import { FlatList, View, Text, Image, TouchableOpacity } from 'react-native';

const HomeScreen = () => {
  const [creators, setCreators] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  
  useEffect(() => {
    loadCreators();
  }, [page]);
  
  const loadCreators = async () => {
    try {
      const { creators, pagination } = await fetchCreators(page);
      setCreators(prevCreators => 
        page === 1 ? creators : [...prevCreators, ...creators]
      );
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };
  
  const renderCreatorCard = ({ item }) => (
    <View style={styles.card}>
      <Image source={{ uri: item.profile_image }} style={styles.avatar} />
      <Text style={styles.name}>{item.name}</Text>
      <Text style={styles.language}>📍 {item.language}</Text>
      {item.interests.length > 0 && (
        <Text style={styles.interest}>🎯 {item.interests[0]}</Text>
      )}
      <Text style={styles.bio}>{item.bio}</Text>
      
      <View style={styles.callButtons}>
        <TouchableOpacity 
          style={[styles.button, !item.audio_call_enabled && styles.disabled]}
          disabled={!item.audio_call_enabled || !item.is_online}
        >
          <Text>📞 {item.audio_call_rate}/min</Text>
        </TouchableOpacity>
        
        <TouchableOpacity 
          style={[styles.button, !item.video_call_enabled && styles.disabled]}
          disabled={!item.video_call_enabled || !item.is_online}
        >
          <Text>🎥 {item.video_call_rate}/min</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
  
  return (
    <FlatList
      data={creators}
      renderItem={renderCreatorCard}
      keyExtractor={item => item.id}
      onEndReached={() => setPage(page + 1)}
      onEndReachedThreshold={0.5}
      refreshing={loading}
      onRefresh={() => {
        setPage(1);
        loadCreators();
      }}
    />
  );
};
```

### Swift (iOS)
```swift
struct Creator: Codable {
    let id: String
    let name: String
    let age: Int
    let profileImage: String
    let bio: String
    let language: String
    let interests: [String]
    let isOnline: Bool
    let rating: Double
    let audioCallRate: Int
    let videoCallRate: Int
    let audioCallEnabled: Bool
    let videoCallEnabled: Bool
    
    enum CodingKeys: String, CodingKey {
        case id, name, age, bio, language, interests, rating
        case profileImage = "profile_image"
        case isOnline = "is_online"
        case audioCallRate = "audio_call_rate"
        case videoCallRate = "video_call_rate"
        case audioCallEnabled = "audio_call_enabled"
        case videoCallEnabled = "video_call_enabled"
    }
}

func fetchCreators(page: Int = 1, online: Bool? = nil) async throws -> [Creator] {
    var urlComponents = URLComponents(string: "\(baseURL)/api/v1/users/females")!
    var queryItems = [URLQueryItem(name: "page", value: "\(page)")]
    
    if let online = online {
        queryItems.append(URLQueryItem(name: "online", value: "\(online)"))
    }
    
    urlComponents.queryItems = queryItems
    
    var request = URLRequest(url: urlComponents.url!)
    request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
    
    let (data, _) = try await URLSession.shared.data(for: request)
    let response = try JSONDecoder().decode(CreatorResponse.self, from: data)
    
    return response.data
}
```

### Kotlin (Android)
```kotlin
data class Creator(
    val id: String,
    val name: String,
    val age: Int,
    @SerializedName("profile_image") val profileImage: String,
    val bio: String,
    val language: String,
    val interests: List<String>,
    @SerializedName("is_online") val isOnline: Boolean,
    val rating: Float,
    @SerializedName("audio_call_rate") val audioCallRate: Int,
    @SerializedName("video_call_rate") val videoCallRate: Int,
    @SerializedName("audio_call_enabled") val audioCallEnabled: Boolean,
    @SerializedName("video_call_enabled") val videoCallEnabled: Boolean
)

suspend fun fetchCreators(page: Int = 1, online: Boolean? = null): List<Creator> {
    val url = "$BASE_URL/api/v1/users/females".toHttpUrl().newBuilder().apply {
        addQueryParameter("page", page.toString())
        online?.let { addQueryParameter("online", it.toString()) }
    }.build()
    
    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $accessToken")
        .build()
    
    return withContext(Dispatchers.IO) {
        val response = client.newCall(request).execute()
        val json = response.body?.string()
        val creatorResponse = gson.fromJson(json, CreatorResponse::class.java)
        creatorResponse.data
    }
}
```

---

## Frequently Asked Questions (FAQ)

### Q1: Why don't I see some creators in the list?
**A:** Creators you have blocked are automatically filtered out from the results.

### Q2: Can female users call this endpoint?
**A:** No, this endpoint is restricted to male users only. Female users will receive a 403 error.

### Q3: How often is the online status updated?
**A:** Online status is updated in real-time. When users open/close the app or change their status manually.

### Q4: What's the difference between `is_verified` and `is_active`?
**A:** 
- `is_verified`: Indicates KYC verification status (shows verification badge)
- `is_active`: Internal flag (inactive users are automatically filtered out)

### Q5: Can call rates differ between creators?
**A:** Currently, all creators have the same call rates as defined in app settings. Future versions may support per-creator pricing.

### Q6: What happens when I reach the last page?
**A:** When `has_next` is `false`, you've reached the end of results. No more data is available.

### Q7: How do I implement infinite scroll?
**A:** Keep track of the current page and increment it when the user reaches the bottom of the list. Check `has_next` before loading more.

---

## Changelog

### v1.0.0 (Current)
- Initial release
- Basic creator listing with pagination
- Filters: online, language, verified
- Call rates included in response
- Automatic blocked user filtering

---

## Support

For technical support or questions:
- Email: support@himaapp.com
- Developer Portal: https://developers.himaapp.com
- API Status: https://status.himaapp.com

---

## Related Endpoints

- **GET** `/api/v1/users/{userId}` - Get detailed creator profile
- **POST** `/api/v1/calls/initiate` - Initiate a call with a creator
- **POST** `/api/v1/users/{userId}/block` - Block a creator
- **GET** `/api/v1/settings/app` - Get current app settings (including call rates)







