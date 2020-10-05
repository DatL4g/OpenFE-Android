package de.datlag.openfe.bottomsheets

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
import de.datlag.openfe.commons.saveContext
import de.datlag.openfe.commons.setMargin
import de.datlag.openfe.commons.show
import de.datlag.openfe.databinding.FileProgressSheetBinding
import de.datlag.openfe.enums.MarginSide
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class FileProgressSheet : BottomSheetDialogFragment() {

    var binding: FileProgressSheetBinding? = null

    var title: String = String()
        set(value) = with(binding) {
            field = value
            this?.fileProgressSheetTitle?.text = field
        }

    var text: String = String()
        set(value) = with(binding) {
            field = value
            this?.fileProgressSheetText?.text = field
        }

    var leftText: String = String()
        set(value) = with(binding) {
            field = value
            this?.fileProgressSheetButtonLeft?.text = field
            if (!field.isNotCleared()) {
                this?.fileProgressSheetButtonLeft?.hide()
                this?.fileProgressSheetButtonRight?.setMargin(0, MarginSide.LEFT, MarginSide.RIGHT)
            } else {
                this?.fileProgressSheetButtonLeft?.show()
                context?.let { this?.fileProgressSheetButtonRight?.setMargin(it.getDimenInPixel(R.dimen.fileProgressSheetButtonSideMargin), *MarginSide.horizontal()) }
            }
        }

    var rightText: String = String()
        set(value) = with(binding) {
            field = value
            this?.fileProgressSheetButtonRight?.text = field
            if (!field.isNotCleared()) {
                this?.fileProgressSheetButtonRight?.hide()
                this?.fileProgressSheetButtonLeft?.setMargin(0, MarginSide.LEFT, MarginSide.RIGHT)
            } else {
                this?.fileProgressSheetButtonRight?.show()
                context?.let { this?.fileProgressSheetButtonLeft?.setMargin(it.getDimenInPixel(R.dimen.fileProgressSheetButtonSideMargin), *MarginSide.horizontal()) }
            }
        }

    var leftClickListener: ((view: View) -> Unit)? = null
        set(value) = with(binding) {
            field = value
            this?.fileProgressSheetButtonLeft?.setOnClickListener {
                leftClickListener?.invoke(it)
                if (closeOnLeftClick) {
                    this@FileProgressSheet.dismiss()
                    this@FileProgressSheet.dialog?.dismiss()
                }
            }
        }

    var rightClickListener: ((view: View) -> Unit)? = null
        set(value) = with(binding) {
            field = value
            this?.fileProgressSheetButtonRight?.setOnClickListener {
                rightClickListener?.invoke(it)
                if (closeOnRightClick) {
                    this@FileProgressSheet.dismiss()
                    this@FileProgressSheet.dialog?.dismiss()
                }
            }
        }

    var closeOnLeftClick: Boolean = false
    var closeOnRightClick: Boolean = false

    var updateable: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FileProgressSheetBinding.inflate(inflater, container, false)

        if (saveContext.packageManager.isTelevision()) {
            dialog?.setOnShowListener {
                it.expand()
            }
        }
        isCancelable = false

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateable?.invoke()
        initViews()
    }

    private fun initViews() = with(binding!!) {
        fileProgressSheetTitle.text = title
        fileProgressSheetText.text = text
        fileProgressSheetButtonLeft.text = leftText
        fileProgressSheetButtonRight.text = rightText

        fileProgressSheetButtonLeft.setOnClickListener {
            leftClickListener?.invoke(it)
            if (closeOnLeftClick) {
                this@FileProgressSheet.dismiss()
                this@FileProgressSheet.dialog?.dismiss()
            }
        }

        fileProgressSheetButtonRight.setOnClickListener {
            rightClickListener?.invoke(it)
            if (closeOnRightClick) {
                this@FileProgressSheet.dismiss()
                this@FileProgressSheet.dialog?.dismiss()
            }
        }

        if (!leftText.isNotCleared()) {
            fileProgressSheetButtonLeft.hide()
            fileProgressSheetButtonRight.setMargin(0, MarginSide.LEFT, MarginSide.RIGHT)
        } else {
            fileProgressSheetButtonLeft.show()
            fileProgressSheetButtonRight.setMargin(saveContext.getDimenInPixel(R.dimen.fileProgressSheetButtonSideMargin), *MarginSide.horizontal())
        }

        if (!rightText.isNotCleared()) {
            fileProgressSheetButtonRight.hide()
            fileProgressSheetButtonLeft.setMargin(0, MarginSide.LEFT, MarginSide.RIGHT)
        } else {
            fileProgressSheetButtonRight.show()
            fileProgressSheetButtonLeft.setMargin(saveContext.getDimenInPixel(R.dimen.fileProgressSheetButtonSideMargin), *MarginSide.horizontal())
        }

        fileProgressBar.max = 0
        fileProgressBar.step = 0
        fileProgressBar.progressPerBar = true
    }

    fun updateProgressList(newArray: FloatArray) = with(binding) {
        this?.fileProgressBar?.updateProgressPerBarList(newArray)
    }

    companion object {
        fun newInstance() = FileProgressSheet()
    }
}
