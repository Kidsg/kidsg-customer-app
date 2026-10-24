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
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.kidsg.core.storage.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

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
        // Method 1: Google Play Services FusedLocationProviderClient (Immediate & Most Accurate)
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationTokenSource = CancellationTokenSource()

            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(15_000)
                .build()

            fusedClient.getCurrentLocation(request, cancellationTokenSource.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        resolveAddress(context, loc, scope, onLocationResolved)
                    } else {
                        // Fallback to lastLocation
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                resolveAddress(context, lastLoc, scope, onLocationResolved)
                            } else {
                                fallbackToLocationManager(context, scope, onLocationResolved)
                            }
                        }.addOnFailureListener {
                            fallbackToLocationManager(context, scope, onLocationResolved)
                        }
                    }
                }
                .addOnFailureListener {
                    fallbackToLocationManager(context, scope, onLocationResolved)
                }
        } catch (_: Exception) {
            fallbackToLocationManager(context, scope, onLocationResolved)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackToLocationManager(
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
                return
            }

            val activeProvider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> null
            }

            if (activeProvider != null) {
                val listener = object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        resolveAddress(context, loc, scope, onLocationResolved)
                        try {
                            locationManager.removeUpdates(this)
                        } catch (_: Exception) {}
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                locationManager.requestLocationUpdates(
                    activeProvider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
            } else {
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
                // If native geocoder fails, try reverse geocoding via OpenStreetMap Nominatim
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
                            return@launch
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
                    val parts = listOfNotNull(city.takeIf { it.isNotBlank() }, region.takeIf { it.isNotBlank() }, postal.takeIf { it.isNotBlank() })
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
        // Get complete, minute, detailed address line (street, area, city, district, state, pin)
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
