package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.ui.SmsViewModel
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SMS Forwarder", appName)
  }

  @Test
  fun `launch MainActivity successfully`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
    val activity = controller.setup().get()
    org.junit.Assert.assertNotNull(activity)
  }

  @Test
  fun `render MainAppScaffold successfully`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = SmsViewModel(application)

    composeTestRule.setContent {
      MyApplicationTheme {
        MainAppScaffold(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun `switch tabs successfully without crashes`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = SmsViewModel(application)

    composeTestRule.setContent {
      MyApplicationTheme {
        MainAppScaffold(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()

    // Switch to Logs Tab
    composeTestRule.onNodeWithTag("tab_logs").performClick()
    composeTestRule.waitForIdle()

    // Switch to Sandbox Tab
    composeTestRule.onNodeWithTag("tab_sandbox").performClick()
    composeTestRule.waitForIdle()

    // Switch to Settings Tab
    composeTestRule.onNodeWithTag("tab_settings").performClick()
    composeTestRule.waitForIdle()

    // Switch back to Rules Tab
    composeTestRule.onNodeWithTag("tab_rules").performClick()
    composeTestRule.waitForIdle()
  }
}
