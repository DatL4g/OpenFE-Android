package de.datlag.openfe.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.openfe.R
import de.datlag.openfe.commons.expand
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.isTelevision
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.databinding.AppsActionInfoSheetBinding
import de.datlag.openfe.recycler.data.AppItem
import de.datlag.openfe.util.NumberUtils.convertToDate
import io.michaelrocks.paranoid.Obfuscate
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
class AppsActionInfoSheet : BottomSheetDialogFragment() {

    private val binding: AppsActionInfoSheetBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (safeContext.packageManager.isTelevision()) {
            dialog?.setOnShowListener {
                it.expand()
            }
        }

        return inflater.inflate(R.layout.apps_action_info_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        infoSheetIcon.setImageDrawable(appData.icon)
        infoSheetName.text = appData.name
        infoSheetPackage.text = appData.packageName
        infoSheetDescriptionData.text = if (appData.description.isNotCleared() && !appData.description.equals("null", true)) appData.description else "(none)"
        infoSheetCategoryData.text = appData.category.toString(safeContext)
        infoSheetInstallLocationData.text = appData.installLocation.toString(safeContext)
        infoSheetFirstInstallData.text = convertToDate(appData.firstInstall)
        infoSheetLastUpdateData.text = convertToDate(appData.lastUpdate)
        infoSheetVersionCodeData.text = appData.versionCode.toString()
        infoSheetVersionNameData.text = appData.versionName
    }

    companion object {
        lateinit var appData: AppItem

        fun newInstance(appData: AppItem): AppsActionInfoSheet {
            this.appData = appData
            return AppsActionInfoSheet()
        }
    }
}
