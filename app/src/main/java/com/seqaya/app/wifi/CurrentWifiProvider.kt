package com.seqaya.app.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentWifiProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val wifiManager: WifiManager? = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as? WifiManager

    val hasLocationPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    fun currentSsid(): String? {
        if (!hasLocationPermission) return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            currentSsidModern()?.let { return it }
        }
        return currentSsidLegacy()
    }

    @SuppressLint("MissingPermission")
    private fun currentSsidModern(): String? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return null
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return null) ?: return null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
        val wifiInfo = (caps.transportInfo as? WifiInfo) ?: return null
        val raw = wifiInfo.ssid ?: return null
        if (raw.isEmpty() || raw == UNKNOWN_SSID) return null
        return raw.removeSurrounding("\"").takeIf { it.isNotBlank() }
    }

    @Suppress("DEPRECATION")
    private fun currentSsidLegacy(): String? {
        val raw = wifiManager?.connectionInfo?.ssid ?: return null
        if (raw.isEmpty() || raw == UNKNOWN_SSID) return null
        return raw.removeSurrounding("\"").takeIf { it.isNotBlank() }
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun scanResultSsids(): List<String> {
        if (!hasLocationPermission) return emptyList()
        val mgr = wifiManager ?: return emptyList()
        return runCatching {
            mgr.scanResults
                .filter { it.frequency in TWO_POINT_FOUR_GHZ_RANGE }
                .filter { !it.SSID.isNullOrBlank() }
                .sortedByDescending { it.level }
                .map { it.SSID }
                .distinct()
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val UNKNOWN_SSID = "<unknown ssid>"
        val TWO_POINT_FOUR_GHZ_RANGE = 2400..2500 // MHz
    }
}
