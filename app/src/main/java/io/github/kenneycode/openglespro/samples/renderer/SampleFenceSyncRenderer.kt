package io.github.kenneycode.openglespro.samples.renderer

import android.graphics.Bitmap
import android.opengl.*
import android.os.Handler
import android.os.HandlerThread
import android.widget.ImageView
import io.github.kenneycode.openglespro.Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *      这是一个使用栅栏做GL命令同步的例子
 *      This is a sample of using fence to synchronize the GL commands
 *
 **/

class SampleFenceSyncRenderer(val imageView : ImageView) : GLSurfaceView.Renderer {

    private val vertexShaderCode =
                "#version 300 es\n" +
                "precision mediump float;\n" +
                "layout(location = 0) in vec4 a_Position;\n" +
                "layout(location = 1) in vec2 a_textureCoordinate;\n" +
                "out vec2 v_textureCoordinate;\n" +
                "void main() {\n" +
                "    v_textureCoordinate = a_textureCoordinate;\n" +
                "    gl_Position = a_Position;\n" +
                "}"

    private val fragmentShaderCode =
                "#version 300 es\n" +
                "precision mediump float;\n" +
                "layout(location = 0) out vec4 fragColor;\n" +
                "in vec2 v_textureCoordinate;\n" +
                "uniform sampler2D u_texture;\n" +
                "void main() {\n" +
                "    float offset = 0.01;\n" +
                "    vec4 colorCenter = texture(u_texture, vec2(v_textureCoordinate.x, v_textureCoordinate.y));\n" +
                "    vec4 colorLeft = texture(u_texture, vec2(v_textureCoordinate.x - offset, v_textureCoordinate.y));\n" +
                "    vec4 colorTop = texture(u_texture, vec2(v_textureCoordinate.x, v_textureCoordinate.y + offset));\n" +
                "    vec4 colorRight = texture(u_texture, vec2(v_textureCoordinate.x + offset, v_textureCoordinate.y));\n" +
                "    vec4 colorBottom = texture(u_texture, vec2(v_textureCoordinate.x, v_textureCoordinate.y - offset));\n" +
                "    fragColor = (colorCenter + colorLeft + colorTop + colorRight + colorBottom) / 5.0;\n" +
                "}"
    // 三角形顶点数据
    // The vertex data of a triangle
    private val vertexData = floatArrayOf(-1f, -1f, -1f, 1f, 1f, 1f, -1f, -1f, 1f, 1f, 1f, -1f)
    private val textureCoordinateData = floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)

    lateinit var vertexDataBuffer : FloatBuffer
    lateinit var textureCoordinateDataBuffer : FloatBuffer

    private var glSurfaceViewWidth = 0
    private var glSurfaceViewHeight = 0

    private val LOCATION_ATTRBUTE_POSITION = 0
    private val LOCATION_ATTRBUTE_TEXTURE_COORDINATE = 1

    private val LOCATION_UNIFORM_POSITION = 0

    // 每个顶点的成份数
    // The num of components of per vertex
    private val VERTEX_COMPONENT_COUNT = 2

    private var frameBuffer = 0
    private var sharedTexture = 0

    private lateinit var otherThreadHandler : Handler

    var flag = false
    var imageTexture = 0

    override fun onDrawFrame(gl: GL10?) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, sharedTexture)
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer)
            GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER,
                GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D,
                sharedTexture,
                0
            )
        
        // 设置清屏颜色
        // Set the color which the screen will be cleared to
        GLES30.glClearColor(0f, 0f, 0f, 1f)

        // 清屏
        // Clear the screen
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // 设置视口，这里设置为整个GLSurfaceView区域
        // Set the viewport to the full GLSurfaceView
        GLES30.glViewport(0, 0, glSurfaceViewWidth, glSurfaceViewHeight)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)
        vertexDataBuffer = ByteBuffer.allocateDirect(vertexData.size * java.lang.Float.SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexDataBuffer.put(vertexData)
        vertexDataBuffer.position(0)
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRBUTE_POSITION)
        GLES30.glVertexAttribPointer(LOCATION_ATTRBUTE_POSITION, VERTEX_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, vertexDataBuffer)

        textureCoordinateDataBuffer = ByteBuffer.allocateDirect(textureCoordinateData.size * java.lang.Float.SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        textureCoordinateDataBuffer.put(textureCoordinateData)
        textureCoordinateDataBuffer.position(0)
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRBUTE_TEXTURE_COORDINATE)
        GLES30.glVertexAttribPointer(LOCATION_ATTRBUTE_TEXTURE_COORDINATE, VERTEX_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, textureCoordinateDataBuffer)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, textureCoordinateData.size / VERTEX_COMPONENT_COUNT)

        // 向OpenGL的Command Buffer中插入一个fence
        // Insert a fence into the OpenGL command buffer
        val fenceSyncObject = GLES30.glFenceSync(GLES30.GL_SYNC_GPU_COMMANDS_COMPLETE, 0)

        // 在另一个线程中读取当前线程的渲染结果
        // Read the render result in the other thread
        otherThreadHandler.post {
            if (!flag) {

                // 等待fence前的OpenGL命令执行完毕
                // Waiting for completion of the OpenGL commands before our fence
                GLES30.glWaitSync(fenceSyncObject, 0, GLES30.GL_TIMEOUT_IGNORED)

                // 删除fence同步对象
                // Delete the fence sync object
                GLES30.glDeleteSync(fenceSyncObject)

                val frameBuffers = IntArray(1)
                GLES30.glGenFramebuffers(frameBuffers.size, frameBuffers, 0)
                                GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, sharedTexture)
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffers[0])
                                GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, sharedTexture, 0)
                                val buffer = ByteBuffer.wrap(ByteArray(glSurfaceViewWidth * glSurfaceViewHeight * 4))
                GLES30.glReadPixels(0, 0, glSurfaceViewWidth, glSurfaceViewHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer)
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
                val bitmap = Bitmap.createBitmap(glSurfaceViewWidth, glSurfaceViewHeight, Bitmap.Config.ARGB_8888)
                buffer.position(0)
                bitmap.copyPixelsFromBuffer(buffer)
                flag = true

                // 将读取到的渲染结果显示到一个ImageView上
                // Display the read render result on a ImageView
                imageView.post {
                    imageView.setImageBitmap(bitmap)
                }
            }

        }

        // 将frame buffer绑回0号，将渲染结果同时也显示到屏幕上
        // Bind frame buffer to 0# and also render the result on screen
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)

        GLES30.glClearColor(0.9f, 0.9f, 0.9f, 1f)

        // 清屏
        // Clear the screen
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // 设置视口，这里设置为整个GLSurfaceView区域
        // Set the viewport to the full GLSurfaceView
        GLES30.glViewport(0, 0, glSurfaceViewWidth, glSurfaceViewHeight)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexData.size / VERTEX_COMPONENT_COUNT)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // 记录GLSurfaceView的宽高
        // Record the width and height of the GLSurfaceView
        glSurfaceViewWidth = width
        glSurfaceViewHeight = height

        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, sharedTexture)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, sharedTexture, 0)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, glSurfaceViewWidth, glSurfaceViewHeight, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 创建GL程序
        // Create GL program
        val programId = GLES30.glCreateProgram()

        // 加载、编译vertex shader和fragment shader
        // Load and compile vertex shader and fragment shader
        val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        val fragmentShader= GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(vertexShader, vertexShaderCode)
        GLES30.glShaderSource(fragmentShader, fragmentShaderCode)
        GLES30.glCompileShader(vertexShader)
        GLES30.glCompileShader(fragmentShader)

        // 将shader程序附着到GL程序上
        // Attach the compiled shaders to the GL program
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)

        // 链接GL程序
        // Link the GL program
        GLES30.glLinkProgram(programId)

        // 将三角形顶点数据放入buffer中
        // Put the triangle vertex data into the vertexDataBuffer
        vertexDataBuffer = ByteBuffer.allocateDirect(vertexData.size * java.lang.Float.SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexDataBuffer.put(vertexData)
        vertexDataBuffer.position(0)

        // 应用GL程序
        // Use the GL program
        GLES30.glUseProgram(programId)

        textureCoordinateDataBuffer = ByteBuffer.allocateDirect(textureCoordinateData.size * java.lang.Float.SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        textureCoordinateDataBuffer.put(textureCoordinateData)
        textureCoordinateDataBuffer.position(0)

        val textures = IntArray(1)
        GLES30.glGenTextures(textures.size, textures, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        val bitmap = Util.decodeBitmapFromAssets("image_0.jpg")
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
        imageTexture = textures[0]
        val b = ByteBuffer.allocate(bitmap.width * bitmap.height * 4)
        bitmap.copyPixelsToBuffer(b)
        b.position(0)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexImage2D(
        GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap.width,
        bitmap.height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, b)
        bitmap.recycle()
        GLES30.glUniform1i(LOCATION_UNIFORM_POSITION, 0)
        
        val sharedTextures = IntArray(1)
        GLES30.glGenTextures(sharedTextures.size, sharedTextures, 0)
        sharedTexture = sharedTextures[0]

        val frameBuffers = IntArray(1)
        GLES30.glGenFramebuffers(frameBuffers.size, frameBuffers, 0)
        frameBuffer = frameBuffers[0]

        // 创建另一个线程
        // Create another thread
        val thread = HandlerThread("OtherThread")
        thread.start()
        otherThreadHandler = Handler(thread.looper)

        // 给创建的线程配置EGL环境，与当前GL线程做共享EGL Context
        // Configure EGL environment for the created thread and share the current thread's EGL context to it
        val sharedContext = EGL14.eglGetCurrentContext()
        otherThreadHandler.post {
            val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (eglDisplay === EGL14.EGL_NO_DISPLAY) {
                throw RuntimeException("unable to get EGL14 display")
            }
            if (EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
                throw RuntimeException()
            }
            val version = IntArray(2)
            if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
                throw RuntimeException()
            }
            if (EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
                throw RuntimeException()
            }
            val attribList = intArrayOf(
                EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT or EGLExt.EGL_OPENGL_ES3_BIT_KHR, EGL14.EGL_NONE, 0,
                EGL14.EGL_NONE
            )
            val eglConfig = arrayOfNulls<android.opengl.EGLConfig>(1)
            val numConfigs = IntArray(1)
            if (!EGL14.eglChooseConfig(
                    eglDisplay, attribList, 0, eglConfig, 0, eglConfig.size,
                    numConfigs, 0
                )) {
                throw RuntimeException()
            }
            if (EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
                throw RuntimeException()
            }
            val myContext = EGL14.eglCreateContext(
                eglDisplay, eglConfig[0], sharedContext,
                intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE), 0
            )
            if (EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
                throw RuntimeException()
            }
            val values = IntArray(1)
            EGL14.eglQueryContext(
                eglDisplay, myContext, EGL14.EGL_CONTEXT_CLIENT_VERSION,
                values, 0
            )
            val pBufferSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig[0], attribList, 0)
            if (!EGL14.eglMakeCurrent(eglDisplay, pBufferSurface, pBufferSurface, myContext)) {
                throw RuntimeException()
            }
        }
    }

}