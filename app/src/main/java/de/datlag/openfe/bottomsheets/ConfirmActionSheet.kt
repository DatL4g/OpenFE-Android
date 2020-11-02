package de.datlag.openfe.bottomsheets

import de.datlag.openfe.R
import de.datlag.openfe.commons.getDimenInPixel
import de.datlag.openfe.commons.hide
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.commons.setMargin
import de.datlag.openfe.commons.show
import de.datlag.openfe.databinding.SheetConfirmActionBinding
import de.datlag.openfe.enums.MarginSide
import de.datlag.openfe.extend.DefaultBottomSheet
import io.michaelrocks.paranoid.Obfuscate
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
class ConfirmActionSheet : DefaultBottomSheet<SheetConfirmActionBinding>(SheetConfirmActionBinding::class.java, R.layout.sheet_confirm_action) {

    override fun titleChange(text: String?) = with(binding) {
        confirmSheetTitle.text = text
    }

    override fun textChange(text: String?) = with(binding) {
        confirmSheetText.text = text
    }

    override fun leftButtonTextChange(text: String?) = with(binding) {
        confirmSheetButtonLeft.text = text

        if (text.isNotCleared()) {
            confirmSheetButtonLeft.show()
            confirmSheetButtonRight.setMargin(safeContext.getDimenInPixel(R.dimen.confirmSheetButtonSideMargin), *MarginSide.horizontal())
        } else {
            confirmSheetButtonLeft.hide()
            confirmSheetButtonRight.setMargin(0, *MarginSide.horizontal())
        }
    }

    override fun rightButtonTextChange(text: String?) = with(binding) {
        confirmSheetButtonRight.text = text

        if (text.isNotCleared()) {
            confirmSheetButtonRight.show()
            confirmSheetButtonLeft.setMargin(safeContext.getDimenInPixel(R.dimen.confirmSheetButtonSideMargin), *MarginSide.horizontal())
        } else {
            confirmSheetButtonRight.hide()
            confirmSheetButtonLeft.setMargin(0, *MarginSide.horizontal())
        }
    }

    override fun leftButtonClickChange() = with(binding) {
        confirmSheetButtonLeft.setOnClickListener(leftButtonClick)
    }

    override fun rightButtonClickChange() = with(binding) {
        confirmSheetButtonRight.setOnClickListener(rightButtonClick)
    }

    companion object {
        fun newInstance() = ConfirmActionSheet()

        fun githubLoginInstance(): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "GitHub Login"
            instance.text = "Become a contributor so that the project is improved.\n" +
                "At the same time, the advertising will be removed if you have worked on the project."
            instance.leftButtonText = "Close"
            instance.rightButtonText = "Login"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }

        fun githubLogoutInstance(): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "GitHub Logout"
            instance.text = "If you are a contributor to the project and you logout, you will be shown advertising again."
            instance.leftButtonText = "Close"
            instance.rightButtonText = "Logout"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }

        fun githubRevokeAccessInstance(): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "Revoke GitHub Access"
            instance.text = "Do you want to revoke the GitHub OAuth access?\n(Required if you want to switch to another user)"
            instance.leftButtonText = "Close"
            instance.rightButtonText = "Revoke Access"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }

        fun backupConfirmInstance(possible: Boolean): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "Backup"
            if (possible) {
                instance.text = "Do you want to create a backup?"
                instance.rightButtonText = "Backup"
                instance.leftButtonText = "Continue"
            } else {
                instance.text = "Creating Backup not possible because of not enough free space. Continue?"
                instance.rightButtonText = "Continue"
                instance.leftButtonText = null
            }
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }

        fun backupInstance(name: String): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "Backup $name"
            instance.text = "The Backup file is created in the OpenFE folder of your Internal Storage"
            instance.leftButtonText = "Cancel"
            instance.rightButtonText = "Backup"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }

        fun deleteInstance(itemSize: Int, backupCreated: Boolean): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "Delete File${if (itemSize > 0) "s" else String()}"
            instance.text = "This can not be undone!\nAre you sure you want to delete these files and folders${if (itemSize > 0) "\nTotal size: $itemSize" else String()}"
            instance.leftButtonText = "Cancel"
            instance.rightButtonText = "Delete"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }
    }
}
