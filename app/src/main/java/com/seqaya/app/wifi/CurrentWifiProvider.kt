package com.seqaya.app.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around [WifiManager] for the Add Device wizard's SSID prefill + picker.
 *
 * Android's security model forbids reading saved Wi-Fi passwords — this is an
 * immutable platform constraint, not something we work around. We only read:
 *  - the SSID of the currently-connected network (for prefill)
 *  - scan results (when the user wants a different network)
 *
 * Both require [Manifest.permission.ACCESS_FINE_LOCATION] on API 29+. If the
 * permission is denied, [currentSsid] returns null and the wizard falls back
 * to an empty field the user fills manually.
 */
@Singleton
class CurrentWifiProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // Wi-Fi hardware is not declared as a required feature in AndroidManifest, so
    // getSystemService can legally return null on devices without Wi-Fi (rare, but
    // possible on some Chrome OS ARC containers and Android TV form factors).
    // Safe-cast and guard every read so null propagates to "no SSID" instead of crashing.
    private val wifiManager: WifiManager? = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as? WifiManager

    /** True if we hold ACCESS_FINE_LOCATION at runtime (required for SSID access). */
    val hasLocationPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Returns the SSID of the currently-connected Wi-Fi network, stripped of
     * quote wrapping, or null if:
     *   - location permission is denied
     *   - Wi-Fi is off
     *   - phone is not connected to a network
     *   - SSID is hidden/unavailable
     */
    fun currentSsid(): String? {
        if (!hasLocationPermission) return null
        val mgr = wifiManager ?: return null
        @Suppress("DEPRECATION")
        val info = mgr.connectionInfo ?: return null
        val raw = info.ssid ?: return null
        if (raw.isEmpty() || raw == UNKNOWN_SSID) return null
        // WifiManager returns SSIDs wrapped in quotes: "MyNetwork" — strip them.
        return raw.removeSurrounding("\"").takeIf { it.isNotBlank() }
    }

    /**
     * 2.4-GHz networks visible in the last scan, sorted by signal strength.
     * Returns empty list if permission denied or scan unavailable.
     */
    // Lint's MissingPermission check can't reason through hasLocationPermission,
    // but we do guard before touching scanResults. SecurityException is caught as
    // belt-and-suspenders in case the permission is revoked between the check and
    // the call on older API levels.
    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION") // WifiManager.startScan / scanResults deprecated on API 28+
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
