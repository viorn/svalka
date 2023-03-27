package ru.taganhorn.svalka.android.ui.filelist

import com.viorn.multitypeadapter.MultiTypeAdapter
import ru.taganhorn.svalka.model.FileDesc

data class FileAdapterItem(val fileDesc: FileDesc) : MultiTypeAdapter.AdapterItem {

}