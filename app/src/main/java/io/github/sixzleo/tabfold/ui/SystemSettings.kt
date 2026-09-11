package io.github.sixzleo.tabfold.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import io.github.sixzleo.tabfold.auto.AutoFoldService

object SystemSettings {
    fun serviceEnabled(context: Context): Boolean {
        val ownService = ComponentName(context, AutoFoldService::class.java)
        return context.getSystemService(AccessibilityManager::class.java)
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { ComponentName(it.resolveInfo.serviceInfo.packageName, it.resolveInfo.serviceInfo.name) == ownService }
    }

    fun accessibility(context: Context) = open(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    fun appInfo(context: Context) = open(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)))
    fun home(context: Context) = open(context, Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))
    fun web(context: Context, url: String) = open(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    private fun open(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (_: android.content.ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
