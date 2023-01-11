package com.giphy.sdk.uidemo.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

data class DialogOptions(
    val title: String,
    val message: String,
    val positiveText: String,
    val negativeText: String,
    val positiveListener: (() -> Unit)? = null,
    val negativeListener: (() -> Unit)? = null,
    var positiveColor: Int,
    var negativeColor: Int,
    var messageColor: Int,
    var titleColor: Int,
    val cancelable: Boolean,
    val isShowNegative: Boolean
)

@DslMarker
annotation class DslDialog
