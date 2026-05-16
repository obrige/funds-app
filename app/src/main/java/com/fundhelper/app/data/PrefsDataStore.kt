package com.fundhelper.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fund_settings")

@Singleton
class PrefsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ds get() = context.dataStore

    val darkMode: Flow<Boolean> = ds.data.map { it[booleanPreferencesKey("dark_mode")] ?: false }
    val showBadge: Flow<Boolean> = ds.data.map { it[booleanPreferencesKey("show_badge")] ?: true }
    val showGSZ: Flow<Boolean> = ds.data.map { it[booleanPreferencesKey("show_gsz")] ?: false }
    val showAmount: Flow<Boolean> = ds.data.map { it[booleanPreferencesKey("show_amount")] ?: false }
    val showGains: Flow<Boolean> = ds.data.map { it[booleanPreferencesKey("show_gains")] ?: false }
    val showCost: Flow<Boolean> = ds.data.map { it[booleanPreferencesKey("show_cost")] ?: false }
    val showCostRate: Flow<Boolean> = ds.data.map { it[booleanPreferencesKey("show_cost_rate")] ?: false }
    val refreshInterval: Flow<Int> = ds.data.map { it[intPreferencesKey("refresh_interval")] ?: 2 }
    val badgeContent: Flow<Int> = ds.data.map { it[intPreferencesKey("badge_content")] ?: 1 }
    val badgeType: Flow<Int> = ds.data.map { it[intPreferencesKey("badge_type")] ?: 1 }

    suspend fun <T> set(key: String, value: T) {
        ds.edit { prefs ->
            when (value) {
                is Boolean -> prefs[booleanPreferencesKey(key)] = value
                is Int -> prefs[intPreferencesKey(key)] = value
                is String -> prefs[stringPreferencesKey(key)] = value
                is Float -> prefs[floatPreferencesKey(key)] = value
                is Long -> prefs[longPreferencesKey(key)] = value
            }
        }
    }
}
