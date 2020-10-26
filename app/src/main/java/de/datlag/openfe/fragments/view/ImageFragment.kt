package de.datlag.openfe.fragments.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.R
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.databinding.FragmentImageViewBinding
import de.datlag.openfe.extend.AdvancedFragment
import io.michaelrocks.paranoid.Obfuscate
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@AndroidEntryPoint
@Obfuscate
class ImageFragment : AdvancedFragment {

    constructor() : super(R.layout.fragment_image_view) { }
    constructor(intent: Intent?) : super(R.layout.fragment_image_view) {
        this.intent = intent
    }

    private var intent: Intent? = null
    private val binding: FragmentImageViewBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() = with(binding) {
        intent?.let {
            Glide.with(safeContext)
                .load(it.data)
                .into(viewerImage)
        }
    }
}
