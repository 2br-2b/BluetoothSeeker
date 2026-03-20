package codegito.xyz.bluetoothseeker

import codegito.xyz.bluetoothseeker.ui.Formatting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class FormattingTest {
    @Test
    fun formatSeenAt_returnsNowForRecentTimestamps() {
        val now = System.currentTimeMillis()
        assertEquals("Now", Formatting.formatSeenAt(now - TimeUnit.MINUTES.toMillis(2), now))
    }

    @Test
    fun formatSeenAt_returnsWeekdayWithinSevenDays() {
        val now = System.currentTimeMillis()
        val formatted = Formatting.formatSeenAt(now - TimeUnit.DAYS.toMillis(2), now)
        assertTrue(formatted.contains(":"))
    }

    @Test
    fun formatSeenAt_returnsDateForOlderTimestamps() {
        val now = System.currentTimeMillis()
        val formatted = Formatting.formatSeenAt(now - TimeUnit.DAYS.toMillis(10), now)
        assertTrue(formatted.contains(","))
    }

    @Test
    fun mapsUri_usesGeoScheme() {
        val uri = Formatting.mapsUri(40.0, -74.0, "Headphones")
        assertTrue(uri.startsWith("geo:"))
        assertTrue(uri.contains("Headphones"))
    }
}
