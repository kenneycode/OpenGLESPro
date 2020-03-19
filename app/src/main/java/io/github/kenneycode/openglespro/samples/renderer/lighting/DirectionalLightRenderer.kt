package io.github.kenneycode.openglespro.samples.renderer.lighting

import android.opengl.GLES30
import javax.microedition.khronos.opengles.GL10

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *      平行光光照例子
 *      Directional light sample
 *
 **/

class DirectionalLightRenderer : LightingRenderer("lighting/directionallight.vs", "lighting/directionallight.fs") {

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        GLES30.glUniform3f(GLES30.glGetUniformLocation(programId, "lightDirection"), -5f, 0f, 0f)
        assert(GLES30.glGetError() == 0)
    }

}