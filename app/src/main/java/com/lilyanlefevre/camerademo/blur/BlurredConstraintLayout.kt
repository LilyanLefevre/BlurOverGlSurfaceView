package com.lilyanlefevre.camerademo.blur

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.Choreographer
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import com.lilyanlefevre.camerademo.engine.BitmapProvider

/**
 * A [ConstraintLayout] that applies a blur effect to its background based on a given [BitmapProvider] instance.
 *
 * The layout continuously updates its background to reflect a blurred version of the content captured
 * by the [BitmapProvider]. This class uses [RenderScript] for blurring and handles view lifecycle
 * events to manage updates.
 */
class BlurredConstraintLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var bitmapProvider: BitmapProvider? = null

    private val handler = Handler(Looper.getMainLooper())
    private val choreographer = Choreographer.getInstance()
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            updateBlurredBackground()
            choreographer.postFrameCallback(this)
        }
    }

    private val renderScript: RenderScript by lazy {
        RenderScript.create(context)
    }

    private val blurRadius = 25f
    private val cornerRadius = 16f
    private val borderWidth = 4f
    private val borderColor = Color.WHITE

    /**
     * Starts updating the blurred background by scheduling frame callbacks.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startUpdating()
    }

    /**
     * Stops updating the blurred background by removing frame callbacks and releases [RenderScript] resources.
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopUpdating()
        renderScript.destroy()
    }

    /**
     * Begins updating the blurred background by scheduling frame callbacks.
     */
    private fun startUpdating() {
        choreographer.postFrameCallback(frameCallback)
    }

    /**
     * Stops updating the blurred background by removing frame callbacks.
     */
    private fun stopUpdating() {
        choreographer.removeFrameCallback(frameCallback)
    }

    /**
     * Updates the background of this layout with a blurred version of the content beneath it.
     */
    private fun updateBlurredBackground() {
        clipToOutline = true
        clipChildren = true
        clipToPadding = true

        val currentBitmap = bitmapProvider?.getCurrentBitmap() ?: return
        val croppedBitmap = Bitmap.createBitmap(
            currentBitmap, x.toInt(), y.toInt(), width, height
        )
        val blurredBitmap = blurBitmap(croppedBitmap)

        handler.post {
            val roundedDrawable = createRoundedDrawableWithBorder(
                BitmapDrawable(resources, blurredBitmap),
                cornerRadius,
                borderWidth,
                borderColor
            )
            background = roundedDrawable
        }
    }

    /**
     * Creates a [BitmapDrawable] with rounded corners and a border.
     *
     * @param drawable The [BitmapDrawable] to be modified.
     * @param cornerRadius Radius of the rounded corners in pixels.
     * @param borderWidth Width of the border in pixels.
     * @param borderColor Color of the border.
     * @return A new [BitmapDrawable] with rounded corners and a border.
     */
    private fun createRoundedDrawableWithBorder(
        drawable: BitmapDrawable,
        cornerRadius: Float,
        borderWidth: Float,
        @ColorInt borderColor: Int
    ): BitmapDrawable {
        val bitmap = drawable.bitmap
        val width = bitmap.width
        val height = bitmap.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val path = Path().apply {
            addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawPath(path, paint)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        paint.apply {
            xfermode = null
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
        canvas.drawPath(path, paint)
        return BitmapDrawable(resources, output)
    }

    /**
     * Applies a blur effect to the specified [Bitmap].
     *
     * @param bitmap The [Bitmap] to be blurred.
     * @return A new [Bitmap] instance with the blur effect applied.
     */
    private fun blurBitmap(bitmap: Bitmap): Bitmap {
        val input = Allocation.createFromBitmap(renderScript, bitmap)
        val output = Allocation.createTyped(renderScript, input.type)
        ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript)).apply {
            setRadius(blurRadius)
            setInput(input)
            forEach(output)
        }
        output.copyTo(bitmap)
        return bitmap
    }

    /**
     * Sets the [BitmapProvider] used to provide the bitmap for blurring.
     *
     * @param bitmapProvider The [BitmapProvider] instance.
     */
    fun setRender(bitmapProvider: BitmapProvider) {
        this.bitmapProvider = bitmapProvider
        startUpdating()
    }
}