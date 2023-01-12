package com.giphy.sdk.uidemo.context

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.giphy.sdk.uidemo.widget.AnimationType
import com.giphy.sdk.uidemo.widget.SLIDE_LEFT
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.reflect.KClass

fun Activity.hideSoftKeyboard() {
    currentFocus?.let {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun showSoftKeyboard(view: View?) {
    if (view != null) {
        val inputManager = view.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.toggleSoftInput(
            InputMethodManager.HIDE_IMPLICIT_ONLY,
            0
        )
    }
}
fun showSoftKeyboard(activity: Activity) {
    showSoftKeyboard(activity.window.decorView.rootView)
}

fun FragmentActivity.replaceFragment(
    @IdRes containerId: Int,
    fragment: Fragment,
    addToBackStack: Boolean = true,
    tag: String = fragment::class.java.simpleName,
    @AnimationType animateType: Int = SLIDE_LEFT
) {
    supportFragmentManager.transact(animateType) {
        if (addToBackStack) {
            addToBackStack(tag)
        }
        replace(containerId, fragment, tag)
    }
}

fun FragmentActivity.addFragment(
    @IdRes containerId: Int,
    fragment: Fragment,
    addToBackStack: Boolean = true,
    tag: String = fragment::class.java.simpleName,
    @AnimationType animateType: Int = SLIDE_LEFT
) {
    supportFragmentManager.transact(animateType) {
        if (addToBackStack) {
            addToBackStack(tag)
        }
        add(containerId, fragment, tag)
    }
}

fun FragmentActivity.isVisibleFragment(tag: String): Boolean {
    val fragment = supportFragmentManager.findFragmentByTag(tag)
    return fragment?.isAdded ?: false && fragment?.isVisible ?: false
}

inline fun <reified T : Any> FragmentActivity.getFragment(clazz: KClass<T>): T? {
    val tag = clazz.java.simpleName
    return supportFragmentManager.findFragmentByTag(tag) as? T?
}

/**
 * val test = extra<String>("test")
 * */
inline fun <reified T : Any> FragmentActivity.extra(key: String, default: T? = null) = lazy {
    val value = intent?.extras?.get(key)
    if (value is T) value else default
}

fun FragmentActivity.getCurrentFragment(@IdRes containerId: Int): Fragment? {
    return supportFragmentManager.findFragmentById(containerId)
}

fun FragmentActivity.setTransparentStatusBar(isDarkBackground: Boolean = false) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = if (isDarkBackground)
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        else
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

fun FragmentActivity.setStatusBarColor(@ColorRes color: Int, isDarkColor: Boolean = false) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window?.apply {
            decorView.systemUiVisibility = if (isDarkColor) 0 else View.SYSTEM_UI_FLAG_VISIBLE
            statusBarColor = ContextCompat.getColor(context, color)
        }
    }
}

fun FragmentActivity.openWithUrl(url: String) {
    val defaultBrowser =
        Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
    defaultBrowser.data = Uri.parse(url)
    this.startActivity(defaultBrowser)
}

fun Activity.getRootView(): View {
    return findViewById<View>(android.R.id.content)
}

fun Context.convertDpToPx(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        this.resources.displayMetrics
    )
}

fun Activity.isKeyboardOpen(): Boolean {
    val visibleBounds = Rect()
    this.getRootView().getWindowVisibleDisplayFrame(visibleBounds)
    val heightDiff = getRootView().height - visibleBounds.height()
    Timber.d("xxxxroot:${getRootView().height}---visibleBounds${visibleBounds.height()}")
    Timber.tag("####")
        .d("xxxxroot:" + getRootView().height + "---visibleBounds" + visibleBounds.height())
    val marginOfError = this.convertDpToPx(50F).roundToInt()
    return heightDiff > marginOfError
}

fun Activity.getHeightKeyBoard(): Int {
    val visibleBounds = Rect()
    this.getRootView().getWindowVisibleDisplayFrame(visibleBounds)
    return getRootView().height - visibleBounds.height()
}

fun Activity.isKeyboardClosed(): Boolean {
    return !this.isKeyboardOpen()
}


