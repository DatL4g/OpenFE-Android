package de.datlag.openfe.bottomsheets

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.openfe.commons.expand
import de.datlag.openfe.commons.isTelevision
import de.datlag.openfe.commons.saveContext
import de.datlag.openfe.databinding.ConfirmActionSheetBinding

class ConfirmActionSheet : BottomSheetDialogFragment() {

    private lateinit var binding: ConfirmActionSheetBinding
    var title: String = String()
    var text: String = String()
    var leftText: String = String()
    var rightText: String = String()
    var leftClickListener: ((view: View) -> Unit)? = null
    var rightClickListener: ((view: View) -> Unit)? = null
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

        return binding.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
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
    }

    companion object {
        fun newInstance() = ConfirmActionSheet()
    }

}