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
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                fallbackToIpLocation(scope, onLocationResolved)
                return
            }

            // 1. Immediately check best cached location from all active providers
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

            // 2. Request real-time hardware location update from available providers
            val targetProvider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> providers.firstOrNull()
            }

            if (targetProvider != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Native Android 11+ one-shot high-accuracy location (zero play services required)
                    val signal = CancellationSignal()
                    locationManager.getCurrentLocation(
                        targetProvider,
                        signal,
                        context.mainExecutor,
                        Consumer<Location?> { location ->
                            if (location != null) {
                                resolveAddress(context, location, scope, onLocationResolved)
                            }
                        }
                    )
                } else {
                    // Android 10 and below standard updates with Looper.getMainLooper()
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
                        targetProvider,
                        0L,
                        0f,
                        listener,
                        Looper.getMainLooper()
                    )
                }
            } else if (bestLocation == null) {
                fallbackToIpLocation(scope, onLocationResolved)
            }
        } catch (_: Exception) {
            fallbackToIpLocation(scope, onLocationResolved)
        }
    }

    private fun resolveAddress(
        context: Context,
        location: Location,
        scope: CoroutineScope,
        onLocationResolved: (String) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale("en", "IN"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        val formatted = formatAddress(address)
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
                    val formatted = formatAddress(addresses?.firstOrNull())
                    if (formatted.isNotBlank()) {
                        SessionStorage.saveCurrentLocation(formatted)
                        scope.launch(Dispatchers.Main) {
                            onLocationResolved(formatted)
                        }
                    }
                }
            } catch (_: Exception) {
                // Network reverse geocoding fallback if native geocoder times out
                try {
                    val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=${location.latitude}&lon=${location.longitude}&addressdetails=1")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.setRequestProperty("User-Agent", "BookBuddys-KidsG/1.0")
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    if (conn.responseCode == 200) {
                        val respText = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(respText)
                        val displayName = json.optString("display_name", "")
                        if (displayName.isNotBlank()) {
                            SessionStorage.saveCurrentLocation(displayName)
                            withContext(Dispatchers.Main) {
                                onLocationResolved(displayName)
                            }
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
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(text)
                    val city = json.optString("city", "")
                    val region = json.optString("region", "")
                    val postal = json.optString("postal", "")
                    val parts = listOfNotNull(
                        city.takeIf { it.isNotBlank() },
                        region.takeIf { it.isNotBlank() },
                        postal.takeIf { it.isNotBlank() }
                    )
                    if (parts.isNotEmpty()) {
                        val result = parts.joinToString(", ")
                        SessionStorage.saveCurrentLocation(result)
                        withContext(Dispatchers.Main) {
                            onLocationResolved(result)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun formatAddress(address: android.location.Address?): String {
        if (address == null) return ""
        // Get complete, detailed address line (street, locality, district, state, pin)
        val fullLine = address.getAddressLine(0)?.trim()
        if (!fullLine.isNullOrBlank()) {
            return fullLine
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
        return joined.ifBlank { "Malur, Karnataka" }
    }
}
