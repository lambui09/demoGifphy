package com.giphy.sdk.uidemo

import android.app.Activity
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

class PickGifBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TIME_DELAY = 200L
        fun newInstance(
            pickGif: ((Media) -> Unit)? = null,
        ): PickGifBottomSheet {
            return PickGifBottomSheet().apply {
                this.pickGif = pickGif
            }
        }
    }

    private var _binding: LayoutGifphyBottomsheetBinding? = null
    private val binding get() = _binding!!
    private var pickGif: ((Media) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutGifphyBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
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
                    pickGif?.invoke(media)
                }
            }
        }
        binding.layoutSearchGif.imvClear.setOnClickListener {
            binding.layoutSearchGif.edtSearch.setText("")
        }
        binding.layoutSearchGif.tvGifPhyDone.setOnClickListener {

        }
        binding.layoutSearchGif.edtSearch.textInputAsFlow().map { editable ->
            val searchText = editable.isNullOrBlank()
            binding.layoutSearchGif.imvClear.show(!searchText)
            return@map editable
        }.debounce(TIME_DELAY).onEach { text ->
            binding.gifsGridView.content = GPHContent.searchQuery(text.toString(), MediaType.gif)
        }.launchIn(lifecycleScope)
    }
}