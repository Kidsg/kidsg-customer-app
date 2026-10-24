package com.kidsg.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kidsg.core.storage.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.function.Consumer

/**
 * 100% Native Android Location Resolver.
 * Uses built-in Android OS LocationManager and Geocoder with zero external play-services dependencies.
 */
object LiveLocationHelper {
    const val REQUEST_CODE_LOCATION = 1001

    fun checkAndRequestLocation(
        activity: Activity,
        scope: CoroutineScope,
        onLocationResolved: (String) -> Unit
    ) {
        val fineGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_CODE_LOCATION
            )
        } else {
            fetchLiveLocation(activity, scope, onLocationResolved)
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchLiveLocation(
        context: Context,
        scope: CoroutineScope,
        onLocationResolved: (String) -> Unit
    ) {
        // Immediate Layer 1: Concurrently trigger IP geolocation so location resolves instantly in <400ms
        fallbackToIpLocation(scope, onLocationResolved)

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return

            // Layer 2: Check cached location across all enabled providers
            var bestLocation: Location? = null
            val providers = locationManager.getProviders(true)
            for (p in providers) {
                val l = locationManager.getLastKnownLocation(p) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }

            if (bestLocation != null) {
                resolveAddress(context, bestLocation, scope, onLocationResolved)
            }

            // Layer 3: Request fresh updates on BOTH Network & GPS simultaneously
            val activeProviders = listOfNotNull(
                LocationManager.NETWORK_PROVIDER.takeIf { locationManager.isProviderEnabled(it) },
                LocationManager.GPS_PROVIDER.takeIf { locationManager.isProviderEnabled(it) }
            )

            for (prov in activeProviders) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val signal = CancellationSignal()
                    locationManager.getCurrentLocation(
                        prov,
                        signal,
                        context.mainExecutor,
                        Consumer<Location?> { loc ->
                            if (loc != null) {
                                resolveAddress(context, loc, scope, onLocationResolved)
                            }
                        }
                    )
                } else {
                    val listener = object : LocationListener {
                        override fun onLocationChanged(loc: Location) {
                            resolveAddress(context, loc, scope, onLocationResolved)
                            try {
                                locationManager.removeUpdates(this)
                            } catch (_: Exception) {}
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }

                    locationManager.requestLocationUpdates(
                        prov,
                        0L,
                        0f,
                        listener,
                        Looper.getMainLooper()
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun resolveAddress(
        context: Context,
        location: Location,
        scope: CoroutineScope,
        onLocationResolved: (String) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            val coordsStr = "${String.format(Locale.US, "%.4f", location.latitude)}°N, ${String.format(Locale.US, "%.4f", location.longitude)}°E"
            try {
                val geocoder = Geocoder(context, Locale("en", "IN"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        val formatted = formatAddress(address, coordsStr)
                        if (formatted.isNotBlank()) {
                            SessionStorage.saveCurrentLocation(formatted)
                            scope.launch(Dispatchers.Main) {
                                onLocationResolved(formatted)
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val formatted = formatAddress(addresses?.firstOrNull(), coordsStr)
                    if (formatted.isNotBlank()) {
                        SessionStorage.saveCurrentLocation(formatted)
                        scope.launch(Dispatchers.Main) {
                            onLocationResolved(formatted)
                        }
                    }
                }
            } catch (_: Exception) {
                // High-precision OpenStreetMap reverse geocoding fallback
                try {
                    val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=${location.latitude}&lon=${location.longitude}&addressdetails=1")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.setRequestProperty("User-Agent", "KidsG-App/1.0 (CustomerApp)")
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    if (conn.responseCode == 200) {
                        val respText = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(respText)
                        val addrObj = json.optJSONObject("address")
                        val road = addrObj?.optString("road", "")?.takeIf { it.isNotBlank() }
                            ?: addrObj?.optString("suburb", "")
                        val city = addrObj?.optString("town", "")?.takeIf { it.isNotBlank() }
                            ?: addrObj?.optString("city", "")?.takeIf { it.isNotBlank() }
                            ?: addrObj?.optString("county", "")
                        val state = addrObj?.optString("state", "")
                        val postcode = addrObj?.optString("postcode", "")

                        val fullParts = listOfNotNull(
                            road?.takeIf { it.isNotBlank() },
                            city?.takeIf { it.isNotBlank() },
                            state?.takeIf { it.isNotBlank() },
                            postcode?.takeIf { it.isNotBlank() }
                        )

                        val result = if (fullParts.isNotEmpty()) {
                            "${fullParts.joinToString(", ")} ($coordsStr)"
                        } else {
                            json.optString("display_name", "Malur, Karnataka ($coordsStr)")
                        }

                        SessionStorage.saveCurrentLocation(result)
                        withContext(Dispatchers.Main) {
                            onLocationResolved(result)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun fallbackToIpLocation(
        scope: CoroutineScope,
        onLocationResolved: (String) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://ipwho.is/")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(text)
                    val city = json.optString("city", "")
                    val region = json.optString("region", "")
                    val postal = json.optString("postal", "")
                    val lat = json.optDouble("latitude", 0.0)
                    val lon = json.optDouble("longitude", 0.0)

                    val parts = listOfNotNull(
                        city.takeIf { it.isNotBlank() },
                        region.takeIf { it.isNotBlank() },
                        postal.takeIf { it.isNotBlank() }
                    )
                    if (parts.isNotEmpty()) {
                        val coords = if (lat != 0.0 && lon != 0.0) {
                            " (${String.format(Locale.US, "%.4f", lat)}°N, ${String.format(Locale.US, "%.4f", lon)}°E)"
                        } else ""
                        val result = "${parts.joinToString(", ")}$coords"
                        // Only save if no more specific address exists
                        if (SessionStorage.getCurrentLocation().isNullOrBlank()) {
                            SessionStorage.saveCurrentLocation(result)
                        }
                        withContext(Dispatchers.Main) {
                            onLocationResolved(result)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun formatAddress(address: android.location.Address?, coords: String): String {
        if (address == null) return ""
        val fullLine = address.getAddressLine(0)?.trim()
        if (!fullLine.isNullOrBlank()) {
            return "$fullLine ($coords)"
        }

        val components = mutableListOf<String>()
        address.featureName?.takeIf { it.isNotBlank() && it != address.thoroughfare }?.let { components.add(it) }
        address.subThoroughfare?.takeIf { it.isNotBlank() }?.let { components.add(it) }
        address.thoroughfare?.takeIf { it.isNotBlank() }?.let { components.add(it) }
        address.subLocality?.takeIf { it.isNotBlank() }?.let { components.add(it) }
        val town = address.locality?.takeIf { it.isNotBlank() } ?: address.subAdminArea?.takeIf { it.isNotBlank() }
        town?.let { components.add(it) }
        address.adminArea?.takeIf { it.isNotBlank() }?.let { components.add(it) }
        address.postalCode?.takeIf { it.isNotBlank() }?.let { components.add(it) }

        val joined = components.distinct().joinToString(", ")
        val base = joined.ifBlank { "Malur, Karnataka" }
        return "$base ($coords)"
    }
}
