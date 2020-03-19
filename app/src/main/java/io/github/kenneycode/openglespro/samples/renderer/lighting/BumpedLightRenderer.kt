package io.github.kenneycode.openglespro.samples.renderer.lighting

import android.opengl.GLES30
import io.github.kenneycode.openglespro.Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sqrt

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *      法向图例子
 *      Bumped sample
 *
 **/

class BumpedLightRenderer : LightingRenderer("lighting/bumpedlighting.vs", "lighting/bumpedlighting.fs") {

    private var normalTexture = 0

    private val LOCATION_ATTRIBUTE_TANGENT = 3
    private val LOCATION_ATTRIBUTE_BITANGENT = 4

    private val TANGENT_COMPONENT_COUNT = 3
    private lateinit var tangentDataBuffer : FloatBuffer

    private val BITANGENT_COMPONENT_COUNT = 3
    private lateinit var bitangentDataBuffer : FloatBuffer

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_TANGENT)
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_TANGENT, TANGENT_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, tangentDataBuffer)
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_BITANGENT)
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_BITANGENT, BITANGENT_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, bitangentDataBuffer)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, normalTexture)
        GLES30.glUniform3f(GLES30.glGetUniformLocation(programId, "lightPos"), 2f, 0f, 2f)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        val textures = IntArray(1)
        GLES30.glGenTextures(textures.size, textures, 0)
        normalTexture = textures[0]
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        val bitmap2 = Util.decodeBitmapFromAssets("lighting/brickn.png")
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, normalTexture)
        val b2 = ByteBuffer.allocate(bitmap2.width * bitmap2.height * 4)
        bitmap2.copyPixelsToBuffer(b2)
        b2.position(0)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap2.width,
            bitmap2.height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, b2)
        bitmap2.recycle()
        GLES30.glUniform1i(GLES30.glGetUniformLocation(programId, "normalTex"), 1)
        calculateTangents()
    }

    private fun calculateTangents() {
        tangentDataBuffer = ByteBuffer.allocateDirect(vertexData.size * java.lang.Float.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        bitangentDataBuffer = ByteBuffer.allocateDirect(vertexData.size * java.lang.Float.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        val tangents = mutableListOf<Float>()
        val bitangents = mutableListOf<Float>()
        for (i in 0 until vertexData.size / 9) {
            val e1x = vertexData[i * 9 + 3] - vertexData[i * 9]
            val e1y = vertexData[i * 9 + 3 + 1] - vertexData[i * 9 + 1]
            val e1z = vertexData[i * 9 + 3 + 2] - vertexData[i * 9 + 2]
            val e2x = vertexData[i * 9 + 6] - vertexData[i * 9]
            val e2y = vertexData[i * 9 + 6 + 1] - vertexData[i * 9 + 1]
            val e2z = vertexData[i * 9 + 6 + 2] - vertexData[i * 9 + 2]
            val deltaUV1x = textureCoordinateData[i * 6 + 2] - textureCoordinateData[i * 6]
            val deltaUV1y = textureCoordinateData[i * 6 + 2 + 1] - textureCoordinateData[i * 6 + 1]
            val deltaUV2x = textureCoordinateData[i * 6 + 4] - textureCoordinateData[i * 6]
            val deltaUV2y = textureCoordinateData[i * 6 + 4 + 1] - textureCoordinateData[i * 6 + 1]
            val f = 1f / (deltaUV1x * deltaUV2y - deltaUV2x * deltaUV1y)
            var tangentx = f * (deltaUV2y * e1x - deltaUV1y * e2x)
            var tangenty = f * (deltaUV2y * e1y - deltaUV1y * e2y)
            var tangentz = f * (deltaUV2y * e1z - deltaUV1y * e2z)
            val tangentLength = sqrt(tangentx * tangentx + tangenty * tangenty + tangentz * tangentz)
            tangentx /= tangentLength
            tangenty /= tangentLength
            tangentz /= tangentLength
            var bitangentx = f * (-deltaUV2x * e1x + deltaUV1x * e2x)
            var bitangenty = f * (-deltaUV2x * e1y + deltaUV1x * e2y)
            var bitangentz = f * (-deltaUV2x * e1z + deltaUV1x * e2z)
            val bitangentLength = sqrt(bitangentx * bitangentx + bitangenty * bitangenty + bitangentz * bitangentz)
            bitangentx /= bitangentLength
            bitangenty /= bitangentLength
            bitangentz /= bitangentLength
            for (j in 0 until 9) {
                when (j % 3) {
                    0 -> {
                        tangents.add(tangentx)
                        bitangents.add(bitangentx)
                    }
                    1 -> {
                        tangents.add(tangenty)
                        bitangents.add(bitangenty)
                    }
                    2 -> {
                        tangents.add(tangentz)
                        bitangents.add(bitangentz)
                    }
                }
            }
        }
        tangentDataBuffer.put(tangents.toFloatArray())
        tangentDataBuffer.position(0)
        bitangentDataBuffer.put(bitangents.toFloatArray())
        bitangentDataBuffer.position(0)
    }

}