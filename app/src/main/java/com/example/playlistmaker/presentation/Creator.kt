package com.example.playlistmaker.presentation

import android.content.Context
import com.example.playlistmaker.data.network.NetworkClient
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.domain.interactor.ManageSearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.ManageSettingsInteractor
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.repository.SettingsRepository
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.presentation.search.SearchViewModel

object Creator {

    private var networkClient: NetworkClient? = null
    private var trackRepository: TrackRepository? = null
    private var searchHistoryRepository: SearchHistoryRepository? = null
    private var settingsRepository: SettingsRepository? = null

    fun provideNetworkClient(context: Context): NetworkClient {
        return networkClient ?: NetworkClient(context).also { networkClient = it }
    }

    fun provideTrackRepository(context: Context): TrackRepository {
        return trackRepository ?: TrackRepositoryImpl(
            provideNetworkClient(context)
        ).also { trackRepository = it }
    }

    fun provideSearchHistoryRepository(context: Context): SearchHistoryRepository {
        return searchHistoryRepository ?: SearchHistoryRepositoryImpl(
            context.getSharedPreferences("playlist_maker", Context.MODE_PRIVATE)
        ).also { searchHistoryRepository = it }
    }

    fun provideSettingsRepository(context: Context): SettingsRepository {
        return settingsRepository ?: SettingsRepositoryImpl(
            context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        ).also { settingsRepository = it }
    }

    fun provideSearchTracksInteractor(context: Context): SearchTracksInteractor {
        return SearchTracksInteractor(
            provideTrackRepository(context)
        )
    }

    fun provideManageSearchHistoryInteractor(context: Context): ManageSearchHistoryInteractor {
        return ManageSearchHistoryInteractor(
            provideSearchHistoryRepository(context)
        )
    }

    fun provideManageSettingsInteractor(context: Context): ManageSettingsInteractor {
        return ManageSettingsInteractor(
            provideSettingsRepository(context)
        )
    }

    fun provideSearchViewModel(context: Context): SearchViewModel {
        return SearchViewModel(
            provideSearchTracksInteractor(context)
        )
    }
}
