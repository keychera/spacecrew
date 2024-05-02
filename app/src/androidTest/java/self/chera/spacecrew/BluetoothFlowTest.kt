package self.chera.spacecrew

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class BluetoothFlowTest {
    @get:Rule
    val mainActivityTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testNormalScenario() {
        with(mainActivityTestRule) {
            onNodeWithText("Turn ON").performClick()
            onNodeWithText("Bluetooth is ON").assertIsDisplayed()
        }
    }
}