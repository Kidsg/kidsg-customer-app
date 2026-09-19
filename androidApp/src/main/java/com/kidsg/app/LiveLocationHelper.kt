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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kidsg.core.storage.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

object LiveLocationHelper {
    const val REQUEST_CODE_LOCATION = 1001

    fun checkAndRequestLocation(
        activity: Activity,
        scope: CoroutineScope,
        onLocationResolved: (String) -> Unit
    ) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {
            // Request permission from the user on entering app
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_CODE_LOCATION
            )
        } else {
            // Fetch live location
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
                ?: return

            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null

            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }

            if (bestLocation != null) {
                resolveAddress(context, bestLocation, scope, onLocationResolved)
            } else {
                // Request a single quick update from network or gps
                val provider = when {
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                    else -> null
                }

                if (provider != null) {
                    val singleListener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            resolveAddress(context, location, scope, onLocationResolved)
                            locationManager.removeUpdates(this)
                        }
                        override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                        override fun onProviderEnabled(p: String) {}
                        override fun onProviderDisabled(p: String) {}
                    }
                    locationManager.requestSingleUpdate(provider, singleListener, null)
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
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        val result = formatAddress(address)
                        if (result.isNotBlank()) {
                            SessionStorage.saveCurrentLocation(result)
                            scope.launch(Dispatchers.Main) {
                                onLocationResolved(result)
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val address = addresses?.firstOrNull()
                    val result = formatAddress(address)
                    if (result.isNotBlank()) {
                        SessionStorage.saveCurrentLocation(result)
                        scope.launch(Dispatchers.Main) {
                            onLocationResolved(result)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun formatAddress(address: android.location.Address?): String {
        if (address == null) return ""
        val subLocality = address.subLocality ?: address.thoroughfare
        val locality = address.locality ?: address.subAdminArea ?: address.adminArea
        return when {
            !subLocality.isNullOrBlank() && !locality.isNullOrBlank() -> "$subLocality, $locality"
            !locality.isNullOrBlank() -> locality
            !subLocality.isNullOrBlank() -> subLocality
            else -> address.featureName ?: "Bengaluru"
        }
    }
}
