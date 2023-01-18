package com.giphy.sdk.uidemo.popup

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.giphy.sdk.uidemo.DemoConfig.DURATION_SCROLL
import com.giphy.sdk.uidemo.DemoConfig.MIN_PEEK_HEIGHT_POPUP
import com.giphy.sdk.uidemo.EnumStatePopup
import com.giphy.sdk.uidemo.PickGifBottomSheetDialog
import com.giphy.sdk.uidemo.R
import com.giphy.sdk.uidemo.databinding.BottomSheetCommentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

class CommentBottomSheetFragment : BottomSheetDialogFragment() {
    private var _viewBinding: BottomSheetCommentBinding? = null
    private val viewBinding get() = _viewBinding!!
    private var showGif: ((isShow: Boolean) -> Unit)? = null
    private var isShowGif: Boolean = false
    private var bottomSheetGifPhy: PickGifBottomSheetDialog? = null
    private var stateOfPopup = EnumStatePopup.HIDE.value
    private var isShowPopupGif = false

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
                bottomSheet.height.let {
                    BottomSheetBehavior.from(bottomSheet).peekHeight =
                        resources.displayMetrics.heightPixels
                }
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
        //
        //handle gifphy
        bottomSheetGifPhy = PickGifBottomSheetDialog.newInstance(
            pickGif = { media ->

            },
            focusEdittext = { isFocus ->
                if (isFocus && stateOfPopup != EnumStatePopup.FULL_SCREEN.value) {
                    setHeightPopupGif(EnumStatePopup.FULL_SCREEN.value)
                }
            },
            onHeightKeyboard = {
            },
            onCollapsePopup = {
                setHeightPopupGif(EnumStatePopup.COLLAPSE.value)
            }
        )
        bottomSheetGifPhy?.let { instance ->
            childFragmentManager.beginTransaction()
                .add(R.id.frameGifphy, instance)
                .commitAllowingStateLoss()
        }
        handleFragBottomSheet()
        //
        viewBinding.imvGif.setOnClickListener {
            if (!isShowGif) {
                viewBinding.imvGif.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#d8d8d8"))
            } else {
                viewBinding.imvGif.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#000000"))
            }
            isShowGif = !isShowGif
            showGif?.invoke(isShowGif)
        }
        viewBinding.imvTest.setOnClickListener {
            Toast.makeText(requireContext(), "TEST CLICKAAAAAAAAAAA", Toast.LENGTH_LONG).show()
        }
        viewBinding.imvGif.setOnClickListener {
            dismissKeyboard()
            if (!isShowPopupGif) {
                showPopUpGif()
            } else {
                hidePopUpGif()
            }
            isShowPopupGif = !isShowPopupGif
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleFragBottomSheet() {
        var downY = 0f
        var time = 0L
        viewBinding.frameGifphy.setOnTouchListener { v, event ->
            val layoutParamsGifphyPopup = viewBinding.frameGifphy.layoutParams
            val heightPopup = viewBinding.frameGifphy.height
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    val rangeTime = System.currentTimeMillis() - time
                    when {
                        //scroll fast
                        (downY - event.y) / rangeTime > 0.2 -> {
                            setHeightPopupGif(EnumStatePopup.FULL_SCREEN.value)
                        }
                        (event.y - downY) / rangeTime > 0.2 -> {
                            setHeightPopupGif(EnumStatePopup.COLLAPSE.value)
                        }
                        //top: scroll full screen
                        (heightPopup > (viewBinding.containerRoot.height - dpToPx(
                            200f
                        ))) -> {
                            setHeightPopupGif(EnumStatePopup.FULL_SCREEN.value)
                        }
                        //down to pin 300
                        (heightPopup < (viewBinding.containerRoot.height - dpToPx(
                            200f
                        ))) -> {
                            setHeightPopupGif(EnumStatePopup.COLLAPSE.value)
                        }
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    downY = event.y
                    time = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (heightPopup >= dpToPx(200f)) {
                        Timber.e("xxx${heightPopup + (downY - event.y).toInt()}")
                        layoutParamsGifphyPopup.height = heightPopup + (downY - event.y).toInt()
                    } else {
                        Timber.e("xxx${heightPopup + (downY - event.y).toInt()}")
                        return@setOnTouchListener true
                    }
                }
            }
            viewBinding.frameGifphy.layoutParams = layoutParamsGifphyPopup
            return@setOnTouchListener true
        }
    }

    //animation
    private fun animMovePopup(target: Int, duration: Long) {
        viewBinding.frameGifphy.apply {
            val valueAnimator = ValueAnimator.ofInt(height, target)
            valueAnimator.addUpdateListener { animation ->
                val lp = layoutParams
                lp.height = animation.animatedValue as Int
                if (lp.height >= viewBinding.containerRoot.height) {
                    lp.height = ViewGroup.LayoutParams.MATCH_PARENT
                }
                layoutParams = lp
            }
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.duration = duration
            valueAnimator.start()
        }
    }

    private fun setHeightPopupGif(state: Int) {
        val lp = viewBinding.frameGifphy.layoutParams
        when (state) {
            EnumStatePopup.COLLAPSE.value -> {
                dismissKeyboard()
                stateOfPopup = EnumStatePopup.COLLAPSE.value
                lp.height = dpToPx(MIN_PEEK_HEIGHT_POPUP)
                animMovePopup(dpToPx(MIN_PEEK_HEIGHT_POPUP), DURATION_SCROLL)
                bottomSheetGifPhy?.setState(EnumStatePopup.COLLAPSE.value)
            }
            EnumStatePopup.FULL_SCREEN.value -> {
                stateOfPopup = EnumStatePopup.FULL_SCREEN.value
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT
                animMovePopup(viewBinding.containerRoot.height, DURATION_SCROLL)
                bottomSheetGifPhy?.setState(EnumStatePopup.FULL_SCREEN.value)
            }
            else -> {
                bottomSheetGifPhy?.setState(EnumStatePopup.HIDE.value)
            }
        }
    }

    private fun showPopUpGif() {
        viewBinding.frameGifphy.visibility = View.VISIBLE
        stateOfPopup = EnumStatePopup.COLLAPSE.value
    }

    private fun hidePopUpGif() {
        viewBinding.frameGifphy.visibility = View.GONE
        stateOfPopup = EnumStatePopup.HIDE.value
    }

    fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    private fun dismissKeyboard() {
        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(viewBinding.containerRoot.windowToken, 0)
    }
}