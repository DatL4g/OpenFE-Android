package de.datlag.openfe.util

import android.Manifest
import android.content.Context
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.single.PermissionListener
import de.datlag.openfe.bottomsheets.ConfirmActionSheet
import io.michaelrocks.paranoid.Obfuscate
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
object PermissionChecker {

    fun checkReadStorage(context: Context, permissionListener: PermissionListener) {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(permissionListener).check()
    }

    fun checkWriteStorage(context: Context, permissionListener: PermissionListener) {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(permissionListener).check()
    }

    fun storagePermissionSheet(context: Context, p1: PermissionToken?): ConfirmActionSheet {
        val confirmActionSheet = ConfirmActionSheet.newInstance()
        confirmActionSheet.title = "Storage Permission"
        confirmActionSheet.text = "This permission is required to read the Files and Folders of the selected Storage.\nOtherwise this feature cannot be used!"
        confirmActionSheet.leftButtonText = "Cancel"
        confirmActionSheet.rightButtonText = "Grant"
        confirmActionSheet.setLeftButtonClickListener {
            p1?.cancelPermissionRequest()
        }
        confirmActionSheet.setRightButtonClickListener {
            p1?.continuePermissionRequest()
        }
        confirmActionSheet.closeOnLeftClick = true
        confirmActionSheet.closeOnRightClick = true
        confirmActionSheet.setOnCancelListener {
            p1?.cancelPermissionRequest()
        }
        return confirmActionSheet
    }
}
