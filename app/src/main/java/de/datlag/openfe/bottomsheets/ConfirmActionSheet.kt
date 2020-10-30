package de.datlag.openfe.bottomsheets

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.openfe.R
import de.datlag.openfe.commons.expand
import de.datlag.openfe.commons.getDimenInPixel
import de.datlag.openfe.commons.hide
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.isTelevision
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.commons.setMargin
import de.datlag.openfe.commons.show
import de.datlag.openfe.databinding.ConfirmActionSheetBinding
import de.datlag.openfe.enums.MarginSide
import io.michaelrocks.paranoid.Obfuscate
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
class ConfirmActionSheet : BottomSheetDialogFragment() {

    private var binding: ConfirmActionSheetBinding? = null

    var title: String = String()
        set(value) = with(binding) {
            field = value
            this?.confirmSheetTitle?.text = field
        }

    var text: String = String()
        set(value) = with(binding) {
            field = value
            this?.confirmSheetText?.text = field
        }

    var leftText: String = String()
        set(value) = with(binding) {
            field = value
            this?.confirmSheetButtonLeft?.text = field
            if (!field.isNotCleared()) {
                this?.confirmSheetButtonLeft?.hide()
                this?.confirmSheetButtonRight?.setMargin(0, *MarginSide.horizontal())
            } else {
                this?.confirmSheetButtonLeft?.show()
                context?.let { this?.confirmSheetButtonRight?.setMargin(it.getDimenInPixel(R.dimen.confirmSheetButtonSideMargin), *MarginSide.horizontal()) }
            }
        }

    var rightText: String = String()
        set(value) = with(binding) {
            field = value
            this?.confirmSheetButtonRight?.text = field
            if (!field.isNotCleared()) {
                this?.confirmSheetButtonRight?.hide()
                this?.confirmSheetButtonLeft?.setMargin(0, *MarginSide.horizontal())
            } else {
                this?.confirmSheetButtonRight?.show()
                context?.let { this?.confirmSheetButtonLeft?.setMargin(it.getDimenInPixel(R.dimen.confirmSheetButtonSideMargin), *MarginSide.horizontal()) }
            }
        }

    var leftClickListener: ((view: View) -> Unit)? = null
        set(value) = with(binding) {
            field = value
            this?.confirmSheetButtonLeft?.setOnClickListener {
                leftClickListener?.invoke(it)
                if (closeOnLeftClick) {
                    this@ConfirmActionSheet.dialog?.dismiss()
                }
            }
        }

    var rightClickListener: ((view: View) -> Unit)? = null
        set(value) = with(binding) {
            field = value
            this?.confirmSheetButtonRight?.setOnClickListener {
                rightClickListener?.invoke(it)
                if (closeOnRightClick) {
                    this@ConfirmActionSheet.dismiss()
                }
            }
        }

    var closeOnLeftClick: Boolean = false
    var closeOnRightClick: Boolean = false
    var cancelListener: ((dialogInterface: DialogInterface) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConfirmActionSheetBinding.inflate(inflater, container, false)

        if (safeContext.packageManager.isTelevision()) {
            dialog?.setOnShowListener {
                it.expand()
            }
        }

        return binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val touchOutsideView = dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.touch_outside)
        touchOutsideView?.setOnClickListener {
            dialog?.cancel()
        }
        dialog?.setOnCancelListener {
            cancelListener?.invoke(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() = with(binding!!) {
        confirmSheetTitle.text = title
        confirmSheetText.text = text
        confirmSheetButtonLeft.text = leftText
        confirmSheetButtonRight.text = rightText

        confirmSheetButtonLeft.setOnClickListener {
            leftClickListener?.invoke(it)
            if (closeOnLeftClick) {
                this@ConfirmActionSheet.dialog?.dismiss()
            }
        }

        confirmSheetButtonRight.setOnClickListener {
            rightClickListener?.invoke(it)
            if (closeOnRightClick) {
                this@ConfirmActionSheet.dismiss()
            }
        }

        if (!leftText.isNotCleared()) {
            confirmSheetButtonLeft.hide()
            confirmSheetButtonRight.setMargin(0, *MarginSide.horizontal())
        } else {
            confirmSheetButtonLeft.show()
            confirmSheetButtonRight.setMargin(safeContext.getDimenInPixel(R.dimen.confirmSheetButtonSideMargin), *MarginSide.horizontal())
        }

        if (!rightText.isNotCleared()) {
            confirmSheetButtonRight.hide()
            confirmSheetButtonLeft.setMargin(0, *MarginSide.horizontal())
        } else {
            confirmSheetButtonRight.show()
            confirmSheetButtonLeft.setMargin(safeContext.getDimenInPixel(R.dimen.confirmSheetButtonSideMargin), *MarginSide.horizontal())
        }
    }

    companion object {
        fun newInstance() = ConfirmActionSheet()

        fun githubLoginInstance(): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "GitHub Login"
            instance.text = "Become a contributor so that the project is improved.\n" +
                    "At the same time, the advertising will be removed if you have worked on the project."
            instance.leftText = "Close"
            instance.rightText = "Login"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }

        fun githubLogoutInstance(): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "GitHub Logout"
            instance.text = "If you are a contributor to the project and you logout, you will be shown advertising again."
            instance.leftText = "Close"
            instance.rightText = "Logout"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }

        fun githubRevokeAccessInstance(): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "Revoke GitHub Access"
            instance.text = "Do you want to revoke the GitHub OAuth access?\n(Required if you want to switch to another user)"
            instance.leftText = "Close"
            instance.rightText = "Revoke Access"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }

        fun backupInstance(name: String): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "Backup $name"
            instance.text = "The Backup file is created in the OpenFE folder of your Internal Storage"
            instance.leftText = "Cancel"
            instance.rightText = "Backup"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }

        fun pasteInstance(multiple: Boolean): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "Paste File${if (multiple) "s" else String()} here?"
            instance.text = "Are you sure that you want to paste the copied file${if (multiple) "s" else String()} in this directory?"
            instance.leftText = "Cancel"
            instance.rightText = "Paste"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }

        fun deleteInstance(multiple: Boolean, itemSize: Int = 0): ConfirmActionSheet {
            val instance = ConfirmActionSheet()
            instance.title = "Delete File${if (multiple) "s" else String()}"
            instance.text = "This can not be undone!\nAre you sure you want to delete these files and folders${if (itemSize > 0) "\nTotal size: $itemSize" else String()}"
            instance.leftText = "Cancel"
            instance.rightText = "Delete"
            instance.closeOnLeftClick = true
            instance.closeOnRightClick = true
            return instance
        }
    }
}
