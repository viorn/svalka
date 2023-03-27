package ru.taganhorn.svalka.android.ui.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.launch
import ru.taganhorn.svalka.android.*
import ru.taganhorn.svalka.android.store.Setting

/**
 * A simple [Fragment] subclass.
 * Use the [ServerSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ServerSettingFragment : AbstFragment() {
    private val settingStore get() = requireContext().stores().settingStore
    private val sUrlEditText get() = view?.findViewById<EditText>(R.id.sUrlEditText)
    private val saveButton get() = view?.findViewById<Button>(R.id.saveButton)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_server_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sUrlEditText?.setText(settingStore.setting.value.serverUrl)
        saveButton?.setOnClickListener {
            mainCoroutineScope.launch {
                try {
                    settingStore.saveSettingAsync(
                        Setting(
                            serverUrl = sUrlEditText?.text?.toString() ?: ""
                        )
                    )
                    parentFragmentManager.popBackStack()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }

    override fun allowBack(): Boolean {
        return false
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            ServerSettingFragment()
    }
}