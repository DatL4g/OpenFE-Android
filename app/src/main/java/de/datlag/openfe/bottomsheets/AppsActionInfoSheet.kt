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
import de.datlag.openfe.recycler.data.AppItem
import de.datlag.openfe.util.NumberUtils
import de.datlag.openfe.util.NumberUtils.CATEGORY_AUDIO
import de.datlag.openfe.util.NumberUtils.CATEGORY_GAME
import de.datlag.openfe.util.NumberUtils.CATEGORY_IMAGE
import de.datlag.openfe.util.NumberUtils.CATEGORY_MAPS
import de.datlag.openfe.util.NumberUtils.CATEGORY_NEWS
import de.datlag.openfe.util.NumberUtils.CATEGORY_PRODUCTIVITY
import de.datlag.openfe.util.NumberUtils.CATEGORY_SOCIAL
import de.datlag.openfe.util.NumberUtils.CATEGORY_VIDEO
import de.datlag.openfe.util.NumberUtils.INSTALL_LOCATION_INTERNAL_ONLY
import de.datlag.openfe.util.NumberUtils.INSTALL_LOCATION_PREFER_EXTERNAL
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
        infoSheetCategoryData.text = categoryToString(getAppCategory(appData.category))
        infoSheetInstallLocationData.text = installLocationToString(appData.installLocation)
        infoSheetFirstInstallData.text = convertToDate(appData.firstInstall)
        infoSheetLastUpdateData.text = convertToDate(appData.lastUpdate)
        infoSheetVersionCodeData.text = appData.versionCode.toString()
        infoSheetVersionNameData.text = appData.versionName
    }

    private fun categoryToString(@NumberUtils.AppCategory category: Int): String {
        return when (category) {
            CATEGORY_GAME -> "Game"
            CATEGORY_AUDIO -> "Audio"
            CATEGORY_VIDEO -> "Video"
            CATEGORY_IMAGE -> "Image"
            CATEGORY_SOCIAL -> "Social"
            CATEGORY_NEWS -> "News"
            CATEGORY_MAPS -> "Maps"
            CATEGORY_PRODUCTIVITY -> "Productivity"
            else -> "(undefined)"
        }
    }

    private fun installLocationToString(@NumberUtils.AppInstallLocation installLocation: Int): String {
        return when (installLocation) {
            INSTALL_LOCATION_INTERNAL_ONLY -> "Internal Only"
            INSTALL_LOCATION_PREFER_EXTERNAL -> "Prefer External"
            else -> "Automatic"
        }
    }

    companion object {
        lateinit var appData: AppItem

        fun newInstance(appData: AppItem): AppsActionInfoSheet {
            this.appData = appData
            return AppsActionInfoSheet()
        }
    }
}
