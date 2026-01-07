package com.onlycare.app.presentation.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Call
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.utils.getDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecentCallsState(
    val calls: List<Call> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val callAvailabilityByUserId: Map<String, CallAvailability> = emptyMap(),
    val displayNameByUserId: Map<String, String> = emptyMap(),
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val selectedFilter: String = "recent" // "recent", "talktime", "az"
)

data class CallAvailability(
    val audioEnabled: Boolean,
    val videoEnabled: Boolean
)

@HiltViewModel
class RecentCallsViewModel @Inject constructor(
    private val repository: ApiDataRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(RecentCallsState())
    val state: StateFlow<RecentCallsState> = _state.asStateFlow()

    // Avoid refetching the same users repeatedly while paginating
    private val inFlightUserIds = mutableSetOf<String>()
    
    init {
        loadCalls()
    }
    
    fun changeFilter(filter: String) {
        _state.update { it.copy(
            selectedFilter = filter,
            calls = emptyList(),
            currentPage = 1,
            hasMorePages = true
        ) }
        loadCalls(isRefresh = true)
    }
    
    fun loadCalls(isRefresh: Boolean = false) {
        if (isRefresh) {
            _state.update { it.copy(
                isLoading = true,
                error = null,
                currentPage = 1,
                calls = emptyList(),
                callAvailabilityByUserId = emptyMap()
            ) }
        } else {
            _state.update { it.copy(isLoading = true, error = null) }
        }
        
        viewModelScope.launch {
            repository.getCallHistory(
                filter = _state.value.selectedFilter,
                page = if (isRefresh) 1 else _state.value.currentPage,
                limit = 20
            ).collect { result ->
                result.onSuccess { calls ->
                    _state.update {
                        it.copy(
                            calls = if (isRefresh) calls else it.calls + calls,
                            isLoading = false,
                            isLoadingMore = false,
                            hasMorePages = calls.size >= 20 // If we got 20 items, there might be more
                        )
                    }
                    // Prefetch call availability for other users so we can disable buttons correctly
                    prefetchCallAvailability(calls)
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = error.message ?: "Failed to load calls"
                        )
                    }
                }
            }
        }
    }
    
    fun loadMoreCalls() {
        if (_state.value.isLoadingMore || !_state.value.hasMorePages) return
        
        _state.update { it.copy(
            currentPage = it.currentPage + 1,
            isLoadingMore = true
        ) }
        
        viewModelScope.launch {
            repository.getCallHistory(
                filter = _state.value.selectedFilter,
                page = _state.value.currentPage,
                limit = 20
            ).collect { result ->
                result.onSuccess { calls ->
                    _state.update {
                        it.copy(
                            calls = it.calls + calls,
                            isLoadingMore = false,
                            hasMorePages = calls.size >= 20
                        )
                    }
                    // Prefetch for newly loaded page
                    prefetchCallAvailability(calls)
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            error = error.message ?: "Failed to load more calls"
                        )
                    }
                }
            }
        }
    }
    
    fun refresh() {
        loadCalls(isRefresh = true)
    }
    
    fun getGender(): Gender {
        return sessionManager.getGender()
    }

    private fun prefetchCallAvailability(newCalls: List<Call>) {
        val userIds = newCalls.map { it.otherUserId }.filter { it.isNotBlank() }.distinct()
        if (userIds.isEmpty()) return

        val alreadyKnown = _state.value.callAvailabilityByUserId.keys
        val toFetch = userIds.filter { id ->
            !alreadyKnown.contains(id) && !inFlightUserIds.contains(id)
        }
        if (toFetch.isEmpty()) return

        viewModelScope.launch {
            toFetch.forEach { otherUserId ->
                inFlightUserIds.add(otherUserId)
                repository.getUserById(otherUserId).onSuccess { user ->
                    _state.update { current ->
                        current.copy(
                            callAvailabilityByUserId = current.callAvailabilityByUserId + (
                                otherUserId to CallAvailability(
                                    audioEnabled = user.audioCallEnabled,
                                    videoEnabled = user.videoCallEnabled
                                )
                            ),
                            // Ensure Recent screen uses the same creator display name rules everywhere.
                            displayNameByUserId = current.displayNameByUserId + (
                                otherUserId to user.getDisplayName()
                            )
                        )
                    }
                }.onFailure {
                    // If user fetch fails, we don't block calling; just leave availability unknown.
                }
                inFlightUserIds.remove(otherUserId)
            }
        }
    }
}


