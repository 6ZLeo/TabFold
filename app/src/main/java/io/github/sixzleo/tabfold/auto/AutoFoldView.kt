package io.github.sixzleo.tabfold.auto

import android.content.Context
import android.graphics.*
import android.view.View
import io.github.sixzleo.tabfold.R
import io.github.sixzleo.tabfold.sensors.OpeningMath

/** A touch-through visual only. The screenshot lives only for this opening. */
class AutoFoldView @JvmOverloads constructor(context: Context, private val snapshot: Bitmap? = null) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val imageScale = Matrix()
    private val shadeScale = Matrix()
    private val shade = LinearGradient(0f, 0f, 0f, 1f,
        intArrayOf(Color.argb(180, 10, 19, 34), Color.argb(90, 46, 66, 89), Color.TRANSPARENT),
        floatArrayOf(0f, .7f, 1f), Shader.TileMode.CLAMP)
    private val content = snapshot?.let { BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }
    private val fold = if (content == null) null else RuntimeShader(
        resources.openRawResource(R.raw.fold_surface).bufferedReader().use { it.readText() }
    ).apply {
        setInputShader("content", content)
        setFloatUniform("eyeDistancePx", resources.displayMetrics.xdpi / 25.4f * 500f)
        setFloatUniform("blurSpread", .16f)
        setFloatUniform("darkening", .011f)
    }
    var opening = 0f
    var fullyOpen = 110f
    var strength = 65f
    var effectAmount = 1f

    init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS }
    override fun getAccessibilityClassName(): CharSequence = AutoFoldView::class.java.name

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        fold?.setFloatUniform("resolution", w.toFloat(), h.toFloat())
        if (snapshot != null) {
            imageScale.setScale(w.toFloat() / snapshot.width, h.toFloat() / snapshot.height)
            content?.setLocalMatrix(imageScale)
            // RuntimeShader retains the input shader state at binding time.
            // Rebind after scaling so different display/image sizes do not clamp at the old edge.
            content?.let { fold?.setInputShader("content", it) }
        }
        shadeScale.setScale(1f, h.toFloat())
        shade.setLocalMatrix(shadeScale)
    }

    override fun onDraw(canvas: Canvas) {
        val progress = OpeningMath.progress(opening, fullyOpen)
        if (fold != null) {
            paint.alpha = 255
            fold.setFloatUniform("tiltDegrees", OpeningMath.tilt(opening, fullyOpen, strength) * effectAmount)
            paint.shader = fold
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        } else {
            // Secure windows stay live underneath. No replacement image or cached home content.
            val amount = (1f - progress * progress).coerceIn(0f, 1f) * effectAmount
            paint.shader = shade
            paint.alpha = (amount * 255).toInt().coerceIn(0, 255)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
    }
}
