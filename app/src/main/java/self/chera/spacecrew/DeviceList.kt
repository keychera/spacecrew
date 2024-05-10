package self.chera.spacecrew

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModel


enum class BluetoothScanningState { IDLE, SCANNING, FINISHED }

class BluetoothDeviceListViewModel : ViewModel() {
    private val _deviceList = mutableStateListOf<BluetoothDevice>()
    private val _state = mutableStateOf(BluetoothScanningState.IDLE)
    val deviceList get() = _deviceList
    val state get() = _state.value

    fun scanDevices(context: Context) {
        _deviceList.clear()
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            System.err.println("Bluetooth SCAN not allowed")
            return
        }
        _deviceList.addAll(adapter.bondedDevices)

        adapter.cancelDiscovery()
        adapter.startDiscovery()
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action.toString()
            println("Hello $intent")
            when (action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _state.value = BluetoothScanningState.SCANNING
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = IntentCompat.getParcelableExtra(
                        intent,
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                    device?.let { _deviceList.add(it) }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _state.value = BluetoothScanningState.FINISHED
                }
            }
        }
    }
}

@Composable
fun DeviceListScreen(
    bluetoothDeviceListViewModel: BluetoothDeviceListViewModel
) {
    with(bluetoothDeviceListViewModel) {
        when (state) {
            BluetoothScanningState.SCANNING -> {
                Text("Scanning for devices...")
            }

            BluetoothScanningState.FINISHED -> {
                Text("Finished scanning")
            }

            BluetoothScanningState.IDLE -> {
                Text(" ")
            }
        }
        if (deviceList.isNotEmpty()) {
            LazyColumn {
                items(deviceList) {
                    DeviceCard(device = it)
                }
            }
        } else {
            Text("No device found")
        }
    }
}