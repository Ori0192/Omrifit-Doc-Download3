package com.example.omrifit.photo_editor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.omrifit.R
import java.util.ArrayList

/**
 * Adapter for the editing tools in the photo editor.
 *
 * @constructor
 * @param mOnItemSelected A listener for item selection events.
 */
class EditingToolsAdapter(private val mOnItemSelected: OnItemSelected) :
    RecyclerView.Adapter<EditingToolsAdapter.ViewHolder>() {

    private val mToolList: MutableList<ToolModel> = ArrayList()

    /**
     * Interface for handling item selection events.
     */
    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType)
    }

    /**
     * Model class representing an editing tool.
     *
     * @property mToolName The name of the tool.
     * @property mToolIcon The icon resource ID for the tool.
     * @property mToolType The type of the tool.
     */
    internal inner class ToolModel(
        val mToolName: String,
        val mToolIcon: Int,
        val mToolType: ToolType
    )

    /**
     * Creates a new ViewHolder for the tool item view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_editing_tools, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds the data to the ViewHolder.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mToolList[position]
        holder.txtTool.text = item.mToolName
        holder.imgToolIcon.setImageResource(item.mToolIcon)
    }

    /**
     * Returns the number of items in the adapter.
     */
    override fun getItemCount(): Int {
        return mToolList.size
    }

    /**
     * ViewHolder class for the tool item view.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgToolIcon: ImageView = itemView.findViewById(R.id.imgToolIcon)
        val txtTool: TextView = itemView.findViewById(R.id.txtTool)

        init {
            itemView.setOnClickListener {
                mOnItemSelected.onToolSelected(
                    mToolList[layoutPosition].mToolType
                )
            }
        }
    }

    /**
     * Initializes the adapter with a list of editing tools.
     */
    init {
        mToolList.add(ToolModel("Shape", R.drawable.ic_oval, ToolType.SHAPE))
        mToolList.add(ToolModel("Text", R.drawable.ic_text, ToolType.TEXT))
        mToolList.add(ToolModel("Eraser", R.drawable.ic_eraser, ToolType.ERASER))
        mToolList.add(ToolModel("Filter", R.drawable.ic_photo_filter, ToolType.FILTER))
        mToolList.add(ToolModel("Emoji", R.drawable.ic_insert_emoticon, ToolType.EMOJI))
        mToolList.add(ToolModel("Sticker", R.drawable.ic_sticker, ToolType.STICKER))
    }
}
