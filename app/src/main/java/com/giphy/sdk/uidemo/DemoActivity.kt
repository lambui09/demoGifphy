package com.giphy.sdk.uidemo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.tracking.isVideo
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.themes.GPHTheme
import com.giphy.sdk.ui.themes.GridType
import com.giphy.sdk.ui.utils.GPHAbstractVideoPlayer
import com.giphy.sdk.ui.utils.GPHVideoPlayerState
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.giphy.sdk.uidemo.VideoPlayer.VideoCache
import com.giphy.sdk.uidemo.context.dpToPx
import com.giphy.sdk.uidemo.databinding.ActivityDemoBinding
import com.giphy.sdk.uidemo.feed.*
import timber.log.Timber


/**
 * Created by Cristian Holdunu on 27/02/2019.
 */
enum class EnumStatePopup(val value: Int) {
    HIDE(0),
    COLLAPSE(1),
    FULL_SCREEN(2),
}

class DemoActivity : AppCompatActivity() {

    companion object {
        val TAG = DemoActivity::class.java.simpleName
        val INVALID_KEY = "Fi8pLx5gvGS61VeAW7smZApj0nyjvcQm"
    }

    private lateinit var binding: ActivityDemoBinding
    var settings =
        GPHSettings(gridType = GridType.waterfall, theme = GPHTheme.Light, stickerColumnCount = 3)
    var feedAdapter: MessageFeedAdapter? = null
    var messageItems = ArrayList<FeedDataItem>()
    var contentType = GPHContentType.gif
    var stateOfPopup = EnumStatePopup.HIDE.value

    //TODO: Set a valid API KEY
    val YOUR_API_KEY = INVALID_KEY

    val player: GPHAbstractVideoPlayer = createVideoPlayer()
    private var clipsPlaybackSetting = SettingsDialogFragment.ClipsPlaybackSetting.inline
    var isShow = false
    private var bottomSheetGifPhy: PickGifBottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Giphy.configure(this, YOUR_API_KEY, true)
        VideoCache.initialize(this, 100 * 1024 * 1024)
        binding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomSheetGifPhy = PickGifBottomSheetDialog.newInstance(
            pickGif = { media ->
                Log.d("####", "media${media.id}")
                dismissKeyboard()
                if (stateOfPopup != EnumStatePopup.COLLAPSE.value) {
                    setHeightPopupGif(EnumStatePopup.COLLAPSE.value)
                }
            },
            focusEdittext = { isFocus ->
                //show/height keyboard
                Log.d("####", "isFocus${isFocus}")
                if (isFocus) {
                    setHeightPopupGif(EnumStatePopup.FULL_SCREEN.value)
                } else {
                    dismissKeyboard()
                    setHeightPopupGif(EnumStatePopup.COLLAPSE.value)
                }
            },
        )
        bottomSheetGifPhy?.let { instance ->
            supportFragmentManager.beginTransaction()
                .add(R.id.bottomSheetGifPhy, instance)
                .commit()
        }
        setupToolbar()
        setupFeed()
        handleFragBottomSheet()
        //todo show popup gif
        binding.testGifClick.setOnClickListener {
            binding.composeContainer.apply {
                if (!isShow) {
                    val lp = layoutParams as? MarginLayoutParams
                    lp?.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, dpToPx(250f))
                    binding.bottomSheetGifPhy.visibility = View.VISIBLE
                    stateOfPopup = EnumStatePopup.COLLAPSE.value
                } else {
                    val lp = layoutParams as? MarginLayoutParams
                    lp?.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, dpToPx(16f))
                    binding.bottomSheetGifPhy.visibility = View.GONE
                    stateOfPopup = EnumStatePopup.HIDE.value
                }
                isShow = !isShow
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun handleFragBottomSheet() {
        var downY = 0f
        val lp = binding.bottomSheetGifPhy.layoutParams
        var time = 0L
        binding.bottomSheetGifPhy.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    val rangeTime = System.currentTimeMillis() - time
                    Log.d("####", "handleFragBottomSheet: ${(downY - event.y) / rangeTime}")
                    when {
                        //scroll fast
                        (downY - event.y) / rangeTime > 0.2 -> {
                            lp.height = binding.contentView.height
                        }
                        (event.y - downY) / rangeTime > 0.2 -> {
                            lp.height = dpToPx(250f)
                        }
                        //top: scroll full screen
                        (lp.height > binding.contentView.height - dpToPx(200f)) -> {
                            setHeightPopupGif(EnumStatePopup.FULL_SCREEN.value)
                        }
                        //down to pin 250
                        lp.height < binding.contentView.height - dpToPx(200f) -> {
                            setHeightPopupGif(EnumStatePopup.COLLAPSE.value)
                        }
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    downY = event.y
                    time = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    lp.height += (downY - event.y).toInt()
                }
            }
            Log.d("####", "bottomSheetGifPhy: ${lp.height}")
            binding.bottomSheetGifPhy.layoutParams = lp
            true
        }
    }


    override fun onDestroy() {
        player.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        player.onPause()
    }

    override fun onResume() {
        super.onResume()
        player.onResume()
    }

    private fun getGifSelectionListener() = object : GiphyDialogFragment.GifSelectionListener {
        override fun onGifSelected(
            media: Media,
            searchTerm: String?,
            selectedContentType: GPHContentType
        ) {
            Timber.d(TAG, "onGifSelected")
            if (selectedContentType == GPHContentType.clips && media.isVideo) {
                messageItems.forEach {
                    (it as? ClipItem)?.autoPlay = false
                }
                messageItems.add(ClipItem(media, Author.Me, autoPlay = true))
            } else {
                messageItems.add(GifItem(media, Author.Me))
            }
            feedAdapter?.notifyItemInserted(messageItems.size - 1)
            contentType = selectedContentType
        }

        override fun onDismissed(selectedContentType: GPHContentType) {
            Timber.d(TAG, "onDismissed")
            contentType = selectedContentType
        }

        override fun didSearchTerm(term: String) {
            Timber.d(TAG, "didSearchTerm $term")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.demo_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> showSettingsDialog()
            R.id.action_grid -> openGridViewDemo()
            R.id.action_grid_view -> openGridViewExtensionsDemo()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun openGridViewDemo(): Boolean {
        val intent = Intent(this, GridViewSetupActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun openGridViewExtensionsDemo(): Boolean {
        val intent = Intent(this, GridViewExtensionsActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun setupFeed() {
        messageItems.add(
            MessageItem(
                "Hi there! The SDK is perfect for many contexts, including messaging, reactions, stories and other camera features. This is one example of how the GIPHY SDK can be used in a messaging app.",
                Author.GifBot
            )
        )
        messageItems.add(
            MessageItem(
                "Tap the GIPHY button in the bottom left to see the SDK in action. Tap the settings icon in the top right to try out all of the customization options.",
                Author.GifBot
            )
        )
        if (YOUR_API_KEY == INVALID_KEY) {
            messageItems.add(InvalidKeyItem(Author.GifBot))
        }
        feedAdapter = MessageFeedAdapter(messageItems)
        feedAdapter?.itemSelectedListener = ::onGifSelected
        feedAdapter?.adapterHelper?.player = player
        feedAdapter?.adapterHelper?.clipsPlaybackSetting = clipsPlaybackSetting

        binding.messageFeed.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.messageFeed.adapter = feedAdapter
    }

    private fun createVideoPlayer(): GPHAbstractVideoPlayer {
        val player = VideoPlayerExoPlayer2181Impl(null, true)
        player.addListener { playerState ->
            when (playerState) {
                is GPHVideoPlayerState.MediaChanged -> {
                    val position = messageItems.map {
                        if (it is ClipItem) {
                            return@map it.media
                        }
                        return@map null
                    }.indexOfFirst {
                        it?.id == playerState.media.id
                    }
                    if (position > -1) {
                        binding.messageFeed.smoothScrollToPosition(position)
                    }
                }
                else -> return@addListener
            }
        }
        return player
    }

    private fun onGifSelected(itemData: FeedDataItem) {
        if (itemData is MessageItem) {
            Timber.d("onItemSelected ${itemData.text}")
        } else if (itemData is InvalidKeyItem) {
            Timber.d("onItemSelected InvalidKeyItem")
        } else if (itemData is GifItem) {
            Timber.d("onItemSelected ${itemData.media}")
        } else if (itemData is ClipItem) {
            Timber.d("onItemSelected ${itemData.media}")
            showVideoPlayerDialog(itemData.media)
        }
    }

    private fun showSettingsDialog(): Boolean {
        val dialog = SettingsDialogFragment.newInstance(settings, clipsPlaybackSetting)
        dialog.dismissListener = ::applyNewSettings
        dialog.show(supportFragmentManager, "settings_dialog")
        return true
    }

    private fun showVideoPlayerDialog(media: Media): Boolean {
        val dialog = ClipDialogFragment.newInstance(media)
        dialog.show(supportFragmentManager, "video_player_dialog")
        return true
    }

    private fun applyNewSettings(
        settings: GPHSettings,
        clipsPlaybackSetting: SettingsDialogFragment.ClipsPlaybackSetting
    ) {
        this.settings = settings
        this.clipsPlaybackSetting = clipsPlaybackSetting
        feedAdapter?.adapterHelper?.clipsPlaybackSetting = clipsPlaybackSetting
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun isKeyboardVisible(attachedView: View): Boolean {
        val insets = ViewCompat.getRootWindowInsets(attachedView)
        return insets?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
    }

    fun getKeyboardHeight(attachedView: View): Int {
        val insets = ViewCompat.getRootWindowInsets(attachedView)
        return insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
    }

    private fun dismissKeyboard() {
        val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.contentView.windowToken, 0)
    }

    private fun setHeightPopupGif(state: Int) {
        val lp = binding.bottomSheetGifPhy.layoutParams
        when (state) {
            EnumStatePopup.COLLAPSE.value -> {
                stateOfPopup = EnumStatePopup.COLLAPSE.value
                lp.height = dpToPx(250f)
                binding.bottomSheetGifPhy.layoutParams = lp
                bottomSheetGifPhy?.setState(EnumStatePopup.COLLAPSE.value)
            }
            EnumStatePopup.FULL_SCREEN.value -> {
                stateOfPopup = EnumStatePopup.FULL_SCREEN.value
                lp.height = binding.contentView.height
                binding.bottomSheetGifPhy.layoutParams = lp
                bottomSheetGifPhy?.setState(EnumStatePopup.FULL_SCREEN.value)
            }
            else -> {
                bottomSheetGifPhy?.setState(EnumStatePopup.HIDE.value)
            }
        }
    }
}
