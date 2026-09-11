package io.github.sixzleo.tabfold.debug

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import io.github.sixzleo.tabfold.auto.AutoFoldView

/** Debug-only synthetic image, with no screen capture, service or device-policy override. */
class RenderProbeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bitmap = Bitmap.createBitmap(1200, 750, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (row in 0 until 5) for (column in 0 until 8) {
            paint.color = if ((row + column) % 2 == 0) Color.rgb(45, 133, 144) else Color.rgb(219, 224, 199)
            canvas.drawRect(column * 150f, row * 150f, (column + 1) * 150f, (row + 1) * 150f, paint)
        }
        paint.color = Color.WHITE
        paint.textSize = 60f
        canvas.drawText("TabFold synthetic renderer check", 80f, 390f, paint)
        val opening = intent.getFloatExtra("opening", 110f).coerceIn(0f, 180f)
        setContentView(AutoFoldView(this, bitmap).apply {
            this.opening = opening
            fullyOpen = 110f
        })
        Log.i("TabFoldProbe", "RuntimeShader created; opening=$opening")
    }
}
