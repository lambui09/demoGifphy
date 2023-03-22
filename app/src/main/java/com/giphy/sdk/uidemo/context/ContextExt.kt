package com.giphy.sdk.uidemo.context

import android.content.Context
import android.content.res.AssetManager
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doAfterTextChanged
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun Context.dpToPx(dp: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

fun AssetManager.readFile(fileName: String) = open(fileName)
    .bufferedReader()
    .use { it.readText() }

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)

fun AppCompatEditText.textInputAsFlow() = callbackFlow {
    val watcher: TextWatcher = doAfterTextChanged { text ->
        trySend(text)
    }
    awaitClose { this@textInputAsFlow.removeTextChangedListener(watcher) }
}

/**
 * To hide [View]
 */
fun View.hide(hide: Boolean) {
    if (hide) {
        this.visibility = View.INVISIBLE
    } else {
        this.visibility = View.VISIBLE
    }
}

/**
 * To hide show with condition
 */
fun View.show(show: Boolean) {
    if (show) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}

fun View.gone() {
    this.visibility = View.GONE
}
fun View.showOrInvisible(show: Boolean) {
    if (show) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.INVISIBLE
    }
}

