@file:OptIn(ExperimentalPermissionsApi::class)

package self.chera.spacecrew

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import self.chera.spacecrew.ui.theme.SpacecrewTheme

lateinit var acceptThread : AcceptThread

class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println(applicationContext.packageName)
        setContent {
            SpacecrewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FeatureThatUseBluetooth(this)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun FeatureThatUseBluetooth(activity: ComponentActivity) {
    val bluetoothPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ),
    )



    if (bluetoothPermissionState.allPermissionsGranted) {

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        val btDeviceListViewModel : BluetoothDeviceListViewModel = viewModel()
        activity.registerReceiver(btDeviceListViewModel.receiver, filter)

        val lifecycleOwner = LocalLifecycleOwner.current
        val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()


        LaunchedEffect(lifecycleState) {
            if (lifecycleState == Lifecycle.State.DESTROYED) {
               activity.unregisterReceiver(btDeviceListViewModel.receiver)
            }
        }
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp)
        ) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Text(text = "Bluetooth connect permission not granted")
            } else {
                BluetoothMainScreen(activity, btDeviceListViewModel)
                DeviceListScreen(btDeviceListViewModel)
            }

        }

    } else {
        AskForBluetooth(bluetoothPermissionState)
    }
}

@Composable
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun BluetoothMainScreen(
    activity: ComponentActivity,
    btDeviceListViewModel: BluetoothDeviceListViewModel
) {
    val manager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetooth = manager.adapter
    acceptThread = createAcceptThread(bluetooth)

    var isON by remember { mutableStateOf(bluetooth.isEnabled) }

    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    val toggleBT = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { isON = bluetooth.isEnabled }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.size(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Bluetooth is ${if (isON) "ON" else "OFF"}")
            Spacer(Modifier.size(16.dp))
            if (bluetooth.isEnabled) {
                Button(
                    onClick = { btDeviceListViewModel.scanDevices(activity) }
                ) {
                    Text(text = "Scan for devices")
                }
            } else {
                Button(
                    onClick = { toggleBT.launch(enableIntent) }
                ) {
                    Text(text = "Turn ON")
                }
            }

        }
    }
}

@Composable
fun AskForBluetooth(
    permissionStates: MultiplePermissionsState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Bluetooth is needed")
        Button(onClick = { permissionStates.launchMultiplePermissionRequest() }) {
            Text("Grant permission")
        }
    }
}



@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun Preview() {
    SpacecrewTheme {

    }
}