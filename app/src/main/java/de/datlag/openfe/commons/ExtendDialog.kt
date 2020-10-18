@file:Obfuscate
package de.datlag.openfe.commons

import android.content.DialogInterface
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.michaelrocks.paranoid.Obfuscate

fun DialogInterface.expand() {
    if (this is BottomSheetDialog) {
        val bottomSheet = this
        val sheetInternal: View? = bottomSheet.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        sheetInternal?.let { sheet ->
            BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}
