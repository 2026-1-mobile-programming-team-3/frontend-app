package com.example.siheunggagae.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mapFilterDataStore: DataStore<Preferences> by preferencesDataStore(name = "map_filter")

class MapFilterStore(private val context: Context) {

    companion object {
        private val KEY_CATEGORIES = stringSetPreferencesKey("visible_categories")
        val DEFAULT_CATEGORIES: Set<String> = setOf("CAFE", "RESTAURANT", "PARK", "HOSPITAL", "GROOMING", "PET_HOTEL")

        val NAME_TO_API: Map<String, String> = mapOf(
            "카페"     to "CAFE",
            "식당"     to "RESTAURANT",
            "공원"     to "PARK",
            "동물병원" to "HOSPITAL",
            "미용"     to "GROOMING",
            "펫호텔"   to "PET_HOTEL",
        )
    }

    val visibleCategories: Flow<Set<String>> = context.mapFilterDataStore.data
        .map { prefs -> prefs[KEY_CATEGORIES] ?: DEFAULT_CATEGORIES }

    suspend fun saveCategories(categories: Set<String>) {
        context.mapFilterDataStore.edit { prefs ->
            prefs[KEY_CATEGORIES] = categories
        }
    }
}
