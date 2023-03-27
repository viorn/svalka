package ru.taganhorn.svalka.android

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import ru.taganhorn.svalka.android.ui.filelist.FileListFragment
import ru.taganhorn.svalka.android.ui.setting.ServerSettingFragment

class MainActivity : AppCompatActivity() {
    val settingStore get() = stores().settingStore
    val progressBar by lazy { findViewById<ProgressBar>(R.id.progressBar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainCoroutineScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                withTimeout(5000) {
                    settingStore.setting.first { it.isLoaded }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, FileListFragment.newInstance())
                        .commit()
                    if (settingStore.setting.value.serverUrl.isBlank()) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.container, ServerSettingFragment.newInstance())
                            .addToBackStack(null)
                            .commit()
                    }
                }
            } catch (t: TimeoutCancellationException) {
                t.printStackTrace()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        if (count == 0) {
            super.onBackPressed()
            //additional code
        } else {
            val fragment = supportFragmentManager.findFragmentById(R.id.container) as AbstFragment
            if (fragment.allowBack()) {
                supportFragmentManager.popBackStack()
            }
        }

    }
}