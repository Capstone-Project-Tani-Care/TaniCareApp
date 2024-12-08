import android.content.Context
import android.util.Log
import com.dicoding.tanicare.R
import java.io.BufferedReader
import java.io.InputStreamReader

// Model untuk data zona
data class Zone(val code: String, val cityName: String)

fun loadZonesFromCsv(context: Context): List<Zone> {
    val zones = mutableListOf<Zone>()
    try {
        // Membaca file zone.csv dari res/raw
        val inputStream = context.resources.openRawResource(R.raw.zone)
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            val parts = line?.split(",")?.map { it.trim() } ?: continue
            if (parts.size == 2) {
                val code = parts[0]
                val cityName = parts[1]
                zones.add(Zone(code, cityName))
            }
        }
        reader.close()
    } catch (e: Exception) {
        Log.e("CSV", "Error reading CSV file", e)
    }
    return zones
}
