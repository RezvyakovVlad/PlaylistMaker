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

    fun provideTrackRepository(context: Context): TrackRepository {
        return TrackRepositoryImpl(
            NetworkClient(context)
        )
    }

    fun provideSearchHistoryRepository(context: Context): SearchHistoryRepository {
        return SearchHistoryRepositoryImpl(
            context.getSharedPreferences("playlist_maker", Context.MODE_PRIVATE)
        )
    }

    fun provideSettingsRepository(context: Context): SettingsRepository {
        return SettingsRepositoryImpl(
            context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        )
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

    // ИЗМЕНИЛИ ЭТУ ФУНКЦИЮ - убрали параметр
    fun provideSearchViewModel(): SearchViewModel {
        return SearchViewModel()  // Без параметров
    }
}