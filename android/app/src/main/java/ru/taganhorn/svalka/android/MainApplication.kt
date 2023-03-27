package ru.taganhorn.svalka.android

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.taganhorn.svalka.android.store.SettingStore

val mainCoroutineScope = CoroutineScope(Dispatchers.Main)
val defaultCoroutineScope = CoroutineScope(Dispatchers.Default)
val ioCoroutineScope = CoroutineScope(Dispatchers.IO)
fun Context.stores() = (applicationContext as MainApplication).stores

data class Stores(
    val settingStore: SettingStore
)

class MainApplication : Application() {
    companion object {
        private var _instance: MainApplication? = null
        val instance: MainApplication get() = _instance!!
    }

    init {
        _instance = this
    }

    lateinit var stores: Stores
        private set

    override fun onCreate() {
        super.onCreate()
        stores = Stores(
            settingStore = SettingStore()
        )
    }
}