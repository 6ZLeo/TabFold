package io.github.sixzleo.tabfold

import android.content.Context

object SettingsStore {
    const val FILE = "tabfold"
    const val CONSENT_VERSION = 2
    fun openTolerance(context: Context) = prefs(context).getFloat("open_tolerance", 3f).coerceIn(1f, 8f)
    fun allApps(context: Context) = android.os.Build.VERSION.SDK_INT >= 34 && prefs(context).getBoolean("all_apps", true)
    fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    fun consented(context: Context) = prefs(context).getInt("consent_version", 0) == CONSENT_VERSION
    fun enabled(context: Context) = ConsentPolicy.canActivate(prefs(context).getInt("consent_version", 0),
        CONSENT_VERSION, prefs(context).getBoolean("auto_enabled", false))
}
