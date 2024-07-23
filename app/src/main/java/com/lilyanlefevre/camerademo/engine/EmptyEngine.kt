package com.lilyanlefevre.camerademo.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.lilyanlefevre.camerademo.R
import com.lilyanlefevre.camerademo.camera.Camera
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class EmptyEngine(
    private val context: Context,
    private val opendCamera: Camera,
    private val surfaceView: GLSurfaceView?
) : GLSurfaceView.Renderer, BitmapProvider {
    private var surfaceTextureId = 0
    private var surfaceTexture: SurfaceTexture? = null

    private var programHandle = 0
    private var vertexHandle = 0
    private var fragmentHandle = 0


    private var vertexPositionHandle = 0
    private var vertexMatrixHandle = 0
    private var texureOESHandle = 0
    private var vertexCoordinateHandle = 0

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var vertexOrederBuffer: FloatBuffer
    private val transformMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        createSurfaceTexture()

        opendCamera.startPreviewOnTexture(surfaceTexture)

        vertexBuffer = ByteBuffer.allocateDirect(vertex_coords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertex_coords).position(0)

        vertexOrederBuffer = ByteBuffer.allocateDirect(vertex_coords_order.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertex_coords_order)
        vertexOrederBuffer.position(0)

        programHandle = GLES20.glCreateProgram()

        vertexHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)

        val vertexShader = Utils.readShaderFromResource(context, R.raw.vertex_shader)
        GLES20.glShaderSource(vertexHandle, vertexShader)
        GLES20.glCompileShader(vertexHandle)
        GLES20.glAttachShader(programHandle, vertexHandle)

        fragmentHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)

        val fragmentShader = Utils.readShaderFromResource(context, R.raw.fragment_shader)
        GLES20.glShaderSource(fragmentHandle, fragmentShader)
        GLES20.glCompileShader(fragmentHandle)
        GLES20.glAttachShader(programHandle, fragmentHandle)

        GLES20.glLinkProgram(programHandle)
    }


    private fun createSurfaceTexture() {
        if (surfaceTexture != null) {
            return
        }
        surfaceTextureId = Utils.createOESTextureObject()
        surfaceTexture = SurfaceTexture(surfaceTextureId)
        opendCamera.startPreviewOnTexture(surfaceTexture)

        surfaceTexture!!.setOnFrameAvailableListener { surfaceView!!.requestRender() }
    }

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        GLES20.glViewport(0, 0, width, height)
    }

    private var currentBitmap: Bitmap? = null

    override fun onDrawFrame(gl: GL10) {
        if (surfaceTexture != null) {
            surfaceTexture!!.updateTexImage()
            surfaceTexture!!.getTransformMatrix(transformMatrix)
        }

        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUseProgram(programHandle)

        vertexPositionHandle = GLES20.glGetAttribLocation(programHandle, "avVertex")
        vertexCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "avVertexCoordinate")


        vertexMatrixHandle = GLES20.glGetUniformLocation(programHandle, "umTransformMatrix")
        texureOESHandle = GLES20.glGetUniformLocation(programHandle, "usTextureOes")


        GLES20.glVertexAttribPointer(
            vertexPositionHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            8,
            vertexBuffer
        )
        GLES20.glVertexAttribPointer(
            vertexCoordinateHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            8,
            vertexOrederBuffer
        )

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, surfaceTextureId)
        GLES20.glUniform1i(texureOESHandle, 0)

        GLES20.glUniformMatrix4fv(vertexMatrixHandle, 1, false, transformMatrix, 0)

        GLES20.glEnableVertexAttribArray(vertexPositionHandle)
        GLES20.glEnableVertexAttribArray(vertexCoordinateHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)

        GLES20.glDisableVertexAttribArray(vertexPositionHandle)
        GLES20.glDisableVertexAttribArray(vertexCoordinateHandle)

        saveCurrentSurface()
    }

    private fun saveCurrentSurface() {
        val width = screenWidth
        val height = screenHeight
        val buf = ByteBuffer.allocateDirect(width * height * 4)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        GLES20.glReadPixels(
            0, 0, width, height,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf
        )
        buf.rewind()

        try {
            var bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bmp.copyPixelsFromBuffer(buf)
            val matrix = android.graphics.Matrix().apply {
                postScale(1f, -1f, width / 2f, height / 2f)
            }
            bmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true)
            currentBitmap = bmp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        if (surfaceTexture != null) {
            surfaceTexture!!.release()
            surfaceTexture = null
        }
    }

    companion object {
        private val vertex_coords = floatArrayOf(
            1f, 1f,
            -1f, 1f,
            -1f, -1f,
            1f, 1f,
            -1f, -1f,
            1f, -1f
        )

        private val vertex_coords_order = floatArrayOf(
            1f, 1f,
            0f, 1f,
            0f, 0f,
            1f, 1f,
            0f, 0f,
            1f, 0f
        )
    }

    override fun getCurrentBitmap(): Bitmap? {
        return currentBitmap
    }
}
