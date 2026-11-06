package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `music channels are well formed`() {
    val channels = listOf("All", "Cooking", "Gym", "Home", "Relax", "Motivation", "Storage")
    assertEquals(7, channels.size)
    assertTrue(channels.contains("Relax"))
    assertTrue(channels.contains("Gym"))
  }
}
