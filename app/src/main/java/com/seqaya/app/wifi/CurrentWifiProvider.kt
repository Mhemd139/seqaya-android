package com.seqaya.app.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
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

/**
 * Window into the device's Wi-Fi state. Used by Add Device wizard for SSID
 * prefill and the network picker.
 *
 * Two distinct read paths exist for the *current* SSID, both required:
 *  - `WifiManager.connectionInfo` (deprecated, but still functional in
 *    foreground with fine-location). Used as the synchronous initial emit
 *    and as a fallback when caps are location-redacted.
 *  - `ConnectivityManager.NetworkCallback` with `FLAG_INCLUDE_LOCATION_INFO`
 *    on Android 12+. Required because synchronous polling of
 *    `getNetworkCapabilities(...).transportInfo` returns redacted SSID on S+.
 *
 * The flow is built around `registerDefaultNetworkCallback`, which fires
 * `onCapabilitiesChanged` immediately for the active network on registration —
 * unlike `registerNetworkCallback(NetworkRequest)` which only delivers caps on
 * network *changes* (so a stable connection never delivers anything).
 */
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
        cm.registerDefaultNetworkCallback(callback)
        trySend(readSsidLegacy())
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

    /**
     * Snapshot of all visible Wi-Fi networks (both bands), sorted by signal.
     *
     * Returns 5 GHz networks too — the wizard's picker UI needs them so it can
     * disable them with a "5 GHz, won't work" label rather than hiding them
     * silently and leaving the user wondering where their network went.
     *
     * Returns empty list if location permission is denied or the scan fails.
     */
    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun scanNetworks(): List<WifiNetwork> {
        if (!hasLocationPermission) return emptyList()
        val mgr = wifiManager ?: return emptyList()
        return runCatching {
            mgr.scanResults
                .filter { !it.SSID.isNullOrBlank() }
                // Many home routers broadcast the same SSID on both bands. Prefer
                // the 2.4 GHz variant when collapsing duplicates so the picker
                // doesn't disable a network the device could actually join.
                .groupBy { it.SSID }
                .map { (_, group) ->
                    group.firstOrNull { it.frequency in TWO_POINT_FOUR_GHZ_RANGE }
                        ?: group.maxBy { it.level }
                }
                .sortedByDescending { it.level }
                .map { it.toWifiNetwork() }
        }.getOrDefault(emptyList())
    }

    @Suppress("DEPRECATION")
    private fun ScanResult.toWifiNetwork(): WifiNetwork = WifiNetwork(
        ssid = SSID,
        is24GHz = frequency in TWO_POINT_FOUR_GHZ_RANGE,
        signalBars = WifiManager.calculateSignalLevel(level, SIGNAL_BAR_COUNT),
    )

    private companion object {
        const val UNKNOWN_SSID = "<unknown ssid>"
        const val SIGNAL_BAR_COUNT = 4
        val TWO_POINT_FOUR_GHZ_RANGE = 2400..2500
    }
}

/**
 * Visible Wi-Fi network for picker UI.
 *
 * @property signalBars 0..3 (4 levels), where 3 is strongest.
 * @property is24GHz true if the network is on the 2.4 GHz band the Seqaya
 *   firmware can connect to. 5 GHz networks are surfaced anyway so the picker
 *   can show them as disabled rather than hiding them.
 */
data class WifiNetwork(
    val ssid: String,
    val is24GHz: Boolean,
    val signalBars: Int,
)
