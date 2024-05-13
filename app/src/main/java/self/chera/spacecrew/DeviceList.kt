package self.chera.spacecrew

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModel
import self.chera.spacecrew.ui.theme.SpacecrewTheme


enum class BluetoothScanningState { IDLE, SCANNING, FINISHED }

data class Device(
    val name: String,
    val paired: Boolean
)

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
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun DeviceListScreen(
    bluetoothDeviceListViewModel: BluetoothDeviceListViewModel
) {
    when (bluetoothDeviceListViewModel.state) {
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
    DeviceList(bluetoothDeviceListViewModel.deviceList.map {
        Device(name = "${it.address} - ${it.name}", paired = it.bondState == BluetoothDevice.BOND_BONDED)
    })
}


@Composable
fun DeviceList(devices: List<Device>) {
    if (devices.isNotEmpty()) {
        LazyColumn {
            items(devices) {
                DeviceCard(
                    device = it,
                    state = "not connected",
                ) {

                }
            }
        }
    } else {
        Row(
            Modifier
                .padding(4.dp)
                .shadow(2.dp)
                .padding(16.dp)
        ) {
            Text("No device found")
        }
    }
}


@Composable
fun DeviceCard(
    device: Device,
    state: String,
    onConnectDevice: () -> Unit
) {
    Row(
        Modifier
            .padding(4.dp)
            .shadow(2.dp)
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(text = device.name, Modifier.weight(1F))
        Column {
            Text(text = state)
            Button(onClick = onConnectDevice) {
                Text(text = "Connect")
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 320,)
@Composable
private fun DeviceListPreview() {
    SpacecrewTheme {
        val devices = (1..5).map { Device("Device #$it", it % 2 == 0) }
        DeviceList(devices = devices)
    }
}