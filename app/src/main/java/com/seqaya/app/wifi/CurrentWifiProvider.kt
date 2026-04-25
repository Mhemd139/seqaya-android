package com.seqaya.app.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
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
    private val cm: ConnectivityManager? = context.getSystemService(ConnectivityManager::class.java)

    @Suppress("DEPRECATION")
    private val wifiManager: WifiManager? = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as? WifiManager

    val hasLocationPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    val currentSsid: Flow<String?> = callbackFlow {
        if (cm == null) {
            trySend(readSsidLegacy())
            awaitClose {}
            return@callbackFlow
        }
        val callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    trySend(extractSsidFromCaps(caps) ?: readSsidLegacy())
                }
                override fun onLost(network: Network) { trySend(null) }
            }
        } else {
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    trySend(readSsidLegacy())
                }
                override fun onLost(network: Network) { trySend(null) }
            }
        }
        // registerDefaultNetworkCallback fires onCapabilitiesChanged immediately
        // for the currently-active network, unlike registerNetworkCallback(request)
        // which only fires on network *changes*.
        cm.registerDefaultNetworkCallback(callback)
        trySend(readSsidLegacy()) // synchronous WifiManager read works in foreground
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    @RequiresApi(Build.VERSION_CODES.S)
    private fun extractSsidFromCaps(caps: NetworkCapabilities): String? {
        if (!hasLocationPermission) return null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
        val raw = (caps.transportInfo as? WifiInfo)?.ssid ?: return null
        if (raw.isEmpty() || raw == UNKNOWN_SSID) return null
        return raw.removeSurrounding("\"").takeIf { it.isNotBlank() }
    }

    @Suppress("DEPRECATION")
    private fun readSsidLegacy(): String? {
        if (!hasLocationPermission) return null
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
