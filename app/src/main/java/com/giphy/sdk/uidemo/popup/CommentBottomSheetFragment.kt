package com.giphy.sdk.uidemo.popup

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.giphy.sdk.uidemo.databinding.BottomSheetCommentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentBottomSheetFragment : BottomSheetDialogFragment() {
    private var _viewBinding: BottomSheetCommentBinding? = null
    private val viewBinding get() = _viewBinding!!
    private var showGif: ((isShow: Boolean) -> Unit)? = null
    private var isShowGif: Boolean = false

    companion object {
        fun newInstance(
            showGif: ((isShow: Boolean) -> Unit)? = null
        ): CommentBottomSheetFragment {
            return CommentBottomSheetFragment().apply {
                this.showGif = showGif
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog

            val bottomSheet =
                d.findViewById(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.let { bottomSheet ->
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
                BottomSheetBehavior.from(bottomSheet).skipCollapsed = true
                bottomSheet.height.let { BottomSheetBehavior.from(bottomSheet).peekHeight = it }
            }
        }
        bottomDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return bottomDialog
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewBinding = BottomSheetCommentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.imvGif.setOnClickListener {
            if (!isShowGif) {
                viewBinding.imvGif.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#d8d8d8"))
            } else {
                viewBinding.imvGif.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#00000"))
            }
            isShowGif = !isShowGif
            showGif?.invoke(isShowGif)
        }
        viewBinding.imvTest.setOnClickListener {
            Toast.makeText(requireContext(), "TEST CLICKAAAAAAAAAAA", Toast.LENGTH_LONG).show()
        }
    }
}