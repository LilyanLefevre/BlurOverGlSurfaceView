package com.lilyanlefevre.camerademo.engine

import android.graphics.Bitmap

interface BitmapProvider {
    fun getCurrentBitmap(): Bitmap?
}