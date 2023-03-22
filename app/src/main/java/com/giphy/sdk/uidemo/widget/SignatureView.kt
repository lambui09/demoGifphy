package com.giphy.sdk.uidemo.widget

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
//todo check
class SignatureView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var signaturePaint = Paint()
    private var signatureColor = Color.BLACK
    private var signatureWidth = 5f
    private var paths = mutableListOf<Path>()

    var onStartSign: ((Boolean) -> Unit)? = null

    init {
        signaturePaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            color = signatureColor
            strokeWidth = signatureWidth
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (path in paths) {
            canvas.drawPath(path, signaturePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val path = Path()
                path.moveTo(event.x, event.y)
                paths.add(path)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val currentPath = paths.last()
                currentPath.lineTo(event.x, event.y)
                invalidate()
                onStartSign?.invoke(true)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun clearSignature() {
        paths.clear()
        invalidate()
        onStartSign?.invoke(false)
    }

    fun undoSignature() {
        if (paths.isNotEmpty()) {
            paths.removeAt(paths.lastIndex)
            invalidate()
        }
    }

    fun setSignatureColor(color: Int) {
        signatureColor = color
        signaturePaint.color = signatureColor
        invalidate()
    }

    fun setSignatureWidth(width: Float) {
        signatureWidth = width
        signaturePaint.strokeWidth = signatureWidth
        invalidate()
    }

    fun saveSignature(): String? {
        val bitmap = Bitmap.createBitmap(width-2, height-2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        draw(canvas)
        val directory =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "signatures")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "signature_$timeStamp.png"

        val file = File(directory, fileName)

        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            return file.absolutePath
        } catch (e: Exception) {
            Log.e("SignatureView", "Error saving signature", e)
        }

        return null
    }

}
