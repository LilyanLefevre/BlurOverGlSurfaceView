package com.lilyanlefevre.camerademo.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast

class Camera(c: Activity, facing: Int) {
    private val context: Context = c
    private lateinit var connectManager: CameraManager
    private lateinit var openCamera: CameraDevice
    private val cameraThreader = WorkThreader("camera")
    private var orientationEventListener: OrientationEventListener? = null

    init {
        cameraThreader.start()
        initCamera(facing)
    }

    private fun initCamera(facing: Int) {
        try {
            connectManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val allIds = connectManager.cameraIdList
            var params: CameraCharacteristics?

            for (id in allIds) {
                params = connectManager.getCameraCharacteristics(id)

                if (params.get(CameraCharacteristics.LENS_FACING) == facing) {
                    openCamera(id)
                    break
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(CameraAccessException::class)
    private fun openCamera(id: String) {
        connectManager.openCamera(id, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                openCamera = camera
                updatePreview()
            }

            override fun onDisconnected(camera: CameraDevice) {
                openCamera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
            }
        }, null)
    }

    fun startPreviewOnTexture(texture: SurfaceTexture?) {
        updatePreview(texture)
    }

    private fun updatePreview(texture: SurfaceTexture? = null) {
        try {
            val previewRequestBuilder =
                openCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            val previewTexture = texture ?: SurfaceTexture(0)
            previewTexture.setDefaultBufferSize(1280, 720)
            val previewSurface = Surface(previewTexture)
            previewRequestBuilder.addTarget(previewSurface)

            openCamera.createCaptureSession(
                listOf(previewSurface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        try {
                            previewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )
                            val previewRequest = previewRequestBuilder.build()
                            session.setRepeatingRequest(
                                previewRequest, null, cameraThreader.bindHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(context, "camera ConfigureFailed", Toast.LENGTH_SHORT).show()
                    }
                }, cameraThreader.bindHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        openCamera.close()
        cameraThreader.release()
        orientationEventListener?.disable()
    }
}
