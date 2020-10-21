package de.datlag.openfe.fragments.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.R
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.databinding.FragmentVideoViewBinding
import de.datlag.openfe.extend.AdvancedFragment
import io.michaelrocks.paranoid.Obfuscate

@AndroidEntryPoint
@Obfuscate
class VideoFragment : AdvancedFragment {

    constructor() : super(R.layout.fragment_video_view) { }
    constructor(intent: Intent?) : super(R.layout.fragment_video_view) {
        this.intent = intent
    }

    private var intent: Intent? = null
    private val binding: FragmentVideoViewBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val player = SimpleExoPlayer.Builder(safeContext).build()
        binding.viewerVideo.player = player

        intent?.data?.let {
            player.setMediaItem(MediaItem.fromUri(it))
        }
        player.prepare()
    }

    override fun onResume() {
        super.onResume()
        binding.viewerVideo.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.viewerVideo.onPause()
    }
}
