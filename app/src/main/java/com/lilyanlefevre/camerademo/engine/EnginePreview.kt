package com.lilyanlefevre.camerademo.engine

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class EnginePreview @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs) {
    init {
        initPreview()
    }

    private fun initPreview() {
        setEGLContextClientVersion(2)
    }
}
