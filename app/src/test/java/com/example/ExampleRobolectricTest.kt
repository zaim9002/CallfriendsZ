package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun read_appName_fromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CallfriendsZ", appName)
  }

  @Test
  fun test_deepLink_constants() {
    assertEquals("callfriendsz.app", Constants.DEEP_LINK_HOST)
    assertEquals("https", Constants.DEEP_LINK_SCHEME)
    assertTrue(Constants.BASE_URL.isNotBlank())
  }

  @Test
  fun test_launch_main_activity() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    assertNotNull(activity)
  }
}
