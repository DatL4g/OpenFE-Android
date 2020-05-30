package de.datlag.openfe.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class USBAttachementReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val usbDevice: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE) as UsbDevice?
    }
}
