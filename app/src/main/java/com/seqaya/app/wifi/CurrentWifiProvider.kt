package com.seqaya.app.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentWifiProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val cm = context.getSystemService(ConnectivityManager::class.java)

    @Suppress("DEPRECATION")
    private val wifiManager: WifiManager? = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as? WifiManager

    val hasLocationPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    val currentSsid: Flow<String?> = callbackFlow {
        val emit = { trySend(readSsid()) }
        val callback = object : ConnectivityManager.NetworkCallback(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                FLAG_INCLUDE_LOCATION_INFO else 0
        ) {
            override fun onAvailable(network: Network) { emit() }
            override fun onLost(network: Network) { emit() }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) { emit() }
        }
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        cm.registerNetworkCallback(request, callback)
        emit()
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    @SuppressLint("MissingPermission")
    private fun readSsid(): String? {
        if (!hasLocationPermission) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) readSsidModern()
        else readSsidLegacy()
    }

    private fun readSsidModern(): String? {
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return null) ?: return null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
        val raw = (caps.transportInfo as? WifiInfo)?.ssid ?: return null
        if (raw.isEmpty() || raw == UNKNOWN_SSID) return null
        return raw.removeSurrounding("\"").takeIf { it.isNotBlank() }
    }

    @Suppress("DEPRECATION")
    private fun readSsidLegacy(): String? {
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
        val TWO_POINT_FOUR_GHZ_RANGE = 2400..2500
    }
}
