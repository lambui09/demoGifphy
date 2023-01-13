package com.giphy.sdk.uidemo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.models.enums.MediaType
import com.giphy.sdk.ui.pagination.GPHContent
import com.giphy.sdk.ui.views.GPHGridCallback
import com.giphy.sdk.ui.views.GPHSearchGridCallback
import com.giphy.sdk.ui.views.GifView
import com.giphy.sdk.ui.views.GiphyGridView
import com.giphy.sdk.uidemo.context.dpToPx
import com.giphy.sdk.uidemo.context.hide
import com.giphy.sdk.uidemo.context.show
import com.giphy.sdk.uidemo.context.textInputAsFlow
import com.giphy.sdk.uidemo.databinding.LayoutGifphyBottomsheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import timber.log.Timber

class PickGifBottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        const val TIME_DELAY = 500L
        fun newInstance(
            pickGif: ((Media) -> Unit)? = null,
            focusEdittext: ((Boolean) -> Unit)? = null,
            onHeightKeyboard: ((Boolean) -> Unit)? = null,
            onCollapsePopup: (() -> Unit)? = null
        ): PickGifBottomSheetDialog {
            return PickGifBottomSheetDialog().apply {
                this.pickGif = pickGif
                this.focusEdittext = focusEdittext
                this.onHeightKeyboard = onHeightKeyboard
                this.onCollapsePopup = onCollapsePopup
            }
        }
    }

    private var _binding: LayoutGifphyBottomsheetBinding? = null
    private val binding get() = _binding!!
    private var pickGif: ((Media) -> Unit)? = null
    private var focusEdittext: ((Boolean) -> Unit)? = null
    private var onHeightKeyboard: ((Boolean) -> Unit)? = null
    private var onCollapsePopup: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        _binding = LayoutGifphyBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpGripGif()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    @OptIn(FlowPreview::class)
    private fun setUpGripGif() {
        binding.gifsGridView.apply {
            direction = GiphyGridView.VERTICAL
            spanCount = 2
            cellPadding = dpToPx(8f)
            fixedSizeCells = false
            content = GPHContent.trendingGifs
            callback = object : GPHGridCallback {
                override fun contentDidUpdate(resultCount: Int) {
                    Timber.e("########${resultCount}")
                    val isShow = resultCount > 0
                    binding.gifsGridView.show(isShow)
                    binding.tvEmpty.show(!isShow)
                }

                override fun didSelectMedia(media: Media) {
                    pickGif?.invoke(media)
                }
            }
        }
        binding.gifsGridView.searchCallback = object : GPHSearchGridCallback {
            override fun didTapUsername(username: String) {
                Timber.d("didTapUsername $username")
            }

            override fun didLongPressCell(cell: GifView) {
                Timber.d("didLongPressCell")
            }

            override fun didScroll(dx: Int, dy: Int) {
                Timber.d("didScroll$dx ----- $dy")
                binding.layoutSearchGif.edtSearch.clearFocus()
            }
        }
        binding.layoutSearchGif.imvClear.setOnClickListener {
            binding.layoutSearchGif.edtSearch.setText("")
        }
        binding.layoutSearchGif.tvGifPhyDone.setOnClickListener {
            onCollapsePopup?.invoke()
        }
        binding.layoutSearchGif.tvSearch.setOnClickListener {
            focusEdittext?.invoke(true)
        }
        binding.layoutSearchGif.edtSearch.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                binding.layoutSearchGif.edtSearch.requestFocus()
            }
        }

        binding.layoutSearchGif.containerSearch.setOnClickListener {
            focusEdittext?.invoke(true)
        }

        binding.layoutSearchGif.edtSearch.textInputAsFlow().map { editable ->
            val searchText = editable.isNullOrBlank()
            binding.layoutSearchGif.imvClear.show(!searchText)
            return@map editable
        }.debounce(TIME_DELAY).onEach { text ->
            if (text == null || text.isNullOrEmpty() == true) {
                binding.gifsGridView.content = GPHContent.trendingGifs
            } else {
                binding.gifsGridView.content =
                    GPHContent.searchQuery(text.toString(), MediaType.gif)
            }
        }.launchIn(lifecycleScope)
    }

    fun setState(state: Int) {
        when (state) {
            EnumStatePopup.COLLAPSE.value -> {
                binding.layoutSearchGif.edtSearch.hide(true)
                binding.layoutSearchGif.tvSearch.show(true)
                binding.layoutSearchGif.tvGifPhyDone.show(false)
            }
            EnumStatePopup.FULL_SCREEN.value -> {
                binding.layoutSearchGif.tvGifPhyDone.show(true)
                binding.layoutSearchGif.edtSearch.show(true)
                binding.layoutSearchGif.tvSearch.show(false)
            }
            else -> {
                binding.layoutSearchGif.tvGifPhyDone.show(false)
                binding.layoutSearchGif.tvSearch.show(true)
                binding.layoutSearchGif.edtSearch.hide(true)
            }
        }
    }
}