package self.chera.spacecrew

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

private const val PACKAGE_NAME = "self.chera.spacecrew"
private const val LAUNCH_TIMEOUT = 5000L

class PermissionTest {
    private lateinit var device: UiDevice

    @Before
    fun startActivityFromHome() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from the home screen
        device.pressHome()

        // Wait for launcher
        val launcherPackage: String = device.launcherPackageName
        device.wait(
            Until.hasObject(By.pkg(launcherPackage).depth(0)),
            LAUNCH_TIMEOUT
        )

        // Launch the app
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)?.apply {
            // Clear out any previous instances
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        // Wait for the app to appear
        device.wait(
            Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)),
            LAUNCH_TIMEOUT
        )
    }

    @Test
    fun testBluetoothPermissions() {
        with(device) {
            doClick(By.textContains("Grant"))
            Thread.sleep(1000)
            doClick(By.text("許可"))
            doClick(By.textContains("Turn ON"))
            Thread.sleep(1000)
            doClick(By.text("許可"))
        }

    }
}

private fun UiDevice.doClick(
    bySelector: BySelector) {
    val foundObject = wait(Until.findObject(bySelector), 5000)
    if (foundObject == null) {
        fail("$bySelector not found!")
    } else {
        foundObject.click()
    }
}
