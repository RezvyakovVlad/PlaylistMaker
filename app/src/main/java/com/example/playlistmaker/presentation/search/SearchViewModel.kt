package com.example.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.model.Track

class SearchViewModel : ViewModel() {  // Конструктор без параметров

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    fun searchDebounce(query: String) {
        if (query.length < 2) {
            _searchState.value = SearchState.Empty
            return
        }

        _searchState.value = SearchState.Loading

        // Временная заглушка
        _searchState.value = SearchState.Success(emptyList())
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