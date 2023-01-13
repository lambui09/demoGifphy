package com.giphy.sdk.uidemo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
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
import com.giphy.sdk.uidemo.context.showSoftKeyboard
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
        val width: Int = this.resources.displayMetrics.widthPixels
        val height: Int = this.resources.displayMetrics.heightPixels
        Giphy.configure(this, YOUR_API_KEY, true)
        VideoCache.initialize(this, 100 * 1024 * 1024)
        binding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomSheetGifPhy = PickGifBottomSheetDialog.newInstance(
            pickGif = { media ->
                dismissKeyboard()
                if (stateOfPopup != EnumStatePopup.COLLAPSE.value) {
                    setHeightPopupGif(EnumStatePopup.COLLAPSE.value)
                }
            },
            focusEdittext = { isFocus ->
                if (isFocus && stateOfPopup != EnumStatePopup.FULL_SCREEN.value) {
                    setHeightPopupGif(EnumStatePopup.FULL_SCREEN.value)
                    showSoftKeyboard(this)
                }
            },
            onHeightKeyboard = {
                if (isKeyboardVisible(binding.contentView)) {
                    dismissKeyboard()
                }
            },
            onCollapsePopup = {
                setHeightPopupGif(EnumStatePopup.COLLAPSE.value)
            }
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
                    lp?.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, dpToPx(300f))
                    binding.bottomSheetGifPhy.visibility = View.VISIBLE
                    stateOfPopup = EnumStatePopup.COLLAPSE.value
                    layoutParams = lp
                } else {
                    val lp = layoutParams as? MarginLayoutParams
                    lp?.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, dpToPx(0f))
                    binding.bottomSheetGifPhy.visibility = View.GONE
                    stateOfPopup = EnumStatePopup.HIDE.value
                    layoutParams = lp
                }
                isShow = !isShow
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleFragBottomSheet() {
        var downY = 0f
        var time = 0L
        binding.bottomSheetGifPhy.setOnTouchListener { v, event ->
            val lp = binding.bottomSheetGifPhy.layoutParams
            var heightPopup = binding.bottomSheetGifPhy.height
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    val rangeTime = System.currentTimeMillis() - time
                    Log.d("####", "handleFragBottomSheet: ${(downY - event.y) / rangeTime}")
                    when {
                        //scroll fast
                        (downY - event.y) / rangeTime > 0.2 -> {
                            Log.d("####", "ACTION_UP TOP")
                            setHeightPopupGif(EnumStatePopup.FULL_SCREEN.value)
                        }
                        //scroll bottom
                        (event.y - downY) / rangeTime > 0.2 -> {
                            Log.d("####", "ACTION_UP BOTTOM")
                            setHeightPopupGif(EnumStatePopup.COLLAPSE.value)
                        }
                        //top: scroll full screen
                        (heightPopup > binding.contentView.height - dpToPx(200f)) -> {
                            Log.d("####", "ACTION_UP FULL")
                            setHeightPopupGif(EnumStatePopup.FULL_SCREEN.value)
                        }
                        //down to pin 300
                        heightPopup < binding.contentView.height - dpToPx(200f) -> {
                            Log.d("####", "ACTION_UP COLLAPSE")
                            setHeightPopupGif(EnumStatePopup.COLLAPSE.value)
                        }
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    Log.d("####", "ACTION_DOWN")
                    downY = event.y
                    time = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (heightPopup >= dpToPx(300f)) {
                        Log.d(
                            "####",
                            "ACTION_MOVE HEIGHT > 300F: ${heightPopup} --Y: ${downY - event.y}"
                        )
                        lp.height += (downY - event.y).toInt()
                    } else {
                        Log.d("####", "ACTION_MOVE HEIGHT: ${heightPopup} --Y: ${downY - event.y}")
                        return@setOnTouchListener true
                    }
                }
            }
            binding.bottomSheetGifPhy.layoutParams = lp
            return@setOnTouchListener true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isKeyboardVisible(binding.contentView)) {
                dismissKeyboard()
            } else {
                setHeightPopupGif(EnumStatePopup.COLLAPSE.value)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
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
                dismissKeyboard()
                stateOfPopup = EnumStatePopup.COLLAPSE.value
                lp.height = dpToPx(300f)
                binding.bottomSheetGifPhy.layoutParams = lp
                bottomSheetGifPhy?.setState(EnumStatePopup.COLLAPSE.value)
            }
            EnumStatePopup.FULL_SCREEN.value -> {
                stateOfPopup = EnumStatePopup.FULL_SCREEN.value
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT
                binding.bottomSheetGifPhy.layoutParams = lp
                bottomSheetGifPhy?.setState(EnumStatePopup.FULL_SCREEN.value)
            }
            else -> {
                bottomSheetGifPhy?.setState(EnumStatePopup.HIDE.value)
            }
        }
    }

    fun expand(v: View) {
        if (v.visibility == View.VISIBLE) return
        val durations: Long
        val matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            (v.parent as View).width,
            View.MeasureSpec.EXACTLY
        )
        val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            0,
            View.MeasureSpec.UNSPECIFIED
        )
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = v.measuredHeight

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.layoutParams.height = 1
        v.visibility = View.VISIBLE
        durations = ((targetHeight / v.context.resources
            .displayMetrics.density)).toLong()

        v.alpha = 0.0F
        v.visibility = View.VISIBLE
        v.animate().alpha(1.0F).setDuration(durations).setListener(null)

        val a: Animation = object : Animation() {
            override fun applyTransformation(
                interpolatedTime: Float,
                t: Transformation
            ) {
                v.layoutParams.height =
                    if (interpolatedTime == 1f) LinearLayout.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        // Expansion speed of 1dp/ms
        a.duration = durations
        v.startAnimation(a)
    }

    fun collapse(v: View) {
        if (v.visibility == View.GONE) return
        val durations: Long
        val initialHeight = v.measuredHeight
        val a: Animation = object : Animation() {
            override fun applyTransformation(
                interpolatedTime: Float,
                t: Transformation
            ) {
                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height =
                        initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        durations = (initialHeight / v.context.resources
            .displayMetrics.density).toLong()

        v.alpha = 1.0F
        v.animate().alpha(0.0F).setDuration(durations)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    v.visibility = View.GONE
                    v.alpha = 1.0F
                }
            })

        // Collapse speed of 1dp/ms
        a.duration = durations
        v.startAnimation(a)
    }
}
