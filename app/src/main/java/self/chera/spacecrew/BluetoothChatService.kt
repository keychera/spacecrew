package self.chera.spacecrew

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

private const val NAME_INSECURE = "BluetoothChatInsecure"
private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

data class AcceptCoroutine(
    val cancel: () -> Unit,
) {
    var open: Boolean = false
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
suspend fun acceptCoroutine(
    activity: ComponentActivity,
    onUpdate: (Int) -> Unit
): AcceptCoroutine {
    val manager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetooth = manager.adapter
    val mmServerSocket: BluetoothServerSocket =
        bluetooth.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE)
    val acceptCoroutine = AcceptCoroutine { mmServerSocket.close() }
    coroutineScope {
        launch {
            withTimeout(8.seconds) {
                var i = 0
                while (acceptCoroutine.open) {
                    delay(1000)
                    i += 1
                    onUpdate(i)
                }
            }
            mmServerSocket.close()
        }
    }
    return acceptCoroutine
}