require('dotenv').config();
const express = require('express');
const http = require('http');
const socketIO = require('socket.io');
const cors = require('cors');
const axios = require('axios');

const app = express();
const server = http.createServer(app);

// Socket.io configuration
const io = socketIO(server, {
    cors: {
        origin: "*", // In production, specify your app's domain
        methods: ["GET", "POST"]
    },
    pingTimeout: 60000,
    pingInterval: 25000
});

app.use(cors());
app.use(express.json());

// ========================================
// DATA STORES
// ========================================

// Store connected users: userId -> socketId
const connectedUsers = new Map();

// Store active calls: callId -> { callerId, receiverId, status, channelName }
const activeCalls = new Map();

// ========================================
// HELPER FUNCTIONS
// ========================================

/**
 * Verify user token with Laravel backend
 */
async function verifyUserToken(token) {
    try {
        // Try both endpoints - Laravel API might use /api/v1/users/me
        const endpoints = [
            `${process.env.LARAVEL_API_URL}/api/v1/users/me`,
            `${process.env.LARAVEL_API_URL}/users/me`
        ];
        
        for (const endpoint of endpoints) {
            try {
                console.log(`[websocket_check] Verifying token with: ${endpoint}`);
                const response = await axios.get(endpoint, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Accept': 'application/json'
                    },
                    timeout: 5000
                });
                
                if (response.data.success && response.data.data) {
                    console.log(`[websocket_check] ✅ Token verified successfully via ${endpoint}`);
                    return response.data.data; // Returns user object
                }
            } catch (endpointError) {
                console.log(`[websocket_check] ⚠️ Endpoint ${endpoint} failed: ${endpointError.message}`);
                // Try next endpoint
                continue;
            }
        }
        
        console.error('[websocket_check] ❌ Token verification failed: All endpoints failed');
        return null;
    } catch (error) {
        console.error('[websocket_check] ❌ Token verification exception:', error.message);
        if (error.response) {
            console.error('[websocket_check]   Status:', error.response.status);
            console.error('[websocket_check]   Data:', error.response.data);
        }
        return null;
    }
}

/**
 * Notify Laravel about WebSocket events (optional logging)
 */
async function notifyLaravel(endpoint, data) {
    try {
        await axios.post(`${process.env.LARAVEL_API_URL}${endpoint}`, data, {
            headers: {
                'X-Internal-Secret': process.env.LARAVEL_API_SECRET,
                'Content-Type': 'application/json'
            }
        });
    } catch (error) {
        console.error('Failed to notify Laravel:', error.message);
    }
}

// ========================================
// SOCKET.IO MIDDLEWARE
// ========================================

io.use(async (socket, next) => {
    try {
        console.log('========================================');
        console.log('[websocket_check] New connection attempt');
        console.log('========================================');
        
        const token = socket.handshake.auth.token;
        const userId = socket.handshake.auth.userId;
        
        console.log('[websocket_check] Auth data received:');
        console.log('  Token: ' + (token ? `${token.substring(0, 20)}...` : 'MISSING'));
        console.log('  User ID: ' + (userId || 'MISSING'));
        
        if (!token || !userId) {
            console.log('[websocket_check] ❌ Authentication required - missing token or userId');
            return next(new Error('Authentication required'));
        }
        
        // Verify token with Laravel
        console.log('[websocket_check] Verifying token with Laravel...');
        const user = await verifyUserToken(token);
        
        if (!user) {
            console.log('[websocket_check] ❌ Token verification failed - user is null');
            return next(new Error('Invalid token'));
        }
        
        console.log('[websocket_check] Token verified - User:', user.id, user.name);
        
        if (user.id !== userId) {
            console.log('[websocket_check] ❌ User ID mismatch:');
            console.log('  Expected:', userId);
            console.log('  Got:', user.id);
            return next(new Error('Invalid token'));
        }
        
        // Attach user data to socket
        socket.userId = userId;
        socket.userName = user.name;
        socket.userGender = user.gender;
        
        console.log('[websocket_check] ✅ Authentication successful');
        console.log('========================================');
        
        next();
    } catch (error) {
        console.error('[websocket_check] ❌ Authentication exception:', error.message);
        console.error('  Stack:', error.stack);
        next(new Error('Authentication failed'));
    }
});

// ========================================
// SOCKET.IO EVENT HANDLERS
// ========================================

io.on('connection', (socket) => {
    console.log(`✅ User connected: ${socket.userId} (${socket.userName})`);
    
    // Register user
    connectedUsers.set(socket.userId, socket.id);
    
    // Broadcast online status to friends (optional)
    socket.broadcast.emit('user:online', {
        userId: socket.userId,
        timestamp: Date.now()
    });
    
    // =====================================
    // EVENT: call:initiate
    // =====================================
    socket.on('call:initiate', async (data, callback) => {
        try {
            const { receiverId, callId, callType, channelName, agoraToken } = data;
            
            console.log('🔍 ========================================');
            console.log('🔍 call:initiate received');
            console.log('🔍 Caller userId:', socket.userId);
            console.log('🔍 Receiver userId:', receiverId);
            console.log('🔍 CallId:', callId);
            console.log('🔍 CallType:', callType);
            console.log('🔍 ChannelName:', channelName);
            console.log(`📞 Call initiated: ${socket.userId} → ${receiverId} (Type: ${callType})`);
            
            // Check if receiver is connected
            const receiverSocketId = connectedUsers.get(receiverId);
            console.log('🔍 Receiver connected?', receiverSocketId ? 'YES ✅' : 'NO ❌');
            
            if (!receiverSocketId) {
                // Receiver not connected - fallback to FCM
                console.log('⚠️ Receiver offline - NOT adding to activeCalls');
                console.log('🔍 ========================================');
                callback({ 
                    success: false, 
                    error: 'User offline - FCM notification sent',
                    useFcmFallback: true 
                });
                return;
            }
            
            // Check if receiver is already in a call
            const isReceiverBusy = Array.from(activeCalls.values())
                .some(call => 
                    (call.callerId === receiverId || call.receiverId === receiverId) &&
                    call.status === 'ringing'
                );
            
            console.log('🔍 Receiver busy?', isReceiverBusy ? 'YES' : 'NO');
            
            if (isReceiverBusy) {
                console.log('⚠️ Receiver busy - NOT adding to activeCalls');
                console.log('🔍 ========================================');
                callback({ 
                    success: false, 
                    error: 'User is busy',
                    busy: true 
                });
                
                // Notify caller immediately
                socket.emit('call:busy', { callId });
                return;
            }
            
            // Store call info
            console.log('✅ Adding call to activeCalls Map...');
            console.log('🔍 Before: activeCalls size =', activeCalls.size);
            
            activeCalls.set(callId, {
                callId,
                callerId: socket.userId,
                callerName: socket.userName,
                receiverId,
                callType,
                channelName,
                status: 'ringing',
                startTime: Date.now()
            });
            
            console.log('🔍 After: activeCalls size =', activeCalls.size);
            console.log('🔍 Stored with key:', callId);
            console.log('🔍 All keys in activeCalls:', Array.from(activeCalls.keys()));
            console.log('✅ Call added to activeCalls successfully');
            
            // Send to receiver INSTANTLY via WebSocket
            io.to(receiverSocketId).emit('call:incoming', {
                callId,
                callerId: socket.userId,
                callerName: socket.userName,
                callType,
                channelName,
                agoraToken, // Receiver needs this to join
                timestamp: Date.now()
            });
            
            console.log(`✅ Call signal sent to receiver: ${receiverId}`);
            console.log('🔍 ========================================');
            
            // Send success response to caller
            callback({
                success: true,
                callId,
                message: 'Call initiated successfully'
            });
            
            // Auto-timeout after 30 seconds
            setTimeout(() => {
                const call = activeCalls.get(callId);
                if (call && call.status === 'ringing') {
                    // Call not answered - notify both parties
                    const callerSocketId = connectedUsers.get(call.callerId);
                    
                    if (callerSocketId) {
                        io.to(callerSocketId).emit('call:timeout', {
                            callId,
                            reason: 'No answer'
                        });
                    }
                    
                    if (receiverSocketId) {
                        io.to(receiverSocketId).emit('call:cancelled', {
                            callId,
                            reason: 'Timeout'
                        });
                    }
                    
                    activeCalls.delete(callId);
                    console.log(`⏱️ Call ${callId} timed out`);
                }
            }, 30000);
            
        } catch (error) {
            console.error('Error in call:initiate:', error);
            callback({ success: false, error: 'Server error' });
        }
    });
    
    // =====================================
    // EVENT: call:accept ⭐ CRITICAL
    // =====================================
    socket.on('call:accept', (data) => {
        try {
            console.log('========================================');
            console.log('[websocket_check] Received call:accept event');
            console.log('========================================');
            console.log('Socket user ID:', socket.userId);
            console.log('Raw data:', JSON.stringify(data, null, 2));
            
            const { callId } = data;
            console.log('Call ID:', callId);
            console.log('Active calls count:', activeCalls.size);
            console.log('Active call IDs:', Array.from(activeCalls.keys()));
            
            const call = activeCalls.get(callId);
            
            if (!call) {
                console.log(`❌ [websocket_check] Call ${callId} not found in activeCalls`);
                console.log('   This might be normal if call was accepted via REST API');
                console.log('========================================');
                return;
            }
            
            console.log(`✅ [websocket_check] Call found in activeCalls: ${callId}`);
            console.log('   Caller ID:', call.callerId);
            console.log('   Receiver ID:', call.receiverId);
            
            // Update call status
            call.status = 'accepted';
            call.acceptedAt = Date.now();
            activeCalls.set(callId, call);
            
            // Notify caller INSTANTLY
            const callerSocketId = connectedUsers.get(call.callerId);
            console.log('   Caller socket ID:', callerSocketId || 'NOT FOUND');
            console.log('   Total connected users:', connectedUsers.size);
            
            if (callerSocketId) {
                const eventData = {
                    callId,
                    timestamp: Date.now()
                };
                
                io.to(callerSocketId).emit('call:accepted', eventData);
                
                console.log(`✅ [websocket_check] Emitted call:accepted to caller`);
                console.log('   Caller ID:', call.callerId);
                console.log('   Socket ID:', callerSocketId);
                console.log('   Event data:', JSON.stringify(eventData, null, 2));
                console.log('========================================');
            } else {
                console.log(`⚠️ [websocket_check] Caller ${call.callerId} NOT connected`);
                console.log('   Available user IDs:', Array.from(connectedUsers.keys()));
                console.log('   FCM will handle notification');
                console.log('========================================');
            }
            
        } catch (error) {
            console.error('❌ [websocket_check] Error in call:accept:', error);
            console.error('   Stack:', error.stack);
        }
    });
    
    // =====================================
    // EVENT: call:reject ⭐⭐ MOST CRITICAL
    // =====================================
    socket.on('call:reject', async (data) => {
        try {
            const { callId, reason } = data;
            
            console.log('📥 Received call:reject from receiver');
            console.log('   Call ID:', callId);
            console.log('   Reason:', reason);
            
            // 1. Update database via Laravel API
            try {
                const apiCallId = callId.startsWith('CALL_') ? callId.replace('CALL_', '') : callId;
                const apiUrl = `${process.env.LARAVEL_API_URL}/api/v1/calls/${apiCallId}/reject`;
                
                console.log('🔄 Updating database via Laravel API...');
                
                // Use user's token from socket for authentication
                const token = socket.handshake.auth.token;
                
                await axios.post(apiUrl, {
                    reason: reason || 'User declined'
                }, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    }
                });
                
                console.log('✅ Database updated: call status = REJECTED');
            } catch (error) {
                console.error('⚠️ Failed to update database:', error.response?.data || error.message);
                // Continue even if database update fails - WebSocket notification is critical
            }
            
            // 🔍 ENHANCED DEBUG LOGGING
            console.log('🔍 ========================================');
            console.log('🔍 call:reject processing');
            console.log('🔍 Received callId:', callId);
            console.log('🔍 Received reason:', reason);
            console.log('🔍 Receiver userId:', socket.userId);
            console.log('🔍 activeCalls size:', activeCalls.size);
            console.log('🔍 activeCalls keys:', Array.from(activeCalls.keys()));
            console.log('🔍 Looking for call:', callId);
            
            let call = activeCalls.get(callId);
            
            // 🔍 TRY ALTERNATE FORMAT if not found
            if (!call) {
                const alternateId = callId.startsWith('CALL_') 
                    ? callId.replace('CALL_', '') 
                    : 'CALL_' + callId;
                
                console.log('🔍 Trying alternate format:', alternateId);
                call = activeCalls.get(alternateId);
                
                if (call) {
                    console.log('✅ Found call with alternate format!');
                    console.log('⚠️ FORMAT MISMATCH DETECTED:');
                    console.log(`   Android sent: "${callId}"`);
                    console.log(`   Backend stored: "${alternateId}"`);
                }
            }
            
            console.log('🔍 Call found?', call ? 'YES ✅' : 'NO ❌');
            
            if (!call) {
                console.log(`❌ Call ${callId} not found in activeCalls`);
                console.log('🔍 This is why caller is NOT being notified!');
                console.log('🔍 Possible reasons:');
                console.log('   1. Call never added to activeCalls (call:initiate not triggered)');
                console.log('   2. CallId format mismatch (e.g., "CALL_123" vs "123")');
                console.log('   3. Call already removed (duplicate rejection or timeout)');
                console.log('   4. Race condition: Rejection happened before call:initiate WebSocket event');
                console.log('');
                console.log('🔍 Attempting database fallback...');
                
                // ✅ FALLBACK: Query Laravel database via API
                try {
                    // Use the Laravel API endpoint: GET /api/v1/calls/{callId}
                    const apiCallId = callId.startsWith('CALL_') ? callId.replace('CALL_', '') : callId;
                    const apiUrl = `${process.env.LARAVEL_API_URL}/api/v1/calls/${apiCallId}`;
                    
                    console.log('🔍 Fetching from Laravel API:', apiUrl);
                    
                    // Use user's token for authentication
                    const token = socket.handshake.auth.token;
                    
                    const response = await axios.get(apiUrl, {
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Accept': 'application/json'
                        }
                    });
                    
                    if (response.data.success && response.data.data) {
                        const dbCall = response.data.data;
                        console.log('✅ Found call in database!');
                        console.log('🔍 Call details from DB:', {
                            id: dbCall.id,
                            caller_id: dbCall.caller_id,
                            receiver_id: dbCall.receiver_id,
                            status: dbCall.status,
                            call_type: dbCall.call_type
                        });
                        
                        // Reconstruct call object from database
                        call = {
                            callId: dbCall.id,
                            callerId: dbCall.caller_id,
                            receiverId: dbCall.receiver_id,
                            callType: dbCall.call_type,
                            status: dbCall.status
                        };
                        
                        console.log('✅ Using database fallback to notify caller');
                    } else {
                        console.log('❌ Call not found in database either');
                        console.log('🔍 ========================================');
                        return;
                    }
                } catch (error) {
                    console.error('❌ Database fallback failed:', error.response?.data || error.message);
                    console.log('🔍 ========================================');
                    return;
                }
            }
            
            console.log('🔍 Call details:', {
                callerId: call.callerId,
                receiverId: call.receiverId,
                status: call.status,
                startTime: call.startTime ? new Date(call.startTime).toISOString() : 'N/A'
            });
            console.log('🔍 ========================================');
            
            // 2. Get the call to find the caller
            const callerId = call.callerId;
            
            if (!callerId) {
                console.error('❌ Caller ID not found in call object');
                return;
            }
            
            console.log(`❌ Call rejected: ${callId} - Reason: ${reason || 'User declined'}`);
            
            // 3. ⚡ SEND call:rejected TO THE CALLER (MALE)
            // This is the CRITICAL part!
            const callerSocketId = connectedUsers.get(callerId);
            
            if (callerSocketId) {
                io.to(callerSocketId).emit('call:rejected', {
                    callId: callId,
                    reason: reason || 'User declined',
                    timestamp: Date.now()
                });
                
                console.log('✅ Sent call:rejected to caller:', callerId);
            } else {
                console.log(`⚠️ Caller ${callerId} is NOT CONNECTED - cannot notify via WebSocket`);
            }
            
            // Remove call from active calls
            activeCalls.delete(callId);
            console.log(`🗑️ Removed call ${callId} from activeCalls (size now: ${activeCalls.size})`);
            
            // Send confirmation to receiver
            socket.emit('call:reject:confirmed', { callId });
            
        } catch (error) {
            console.error('Error in call:reject:', error);
        }
    });
    
    // =====================================
    // EVENT: call:cancel (Caller cancels before receiver accepts)
    // =====================================
    socket.on('call:cancel', async (data) => {
        try {
            const { callId, reason } = data;
            
            console.log('🔍 ========================================');
            console.log('🔍 call:cancel received');
            console.log('🔍 Caller userId:', socket.userId);
            console.log('🔍 CallId:', callId);
            console.log('🔍 Reason:', reason || 'Caller ended call');
            
            // Find the call in active calls
            let call = activeCalls.get(callId);
            
            // Try alternate format if not found
            if (!call) {
                const alternateId = callId.startsWith('CALL_') 
                    ? callId.replace('CALL_', '') 
                    : 'CALL_' + callId;
                call = activeCalls.get(alternateId);
            }
            
            if (!call) {
                console.log(`❌ Call ${callId} not found in activeCalls`);
                // Try database fallback
                try {
                    const apiCallId = callId.startsWith('CALL_') ? callId.replace('CALL_', '') : callId;
                    const apiUrl = `${process.env.LARAVEL_API_URL}/api/v1/calls/${apiCallId}`;
                    
                    const response = await axios.get(apiUrl, {
                        headers: { 'Accept': 'application/json' }
                    });
                    
                    if (response.data.success && response.data.data) {
                        const dbCall = response.data.data;
                        call = {
                            callId: dbCall.id,
                            callerId: dbCall.caller_id,
                            receiverId: dbCall.receiver_id,
                            callType: dbCall.call_type,
                            status: dbCall.status
                        };
                        console.log('✅ Found call in database fallback');
                    } else {
                        console.log('❌ Call not found in database');
                        return;
                    }
                } catch (error) {
                    console.error('❌ Database fallback failed:', error.message);
                    return;
                }
            }
            
            // Verify caller is the one canceling
            if (call.callerId !== socket.userId) {
                console.log(`❌ Unauthorized: User ${socket.userId} cannot cancel call ${callId}`);
                return;
            }
            
            console.log(`✅ Call cancelled: ${callId} by caller ${socket.userId}`);
            console.log(`📤 Receiver ID: ${call.receiverId}`);
            
            // 1. Update database via Laravel API
            try {
                const apiCallId = callId.startsWith('CALL_') ? callId.replace('CALL_', '') : callId;
                await axios.post(
                    `${process.env.LARAVEL_API_URL}/api/v1/calls/${apiCallId}/cancel`,
                    { reason: reason || 'Caller ended call' },
                    {
                        headers: {
                            'X-Internal-Secret': process.env.LARAVEL_API_SECRET,
                            'Content-Type': 'application/json'
                        }
                    }
                );
                console.log('✅ Database updated: Call status = CANCELLED');
            } catch (error) {
                console.error('❌ Failed to update database:', error.response?.data || error.message);
                // Continue anyway - notify receiver even if DB update fails
            }
            
            // 2. ⚡ CRITICAL: Send WebSocket event to receiver (INSTANT - <100ms)
            const receiverSocketId = connectedUsers.get(call.receiverId);
            console.log(`🔍 Receiver socket lookup: ${call.receiverId} → ${receiverSocketId ? 'FOUND ✅' : 'NOT FOUND ❌'}`);
            
            if (receiverSocketId) {
                const cancelledEvent = {
                    callId: callId,
                    reason: reason || 'Caller ended call',
                    timestamp: Date.now()
                };
                
                io.to(receiverSocketId).emit('call:cancelled', cancelledEvent);
                console.log(`✅ ✅ ✅ EMITTED call:cancelled to receiver ${call.receiverId}`);
                console.log(`   Socket ID: ${receiverSocketId}`);
                console.log(`   Event data:`, JSON.stringify(cancelledEvent));
            } else {
                console.log(`⚠️ Receiver ${call.receiverId} not connected - will use FCM`);
                console.log(`   Connected users:`, Array.from(connectedUsers.keys()));
            }
            
            // 3. Send FCM notification to receiver (backup)
            try {
                await axios.post(
                    `${process.env.LARAVEL_API_URL}/api/v1/calls/notify-cancelled`,
                    {
                        receiverId: call.receiverId,
                        callerId: call.callerId,
                        callId: callId,
                        reason: reason || 'Caller ended call'
                    },
                    {
                        headers: {
                            'X-Internal-Secret': process.env.LARAVEL_API_SECRET,
                            'Content-Type': 'application/json'
                        }
                    }
                );
                console.log('✅ FCM notification sent to receiver');
            } catch (error) {
                console.error('❌ FCM notification failed:', error.message);
            }
            
            // 4. Clean up active calls
            activeCalls.delete(callId);
            if (call.callId && call.callId !== callId) {
                activeCalls.delete(call.callId);
            }
            
            console.log('🔍 ========================================');
            
        } catch (error) {
            console.error('❌ Error in call:cancel handler:', error);
        }
    });
    
    // =====================================
    // EVENT: call:end
    // =====================================
    socket.on('call:end', (data) => {
        try {
            const { callId } = data;
            const call = activeCalls.get(callId);
            
            if (!call) return;
            
            console.log(`📴 Call ended: ${callId}`);
            
            // Determine other user
            const otherUserId = call.callerId === socket.userId 
                ? call.receiverId 
                : call.callerId;
            
            const otherSocketId = connectedUsers.get(otherUserId);
            
            // Notify other user
            if (otherSocketId) {
                io.to(otherSocketId).emit('call:ended', {
                    callId,
                    endedBy: socket.userId,
                    reason: 'Remote user ended call',
                    timestamp: Date.now()
                });
            }
            
            // Remove call
            activeCalls.delete(callId);
            
        } catch (error) {
            console.error('Error in call:end:', error);
        }
    });
    
    // =====================================
    // EVENT: disconnect
    // =====================================
    socket.on('disconnect', () => {
        console.log(`❌ User disconnected: ${socket.userId}`);
        
        // Remove from connected users
        connectedUsers.delete(socket.userId);
        
        // End any active calls
        activeCalls.forEach((call, callId) => {
            if (call.callerId === socket.userId || call.receiverId === socket.userId) {
                // Notify other party
                const otherUserId = call.callerId === socket.userId 
                    ? call.receiverId 
                    : call.callerId;
                
                const otherSocketId = connectedUsers.get(otherUserId);
                if (otherSocketId) {
                    io.to(otherSocketId).emit('call:ended', {
                        callId,
                        reason: 'User disconnected',
                        timestamp: Date.now()
                    });
                }
                
                activeCalls.delete(callId);
            }
        });
        
        // Broadcast offline status
        socket.broadcast.emit('user:offline', {
            userId: socket.userId,
            timestamp: Date.now()
        });
    });
});

// ========================================
// HTTP ENDPOINTS (For Laravel Integration)
// ========================================

// Health check
app.get('/health', (req, res) => {
    res.json({
        status: 'OK',
        connectedUsers: connectedUsers.size,
        activeCalls: activeCalls.size,
        timestamp: Date.now()
    });
});

// Check if user is online
app.get('/api/users/:userId/online', (req, res) => {
    const { userId } = req.params;
    const isOnline = connectedUsers.has(userId);
    
    res.json({
        userId,
        isOnline,
        socketId: isOnline ? connectedUsers.get(userId) : null
    });
});

// Get all connected users
app.get('/api/connected-users', (req, res) => {
    const users = Array.from(connectedUsers.keys());
    res.json({
        count: users.length,
        users
    });
});

// Emit call:cancelled event via HTTP (called by Laravel)
app.post('/api/emit/call-cancelled', async (req, res) => {
    try {
        const { receiverId, callId, reason } = req.body;
        const secret = req.headers['x-internal-secret'];
        
        // Verify internal secret
        if (secret !== process.env.LARAVEL_API_SECRET) {
            return res.status(401).json({ error: 'Unauthorized' });
        }
        
        // Get receiver's socket ID
        const receiverSocketId = connectedUsers.get(receiverId);
        
        if (receiverSocketId) {
            io.to(receiverSocketId).emit('call:cancelled', {
                callId,
                reason: reason || 'Caller ended call',
                timestamp: Date.now()
            });
            
            console.log(`✅ Emitted call:cancelled to receiver ${receiverId} via HTTP`);
            res.json({ success: true, emitted: true });
        } else {
            console.log(`⚠️ Receiver ${receiverId} not connected`);
            res.json({ success: true, emitted: false, reason: 'User not connected' });
        }
    } catch (error) {
        console.error('❌ Error emitting call:cancelled:', error);
        res.status(500).json({ error: error.message });
    }
});

// Emit call:accepted event via HTTP (called by Laravel when female accepts)
app.post('/api/emit/call-accepted', async (req, res) => {
    try {
        const { callerId, callId, receiverId } = req.body;
        const secret = req.headers['x-internal-secret'];
        
        console.log('========================================');
        console.log('[websocket_check] Laravel requested call:accepted notification');
        console.log('========================================');
        console.log('Caller ID:', callerId);
        console.log('Call ID:', callId);
        console.log('Receiver ID:', receiverId);
        console.log('Secret provided:', secret ? 'YES' : 'NO');
        console.log('Total connected users:', connectedUsers.size);
        console.log('Connected user IDs:', Array.from(connectedUsers.keys()));
        
        // Verify internal secret
        if (secret !== process.env.LARAVEL_API_SECRET) {
            console.log('❌ [websocket_check] Unauthorized - Secret mismatch');
            return res.status(401).json({ error: 'Unauthorized' });
        }
        
        // Get caller's socket ID (male user who needs to be notified)
        const callerSocketId = connectedUsers.get(callerId);
        
        console.log('Caller socket ID:', callerSocketId || 'NOT FOUND');
        
        if (callerSocketId) {
            const eventData = {
                callId,
                receiverId,
                timestamp: Date.now()
            };
            
            io.to(callerSocketId).emit('call:accepted', eventData);
            
            console.log('✅ [websocket_check] Emitted call:accepted to caller');
            console.log('   Event data:', JSON.stringify(eventData, null, 2));
            console.log('   Socket ID:', callerSocketId);
            console.log('========================================');
            
            res.json({ success: true, emitted: true });
        } else {
            console.log('⚠️ [websocket_check] Caller NOT connected to WebSocket');
            console.log('   Caller ID:', callerId);
            console.log('   Available user IDs:', Array.from(connectedUsers.keys()));
            console.log('   FCM will handle notification instead');
            console.log('========================================');
            
            res.json({ success: true, emitted: false, reason: 'Caller not connected' });
        }
    } catch (error) {
        console.error('❌ [websocket_check] Error emitting call:accepted:', error);
        console.error('   Stack:', error.stack);
        res.status(500).json({ error: error.message });
    }
});

// Start server
const PORT = process.env.PORT || 3001;
server.listen(PORT, () => {
    console.log(`🚀 Socket.io server running on port ${PORT}`);
    console.log(`📡 Environment: ${process.env.NODE_ENV}`);
    console.log(`🔗 Laravel API: ${process.env.LARAVEL_API_URL}`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('SIGTERM received, closing server...');
    server.close(() => {
        console.log('Server closed');
        process.exit(0);
    });
});





