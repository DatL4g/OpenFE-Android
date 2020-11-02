package de.datlag.openfe.bottomsheets

import de.datlag.openfe.R
import de.datlag.openfe.databinding.SheetFileCreateBinding
import de.datlag.openfe.extend.DefaultBottomSheet
import io.michaelrocks.paranoid.Obfuscate
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
class FileCreateSheet : DefaultBottomSheet<SheetFileCreateBinding>(SheetFileCreateBinding::class.java, R.layout.sheet_file_create) {

    override fun titleChange(text: String?) = with(binding) {
        createFileSheetTitle.text = text
    }

    override fun textChange(text: String?) = with(binding) {
        createFileSheetText.text = text
    }

    override fun leftButtonTextChange(text: String?) = with(binding) {
        createFileSheetButtonLeft.text = text
    }

    override fun rightButtonTextChange(text: String?) = with(binding) {
        createFileSheetButtonRight.text = text
    }

    override fun leftButtonClickChange() = with(binding) {
        createFileSheetButtonLeft.setOnClickListener(leftButtonClick)
    }

    override fun rightButtonClickChange() = with(binding) {
        createFileSheetButtonRight.setOnClickListener(rightButtonClick)
    }
}
