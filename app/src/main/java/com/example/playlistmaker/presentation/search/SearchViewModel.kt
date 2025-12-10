package com.example.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchTracksInteractor
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    fun searchDebounce(query: String) {
        if (query.length < 2) {
            _searchState.value = SearchState.Empty
            return
        }

        _searchState.value = SearchState.Loading

        viewModelScope.launch {
            try {
                val tracks = searchInteractor.execute(query)
                if (tracks.isEmpty()) {
                    _searchState.value = SearchState.Empty
                } else {
                    _searchState.value = SearchState.Success(tracks)
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Unknown error")
            }
        }
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
