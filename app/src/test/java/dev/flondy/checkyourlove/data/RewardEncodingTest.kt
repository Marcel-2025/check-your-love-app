package dev.flondy.checkyourlove.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RewardEncodingTest {
    @Test fun `round trip and corrupt entries are skipped`() {
        val list = listOf(RewardUnlock("palette:aurora", 123L), RewardUnlock("effect:heart_rain", 456L))
        val decoded = SettingsRepository.decodeUnlocks(SettingsRepository.encodeUnlocks(list) + setOf("broken", "x|notanumber", "|5"))
        assertThat(decoded).containsExactlyElementsIn(list)
    }
}
