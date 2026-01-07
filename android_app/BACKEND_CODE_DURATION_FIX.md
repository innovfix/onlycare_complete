# Backend Code - Duration Calculation Fix

## 🎯 GOAL
Fix backend to calculate call duration correctly (only talk time, not ringing time)

---

## 📋 CHANGES REQUIRED

### 1. Database Migration - Add `receiver_joined_at` Column
### 2. Update `/calls/:callId/accept` Endpoint - Set timestamp when receiver picks up
### 3. Update `/calls/:callId/end` Endpoint - Calculate duration correctly
### 4. Add Validation - Compare client vs server duration

---

## 💾 DATABASE MIGRATION

### SQL Migration

```sql
-- Migration: Add receiver_joined_at timestamp to calls table
-- This tracks when the receiver actually picks up the call

ALTER TABLE calls 
ADD COLUMN receiver_joined_at TIMESTAMP NULL;

-- Add index for performance
CREATE INDEX idx_calls_receiver_joined_at ON calls(receiver_joined_at);

-- Add comment
COMMENT ON COLUMN calls.receiver_joined_at IS 'Timestamp when receiver accepted/joined the call';
```

### Updated Calls Table Schema

```sql
CREATE TABLE calls (
    id VARCHAR(36) PRIMARY KEY,
    caller_id VARCHAR(36) NOT NULL,
    receiver_id VARCHAR(36) NOT NULL,
    call_type VARCHAR(10) NOT NULL,  -- 'AUDIO' or 'VIDEO'
    status VARCHAR(20) NOT NULL,      -- 'PENDING', 'ONGOING', 'ENDED', etc.
    
    -- Agora Configuration
    agora_app_id VARCHAR(100),
    agora_token TEXT,
    channel_name VARCHAR(100),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,              -- When caller initiated call
    receiver_joined_at TIMESTAMP NULL,      -- ⭐ NEW: When receiver picked up
    ended_at TIMESTAMP NULL,                -- When call ended
    
    -- Billing
    duration INT DEFAULT 0,                 -- in seconds (talk time only)
    coins_spent INT DEFAULT 0,              -- for caller
    coins_earned INT DEFAULT 0,             -- for receiver
    
    -- Rating
    rating FLOAT NULL,
    feedback TEXT NULL,
    
    FOREIGN KEY (caller_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);
```

---

## 🔧 CODE EXAMPLES

### Example 1: Node.js/Express + PostgreSQL

#### Accept Call Endpoint

```javascript
// POST /calls/:callId/accept
async function acceptCall(req, res) {
    const { callId } = req.params;
    const userId = req.user.id; // From auth middleware
    
    try {
        // Get the call
        const call = await db.query(
            'SELECT * FROM calls WHERE id = $1',
            [callId]
        );
        
        if (!call.rows[0]) {
            return res.status(404).json({
                success: false,
                message: 'Call not found'
            });
        }
        
        const callData = call.rows[0];
        
        // Verify user is the receiver
        if (callData.receiver_id !== userId) {
            return res.status(403).json({
                success: false,
                message: 'Not authorized to accept this call'
            });
        }
        
        // Verify call is in PENDING state
        if (callData.status !== 'PENDING') {
            return res.status(400).json({
                success: false,
                message: `Cannot accept call in ${callData.status} state`
            });
        }
        
        // ⭐ KEY CHANGE: Set receiver_joined_at timestamp
        const receiverJoinedAt = new Date();
        
        // Update call status and set receiver_joined_at
        await db.query(
            `UPDATE calls 
             SET status = $1, 
                 receiver_joined_at = $2,
                 updated_at = $3
             WHERE id = $4`,
            ['ONGOING', receiverJoinedAt, new Date(), callId]
        );
        
        console.log(`✅ Call ${callId} accepted at ${receiverJoinedAt.toISOString()}`);
        
        // Send WebSocket notification to caller
        await sendWebSocketEvent(callData.caller_id, {
            type: 'call_accepted',
            callId: callId,
            receiverJoinedAt: receiverJoinedAt.toISOString()
        });
        
        // Get updated call data
        const updatedCall = await db.query(
            'SELECT * FROM calls WHERE id = $1',
            [callId]
        );
        
        return res.status(200).json({
            success: true,
            message: 'Call accepted',
            data: formatCallDto(updatedCall.rows[0])
        });
        
    } catch (error) {
        console.error('Error accepting call:', error);
        return res.status(500).json({
            success: false,
            message: 'Failed to accept call',
            error: error.message
        });
    }
}
```

#### End Call Endpoint

```javascript
// POST /calls/:callId/end
async function endCall(req, res) {
    const { callId } = req.params;
    const { duration: clientDuration } = req.body; // in seconds
    const userId = req.user.id;
    
    try {
        // Get the call
        const call = await db.query(
            'SELECT * FROM calls WHERE id = $1',
            [callId]
        );
        
        if (!call.rows[0]) {
            return res.status(404).json({
                success: false,
                message: 'Call not found'
            });
        }
        
        const callData = call.rows[0];
        
        // Verify user is caller or receiver
        if (callData.caller_id !== userId && callData.receiver_id !== userId) {
            return res.status(403).json({
                success: false,
                message: 'Not authorized to end this call'
            });
        }
        
        const endedAt = new Date();
        
        // ⭐ KEY CHANGE: Calculate duration from receiver_joined_at, not started_at
        let serverDuration = 0;
        
        if (callData.receiver_joined_at) {
            // Calculate from when receiver picked up
            serverDuration = Math.floor(
                (endedAt - new Date(callData.receiver_joined_at)) / 1000
            );
            console.log(`✅ Duration calculated from receiver_joined_at: ${serverDuration}s`);
        } else {
            // Fallback: Use client duration if receiver never joined
            serverDuration = clientDuration || 0;
            console.warn(`⚠️ No receiver_joined_at timestamp, using client duration: ${serverDuration}s`);
        }
        
        // Validation: Compare client vs server duration
        if (clientDuration && Math.abs(clientDuration - serverDuration) > 30) {
            console.warn('⚠️ Duration mismatch detected:', {
                callId,
                clientDuration,
                serverDuration,
                diff: Math.abs(clientDuration - serverDuration),
                receiverJoinedAt: callData.receiver_joined_at,
                endedAt: endedAt.toISOString()
            });
            
            // Log to monitoring system
            await logDurationMismatch({
                callId,
                clientDuration,
                serverDuration,
                difference: Math.abs(clientDuration - serverDuration)
            });
        }
        
        // Use server duration for billing (more reliable)
        const finalDuration = serverDuration;
        
        // Calculate coins based on call type and duration
        const coinsPerMinute = callData.call_type === 'VIDEO' ? 10 : 6;
        const coinsSpent = Math.ceil((finalDuration / 60) * coinsPerMinute);
        
        // For receiver (female users get coins)
        const coinsEarned = Math.floor((finalDuration / 60) * (coinsPerMinute * 0.7)); // 70% to receiver
        
        console.log(`💰 Billing calculation:`, {
            duration: finalDuration,
            callType: callData.call_type,
            coinsPerMinute,
            coinsSpent,
            coinsEarned
        });
        
        // Update call record
        await db.query(
            `UPDATE calls 
             SET status = $1,
                 ended_at = $2,
                 duration = $3,
                 coins_spent = $4,
                 coins_earned = $5,
                 updated_at = $6
             WHERE id = $7`,
            [
                'ENDED',
                endedAt,
                finalDuration,
                coinsSpent,
                coinsEarned,
                new Date(),
                callId
            ]
        );
        
        // Deduct coins from caller
        await db.query(
            `UPDATE users 
             SET coin_balance = coin_balance - $1 
             WHERE id = $2`,
            [coinsSpent, callData.caller_id]
        );
        
        // Add coins to receiver (if female user)
        await db.query(
            `UPDATE users 
             SET coin_balance = coin_balance + $1,
                 total_earnings = total_earnings + $2
             WHERE id = $3`,
            [coinsEarned, coinsEarned, callData.receiver_id]
        );
        
        // Get updated user balance
        const updatedUser = await db.query(
            'SELECT coin_balance FROM users WHERE id = $1',
            [userId]
        );
        
        // Get final call data
        const finalCall = await db.query(
            'SELECT * FROM calls WHERE id = $1',
            [callId]
        );
        
        console.log(`✅ Call ${callId} ended - Duration: ${finalDuration}s, Coins: ${coinsSpent}`);
        
        // Send WebSocket notification
        const otherUserId = userId === callData.caller_id 
            ? callData.receiver_id 
            : callData.caller_id;
            
        await sendWebSocketEvent(otherUserId, {
            type: 'call_ended',
            callId: callId,
            duration: finalDuration,
            coinsSpent: coinsSpent,
            coinsEarned: coinsEarned
        });
        
        return res.status(200).json({
            success: true,
            message: 'Call ended',
            call: formatCallDto(finalCall.rows[0]),
            updated_balance: updatedUser.rows[0].coin_balance
        });
        
    } catch (error) {
        console.error('Error ending call:', error);
        return res.status(500).json({
            success: false,
            message: 'Failed to end call',
            error: error.message
        });
    }
}
```

#### Helper Functions

```javascript
/**
 * Format call data for API response
 */
function formatCallDto(call) {
    return {
        id: call.id,
        caller_id: call.caller_id,
        receiver_id: call.receiver_id,
        call_type: call.call_type,
        status: call.status,
        
        agora_app_id: call.agora_app_id,
        agora_token: call.agora_token,
        channel_name: call.channel_name,
        
        started_at: call.started_at?.toISOString(),
        receiver_joined_at: call.receiver_joined_at?.toISOString(),
        ended_at: call.ended_at?.toISOString(),
        
        duration: call.duration || 0,
        coins_spent: call.coins_spent || 0,
        coins_earned: call.coins_earned || 0,
        
        rating: call.rating,
        timestamp: call.created_at.getTime()
    };
}

/**
 * Log duration mismatch for monitoring
 */
async function logDurationMismatch(data) {
    try {
        await db.query(
            `INSERT INTO duration_mismatch_logs 
             (call_id, client_duration, server_duration, difference, created_at)
             VALUES ($1, $2, $3, $4, $5)`,
            [
                data.callId,
                data.clientDuration,
                data.serverDuration,
                data.difference,
                new Date()
            ]
        );
        
        // Also send to monitoring service (e.g., Sentry, DataDog)
        if (process.env.NODE_ENV === 'production') {
            console.error('Duration mismatch alert:', data);
            // Sentry.captureMessage('Duration mismatch', { extra: data });
        }
    } catch (error) {
        console.error('Failed to log duration mismatch:', error);
    }
}

/**
 * Send WebSocket event to user
 */
async function sendWebSocketEvent(userId, event) {
    // Implementation depends on your WebSocket setup
    // Example with Socket.io:
    const io = require('../websocket').getIO();
    io.to(`user_${userId}`).emit('call_event', event);
}
```

#### Create Monitoring Table

```sql
-- Table to track duration mismatches for investigation
CREATE TABLE duration_mismatch_logs (
    id SERIAL PRIMARY KEY,
    call_id VARCHAR(36) NOT NULL,
    client_duration INT NOT NULL,
    server_duration INT NOT NULL,
    difference INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (call_id) REFERENCES calls(id)
);

CREATE INDEX idx_duration_mismatch_call_id ON duration_mismatch_logs(call_id);
CREATE INDEX idx_duration_mismatch_created_at ON duration_mismatch_logs(created_at);
```

---

### Example 2: Python/Django

#### Models

```python
# models.py

from django.db import models
from django.utils import timezone
import math

class Call(models.Model):
    CALL_TYPES = [
        ('AUDIO', 'Audio'),
        ('VIDEO', 'Video'),
    ]
    
    STATUSES = [
        ('PENDING', 'Pending'),
        ('ONGOING', 'Ongoing'),
        ('ENDED', 'Ended'),
        ('REJECTED', 'Rejected'),
        ('MISSED', 'Missed'),
    ]
    
    id = models.CharField(max_length=36, primary_key=True)
    caller = models.ForeignKey('User', on_delete=models.CASCADE, related_name='outgoing_calls')
    receiver = models.ForeignKey('User', on_delete=models.CASCADE, related_name='incoming_calls')
    call_type = models.CharField(max_length=10, choices=CALL_TYPES)
    status = models.CharField(max_length=20, choices=STATUSES, default='PENDING')
    
    # Agora
    agora_app_id = models.CharField(max_length=100, null=True, blank=True)
    agora_token = models.TextField(null=True, blank=True)
    channel_name = models.CharField(max_length=100, null=True, blank=True)
    
    # Timestamps
    created_at = models.DateTimeField(auto_now_add=True)
    started_at = models.DateTimeField(null=True, blank=True)
    receiver_joined_at = models.DateTimeField(null=True, blank=True)  # ⭐ NEW
    ended_at = models.DateTimeField(null=True, blank=True)
    
    # Billing
    duration = models.IntegerField(default=0)  # seconds
    coins_spent = models.IntegerField(default=0)
    coins_earned = models.IntegerField(default=0)
    
    # Rating
    rating = models.FloatField(null=True, blank=True)
    feedback = models.TextField(null=True, blank=True)
    
    def calculate_duration(self):
        """Calculate duration from receiver_joined_at to ended_at"""
        if self.receiver_joined_at and self.ended_at:
            delta = self.ended_at - self.receiver_joined_at
            return int(delta.total_seconds())
        return 0
    
    def calculate_coins(self):
        """Calculate coins based on duration and call type"""
        if self.duration == 0:
            return 0, 0
            
        coins_per_minute = 10 if self.call_type == 'VIDEO' else 6
        coins_spent = math.ceil((self.duration / 60) * coins_per_minute)
        coins_earned = math.floor((self.duration / 60) * coins_per_minute * 0.7)
        
        return coins_spent, coins_earned
    
    class Meta:
        db_table = 'calls'
        ordering = ['-created_at']
```

#### Views

```python
# views.py

from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from django.utils import timezone
from .models import Call, User
from .serializers import CallSerializer
import logging

logger = logging.getLogger(__name__)

@api_view(['POST'])
@permission_classes([IsAuthenticated])
def accept_call(request, call_id):
    """Accept incoming call and set receiver_joined_at timestamp"""
    try:
        call = Call.objects.get(id=call_id)
        
        # Verify user is the receiver
        if call.receiver.id != request.user.id:
            return Response({
                'success': False,
                'message': 'Not authorized to accept this call'
            }, status=status.HTTP_403_FORBIDDEN)
        
        # Verify call is in PENDING state
        if call.status != 'PENDING':
            return Response({
                'success': False,
                'message': f'Cannot accept call in {call.status} state'
            }, status=status.HTTP_400_BAD_REQUEST)
        
        # ⭐ KEY CHANGE: Set receiver_joined_at timestamp
        call.receiver_joined_at = timezone.now()
        call.status = 'ONGOING'
        call.save()
        
        logger.info(f"✅ Call {call_id} accepted at {call.receiver_joined_at}")
        
        # Send WebSocket notification to caller
        send_websocket_event(call.caller.id, {
            'type': 'call_accepted',
            'callId': call_id,
            'receiverJoinedAt': call.receiver_joined_at.isoformat()
        })
        
        serializer = CallSerializer(call)
        return Response({
            'success': True,
            'message': 'Call accepted',
            'data': serializer.data
        })
        
    except Call.DoesNotExist:
        return Response({
            'success': False,
            'message': 'Call not found'
        }, status=status.HTTP_404_NOT_FOUND)
    except Exception as e:
        logger.error(f"Error accepting call: {str(e)}")
        return Response({
            'success': False,
            'message': 'Failed to accept call',
            'error': str(e)
        }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def end_call(request, call_id):
    """End call and calculate duration correctly"""
    try:
        call = Call.objects.get(id=call_id)
        client_duration = request.data.get('duration', 0)
        
        # Verify user is caller or receiver
        if call.caller.id != request.user.id and call.receiver.id != request.user.id:
            return Response({
                'success': False,
                'message': 'Not authorized to end this call'
            }, status=status.HTTP_403_FORBIDDEN)
        
        # Set ended_at
        call.ended_at = timezone.now()
        
        # ⭐ KEY CHANGE: Calculate duration from receiver_joined_at
        if call.receiver_joined_at:
            server_duration = call.calculate_duration()
            logger.info(f"✅ Duration calculated from receiver_joined_at: {server_duration}s")
        else:
            # Fallback to client duration
            server_duration = client_duration or 0
            logger.warning(f"⚠️ No receiver_joined_at, using client duration: {server_duration}s")
        
        # Validation: Compare client vs server
        if client_duration and abs(client_duration - server_duration) > 30:
            logger.warning(f"⚠️ Duration mismatch: client={client_duration}s, server={server_duration}s")
            log_duration_mismatch(call_id, client_duration, server_duration)
        
        # Use server duration
        call.duration = server_duration
        call.status = 'ENDED'
        
        # Calculate and apply coins
        coins_spent, coins_earned = call.calculate_coins()
        call.coins_spent = coins_spent
        call.coins_earned = coins_earned
        call.save()
        
        # Update user balances
        call.caller.coin_balance -= coins_spent
        call.caller.save()
        
        call.receiver.coin_balance += coins_earned
        call.receiver.total_earnings += coins_earned
        call.receiver.save()
        
        logger.info(f"✅ Call {call_id} ended - Duration: {server_duration}s, Coins: {coins_spent}")
        
        # Send WebSocket notification
        other_user_id = call.receiver.id if request.user.id == call.caller.id else call.caller.id
        send_websocket_event(other_user_id, {
            'type': 'call_ended',
            'callId': call_id,
            'duration': server_duration,
            'coinsSpent': coins_spent,
            'coinsEarned': coins_earned
        })
        
        serializer = CallSerializer(call)
        return Response({
            'success': True,
            'message': 'Call ended',
            'call': serializer.data,
            'updated_balance': request.user.coin_balance
        })
        
    except Call.DoesNotExist:
        return Response({
            'success': False,
            'message': 'Call not found'
        }, status=status.HTTP_404_NOT_FOUND)
    except Exception as e:
        logger.error(f"Error ending call: {str(e)}")
        return Response({
            'success': False,
            'message': 'Failed to end call',
            'error': str(e)
        }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


def log_duration_mismatch(call_id, client_duration, server_duration):
    """Log duration mismatch for monitoring"""
    from .models import DurationMismatchLog
    try:
        DurationMismatchLog.objects.create(
            call_id=call_id,
            client_duration=client_duration,
            server_duration=server_duration,
            difference=abs(client_duration - server_duration)
        )
    except Exception as e:
        logger.error(f"Failed to log duration mismatch: {str(e)}")


def send_websocket_event(user_id, event):
    """Send WebSocket event to user"""
    # Implementation depends on your WebSocket setup
    pass
```

---

## 📊 TESTING THE FIX

### Test Cases

```javascript
// Test 1: Normal Call Flow
describe('Call Duration Calculation', () => {
    
    it('should calculate duration from receiver_joined_at', async () => {
        // 1. Initiate call
        const call = await initiateCall(callerId, receiverId, 'AUDIO');
        // started_at = T0
        
        // 2. Wait 30 seconds (ringing)
        await sleep(30000);
        
        // 3. Accept call
        await acceptCall(call.id, receiverId);
        // receiver_joined_at = T0 + 30s
        
        // 4. Talk for 2 minutes
        await sleep(120000);
        
        // 5. End call
        const result = await endCall(call.id, 120); // client says 120s
        
        // Assertions
        expect(result.call.duration).toBeCloseTo(120, -1); // ~120s ±10s
        expect(result.call.coins_spent).toBe(12); // 2 min × 6 coins
        
        // Should NOT be 150s (30s ringing + 120s talk)
        expect(result.call.duration).not.toBeCloseTo(150, -1);
    });
    
    it('should handle call never answered', async () => {
        const call = await initiateCall(callerId, receiverId, 'AUDIO');
        
        // Never accept, just end after 30s
        await sleep(30000);
        await endCall(call.id, 0);
        
        // Assertions
        expect(call.receiver_joined_at).toBeNull();
        expect(call.duration).toBe(0);
        expect(call.coins_spent).toBe(0);
    });
    
    it('should detect duration mismatch', async () => {
        const call = await initiateCall(callerId, receiverId, 'AUDIO');
        await acceptCall(call.id, receiverId);
        await sleep(120000); // 2 minutes
        
        // Client reports 60s but server calculates 120s
        await endCall(call.id, 60);
        
        // Should log mismatch
        const logs = await getDurationMismatchLogs(call.id);
        expect(logs.length).toBe(1);
        expect(logs[0].client_duration).toBe(60);
        expect(logs[0].server_duration).toBeCloseTo(120, -1);
        expect(logs[0].difference).toBeCloseTo(60, -1);
    });
});
```

### Manual Testing Steps

```bash
# 1. Check current duration calculation
curl -X POST http://localhost:3000/api/calls/test-call-123/end \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"duration": 120}'

# Expected response:
{
  "success": true,
  "call": {
    "duration": 120,  // Should match client duration (if receiver_joined_at exists)
    "coins_spent": 12
  }
}

# 2. Check database
SELECT 
    id,
    started_at,
    receiver_joined_at,
    ended_at,
    duration,
    coins_spent,
    EXTRACT(EPOCH FROM (ended_at - started_at)) as total_time,
    EXTRACT(EPOCH FROM (ended_at - receiver_joined_at)) as talk_time
FROM calls
WHERE id = 'test-call-123';

# Verify:
# - duration = talk_time (not total_time)
# - receiver_joined_at is NOT NULL
# - talk_time < total_time (because of ringing)
```

---

## 📈 MONITORING & ALERTS

### Create Monitoring Dashboard

```javascript
// Get duration mismatch stats
async function getDurationMismatchStats(days = 7) {
    const result = await db.query(`
        SELECT 
            COUNT(*) as total_mismatches,
            AVG(difference) as avg_difference,
            MAX(difference) as max_difference,
            COUNT(CASE WHEN difference > 60 THEN 1 END) as severe_mismatches
        FROM duration_mismatch_logs
        WHERE created_at >= NOW() - INTERVAL '${days} days'
    `);
    
    return result.rows[0];
}

// Alert if too many mismatches
async function checkDurationMismatchAlerts() {
    const stats = await getDurationMismatchStats(1); // Last 24 hours
    
    if (stats.severe_mismatches > 10) {
        // Send alert to team
        await sendAlert({
            type: 'duration_mismatch',
            severity: 'high',
            message: `${stats.severe_mismatches} severe duration mismatches in last 24 hours`,
            stats: stats
        });
    }
}
```

---

## 🚀 DEPLOYMENT CHECKLIST

### Pre-Deployment
- [ ] Run database migration in staging
- [ ] Test accept call endpoint
- [ ] Test end call endpoint
- [ ] Verify duration calculation
- [ ] Test with existing calls (receiver_joined_at = NULL)
- [ ] Load testing with high call volume

### Deployment
- [ ] Deploy database migration
- [ ] Deploy backend code
- [ ] Monitor error logs for 24 hours
- [ ] Check duration mismatch logs
- [ ] Verify coins calculation is correct

### Post-Deployment
- [ ] Compare before/after metrics
- [ ] Review user complaints about billing
- [ ] Monitor duration_mismatch_logs table
- [ ] Generate report on improvement

---

## 📊 SUCCESS METRICS

After deployment, you should see:

1. **Duration Accuracy**
   - Before: Server duration = Client duration + 30-45s
   - After: Server duration ≈ Client duration (±5s tolerance)

2. **Billing Fairness**
   - Before: Users charged for ringing time
   - After: Users charged only for talk time

3. **Mismatch Reduction**
   - Before: High duration mismatches (>30s)
   - After: Minimal mismatches (<10s)

4. **User Satisfaction**
   - Reduced billing complaints
   - Accurate call duration display

---

## 🔗 API RESPONSE EXAMPLES

### Accept Call Response

```json
{
  "success": true,
  "message": "Call accepted",
  "data": {
    "id": "call-123",
    "status": "ONGOING",
    "receiver_joined_at": "2025-11-23T08:35:00.000Z",
    "started_at": "2025-11-23T08:34:30.000Z"
  }
}
```

### End Call Response

```json
{
  "success": true,
  "message": "Call ended",
  "call": {
    "id": "call-123",
    "status": "ENDED",
    "started_at": "2025-11-23T08:34:30.000Z",
    "receiver_joined_at": "2025-11-23T08:35:00.000Z",
    "ended_at": "2025-11-23T08:37:00.000Z",
    "duration": 120,
    "coins_spent": 12,
    "coins_earned": 8
  },
  "updated_balance": 488
}
```

---

## 🎯 SUMMARY

**Key Changes:**
1. ✅ Add `receiver_joined_at` column to calls table
2. ✅ Set timestamp when receiver accepts call
3. ✅ Calculate duration from `receiver_joined_at` to `ended_at`
4. ✅ Add validation to compare client vs server duration
5. ✅ Log mismatches for monitoring

**Result:** Fair billing based on actual talk time, not ringing time!



