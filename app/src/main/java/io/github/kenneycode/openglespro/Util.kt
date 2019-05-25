package io.github.kenneycode.openglespro

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.util.Log

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *
 **/

class Util {

    companion object {

        lateinit var context: Context

        fun decodeBitmapFromAssets(filename : String) : Bitmap {
            val options = BitmapFactory.Options()
            options.inSampleSize = 1
            val bitmap = BitmapFactory.decodeStream(context.assets.open(filename))
            if (bitmap == null) {
                Log.e("debug", "bitmap decode fail, path = $filename")
            }
            return bitmap
        }

        fun checkGLError() {
            val error = GLES30.glGetError()
            if (error != GLES30.GL_NO_ERROR) {
                val hexErrorCode = Integer.toHexString(error)
                Log.e("debug", "glError: $hexErrorCode")
                throw RuntimeException("GLError")
            }
        }

        fun getScreenWidth() : Int {
            return 1440
        }

        fun getScreenHeight() : Int {
            return 2560
        }

    }



}