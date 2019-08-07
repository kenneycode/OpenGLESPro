package io.github.kenneycode.openglespro.samples.renderer

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import io.github.kenneycode.openglespro.Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *      这是多渲染目标的例子，可以一次渲染到多个纹理上
 *      This is a sample of multiple render targets, with which we can render to multiple textures at a time
 *
 **/

class SampleMultiRenderTargetRenderer : GLSurfaceView.Renderer {

    private val vertexShaderCode =
        "#version 300 es\n" +
        "precision mediump float;\n" +
        "layout(location = 0) in vec4 a_position;\n" +
        "layout(location = 1) in vec2 a_textureCoordinate;\n" +
        "out vec2 v_textureCoordinate;\n" +
        "void main() {\n" +
        "    v_textureCoordinate = a_textureCoordinate;\n" +
        "    gl_Position = a_position;\n" +
        "}"

    // 多渲染目标fragment shader，这里我们输出三种效果，分别将纹理的R、G、B三个通道设为1.0
    // The fragment shader of multiple render targets. Here we render 3 effects, which set the R, G and B to 1.0 respectively
    private val fragmentShaderMRTCode =
        "#version 300 es\n" +
        "precision mediump float;\n" +
        "layout(location = 0) out vec4 fragColor0;\n" +
        "layout(location = 1) out vec4 fragColor1;\n" +
        "layout(location = 2) out vec4 fragColor2;\n" +
        "uniform sampler2D u_texture;\n" +
        "in vec2 v_textureCoordinate;\n" +
        "void main() {\n" +
        "    vec4 color = texture(u_texture, v_textureCoordinate);\n" +
        "    fragColor0 = vec4(1.0, color.g, color.b, color.a);\n" +
        "    fragColor1 = vec4(color.r, 1.0, color.b, color.a);\n" +
        "    fragColor2 = vec4(color.r, color.g, 1.0, color.a);\n" +
        "}"

    private val fragmentShaderCode =
        "#version 300 es\n" +
        "precision mediump float;\n" +
        "layout(location = 0) out vec4 fragColor;\n" +
        "in vec2 v_textureCoordinate;\n" +
        "uniform sampler2D u_texture;\n" +
        "void main() {\n" +
        "    fragColor = texture(u_texture, v_textureCoordinate);\n" +
        "}"

    private var glSurfaceViewWidth = 0
    private var glSurfaceViewHeight = 0

    private val LOCATION_ATTRBUTE_POSITION = 0
    private val LOCATION_ATTRBUTE_TEXTURE_COORDINATE = 1
    private val VERTEX_COMPONENT_COUNT = 2
    private val TEXTURE_COORDINATE_COMPONENT_COUNT = 2

    val vertexData = floatArrayOf(-1f, -1f, -1f, 1f, 1f, 1f, -1f, -1f, 1f, 1f, 1f, -1f)
    val textureCoordinateData = floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)
    private lateinit var vertexDataBuffer : FloatBuffer
    private lateinit var textureCoordinateDataBuffer : FloatBuffer

    private var imageTexture = 0
    private var programIdMTR = 0
    private var programIdRTS = 0
    private var frameBufferMTR = 0
    private lateinit var targets : IntArray

    override fun onDrawFrame(gl: GL10?) {

        GLES30.glUseProgram(programIdMTR)

        GLES30.glEnableVertexAttribArray(LOCATION_ATTRBUTE_POSITION)
        GLES30.glVertexAttribPointer(LOCATION_ATTRBUTE_POSITION, VERTEX_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, vertexDataBuffer)
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRBUTE_TEXTURE_COORDINATE)
        GLES30.glVertexAttribPointer(LOCATION_ATTRBUTE_TEXTURE_COORDINATE, TEXTURE_COORDINATE_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, textureCoordinateDataBuffer)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)
        GLES30.glUniform1i(GLES30.glGetUniformLocation(programIdMTR, "u_texture"), 0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferMTR)

        // 设置清屏颜色
        // Set the color which the screen will be cleared to
        GLES30.glClearColor(0.9f, 0.9f, 0.9f, 1f)

        // 清屏
        // Clear the screen
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // 设置视口，这里设置为整个GLSurfaceView区域
        // Set the viewport to the full GLSurfaceView
        GLES30.glViewport(0, 0, glSurfaceViewWidth, glSurfaceViewHeight)

        // 调用draw方法用TRIANGLES的方式执行渲染，顶点数量为3个
        // Call the draw method with GL_TRIANGLES to render 3 vertices
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexData.size / VERTEX_COMPONENT_COUNT)

        // 将渲染得到的多个结果渲染到屏幕上
        // Render the multiple render results to screen
        renderTargetsToScreen(targets)
    }

    private fun renderTargetsToScreen(targets : IntArray) {
        GLES30.glUseProgram(programIdRTS)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glClearColor(0.9f, 0.9f, 0.9f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glViewport(0, 0, glSurfaceViewWidth, glSurfaceViewHeight)
        val vertexDataLeftBottom = floatArrayOf(-1f, -1f, -1f, 0f, 0f, 0f, -1f, -1f, 0f, 0f, 0f, -1f)
        val offsetVertexData = FloatArray(vertexDataLeftBottom.size)
        for (i in 0 until targets.size) {
            for (j in 0 until vertexDataLeftBottom.size) {
                offsetVertexData[j] = vertexDataLeftBottom[j] + 0.5f * i
            }
            val vertexDataBuffer = ByteBuffer.allocateDirect(12 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            vertexDataBuffer.put(offsetVertexData)
            vertexDataBuffer.position(0)
            GLES30.glEnableVertexAttribArray(LOCATION_ATTRBUTE_POSITION)
            GLES30.glVertexAttribPointer(LOCATION_ATTRBUTE_POSITION, VERTEX_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, vertexDataBuffer)
            textureCoordinateDataBuffer.put(textureCoordinateData)
            textureCoordinateDataBuffer.position(0)
            GLES30.glEnableVertexAttribArray(LOCATION_ATTRBUTE_TEXTURE_COORDINATE)
            GLES30.glVertexAttribPointer(LOCATION_ATTRBUTE_TEXTURE_COORDINATE, TEXTURE_COORDINATE_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, textureCoordinateDataBuffer)
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, targets[i])
            GLES30.glUniform1i(GLES30.glGetUniformLocation(programIdRTS, "u_texture"), 0)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, offsetVertexData.size / VERTEX_COMPONENT_COUNT)
        }
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        // 记录GLSurfaceView的宽高
        // Record the width and height of the GLSurfaceView
        glSurfaceViewWidth = width
        glSurfaceViewHeight = height


        // 创建一个frame buffer用于绑定多个渲染目标
        // Create a frame buffer to bind multiple render targets
        val frameBuffers = IntArray(1)
        GLES30.glGenFramebuffers(frameBuffers.size, frameBuffers, 0)
        frameBufferMTR = frameBuffers[0]
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferMTR)

        // 将3个渲染目标绑定到frame buffer上的3个attachment上
        // Bind 3 render targets to the 3 attachments of frame buffer
        for (i in 0 until targets.size) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, targets[i])
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, glSurfaceViewWidth, glSurfaceViewHeight, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0 + i, GLES30.GL_TEXTURE_2D, targets[i], 0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        }

        // 将0~2号attachment设置为draw目标
        // Set 0~2# attachments to draw target
        val attachments = intArrayOf(GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_COLOR_ATTACHMENT1, GLES30.GL_COLOR_ATTACHMENT2)
        val attachmentBuffer = IntBuffer.allocate(attachments.size)
        attachmentBuffer.put(attachments)
        attachmentBuffer.position(0)
        GLES30.glDrawBuffers(attachments.size, attachmentBuffer)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        // 初始化多渲染目标效果的GL程序
        // Initialize the GL program of multiple render targets
        initMTRProgram()

        // 初始化渲染到屏幕的GL程序
        // Initialize the GL program of rendering to screen
        initRTSProgram()

        initData()
    }

    private fun initMTRProgram() {
        programIdMTR = GLES30.glCreateProgram()
        val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        val fragmentShader= GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(vertexShader, vertexShaderCode)
        GLES30.glShaderSource(fragmentShader, fragmentShaderMRTCode)
        GLES30.glCompileShader(vertexShader)
        GLES30.glCompileShader(fragmentShader)
        val shaderInfo = GLES30.glGetShaderInfoLog(fragmentShader)
        GLES30.glAttachShader(programIdMTR, vertexShader)
        GLES30.glAttachShader(programIdMTR, fragmentShader)
        GLES30.glLinkProgram(programIdMTR)
    }

    private fun initRTSProgram() {
        programIdRTS = GLES30.glCreateProgram()
        val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        val fragmentShader= GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(vertexShader, vertexShaderCode)
        GLES30.glShaderSource(fragmentShader, fragmentShaderCode)
        GLES30.glCompileShader(vertexShader)
        GLES30.glCompileShader(fragmentShader)
        val shaderInfo = GLES30.glGetShaderInfoLog(fragmentShader)
        GLES30.glAttachShader(programIdRTS, vertexShader)
        GLES30.glAttachShader(programIdRTS, fragmentShader)
        GLES30.glLinkProgram(programIdRTS)
        Log.e("debug", "glGetProgramInfoLog = ${GLES30.glGetProgramInfoLog(programIdRTS)}")
    }

    private fun initData() {
        vertexDataBuffer = ByteBuffer.allocateDirect(vertexData.size * java.lang.Float.SIZE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        vertexDataBuffer.put(vertexData)
        vertexDataBuffer.position(0)
        textureCoordinateDataBuffer = ByteBuffer.allocateDirect(textureCoordinateData.size * java.lang.Float.SIZE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        textureCoordinateDataBuffer.put(textureCoordinateData)
        textureCoordinateDataBuffer.position(0)
                val textures = IntArray(1)
        GLES30.glGenTextures(textures.size, textures, 0)
        imageTexture = textures[0]
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        val bitmap = Util.decodeBitmapFromAssets("image_0.jpg")
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)
        val b = ByteBuffer.allocate(bitmap.width * bitmap.height * 4)
        bitmap.copyPixelsToBuffer(b)
        b.position(0)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap.width, bitmap.height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, b)
        bitmap.recycle()
        GLES30.glUseProgram(programIdMTR)
        val locationUniformTexture = GLES30.glGetUniformLocation(programIdMTR, "u_texture")
        GLES30.glUniform1i(locationUniformTexture, 0)
        
        val targetCount = 3
        targets = IntArray(targetCount)
        GLES30.glGenTextures(targets.size, targets, 0)
    }

}