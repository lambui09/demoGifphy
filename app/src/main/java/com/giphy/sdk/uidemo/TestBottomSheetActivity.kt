package com.giphy.sdk.uidemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.fragment.app.FragmentContainerView
import com.giphy.sdk.uidemo.context.setStatusBarColor
import com.giphy.sdk.uidemo.databinding.ActivityTestBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class TestBottomSheetActivity : AppCompatActivity() {
    companion object {
        const val BOTTOM_SHEET_HEIGHT_RATIO = 0.9
        const val DELAY_TIME_TO_SHOW_BOTTOM_SHEET = 200L
    }

    private var bottomSheetBehavior: BottomSheetBehavior<FragmentContainerView>? = null
    private var viewBinding: ActivityTestBottomSheetBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityTestBottomSheetBinding.inflate(layoutInflater)
        val view = viewBinding!!.root
        setContentView(view)
        setStatusBarColor(R.color.transparent)
        viewBinding?.run {
            bottomSheetBehavior = BottomSheetBehavior.from(container)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            coordinatorLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    coordinatorLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    container.layoutParams = container.layoutParams.apply {
                        height = (coordinatorLayout.height * BOTTOM_SHEET_HEIGHT_RATIO).toInt()
                    }
                }
            })
        }
        Handler(mainLooper).postDelayed({
            initBottomSheetBehavior()
        }, DELAY_TIME_TO_SHOW_BOTTOM_SHEET)

        onBackPressedDispatcher.addCallback {
            finishByCollapseBottomSheet()
        }
    }

    override fun finish() {
        finishByCollapseBottomSheet()
    }

    private fun finishByCollapseBottomSheet() {
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun superFinish() {
        super.finish()
    }

    private fun initBottomSheetBehavior() {
        bottomSheetBehavior?.apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            skipCollapsed = true
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        superFinish()
                        overridePendingTransition(0, 0)
                    }
                }
            })
        }
    }
}
