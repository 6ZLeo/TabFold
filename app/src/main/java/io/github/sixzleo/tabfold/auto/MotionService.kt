package io.github.sixzleo.tabfold.auto

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import io.github.sixzleo.tabfold.MainActivity
import io.github.sixzleo.tabfold.R
import io.github.sixzleo.tabfold.SettingsStore
import io.github.sixzleo.tabfold.ui.SystemSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Keeps the sensor-owning process foreground while the user enables cover animations. */
class MotionService : Service() {
    companion object {
        private const val CHANNEL = "cover_motion"
        private const val NOTIFICATION = 41
        private const val PAUSE = "io.github.sixzleo.tabfold.PAUSE"
        private val state = MutableStateFlow(false)
        val running = state.asStateFlow()
        fun start(context: Context): Boolean = try {
            context.startForegroundService(Intent(context, MotionService::class.java))
            true
        } catch (e: RuntimeException) {
            Log.w("TabFold", "background_start_failed", e)
            false
        }
        fun stop(context: Context) { context.stopService(Intent(context, MotionService::class.java)) }
    }
    private val handler = Handler(Looper.getMainLooper())
    private val check = object : Runnable {
        override fun run() {
            if (!SettingsStore.enabled(this@MotionService) || !SystemSettings.serviceEnabled(this@MotionService)) {
                stopSelf(); return
            }
            handler.postDelayed(this, 2_000)
        }
    }
    override fun onBind(intent: Intent?) = null
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        if (state.value) onStartCommand(null, 0, 0)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == PAUSE) { AutoFoldService.enable(this, false); stopSelf(); return START_NOT_STICKY }
        if (!SettingsStore.enabled(this) || !SystemSettings.serviceEnabled(this)) {
            stopSelf(); return START_NOT_STICKY
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, getString(R.string.background_channel),
            NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) })
        val open = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pause = PendingIntent.getService(this, 1, Intent(this, MotionService::class.java).setAction(PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_tabfold).setContentTitle(getString(R.string.background_title))
            .setContentText(getString(R.string.background_text)).setContentIntent(open)
            .setOngoing(true).setOnlyAlertOnce(true).setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(0, getString(R.string.pause), pause).build()
        try {
            if (Build.VERSION.SDK_INT >= 34) startForeground(NOTIFICATION, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            else startForeground(NOTIFICATION, notification)
        } catch (e: RuntimeException) {
            Log.w("TabFold", "background_notification_failed", e); stopSelf(); return START_NOT_STICKY
        }
        state.value = true
        AutoFoldService.backgroundReady()
        handler.removeCallbacks(check); handler.post(check)
        return START_STICKY
    }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        state.value = false
        AutoFoldService.backgroundStopped()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
