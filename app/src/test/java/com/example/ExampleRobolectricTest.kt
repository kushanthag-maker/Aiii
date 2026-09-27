package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.persona.Personas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nova AI", appName)
  }

  @Test
  fun `verify default coder persona`() {
    val coder = Personas.Coder
    assertNotNull(coder)
    assertEquals("coder", coder.id)
    assertEquals("Senior Code Architect", coder.name)
  }
}
