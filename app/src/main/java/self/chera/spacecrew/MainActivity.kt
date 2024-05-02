@file:OptIn(ExperimentalPermissionsApi::class)

package self.chera.spacecrew

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import self.chera.spacecrew.ui.theme.SpacecrewTheme

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
            Manifest.permission.BLUETOOTH_CONNECT
        ),
    )

    if (bluetoothPermissionState.allPermissionsGranted) {
        BluetoothMainScreen(activity)
    } else {
        AskForBluetooth(bluetoothPermissionState)
    }
}

@Composable
fun BluetoothMainScreen(activity: ComponentActivity) {
    val manager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetooth = manager.adapter

    var isON by remember { mutableStateOf(bluetooth.isEnabled) }

    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    val toggleBT = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { isON = bluetooth.isEnabled }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Bluetooth is allowed")
            Text(text = "Bluetooth is ${if (isON) "ON" else "OFF"}")
            Button(
                enabled = !bluetooth.isEnabled,
                onClick = { toggleBT.launch(enableIntent) }
            ) {
                Text(text = "Turn ON")
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
fun GreetingPreview() {
    SpacecrewTheme {

    }
}