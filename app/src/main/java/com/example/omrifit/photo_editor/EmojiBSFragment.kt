package com.example.omrifit.photo_editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.omrifit.R

/**
 * Emoji Bottom Sheet Fragment for selecting emojis.
 */
class EmojiBSFragment : BottomSheetDialogFragment() {

    private var mEmojiListener: EmojiListener? = null
    private lateinit var emojisList: ArrayList<String>

    /**
     * Interface for emoji click events.
     */
    interface EmojiListener {
        fun onEmojiClick(emojiUnicode: String)
    }

    /**
     * Inflates the view and sets up the RecyclerView.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_sticker_emoji_dialog, container, false)
        emojisList = loadEmojis()  // Load emojis after the context is available
        setupRecyclerView(view)
        return view
    }

    /**
     * Sets up the RecyclerView for displaying emojis.
     */
    private fun setupRecyclerView(view: View) {
        val rvEmoji: RecyclerView = view.findViewById(R.id.rvEmoji)
        rvEmoji.layoutManager = GridLayoutManager(context, 5)
        rvEmoji.adapter = EmojiAdapter()
        rvEmoji.setHasFixedSize(true)
    }

    /**
     * Loads emojis from resources.
     */
    private fun loadEmojis(): ArrayList<String> {
        val convertedEmojiList = ArrayList<String>()
        val emojiList = resources.getStringArray(R.array.photo_editor_emoji)
        for (emojiUnicode in emojiList) {
            convertedEmojiList.add(convertEmoji(emojiUnicode))
        }
        return convertedEmojiList
    }

    /**
     * Converts emoji Unicode to character.
     */
    private fun convertEmoji(emoji: String): String {
        return try {
            val convertEmojiToInt = emoji.substring(2).toInt(16)
            String(Character.toChars(convertEmojiToInt))
        } catch (e: NumberFormatException) {
            ""
        }
    }

    /**
     * Sets the EmojiListener.
     */
    fun setEmojiListener(emojiListener: EmojiListener?) {
        mEmojiListener = emojiListener
    }

    /**
     * Adapter for displaying emojis in the RecyclerView.
     */
    inner class EmojiAdapter : RecyclerView.Adapter<EmojiAdapter.ViewHolder>() {

        /**
         * Creates a new ViewHolder for the emoji item view.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_emoji, parent, false)
            return ViewHolder(view)
        }

        /**
         * Binds the data to the ViewHolder.
         */
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.txtEmoji.text = emojisList[position]
        }

        /**
         * Returns the number of items in the adapter.
         */
        override fun getItemCount(): Int {
            return emojisList.size
        }

        /**
         * ViewHolder class for the emoji item view.
         */
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtEmoji: TextView = itemView.findViewById(R.id.txtEmoji)

            init {
                itemView.setOnClickListener {
                    mEmojiListener?.onEmojiClick(emojisList[adapterPosition])
                    dismiss()
                }
            }
        }
    }
}
