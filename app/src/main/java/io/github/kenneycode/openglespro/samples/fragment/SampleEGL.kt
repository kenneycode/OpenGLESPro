package io.github.kenneycode.openglespro.samples.fragment

import android.opengl.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.kenneycode.openglespro.R

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *      这是一个使用EGL的例子
 *      This is a sample of using EGL
 *
 **/

class SampleEGL : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_common_sample, container,  false)

        Thread {

            val egl = EGL()
            egl.init()

            egl.bind()

            val textures = IntArray(1)
            GLES30.glGenTextures(textures.size, textures, 0)
            val imageTexture = textures[0]
            assert(GLES30.glIsTexture(imageTexture))

            egl.release()

        }.start()

        return rootView
    }

    inner class EGL {

        private var eglDisplay = EGL14.EGL_NO_DISPLAY
        private var eglSurface = EGL14.EGL_NO_SURFACE
        private var eglContext = EGL14.EGL_NO_CONTEXT

        fun init() {

            // 获取显示设备
            // Get the display
            eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)

            // 初始化显示设备
            // Initialize the display
            EGL14.eglInitialize(eglDisplay, version, 0, version, 1)
            val attribList = intArrayOf(
                    EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT or EGLExt.EGL_OPENGL_ES3_BIT_KHR, EGL14.EGL_NONE
            )

            // 选择config
            // Choose config
            val eglConfig = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(
                    eglDisplay, attribList, 0, eglConfig, 0, eglConfig.size,
                    numConfigs, 0
            )

            // 创建EGL Context
            // Create EGL Context
            eglContext = EGL14.eglCreateContext(
                    eglDisplay, eglConfig[0], EGL14.EGL_NO_CONTEXT,
                    intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE), 0
            )
            val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)

            // 创建Pbuffer Surface
            // Create Pbuffer Surface
            eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig[0], surfaceAttribs, 0)

        }

        fun bind() {
            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
        }

        fun release() {
            if (eglDisplay !== EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                EGL14.eglDestroySurface(eglDisplay, eglSurface)
                EGL14.eglDestroyContext(eglDisplay, eglContext)
                EGL14.eglReleaseThread()
                EGL14.eglTerminate(eglDisplay)
            }
            eglDisplay = EGL14.EGL_NO_DISPLAY
            eglContext = EGL14.EGL_NO_CONTEXT
            eglSurface = EGL14.EGL_NO_SURFACE

        }

        private fun checkEglError(msg: String) {
            val error= EGL14.eglGetError()
            if (error != EGL14.EGL_SUCCESS) {
                throw RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error))
            }
        }

    }

}