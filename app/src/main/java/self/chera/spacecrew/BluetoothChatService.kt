package self.chera.spacecrew

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.IOException
import java.util.UUID

private const val NAME = "BluetoothChat"
private val MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun createAcceptThread(bluetoothAdapter: BluetoothAdapter): AcceptThread {
    val mmServerSocket: BluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID)
    return AcceptThread(mmServerSocket);
}

class AcceptThread(private val mmServerSocket: BluetoothServerSocket) : Thread() {
    override fun run() {
        // Keep listening until exception occurs or a socket is returned.
        var shouldLoop = true
        while (shouldLoop) {
            val socket: BluetoothSocket? = try {
                mmServerSocket.accept()
            } catch (e: IOException) {
                Log.e(TAG, "Socket's accept() method failed", e)
                shouldLoop = false
                null
            }
            socket?.also {
//                manageMyConnectedSocket(it)
                mmServerSocket.close()
                shouldLoop = false
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    fun cancel() {
        try {
            mmServerSocket.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e)
        }
    }
}