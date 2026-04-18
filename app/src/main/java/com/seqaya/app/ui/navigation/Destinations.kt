package com.seqaya.app.ui.navigation

import com.seqaya.app.R

enum class TopLevelDestination(
    val route: String,
    val labelRes: Int,
) {
    Home(route = "home", labelRes = R.string.nav_home),
    Scan(route = "scan", labelRes = R.string.nav_scan),
    Library(route = "library", labelRes = R.string.nav_library),
}
