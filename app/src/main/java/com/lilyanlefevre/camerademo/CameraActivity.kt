package com.lilyanlefevre.camerademo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.lilyanlefevre.camerademo.blur.BlurredConstraintLayout
import com.lilyanlefevre.camerademo.camera.Camera
import com.lilyanlefevre.camerademo.engine.EmptyEngine
import com.lilyanlefevre.camerademo.engine.EnginePreview

class CameraActivity : Activity() {
    private lateinit var preview: EnginePreview
    private lateinit var camera: Camera
    private lateinit var engine: EmptyEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        preview = findViewById(R.id.preview)
        camera = Camera(this, CameraCharacteristics.LENS_FACING_BACK)
        engine = EmptyEngine(this, camera, preview)
        preview.setRenderer(engine)
    }

    override fun onResume() {
        super.onResume()
        val blurredLayout1 = findViewById<BlurredConstraintLayout>(R.id.blurredLayout1)
        //blurredLayout1.setRender(engine)
        blurredLayout1.visibility = View.INVISIBLE
        blurredLayout1.scaleInAndFadeIn(startDelay = 1000)

        val blurredLayout2 = findViewById<BlurredConstraintLayout>(R.id.blurredLayout2)
        blurredLayout2.setRender(engine)
        blurredLayout2.visibility = View.INVISIBLE
        blurredLayout2.scaleInAndFadeIn(startDelay = 1500)

        val blurredLayout3 = findViewById<BlurredConstraintLayout>(R.id.blurredLayout3)
        //blurredLayout3.setRender(engine)
        blurredLayout3.visibility = View.INVISIBLE
        blurredLayout3.scaleInAndFadeIn(startDelay = 2000)
    }

    override fun onDestroy() {
        super.onDestroy()

        camera.stop()
        engine.release()
    }
}

/**
 * Extension function to scale in and fade in the view.
 *
 * @param duration The duration of the animation in milliseconds.
 */
fun View.scaleInAndFadeIn(duration: Long = 800L, startDelay: Long = 0) {
    this.visibility = View.VISIBLE
    this.alpha = 0f
    this.scaleX = 0f
    this.scaleY = 0f
    this.animate()
        .setStartDelay(startDelay)
        .alpha(1f)
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .start()
}