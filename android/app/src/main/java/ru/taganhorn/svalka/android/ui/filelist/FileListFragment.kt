package ru.taganhorn.svalka.android.ui.filelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viorn.multitypeadapter.MultiTypeAdapter
import ru.taganhorn.svalka.android.AbstFragment
import ru.taganhorn.svalka.android.R
import ru.taganhorn.svalka.android.stores
import ru.taganhorn.svalka.model.FileDesc

const val spanCount = 3

class FileListFragment : AbstFragment() {

    private val settingStore get() = requireContext().stores().settingStore

    val myadapter by lazy {
        MultiTypeAdapter()
            .registerRenderer(FileAdapterItem::class.java, FileViewRenderer(spanCount))
    }

    companion object {
        fun newInstance() = FileListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_file_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.rv_main).apply {
            layoutManager = GridLayoutManager(context, spanCount, RecyclerView.VERTICAL, false)
            this.adapter = myadapter
        }

        myadapter.applyData(listOf(
            FileAdapterItem(FileDesc("test1.png","",0U,FileDesc.Type.FILE)),
            FileAdapterItem(FileDesc("test2.png","",0U,FileDesc.Type.FILE)),
            FileAdapterItem(FileDesc("test3.png","",0U,FileDesc.Type.FILE)),
            FileAdapterItem(FileDesc("test4.png","",0U,FileDesc.Type.FILE)),
            FileAdapterItem(FileDesc("test5.png","",0U,FileDesc.Type.FILE)),
            FileAdapterItem(FileDesc("test6.png","",0U,FileDesc.Type.FILE)),
            FileAdapterItem(FileDesc("test7.png","",0U,FileDesc.Type.FILE)),
        ))
    }
}