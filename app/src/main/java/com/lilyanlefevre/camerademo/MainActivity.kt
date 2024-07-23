package com.lilyanlefevre.camerademo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.camera_card).setOnClickListener(View.OnClickListener {
            if (!checkCameraPermission()) {
                Toast.makeText(this@MainActivity, "You must accept permissions!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            val i = Intent(this, CameraActivity::class.java)
            startActivity(i)
        })

        if (!checkCameraPermission()) {
            requestCameraPermission()
        }
    }


    private fun checkCameraPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
    }


    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Permissions accepted!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
