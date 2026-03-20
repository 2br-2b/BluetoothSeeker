package codegito.xyz.bluetoothseeker.ui

import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object Formatting {
    private val weekdayFormat = SimpleDateFormat("EEEE h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    fun formatSeenAt(timestamp: Long?, now: Long = System.currentTimeMillis()): String {
        if (timestamp == null) return "Not seen yet"
        val diff = now - timestamp
        if (diff < TimeUnit.MINUTES.toMillis(5)) return "Now"
        if (diff < TimeUnit.DAYS.toMillis(7)) return weekdayFormat.format(Date(timestamp))
        return dateFormat.format(Date(timestamp))
    }

    fun mapsUri(latitude: Double, longitude: Double, label: String): String =
        "geo:$latitude,$longitude?q=$latitude,$longitude(${URLEncoder.encode(label, Charsets.UTF_8.name())})"

    fun mapsIntent(latitude: Double, longitude: Double, label: String): Intent =
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(mapsUri(latitude, longitude, label)),
        )
}
