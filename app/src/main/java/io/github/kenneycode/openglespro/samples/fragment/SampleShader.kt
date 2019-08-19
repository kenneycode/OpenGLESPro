package io.github.kenneycode.openglespro.samples.fragment

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.kenneycode.openglespro.R
import io.github.kenneycode.openglespro.samples.renderer.SampleShaderRenderer

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *      这是一个演示OpenGL 3.0 shader的例子，主要演示其中的location字段的作用
 *      This sample demonstrates the usage of location in OpenGL 3.0 shader
 *
 **/

class SampleShader : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_common_sample, container,  false)
        val glSurfaceView = rootView.findViewById<GLSurfaceView>(R.id.glsurfaceview)
        // 设置GL版本，这里设置为3.0
        // Set GL version, here I set it to 3.0
        glSurfaceView.setEGLContextClientVersion(3)
        // 设置RGBA颜色缓冲、深度缓冲及stencil缓冲大小
        // Set the size of RGBA、depth and stencil vertexDataBuffer
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0)

        // 设置对应sample的渲染器
        // Set the corresponding sample renderer
        glSurfaceView.setRenderer(SampleShaderRenderer())
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        return rootView
    }
}