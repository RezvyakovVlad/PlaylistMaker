package com.example.playlistmaker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    fun searchDebounce(query: String) {
        _searchState.value = SearchState.Loading
    }

    fun clearSearch() {
        _searchState.value = SearchState.Empty
    }
}

sealed class SearchState {
    object Loading : SearchState()
    data class Success(val tracks: List<Track>) : SearchState()
    data class Error(val message: String) : SearchState()
    object Empty : SearchState()
    object History : SearchState()
}