package de.datlag.openfe.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.openfe.commons.expand
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.isTelevision
import de.datlag.openfe.commons.saveContext
import de.datlag.openfe.databinding.AppsActionInfoSheetBinding
import de.datlag.openfe.enums.AppInstallLocation
import de.datlag.openfe.enums.AppInstallLocation.AUTO
import de.datlag.openfe.enums.AppInstallLocation.INTERNAL_ONLY
import de.datlag.openfe.enums.AppInstallLocation.PREFER_EXTERNAL
import de.datlag.openfe.recycler.data.AppItem
import de.datlag.openfe.util.NumberUtils.convertToDate
import de.datlag.openfe.util.NumberUtils.getAppCategory
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class AppsActionInfoSheet : BottomSheetDialogFragment() {

    private lateinit var binding: AppsActionInfoSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AppsActionInfoSheetBinding.inflate(inflater, container, false)

        if (saveContext.packageManager.isTelevision()) {
            dialog?.setOnShowListener {
                it.expand()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        infoSheetIcon.setImageDrawable(appData.icon)
        infoSheetName.text = appData.name
        infoSheetPackage.text = appData.packageName
        infoSheetDescriptionData.text = if (appData.description.isNotCleared() && !appData.description.equals("null", true)) appData.description else "(none)"
        infoSheetCategoryData.text = appData.category.toString(saveContext)
        infoSheetInstallLocationData.text = appData.installLocation.toString(saveContext)
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
