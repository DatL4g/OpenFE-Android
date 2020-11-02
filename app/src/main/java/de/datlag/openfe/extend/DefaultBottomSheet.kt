package de.datlag.openfe.extend

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.openfe.commons.expand
import de.datlag.openfe.commons.isTelevision
import de.datlag.openfe.commons.safeContext
import io.michaelrocks.paranoid.Obfuscate
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
abstract class DefaultBottomSheet<T : ViewBinding> constructor(
    val type: Class<T>,
    @LayoutRes private val layoutId: Int
) : BottomSheetDialogFragment() {

    protected val binding: T by viewBinding(type)
    private var created: Boolean = false

    var title: String = String()
        set(value) {
            field = value
            if (created) {
                titleChange(field)
            }
        }

    var text: String = String()
        set(value) {
            field = value
            if (created) {
                textChange(field)
            }
        }

    var leftButtonText: String? = String()
        set(value) {
            field = value
            if (created) {
                leftButtonTextChange(field)
            }
        }

    var rightButtonText: String? = String()
        set(value) {
            field = value
            if (created) {
                rightButtonTextChange(field)
            }
        }

    var leftButtonClick = View.OnClickListener { if (closeOnLeftClick) { close() } }
        private set(value) {
            field = value
            if (created) {
                rightButtonClickChange()
            }
        }

    var rightButtonClick = View.OnClickListener { if (closeOnRightClick) { close() } }
        private set(value) {
            field = value
            if (created) {
                rightButtonClickChange()
            }
        }

    var closeOnLeftClick: Boolean = false
    var closeOnRightClick: Boolean = false

    protected var cancelListener: ((dialogInterface: DialogInterface) -> Unit)? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layoutId, container, false)

        if (safeContext.packageManager.isTelevision()) {
            dialog?.setOnShowListener {
                it.expand()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        created = true
        initViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        created = false
    }

    override fun onDestroy() {
        super.onDestroy()
        created = false
    }

    protected open fun initViews() {
        titleChange(title)
        textChange(text)
        leftButtonTextChange(leftButtonText)
        rightButtonTextChange(rightButtonText)
        leftButtonClickChange()
        rightButtonClickChange()
    }

    protected abstract fun titleChange(text: String?)

    protected abstract fun textChange(text: String?)

    protected abstract fun leftButtonTextChange(text: String?)

    protected abstract fun rightButtonTextChange(text: String?)

    protected abstract fun leftButtonClickChange()

    protected abstract fun rightButtonClickChange()

    private fun close() {
        this@DefaultBottomSheet.dismiss()
        this@DefaultBottomSheet.dialog?.dismiss()
    }

    fun setLeftButtonClickListener(listener: ((view: View) -> Unit)? = null) {
        leftButtonClick = View.OnClickListener {
            if (closeOnLeftClick) {
                close()
            }
            listener?.invoke(it)
        }
    }

    fun setRightButtonClickListener(listener: ((view: View) -> Unit)? = null) {
        rightButtonClick = View.OnClickListener {
            if (closeOnRightClick) {
                close()
            }
            listener?.invoke(it)
        }
    }

    fun setOnCancelListener(listener: ((dialogInterface: DialogInterface) -> Unit)? = null) {
        cancelListener = listener
    }
}
