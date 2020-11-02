package de.datlag.openfe.bottomsheets

import android.os.Bundle
import android.view.View
import de.datlag.openfe.R
import de.datlag.openfe.commons.getDimenInPixel
import de.datlag.openfe.commons.hide
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.commons.setMargin
import de.datlag.openfe.commons.show
import de.datlag.openfe.databinding.SheetFileProgressBinding
import de.datlag.openfe.enums.MarginSide
import de.datlag.openfe.extend.DefaultBottomSheet
import io.michaelrocks.paranoid.Obfuscate
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
class FileProgressSheet : DefaultBottomSheet<SheetFileProgressBinding>(SheetFileProgressBinding::class.java, R.layout.sheet_file_progress) {

    var updateable: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateable?.invoke()
        initViews()
    }

    override fun titleChange(text: String?) = with(binding) {
        fileProgressSheetTitle.text = text
    }

    override fun textChange(text: String?) = with(binding) {
        fileProgressSheetText.text = text
    }

    override fun leftButtonTextChange(text: String?) = with(binding) {
        fileProgressSheetButtonLeft.text = text

        if (text.isNotCleared()) {
            fileProgressSheetButtonLeft.show()
            fileProgressSheetButtonRight.setMargin(safeContext.getDimenInPixel(R.dimen.fileProgressSheetButtonSideMargin), *MarginSide.horizontal())
        } else {
            fileProgressSheetButtonLeft.hide()
            fileProgressSheetButtonRight.setMargin(0, *MarginSide.horizontal())
        }
    }

    override fun rightButtonTextChange(text: String?) = with(binding) {
        fileProgressSheetButtonRight.text = text

        if (text.isNotCleared()) {
            fileProgressSheetButtonRight.show()
            fileProgressSheetButtonLeft.setMargin(safeContext.getDimenInPixel(R.dimen.fileProgressSheetButtonSideMargin), *MarginSide.horizontal())
        } else {
            fileProgressSheetButtonRight.hide()
            fileProgressSheetButtonLeft.setMargin(0, *MarginSide.horizontal())
        }
    }

    override fun leftButtonClickChange() = with(binding) {
        fileProgressSheetButtonLeft.setOnClickListener(leftButtonClick)
    }

    override fun rightButtonClickChange() = with(binding) {
        fileProgressSheetButtonRight.setOnClickListener(rightButtonClick)
    }

    override fun initViews() = with(binding) {
        super.initViews()

        fileProgressBar.max = 0
        fileProgressBar.step = 0
        fileProgressBar.progressPerBar = true
    }

    fun updateProgressList(newArray: FloatArray) = with(binding) {
        fileProgressBar.updateProgressPerBarList(newArray)
    }

    companion object {
        fun newInstance() = FileProgressSheet()

        fun deleteInstance(itemSize: Int): FileProgressSheet {
            val instance = FileProgressSheet()
            instance.title = "Delete File${if (itemSize > 1) "s" else String()}..."
            instance.text = "Deleting $itemSize file${if (itemSize > 1) "s" else String()}.\nPlease wait..."
            instance.leftButtonText = "Cancel"
            instance.rightButtonText = "Background"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }
    }
}
