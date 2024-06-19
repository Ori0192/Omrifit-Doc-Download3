package com.example.omrifit.photo_editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.omrifit.R

/**
 * PropertiesBSFragment is a BottomSheetDialogFragment that allows users to change properties
 * such as color, opacity, and shape size of the photo editing tools.
 */
class PropertiesBSFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {

    // Interface to communicate property changes
    private var mProperties: Properties? = null

    interface Properties {
        fun onColorChanged(colorCode: Int)
        fun onOpacityChanged(opacity: Int)
        fun onShapeSizeChanged(shapeSize: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_properties_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        val rvColor: RecyclerView = view.findViewById(R.id.rvColors)
        val sbOpacity: SeekBar = view.findViewById(R.id.sbOpacity)
        val sbBrushSize: SeekBar = view.findViewById(R.id.sbSize)

        // Set up seek bar listeners
        sbOpacity.setOnSeekBarChangeListener(this)
        sbBrushSize.setOnSeekBarChangeListener(this)

        // Set up color picker RecyclerView
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvColor.layoutManager = layoutManager
        rvColor.setHasFixedSize(true)

        // Initialize the color picker adapter and set its listener
        val colorPickerAdapter = activity?.let { ColorPickerAdapter(it) }
        colorPickerAdapter?.setOnColorPickerClickListener(object :
            ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                mProperties?.let {
                    dismiss()
                    it.onColorChanged(colorCode)
                }
            }
        })
        rvColor.adapter = colorPickerAdapter
    }

    /**
     * Set the listener to handle property changes.
     *
     * @param properties The listener for property changes.
     */
    fun setPropertiesChangeListener(properties: Properties?) {
        mProperties = properties
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.sbOpacity -> mProperties?.onOpacityChanged(i)
            R.id.sbSize -> mProperties?.onShapeSizeChanged(i)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        // Do nothing
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        // Do nothing
    }
}
