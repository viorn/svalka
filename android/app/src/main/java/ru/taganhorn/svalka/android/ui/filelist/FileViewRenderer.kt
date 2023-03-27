package ru.taganhorn.svalka.android.ui.filelist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.viorn.multitypeadapter.MultiTypeAdapter
import ru.taganhorn.svalka.android.R


class FileViewRenderer(private val columnCount: Int = 3): MultiTypeAdapter.Renderer<FileAdapterItem>() {
    override fun createViewHolder(parent: ViewGroup): MultiTypeAdapter.MultiTypeViewHolder {
        return MultiTypeAdapter.MultiTypeViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.file_item, parent, false).apply {
                    layoutParams.width = parent.measuredWidth / columnCount;
                    layoutParams.height = parent.measuredWidth / columnCount;
                }
        )
    }

    override fun bindItem(
        viewHolder: MultiTypeAdapter.MultiTypeViewHolder,
        model: FileAdapterItem
    ) {
        viewHolder.itemView.apply {
            val root = findViewById<FrameLayout>(R.id.root)
            val image = findViewById<AppCompatImageView>(R.id.image)
            val name = findViewById<AppCompatTextView>(R.id.name)
            name.text = model.fileDesc.name
        }
    }
}