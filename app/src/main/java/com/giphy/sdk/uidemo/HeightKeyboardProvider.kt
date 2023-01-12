package com.giphy.sdk.uidemo

import android.app.Activity
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.PopupWindow


class HeightKeyboardProvider(private val mActivity: Activity) : PopupWindow(mActivity),
    OnGlobalLayoutListener {
    private val rootView: View = View(mActivity)
    private var listener: HeightKeyboardListener? = null
    private var heightMax = 0

    fun init(): HeightKeyboardProvider {
        if (!isShowing) {
            val view = mActivity.window.decorView
            view.post { showAtLocation(view, Gravity.NO_GRAVITY, 0, 0) }
        }
        return this
    }

    fun setHeightKeyboardListener(listener: HeightKeyboardListener?): HeightKeyboardProvider {
        this.listener = listener
        return this
    }

    override fun onGlobalLayout() {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        if (rect.bottom > heightMax) {
            heightMax = rect.bottom
        }

        val keyboardHeight = heightMax - rect.bottom
        listener?.onHeightKeyboardChanged(
            if (keyboardHeight == 0) keyboardHeight else keyboardHeight + 126,
            keyboardHeight
        )
    }

    private fun getNavigationHeight(): Int {
        val resources: Resources = mActivity.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0 && KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    interface HeightKeyboardListener {
        fun onHeightKeyboardChanged(height: Int, heightKeyboar: Int)
    }

    init {
        contentView = rootView
        rootView.viewTreeObserver.addOnGlobalLayoutListener(this)
        setBackgroundDrawable(ColorDrawable(0))
        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        inputMethodMode = INPUT_METHOD_NEEDED
    }
}