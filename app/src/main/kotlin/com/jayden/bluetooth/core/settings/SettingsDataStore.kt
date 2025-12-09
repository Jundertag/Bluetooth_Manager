package com.jayden.bluetooth.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.jayden.bluetooth.datastore.proto.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<AppSettings> by dataStore(
    fileName = "app_settings.pb",
    serializer = AppSettingsSerializer,
)

class SettingsDataStore(
    private val dataStore: DataStore<AppSettings>
) {
    fun themeFlow(): Flow<AppSettings.UI.Theme> = dataStore.data.map { settings ->
        settings.ui.appTheme
    }

    suspend fun updateTheme(to: AppSettings.UI.Theme) {
        dataStore.updateData { settings ->
            settings.toBuilder().apply {
                ui = ui.toBuilder().apply {
                    appTheme = to
                }.build()
            }.build()
        }
    }
}