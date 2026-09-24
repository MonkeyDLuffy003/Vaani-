package com.example

import com.example.ui.DanceMode
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `dance modes have correct display names`() {
    assertEquals("Beat Bounce", DanceMode.BEAT_BOUNCE.label)
    assertEquals("Gear 5 Hype", DanceMode.GEAR_5.label)
    assertEquals("Straw Hat Chill", DanceMode.STRAW_HAT_CHILL.label)
  }

  @Test
  fun `dance modes count is three`() {
    assertEquals(3, DanceMode.values().size)
  }
}
