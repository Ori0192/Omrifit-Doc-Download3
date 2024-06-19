package com.example.omrifit.photo_editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.omrifit.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.shape.ShapeType

/**
 * ShapeBSFragment is a BottomSheetDialogFragment that allows users to pick and customize shapes
 * such as lines, arrows, ovals, and rectangles for photo editing.
 */
class ShapeBSFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {

    // Interface to communicate shape properties changes
    private var mProperties: Properties? = null

    interface Properties {
        fun onColorChanged(colorCode: Int)
        fun onOpacityChanged(opacity: Int)
        fun onShapeSizeChanged(shapeSize: Int)
        fun onShapePicked(shapeType: ShapeType)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_shapes_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        val rvColor: RecyclerView = view.findViewById(R.id.shapeColors)
        val sbOpacity: SeekBar = view.findViewById(R.id.shapeOpacity)
        val sbBrushSize: SeekBar = view.findViewById(R.id.shapeSize)
        val shapeGroup: RadioGroup = view.findViewById(R.id.shapeRadioGroup)

        // Shape picker
        shapeGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            when (checkedId) {
                R.id.lineRadioButton -> mProperties?.onShapePicked(ShapeType.Line)
                R.id.arrowRadioButton -> mProperties?.onShapePicked(ShapeType.Arrow())
                R.id.ovalRadioButton -> mProperties?.onShapePicked(ShapeType.Oval)
                R.id.rectRadioButton -> mProperties?.onShapePicked(ShapeType.Rectangle)
                else -> mProperties?.onShapePicked(ShapeType.Brush)
            }
        }

        // Set up seek bar listeners
        sbOpacity.setOnSeekBarChangeListener(this)
        sbBrushSize.setOnSeekBarChangeListener(this)

        // Set up color picker RecyclerView
        val activity = requireActivity()
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvColor.layoutManager = layoutManager
        rvColor.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(activity)
        colorPickerAdapter.setOnColorPickerClickListener(object :
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
     * Set the listener to handle shape properties changes.
     *
     * @param properties The listener for shape properties changes.
     */
    fun setPropertiesChangeListener(properties: Properties?) {
        mProperties = properties
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.shapeOpacity -> mProperties?.onOpacityChanged(i)
            R.id.shapeSize -> mProperties?.onShapeSizeChanged(i)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        // Do nothing
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        // Do nothing
    }
}
