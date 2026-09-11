package io.github.sixzleo.tabfold

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.sixzleo.tabfold.sensors.FoldMotionModel
import io.github.sixzleo.tabfold.ui.TabFoldApp

class MainActivity : ComponentActivity() {
    companion object { var isForeground = false; private set }
    private lateinit var motion: FoldMotionModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        motion = FoldMotionModel(this) { display?.rotation ?: 0 }
        setContent { TabFoldApp(motion) }
    }
    override fun onResume() {
        super.onResume(); isForeground = true
        if (SettingsStore.consented(this)) motion.start()
    }
    override fun onPause() {
        isForeground = false; motion.stop(); super.onPause()
    }
}
