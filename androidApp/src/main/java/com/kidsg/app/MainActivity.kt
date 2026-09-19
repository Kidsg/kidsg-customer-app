package com.kidsg.app

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.kidsg.feature.app.KidsGApp

class MainActivity : ComponentActivity() {
    private var liveLocationState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Every time entering app: fetch user's live location & request permission if needed
        LiveLocationHelper.checkAndRequestLocation(this, lifecycleScope) { resolvedLoc ->
            liveLocationState = resolvedLoc
        }

        setContent {
            KidsGApp(initialLocation = liveLocationState)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LiveLocationHelper.REQUEST_CODE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
                LiveLocationHelper.fetchLiveLocation(this, lifecycleScope) { resolvedLoc ->
                    liveLocationState = resolvedLoc
                }
            }
        }
    }
}
