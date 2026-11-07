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

  @Test
  fun `cleanMetadataForSpeech removes underscores and file extensions`() {
    assertEquals("lofi hiphop beat", com.example.ai.cleanMetadataForSpeech("01_lofi_hiphop_beat.mp3"))
    assertEquals("Despacito Remix Track 01", com.example.ai.cleanMetadataForSpeech("Despacito_Remix_Track-01.wav"))
    assertEquals("Midnight Chill Session", com.example.ai.cleanMetadataForSpeech("Midnight_Chill_Session"))
    assertEquals("", com.example.ai.cleanMetadataForSpeech(null))
    assertEquals("", com.example.ai.cleanMetadataForSpeech("   "))
  }
}
