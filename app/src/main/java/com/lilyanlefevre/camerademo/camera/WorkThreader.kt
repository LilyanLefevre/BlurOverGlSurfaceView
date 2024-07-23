package com.lilyanlefevre.camerademo.camera

import android.os.Handler
import android.os.HandlerThread

class WorkThreader(name: String?) : HandlerThread(name) {
    var bindHandler: Handler? = null
        private set

    @Synchronized
    override fun start() {
        super.start()
        if (bindHandler == null) bindHandler = Handler(looper)
    }

    fun release() {
        quit()
        if (bindHandler != null) {
            bindHandler = null
        }
    }
}
