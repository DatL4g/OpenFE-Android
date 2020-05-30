package de.datlag.openfe.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.openfe.R
import de.datlag.openfe.recycler.data.AppItem
import kotlinx.android.synthetic.main.apps_action_info_sheet.*

class AppsActionInfoSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.apps_action_info_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        infoSheetIcon.setImageDrawable(appData.icon)
        infoSheetName.text = appData.name
    }

    companion object {
        lateinit var appData: AppItem

        fun newInstance(appData: AppItem): AppsActionInfoSheet {
            this.appData = appData
            return AppsActionInfoSheet()
        }
    }

}