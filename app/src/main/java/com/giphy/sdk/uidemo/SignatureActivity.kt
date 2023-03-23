package com.giphy.sdk.uidemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.giphy.sdk.uidemo.context.gone
import com.giphy.sdk.uidemo.context.show
import com.giphy.sdk.uidemo.context.showOrInvisible
import com.giphy.sdk.uidemo.databinding.ActivitySignatureBinding
import java.io.File

//todo check
class SignatureActivity : AppCompatActivity() {
    companion object {
        const val KEY_IMAGE = "KEY_IMAGE"
    }

    private lateinit var binding: ActivitySignatureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignatureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bindViews()
    }

    fun bindViews() {
        with(binding) {
            signaturePad.onStartSign = { onStartSign ->
                if (onStartSign) {
                    startSign()
                } else {
                    handleSigned()
                }
            }

            imvBack.setOnClickListener {
                finish()
            }
            imvRefresh.setOnClickListener {
                resetDefaultSign()
            }
            tvSignDone.setOnClickListener {
                signaturePad.saveSignature()?.let {
                    addSignView(it)
                }
            }
        }
    }

    private fun resetDefaultSign() {
        binding.run {
            signaturePad.clearSignature()
            tvSignNote.show(true)
            imvRefresh.gone()
            disableBtn()
        }
    }

    private fun startSign() {
        binding.run {
            tvSignNote.showOrInvisible(false)
            imvRefresh.show(true)
            enableBtn()
        }
    }

    private fun enableBtn() {
        binding.tvSignDone.setTextColor(
            Color.parseColor("#7470ef")
        )
        binding.tvSignDone.isEnabled = true
    }

    private fun handleSigned() {
        binding.run {
            enableBtn()
            imvRefresh.show(true)
        }
    }

    private fun disableBtn() {
        binding.tvSignDone.setTextColor(
            ContextCompat.getColor(this, R.color.gray_purple)
        )
        binding.tvSignDone.isEnabled = false
    }

    //test
    private fun getBitmapFromFilePath(filePath: String): Bitmap? {
        val file = File(filePath)
        if (!file.exists()) {
            return null
        }

        return try {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            BitmapFactory.decodeFile(filePath, options)
        } catch (e: Exception) {
            null
        }
    }

    fun addSignView(path: String) {
        getBitmapFromFilePath(path)?.let {
            val bitmap = BitmapDrawable(resources, it)
            bitmap.alpha = 100
            binding.imvSignature.setImageDrawable(bitmap)
        }
    }
}