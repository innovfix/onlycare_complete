package com.onlycare.app.agora

import android.content.Context
import android.util.Log
import com.onlycare.app.utils.AgoraConfig
import com.onlycare.app.utils.NetworkDiagnostics
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Agora RTC Engine Manager
 * Handles all Agora SDK operations for audio and video calls
 */
@Singleton
class AgoraManager @Inject constructor(@ApplicationContext private val context: Context) {
    
    private var rtcEngine: RtcEngine? = null
    private var isInitialized = false
    
    // Callbacks
    private var onUserJoined: ((uid: Int) -> Unit)? = null
    private var onUserOffline: ((uid: Int) -> Unit)? = null
    private var onError: ((errorCode: Int) -> Unit)? = null
    
    /**
     * Initialize Agora RTC Engine
     * @param appId Agora App ID (from backend or config)
     * @param eventListener Optional event listener for Agora callbacks
     */
    fun initialize(appId: String, eventListener: AgoraEventListener? = null): Boolean {
        if (isInitialized) {
            Log.w(TAG, "Agora already initialized")
            return true
        }
        
        try {
            Log.d(TAG, "🔄 Starting Agora initialization...")
            Log.d(TAG, "📱 Context: ${context.javaClass.simpleName}")
            Log.d(TAG, "🔑 App ID: $appId")
            
            if (appId.isEmpty()) {
                Log.e(TAG, "❌ Invalid App ID!")
                return false
            }
            
            // Use the context directly, not applicationContext (which can be null in some cases)
            val appContext = context.applicationContext ?: context
            
            val config = RtcEngineConfig().apply {
                mContext = appContext
                mAppId = appId
                mEventHandler = createEventHandler(eventListener)
            }
            
            Log.d(TAG, "🔨 Creating RTC Engine...")
            rtcEngine = RtcEngine.create(config)
            
            if (rtcEngine == null) {
                Log.e(TAG, "❌ RTC Engine is null after creation!")
                return false
            }
            
            // Set channel profile to COMMUNICATION for 1-to-1 calls (like hima)
            rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            
            // DON'T enable video here - let each call type enable what it needs
            // This matches hima's approach
            Log.d(TAG, "ℹ️ Video/Audio will be enabled per call type (matching hima)")
            
            isInitialized = true
            
            Log.i(TAG, "✅ Agora RTC Engine initialized successfully (hima-style)")
            Log.i(TAG, "🎯 Engine instance: ${rtcEngine?.javaClass?.simpleName}")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize Agora: ${e.message}", e)
            Log.e(TAG, "❌ Exception class: ${e.javaClass.name}")
            e.printStackTrace()
            onError?.invoke(-1)
            return false
        }
    }
    
    /**
     * Join an audio call channel
     */
    fun joinAudioChannel(
        token: String,
        channelName: String,
        uid: Int = 0,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        if (!isInitialized || rtcEngine == null) {
            onFailure("Agora not initialized")
            return
        }
        
        try {
            Log.d(TAG, "\n🔍 PRE-JOIN NETWORK CHECK:")
            val networkType = NetworkDiagnostics.getNetworkTypeString(context)
            Log.d(TAG, "  📶 Network Type: $networkType")
            
            if (networkType == "NONE") {
                Log.e(TAG, "  ❌ NO NETWORK CONNECTION!")
                onFailure("No network connection")
                return
            }
            
            // MATCH HIMA: Only enable audio for audio calls
            Log.d(TAG, "🎤 Enabling AUDIO only (matching hima)")
            rtcEngine?.enableAudio()
            
            // Unmute audio by default
            rtcEngine?.muteLocalAudioStream(false)
            
            // CRITICAL: Enable speaker for audio calls (MAXIMUM VOLUME)
            rtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
            rtcEngine?.setEnableSpeakerphone(true)
            
            // Adjust recording volume (150 for louder mic input)
            rtcEngine?.adjustRecordingSignalVolume(150)
            // Adjust playback volume (150 for louder speaker output)
            rtcEngine?.adjustPlaybackSignalVolume(150)
            
            // Set audio profile for call (HIGH QUALITY + COMMUNICATION optimized)
            rtcEngine?.setAudioProfile(
                Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY,
                Constants.AUDIO_SCENARIO_GAME_STREAMING
            )
            
            // Enable echo cancellation and noise suppression
            rtcEngine?.setParameters("{\"che.audio.enable.agc\":true}")
            rtcEngine?.setParameters("{\"che.audio.enable.aec\":true}")
            rtcEngine?.setParameters("{\"che.audio.enable.ns\":true}")
            
            Log.d(TAG, "🎤 Audio unmuted by default")
            Log.d(TAG, "🔊 Speaker enabled for audio call (MAXIMUM)")
            Log.d(TAG, "📢 Volume set to 150% (mic + speaker)")
            Log.d(TAG, "🎚️ Echo cancellation + noise suppression enabled")
            
            // Join channel
            val options = ChannelMediaOptions().apply {
                channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                autoSubscribeAudio = true
                autoSubscribeVideo = false
                publishMicrophoneTrack = true
                publishCameraTrack = false
            }
            
            // Use null for token if project is in "Unsecure" mode (no App Certificate enabled)
            val finalToken = if (token.isEmpty()) null else token
            
            Log.d(TAG, "📝 JOIN CHANNEL DETAILS:")
            Log.d(TAG, "  - Channel: $channelName")
            Log.d(TAG, "  - Token (raw): ${token.take(30)}... (length: ${token.length})")
            Log.d(TAG, "  - Token (final): ${if (finalToken == null) "NULL" else finalToken.take(30) + "..."}")
            Log.d(TAG, "  - UID: $uid")
            Log.d(TAG, "  - Network: $networkType")
            
            val result = rtcEngine?.joinChannel(finalToken, channelName, uid, options)
            
            Log.d(TAG, "📊 Join channel result code: $result")
            Log.d(TAG, "   0 = Success, <0 = Error")
            
            if (result == 0) {
                Log.i(TAG, "✅ Joining audio channel: $channelName")
                Log.i(TAG, "   ⏳ Waiting for Agora server connection...")
                Log.i(TAG, "   📡 If Error 110 occurs, check network diagnostics above")
                onSuccess()
            } else {
                Log.e(TAG, "❌ Failed to join channel, error code: $result")
                onFailure("Failed to join channel: $result")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error joining audio channel: ${e.message}", e)
            onFailure(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Join a video call channel
     */
    fun joinVideoChannel(
        token: String,
        channelName: String,
        uid: Int = 0,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        if (!isInitialized || rtcEngine == null) {
            onFailure("Agora not initialized")
            return
        }
        
        try {
            // Enable BOTH audio and video for video calls (matching hima)
            Log.d(TAG, "🎤📹 Enabling AUDIO + VIDEO for video call")
            rtcEngine?.enableAudio()
            rtcEngine?.enableVideo()
            
            // Set video encoder configuration
            rtcEngine?.setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
                )
            )
            
            // CRITICAL: Enable speaker for video calls (MAXIMUM VOLUME)
            rtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
            rtcEngine?.setEnableSpeakerphone(true)
            
            // Adjust recording volume (150 for louder mic input)
            rtcEngine?.adjustRecordingSignalVolume(150)
            // Adjust playback volume (150 for louder speaker output)
            rtcEngine?.adjustPlaybackSignalVolume(150)
            
            // Set audio profile for video calls (HIGH QUALITY + COMMUNICATION optimized)
            rtcEngine?.setAudioProfile(
                Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY,
                Constants.AUDIO_SCENARIO_GAME_STREAMING
            )
            
            // Enable echo cancellation and noise suppression
            rtcEngine?.setParameters("{\"che.audio.enable.agc\":true}")
            rtcEngine?.setParameters("{\"che.audio.enable.aec\":true}")
            rtcEngine?.setParameters("{\"che.audio.enable.ns\":true}")
            
            // NOTE: Don't call startPreview() here yet!
            // It will be called from setupLocalVideo() after the SurfaceView is created
            
            // Unmute by default
            rtcEngine?.muteLocalAudioStream(false)
            rtcEngine?.muteLocalVideoStream(false)
            
            Log.d(TAG, "📹 Video and audio ready for channel")
            Log.d(TAG, "🎤 Audio unmuted by default")
            Log.d(TAG, "🔊 Speaker enabled for video call (MAXIMUM)")
            Log.d(TAG, "📢 Volume set to 150% (mic + speaker)")
            Log.d(TAG, "🎚️ Echo cancellation + noise suppression enabled")
            Log.d(TAG, "📷 Video unmuted by default")
            Log.d(TAG, "⏳ Waiting for SurfaceView to be created before starting preview...")
            
            // Join channel
            val options = ChannelMediaOptions().apply {
                channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                autoSubscribeAudio = true
                autoSubscribeVideo = true
                publishMicrophoneTrack = true
                publishCameraTrack = true
            }
            
            // Use null for token if project is in "Unsecure" mode (no App Certificate enabled)
            val finalToken = if (token.isEmpty()) null else token
            
            Log.d(TAG, "📝 JOIN VIDEO CHANNEL DETAILS:")
            Log.d(TAG, "  - Channel: $channelName")
            Log.d(TAG, "  - Token (raw): ${token.take(30)}... (length: ${token.length})")
            Log.d(TAG, "  - Token (final): ${if (finalToken == null) "NULL" else finalToken.take(30) + "..."}")
            Log.d(TAG, "  - UID: $uid")
            
            val result = rtcEngine?.joinChannel(finalToken, channelName, uid, options)
            
            Log.d(TAG, "📊 Join video channel result code: $result")
            
            if (result == 0) {
                Log.i(TAG, "✅ Joining video channel: $channelName")
                onSuccess()
            } else {
                Log.e(TAG, "❌ Failed to join channel, error code: $result")
                onFailure("Failed to join channel: $result")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error joining video channel: ${e.message}", e)
            onFailure(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Leave channel and cleanup
     */
    fun leaveChannel() {
        try {
            rtcEngine?.leaveChannel()
            Log.i(TAG, "✅ Left Agora channel")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error leaving channel: ${e.message}", e)
        }
    }
    
    /**
     * Mute/unmute local audio
     */
    fun muteLocalAudio(mute: Boolean) {
        rtcEngine?.muteLocalAudioStream(mute)
        Log.d(TAG, if (mute) "🔇 Audio muted" else "🔊 Audio unmuted")
    }
    
    /**
     * Enable/disable speaker
     */
    fun enableSpeaker(enable: Boolean) {
        rtcEngine?.setEnableSpeakerphone(enable)
        Log.d(TAG, if (enable) "🔊 Speaker enabled" else "📱 Earpiece enabled")
    }
    
    /**
     * Mute/unmute local video
     */
    fun muteLocalVideo(mute: Boolean) {
        rtcEngine?.muteLocalVideoStream(mute)
        Log.d(TAG, if (mute) "📷 Video muted" else "📹 Video unmuted")
    }
    
    /**
     * Switch camera (front/back)
     */
    fun switchCamera() {
        rtcEngine?.switchCamera()
        Log.d(TAG, "🔄 Camera switched")
    }
    
    /**
     * Setup local video view
     */
    fun setupLocalVideo(view: android.view.SurfaceView) {
        try {
            Log.d(TAG, "🎥 Setting up local video...")
            Log.d(TAG, "  - SurfaceView: ${view.javaClass.simpleName}")
            Log.d(TAG, "  - RTC Engine: ${if (rtcEngine != null) "Ready" else "NULL"}")
            
            val videoCanvas = VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0)
            rtcEngine?.setupLocalVideo(videoCanvas)
            Log.d(TAG, "✅ Local video canvas configured")
            
            val previewResult = rtcEngine?.startPreview()
            Log.d(TAG, "✅ Camera preview started (result: $previewResult)")
            
            Log.i(TAG, "✅ Local video setup complete - CAMERA SHOULD BE ACTIVE NOW!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setting up local video: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    /**
     * Setup remote video view
     */
    fun setupRemoteVideo(view: android.view.SurfaceView, uid: Int) {
        try {
            rtcEngine?.setupRemoteVideo(VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            Log.i(TAG, "✅ Remote video setup for uid: $uid")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setting up remote video: ${e.message}", e)
        }
    }
    
    /**
     * Destroy engine and release resources
     */
    fun destroy() {
        try {
            rtcEngine?.leaveChannel()
            rtcEngine?.stopPreview()
            RtcEngine.destroy()
            rtcEngine = null
            isInitialized = false
            Log.i(TAG, "✅ Agora engine destroyed")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error destroying engine: ${e.message}", e)
        }
    }
    
    /**
     * Create event handler for Agora callbacks
     */
    private fun createEventHandler(eventListener: AgoraEventListener?) = object : IRtcEngineEventHandler() {
        
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.i(TAG, "✅ onJoinChannelSuccess: channel=$channel, uid=$uid")
            eventListener?.onJoinChannelSuccess(channel ?: "", uid)
        }
        
        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.i(TAG, "👤 onUserJoined: uid=$uid")
            this@AgoraManager.onUserJoined?.invoke(uid)
            eventListener?.onUserJoined(uid)
        }
        
        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(TAG, "👋 onUserOffline: uid=$uid, reason=$reason")
            this@AgoraManager.onUserOffline?.invoke(uid)
            eventListener?.onUserOffline(uid, reason)
        }
        
        override fun onError(err: Int) {
            val errorMsg = when (err) {
                110 -> "ERR_OPEN_CHANNEL_TIMEOUT (110): Connection timeout - Receiver might be OFFLINE or UNAVAILABLE"
                101 -> "ERR_INVALID_CHANNEL_NAME (101): Channel name is empty or invalid"
                2 -> "ERR_INVALID_ARGUMENT (2): Invalid parameters passed to Agora SDK"
                17 -> "ERR_JOIN_CHANNEL_REJECTED (17): Cannot join channel - Receiver may be busy or unavailable"
                109 -> "ERR_INVALID_TOKEN (109): Token is invalid or expired"
                3 -> "ERR_NOT_READY (3): SDK not ready - Check permissions"
                18 -> "ERR_LEAVE_CHANNEL_REJECTED (18): Failed to leave channel"
                19 -> "ERR_ALREADY_IN_USE (19): Resource already in use"
                20 -> "ERR_ABORT (20): SDK aborted the operation"
                1001 -> "ERR_LOAD_MEDIA_ENGINE (1001): Failed to load media engine"
                1002 -> "ERR_START_CALL (1002): Failed to start call after loaded"
                1003 -> "ERR_START_CAMERA (1003): Failed to start camera"
                1004 -> "ERR_START_VIDEO_RENDER (1004): Failed to start video rendering"
                1005 -> "ERR_ADM_GENERAL_ERROR (1005): General error from audio device module"
                else -> "Agora Error Code: $err (Check Agora docs for details)"
            }
            Log.e(TAG, "❌ onError: $errorMsg")
            Log.e(TAG, "  📱 Context: ${context.javaClass.simpleName}")
            
            // Provide specific troubleshooting for common errors
            when (err) {
                110 -> {
                    Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.e(TAG, "🚨 ERROR 110: AGORA CONNECTION TIMEOUT")
                    Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.e(TAG, "  💡 This error means Agora SDK cannot connect to Agora servers")
                    Log.e(TAG, "  ⏱️ Connection attempt timed out")
                    Log.e(TAG, "")
                    
                    // Run immediate network diagnostics
                    Log.e(TAG, "  🔍 RUNNING NETWORK DIAGNOSTICS...")
                    NetworkDiagnostics.performFullDiagnostics(context)
                    
                    // Get network type
                    val networkType = NetworkDiagnostics.getNetworkTypeString(context)
                    Log.e(TAG, "")
                    Log.e(TAG, "  📶 Current Network: $networkType")
                    
                    // Test Agora connectivity asynchronously
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = NetworkDiagnostics.testAgoraConnectivity()
                        if (!result.isFullyReachable()) {
                            Log.e(TAG, "")
                            Log.e(TAG, "  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            Log.e(TAG, "  🚫 ROOT CAUSE IDENTIFIED:")
                            Log.e(TAG, "  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            Log.e(TAG, "  ❌ AGORA SERVERS ARE NOT REACHABLE")
                            Log.e(TAG, "  ")
                            Log.e(TAG, "  🔥 CONFIRMED: Network/Firewall is BLOCKING Agora!")
                            Log.e(TAG, "  ")
                            Log.e(TAG, "  💡 IMMEDIATE SOLUTIONS:")
                            if (networkType == "WiFi") {
                                Log.e(TAG, "  1. ⚡ QUICK FIX: Turn OFF WiFi, use Mobile Data (4G/5G)")
                                Log.e(TAG, "  2. Configure your WiFi router to allow Agora")
                                Log.e(TAG, "  3. Use a VPN to bypass the firewall")
                            } else {
                                Log.e(TAG, "  1. Check if VPN is active (disable it)")
                                Log.e(TAG, "  2. Contact your ISP about VoIP blocking")
                                Log.e(TAG, "  3. Try different network location")
                            }
                            Log.e(TAG, "  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        }
                    }
                    
                    Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                }
                101 -> {
                    Log.e(TAG, "  💡 Troubleshooting Error 101:")
                    Log.e(TAG, "     • Channel name must not be empty")
                    Log.e(TAG, "     • Check API response for valid channel name")
                }
                109 -> {
                    Log.e(TAG, "  💡 Troubleshooting Error 109:")
                    Log.e(TAG, "     • Token may be expired or invalid")
                    Log.e(TAG, "     • Check backend token generation")
                    Log.e(TAG, "     • Verify App Certificate matches")
                    Log.e(TAG, "     • Token must be generated for UID = 0")
                }
                3 -> {
                    Log.e(TAG, "  💡 Troubleshooting Error 3:")
                    Log.e(TAG, "     • Check camera/microphone permissions")
                    Log.e(TAG, "     • Ensure SDK is initialized before joining")
                }
                1005 -> {
                    Log.e(TAG, "  💡 Troubleshooting Error 1005:")
                    Log.e(TAG, "     • Audio device error - check permissions")
                    Log.e(TAG, "     • Another app may be using microphone")
                    Log.e(TAG, "     • Try closing other audio apps")
                }
            }
            
            this@AgoraManager.onError?.invoke(err)
            eventListener?.onError(err)
        }
        
        override fun onConnectionLost() {
            Log.w(TAG, "⚠️ onConnectionLost")
            eventListener?.onConnectionLost()
        }
        
        override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
            // Log only poor quality
            if (txQuality >= Constants.QUALITY_POOR || rxQuality >= Constants.QUALITY_POOR) {
                Log.w(TAG, "⚠️ Poor network quality: tx=$txQuality, rx=$rxQuality")
            }
            eventListener?.onNetworkQuality(uid, txQuality, rxQuality)
        }
        
        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            Log.d(TAG, "📹 Remote video state changed: uid=$uid, state=$state")
            eventListener?.onRemoteVideoStateChanged(uid, state, reason)
        }
        
        override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            Log.d(TAG, "🔊 Remote audio state changed: uid=$uid, state=$state")
            eventListener?.onRemoteAudioStateChanged(uid, state, reason)
        }
    }
    
    /**
     * Set callbacks for user events
     */
    fun setCallbacks(
        onUserJoined: ((uid: Int) -> Unit)? = null,
        onUserOffline: ((uid: Int) -> Unit)? = null,
        onError: ((errorCode: Int) -> Unit)? = null
    ) {
        this.onUserJoined = onUserJoined
        this.onUserOffline = onUserOffline
        this.onError = onError
    }
    
    companion object {
        private const val TAG = "AgoraManager"
    }
}

/**
 * Event listener interface for Agora events
 */
interface AgoraEventListener {
    fun onJoinChannelSuccess(channel: String, uid: Int) {}
    fun onUserJoined(uid: Int) {}
    fun onUserOffline(uid: Int, reason: Int) {}
    fun onError(errorCode: Int) {}
    fun onConnectionLost() {}
    fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {}
    fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int) {}
    fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int) {}
}
