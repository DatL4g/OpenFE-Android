package de.datlag.openfe.util

import android.Manifest
import android.content.Context
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.single.PermissionListener

class PermissionChecker {

    companion object {

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

    }

}