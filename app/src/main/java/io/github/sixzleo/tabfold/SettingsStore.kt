package io.github.sixzleo.tabfold

import android.content.Context

object SettingsStore {
    const val FILE = "tabfold"
    const val CONSENT_VERSION = 1
    fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    fun consented(context: Context) = prefs(context).getInt("consent_version", 0) == CONSENT_VERSION
    fun enabled(context: Context) = ConsentPolicy.canActivate(prefs(context).getInt("consent_version", 0),
        CONSENT_VERSION, prefs(context).getBoolean("auto_enabled", false))
}
