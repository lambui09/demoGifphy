package com.giphy.sdk.uidemo

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.models.enums.MediaType
import com.giphy.sdk.ui.pagination.GPHContent
import com.giphy.sdk.ui.views.GPHGridCallback
import com.giphy.sdk.ui.views.GiphyGridView
import com.giphy.sdk.uidemo.context.dpToPx
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
        const val TIME_DELAY = 200L
        fun newInstance(
            pickGif: ((Media) -> Unit)? = null,
        ): PickGifBottomSheetDialog {
            return PickGifBottomSheetDialog().apply {
                this.pickGif = pickGif
            }
        }
    }

    private var _binding: LayoutGifphyBottomsheetBinding? = null
    private val binding get() = _binding!!
    private var pickGif: ((Media) -> Unit)? = null
    private lateinit var dialog: BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutGifphyBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {

                    }
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpGripGif()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
                    dismissKeyboard()
                    pickGif?.invoke(media)
                }
            }
        }
        binding.layoutSearchGif.imvClear.setOnClickListener {
            binding.layoutSearchGif.edtSearch.setText("")
        }
        binding.layoutSearchGif.tvGifPhyDone.setOnClickListener {
            dismissKeyboard()
        }
        binding.layoutSearchGif.edtSearch.textInputAsFlow().map { editable ->
            val searchText = editable.isNullOrBlank()
            binding.layoutSearchGif.imvClear.show(!searchText)
            return@map editable
        }.debounce(TIME_DELAY).onEach { text ->
            binding.gifsGridView.content = GPHContent.searchQuery(text.toString(), MediaType.gif)
        }.launchIn(lifecycleScope)
    }

    private fun dismissKeyboard() {
        val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.layoutSearchGif.edtSearch.windowToken, 0)
    }
}