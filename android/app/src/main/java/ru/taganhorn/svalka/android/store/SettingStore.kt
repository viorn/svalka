package ru.taganhorn.svalka.android.store

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.SharedPreferences
import androidx.core.content.edit
import ru.taganhorn.svalka.android.MainApplication
import ru.taganhorn.svalka.android.defaultCoroutineScope


data class Setting(
    val isLoaded: Boolean = false,
    val serverUrl: String = ""
)

fun SharedPreferences.save(setting: Setting) {
    edit {
        putString("serverUrl", setting.serverUrl)
    }
}

fun SharedPreferences.loadSetting(): Setting {
    return Setting(
        serverUrl = getString("serverUrl", "") ?: "",
        isLoaded = true
    )
}

class SettingStore {
    private val sp: SharedPreferences by lazy {
        MainApplication.instance.getSharedPreferences(
            "settings",
            0
        )
    }
    private val mSetting: MutableStateFlow<Setting> = MutableStateFlow(Setting())
    val setting: StateFlow<Setting> = mSetting.asStateFlow()

    init {
        defaultCoroutineScope.launch {
            mSetting.emit(sp.loadSetting())
        }
    }

    private fun checkUrl(url: String) {
        if (url.isBlank()) throw SettingIsNull("url")
        if (!Regex("http[s]?:\\/\\/.*").containsMatchIn(url)) throw SettingWrongFormat("url")
    }

    suspend fun saveSettingAsync(setting: Setting): Unit =
        withContext(defaultCoroutineScope.coroutineContext) {
            try {
                checkUrl(setting.serverUrl)
            } catch (t: Throwable) {
                if (t is SettingError) {
                    t.settingName = "serverUrl"
                    throw t
                }
            }
            mSetting.emit(setting)
            sp.save(setting)
        }
}

interface SettingError {
    var settingName: String
}
data class SettingIsNull(override var settingName: String): SettingError, NullPointerException("$settingName is null or empty")
data class SettingWrongFormat(override var settingName: String): SettingError, Throwable("$settingName is wrong format")