package de.datlag.openfe.bottomsheets

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.openfe.commons.expand
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.isTelevision
import de.datlag.openfe.commons.saveContext
import de.datlag.openfe.databinding.ConfirmActionSheetBinding
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
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
                this?.confirmSheetButtonLeft?.visibility = View.GONE
            }
        }

    var rightText: String = String()
        set(value) = with(binding) {
            field = value
            this?.confirmSheetButtonRight?.text = field
            if (!field.isNotCleared()) {
                this?.confirmSheetButtonRight?.visibility = View.GONE
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

        if (saveContext.packageManager.isTelevision()) {
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
            confirmSheetButtonLeft.visibility = View.GONE
        }
        if (!rightText.isNotCleared()) {
            confirmSheetButtonRight.visibility = View.GONE
        }
    }

    companion object {
        fun newInstance() = ConfirmActionSheet()
    }
}
