package com.onlycare.app.presentation.screens.auth

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class VoiceIdentificationState(
    val isRecording: Boolean = false,
    val recordingComplete: Boolean = false,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val errorMessage: String? = null,
    val recordingDuration: Long = 0,
    val voiceFilePath: String? = null,
    val currentRecordingTime: Long = 0 // Current recording time in milliseconds
)

@HiltViewModel
class VoiceIdentificationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ApiDataRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        private const val TAG = "VoiceIdentificationVM"
        private const val MIN_RECORDING_DURATION_MS = 5000L // 5 seconds minimum
        private const val MAX_RECORDING_DURATION_MS = 30000L // 30 seconds maximum
    }

    private val _state = MutableStateFlow(VoiceIdentificationState())
    val state: StateFlow<VoiceIdentificationState> = _state.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var recordingStartTime: Long = 0
    private var voiceFile: File? = null
    private var recordingTimerJob: kotlinx.coroutines.Job? = null

    fun getTamilVoicePromptOrNull(): String? {
        // Show a single Tamil sentence during recording for Tamil language users.
        if (sessionManager.getLanguage() != com.onlycare.app.domain.model.Language.TAMIL) return null

        val prompts = listOf(
            "வணக்கம், இன்று எப்படி இருக்கிறீர்கள்?",
            "இன்று நாள் நல்லா தொடங்கியிருக்கிறது.",
            "சற்று நிதானமாக பேசினால் மனசு அமைதியாகும்.",
            "எனக்கு பேசுவது பிடிக்கும்.",
            "நான் கவனமாக கேட்பவள்.",
            "அமைதியான வார்த்தைகள் நல்ல உணர்வு தரும்.",
            "நான் என் எண்ணங்களை தெளிவாக சொல்கிறேன்.",
            "தேவையில்லாத கோபம் வேண்டாம்.",
            "எல்லாமே நேரத்தோடு சரியாகும்.",
            "சின்ன விஷயங்களிலேயே சந்தோஷம் கிடைக்கும்."
        )

        val uid = sessionManager.getUserId().ifBlank { "tamil" }
        val idx = kotlin.math.abs(uid.hashCode()) % prompts.size
        return prompts[idx]
    }

    fun startRecording() {
        if (_state.value.isRecording) return
        try {
            Log.d(TAG, "Starting voice recording...")
            
            // Create a temporary file for the recording
            val recordingsDir = File(context.cacheDir, "voice_recordings")
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs()
            }
            
            voiceFile = File(recordingsDir, "voice_${System.currentTimeMillis()}.m4a")
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000) // 128 kbps
                setAudioSamplingRate(44100) // 44.1 kHz
                setOutputFile(voiceFile?.absolutePath)
                setMaxDuration(MAX_RECORDING_DURATION_MS.toInt())
                
                setOnInfoListener { _, what, _ ->
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        Log.d(TAG, "Max recording duration reached")
                        stopRecording()
                    }
                }
                
                prepare()
                start()
                
                recordingStartTime = System.currentTimeMillis()
                _state.update { it.copy(
                    isRecording = true,
                    recordingComplete = false,
                    errorMessage = null,
                    currentRecordingTime = 0
                ) }
                
                // Start timer to update recording duration
                startRecordingTimer()
                
                Log.d(TAG, "Recording started successfully")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            _state.update { it.copy(
                isRecording = false,
                errorMessage = "Failed to start recording: ${e.message}"
            ) }
            cleanupRecorder()
        }
    }

    fun stopRecording() {
        try {
            Log.d(TAG, "Stopping voice recording...")
            
            // Stop the timer
            recordingTimerJob?.cancel()
            recordingTimerJob = null
            
            val duration = System.currentTimeMillis() - recordingStartTime
            
            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping MediaRecorder", e)
                }
            }
            mediaRecorder = null
            
            // Check if recording meets minimum duration
            if (duration < MIN_RECORDING_DURATION_MS) {
                _state.update { it.copy(
                    isRecording = false,
                    recordingComplete = false,
                    currentRecordingTime = 0,
                    errorMessage = "Recording too short. Please record at least 5 seconds."
                ) }
                // Delete the short recording
                voiceFile?.delete()
                voiceFile = null
            } else {
                _state.update { it.copy(
                    isRecording = false,
                    recordingComplete = true,
                    recordingDuration = duration,
                    voiceFilePath = voiceFile?.absolutePath,
                    currentRecordingTime = 0,
                    errorMessage = null
                ) }
                Log.d(TAG, "Recording stopped successfully. Duration: ${duration}ms, File size: ${voiceFile?.length() ?: 0} bytes")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            _state.update { it.copy(
                isRecording = false,
                currentRecordingTime = 0,
                errorMessage = "Failed to stop recording: ${e.message}"
            ) }
            cleanupRecorder()
        }
    }

    fun resetRecording() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        cleanupRecorder()
        voiceFile?.delete()
        voiceFile = null
        _state.update { VoiceIdentificationState() }
    }
    
    private fun startRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(100) // Update every 100ms
                val elapsed = System.currentTimeMillis() - recordingStartTime
                _state.update { it.copy(currentRecordingTime = elapsed) }
                
                // Log every second
                if (elapsed % 1000 < 100) {
                    Log.d(TAG, "Recording... ${elapsed / 1000}s")
                }
            }
        }
    }

    fun uploadVoice(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (voiceFile == null || !voiceFile!!.exists()) {
            Log.e(TAG, "Upload failed: No voice recording found")
            onFailure("No voice recording found")
            return
        }

        // DEBUG OTP support: if we logged in via dummy OTP ("000000"), we have a debug token.
        // Backend will reject authenticated upload. In this case, bypass upload and continue.
        val authToken = sessionManager.getAuthToken().orEmpty()
        if (authToken.startsWith("debug_token_")) {
            Log.w(TAG, "DEBUG OTP session detected - bypassing voice upload")
            // Mark verified locally for demo/testing flow
            sessionManager.updateUserProfile(
                voice = voiceFile?.absolutePath ?: "",
                isVerified = true
            )
            // Clean up the temporary file
            voiceFile?.delete()
            voiceFile = null
            _state.update { it.copy(isUploading = false, uploadSuccess = true) }
            onSuccess()
            return
        }
        
        _state.update { it.copy(isUploading = true, errorMessage = null) }
        
        Log.d(TAG, "═══════════════════════════════════════════════════════════")
        Log.d(TAG, "🎤 VOICE UPLOAD INITIATED")
        Log.d(TAG, "═══════════════════════════════════════════════════════════")
        Log.d(TAG, "File Details:")
        Log.d(TAG, "  - File Path: ${voiceFile!!.absolutePath}")
        Log.d(TAG, "  - File Name: ${voiceFile!!.name}")
        Log.d(TAG, "  - File Size: ${voiceFile!!.length()} bytes (${voiceFile!!.length() / 1024}KB)")
        Log.d(TAG, "  - Recording Duration: ${state.value.recordingDuration / 1000}s")
        Log.d(TAG, "  - Format: M4A (AAC encoded)")
        Log.d(TAG, "")
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Calling repository.updateVoice()...")
                
                repository.updateVoice(voiceFile!!).onSuccess { response ->
                    Log.d(TAG, "")
                    Log.d(TAG, "═══════════════════════════════════════════════════════════")
                    Log.d(TAG, "✅ VOICE UPLOAD SUCCESSFUL")
                    Log.d(TAG, "═══════════════════════════════════════════════════════════")
                    Log.d(TAG, "Response Details:")
                    Log.d(TAG, "  - Success: ${response.success}")
                    Log.d(TAG, "  - Message: ${response.message}")
                    Log.d(TAG, "  - Voice URL: ${response.data.voice}")
                    Log.d(TAG, "  - Is Verified: ${response.data.isVerified}")
                    Log.d(TAG, "  - Voice Gender: ${response.data.voiceGender}")
                    Log.d(TAG, "  - Audio Status: ${response.data.audioStatus}")
                    Log.d(TAG, "  - Video Status: ${response.data.videoStatus}")
                    Log.d(TAG, "═══════════════════════════════════════════════════════════")
                    
                    _state.update { it.copy(
                        isUploading = false,
                        uploadSuccess = true
                    ) }
                    
                    // Clean up the temporary file
                    voiceFile?.delete()
                    Log.d(TAG, "Temporary voice file deleted")
                    
                    onSuccess()
                    
                }.onFailure { error ->
                    Log.e(TAG, "")
                    Log.e(TAG, "═══════════════════════════════════════════════════════════")
                    Log.e(TAG, "❌ VOICE UPLOAD FAILED")
                    Log.e(TAG, "═══════════════════════════════════════════════════════════")
                    Log.e(TAG, "Error Details:")
                    Log.e(TAG, "  - Error Type: ${error.javaClass.simpleName}")
                    Log.e(TAG, "  - Error Message: ${error.message}")
                    Log.e(TAG, "  - Stack Trace:", error)
                    Log.e(TAG, "═══════════════════════════════════════════════════════════")
                    
                    _state.update { it.copy(
                        isUploading = false,
                        errorMessage = error.message ?: "Upload failed"
                    ) }
                    
                    onFailure(error.message ?: "Upload failed")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "")
                Log.e(TAG, "═══════════════════════════════════════════════════════════")
                Log.e(TAG, "❌ EXCEPTION DURING VOICE UPLOAD")
                Log.e(TAG, "═══════════════════════════════════════════════════════════")
                Log.e(TAG, "Exception Type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Exception Message: ${e.message}")
                Log.e(TAG, "Stack Trace:", e)
                Log.e(TAG, "═══════════════════════════════════════════════════════════")
                
                _state.update { it.copy(
                    isUploading = false,
                    errorMessage = e.message ?: "Upload error"
                ) }
                
                onFailure(e.message ?: "Upload error")
            }
        }
    }

    private fun cleanupRecorder() {
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    // Ignore if already stopped
                }
                release()
            }
            mediaRecorder = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up recorder", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        recordingTimerJob?.cancel()
        cleanupRecorder()
        // Don't delete the file here in case upload is pending
    }
}
