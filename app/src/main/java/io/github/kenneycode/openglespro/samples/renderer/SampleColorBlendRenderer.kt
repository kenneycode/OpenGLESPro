package io.github.kenneycode.openglespro.samples.renderer

import android.opengl.GLES30
import android.opengl.GLSurfaceView
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
 *      这是一个使用颜色混合的例子
 *      This sample demonstrates color blend
 *
 **/

class SampleColorBlendRenderer : GLSurfaceView.Renderer {

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

    private val fragmentShaderCode =
                "#version 300 es\n" +
                "precision mediump float;\n" +
                "layout(location = 0) out vec4 fragColor;\n" +
                "in vec2 v_textureCoordinate;\n" +
                "uniform sampler2D u_texture;\n" +
                "void main() {\n" +
                "    fragColor = texture(u_texture, v_textureCoordinate);\n" +
                "}"

    // GLSurfaceView的宽高
    // The width and height of GLSurfaceView
    private var glSurfaceViewWidth = 0
    private var glSurfaceViewHeight = 0

    // 纹理顶点数据
    // The vertex data of the texture
    private val vertexData0 = floatArrayOf(-1f, -1f, -1f, 1f, 1f, 1f, -1f, -1f, 1f, 1f, 1f, -1f)
    private val vertexData1 = floatArrayOf(-0.5f, -0.3f, -0.5f, 0.35f, 0.5f, 0.35f, -0.5f, -0.3f, 0.5f, 0.35f, 0.5f, -0.3f)
    private lateinit var vertexDataBuffer0 : FloatBuffer
    private lateinit var vertexDataBuffer1 : FloatBuffer
    private val VERTEX_COMPONENT_COUNT = 2

    // 纹理坐标
    // The texture coordinate
    private val textureCoordinateData = floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)
    private lateinit var textureCoordinateDataBuffer : FloatBuffer
    private val TEXTURE_COORDINATE_COMPONENT_COUNT = 2

    private var programId = 0

    // 图片texture
    // image texture
    private var imageTexture0 = 0
    private var imageTexture1 = 0


    override fun onDrawFrame(gl: GL10?) {

        // 渲染底图
        // Render the background image
        bindGLProgram(programId, imageTexture0, vertexDataBuffer0)
        render()

        // 渲染带透明部分的图
        // Render the image with transparent parts
        bindGLProgram(programId, imageTexture1, vertexDataBuffer1)
        render()

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        // 记录GLSurfaceView的宽高
        // Record the width and height of the GLSurfaceView
        glSurfaceViewWidth = width
        glSurfaceViewHeight = height

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        initData()

        programId = createGLProgram(vertexShaderCode, fragmentShaderCode)

    }

    private fun initData() {

        // 将三角形顶点数据放入buffer中
        // Put the triangle vertex data into the vertexDataBuffer
        vertexDataBuffer0 = ByteBuffer.allocateDirect(vertexData0.size * java.lang.Float.SIZE / 8)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        vertexDataBuffer0.put(vertexData0)
        vertexDataBuffer0.position(0)
        vertexDataBuffer1 = ByteBuffer.allocateDirect(vertexData1.size * java.lang.Float.SIZE / 8)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        vertexDataBuffer1.put(vertexData1)
        vertexDataBuffer1.position(0)

        // 将纹理坐标数据放入buffer中
        // Put the texture coordinates into the textureCoordinateDataBuffer
        textureCoordinateDataBuffer = ByteBuffer.allocateDirect(textureCoordinateData.size * java.lang.Float.SIZE / 8)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        textureCoordinateDataBuffer.put(textureCoordinateData)
        textureCoordinateDataBuffer.position(0)

        // 创建图片纹理
        // Create texture
        val textures = IntArray(2)
        GLES30.glGenTextures(textures.size, textures, 0)
        imageTexture0 = textures[0]
        imageTexture1 = textures[1]

        loadImageIntoTexture("image_2.jpg", imageTexture0)
        loadImageIntoTexture("image_3.png", imageTexture1)

        Util.checkGLError()

    }

    private fun createGLProgram(vertexShaderCode : String, fragmentShaderCode : String) : Int {

        // 创建GL程序
        // Create the GL program
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

        Util.checkGLError()

        return programId

    }

    private fun bindGLProgram(programId : Int, texture : Int, vertexDataBuffer : FloatBuffer) {

        // 应用GL程序
        // Use the GL program
        GLES30.glUseProgram(programId)

        // 获取字段a_position在shader中的位置
        // Get the location of a_position in the shader
        val aPositionLocation = GLES30.glGetAttribLocation(programId, "a_position")

        // 启动对应位置的参数
        // Enable the parameter of the location
        GLES30.glEnableVertexAttribArray(aPositionLocation)

        // 指定a_position所使用的顶点数据
        // Specify the data of a_position
        GLES30.glVertexAttribPointer(aPositionLocation, VERTEX_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, vertexDataBuffer)

        // 获取字段a_textureCoordinate在shader中的位置
        // Get the location of a_textureCoordinate in the shader
        val aTextureCoordinateLocation = GLES30.glGetAttribLocation(programId, "a_textureCoordinate")

        // 启动对应位置的参数
        // Enable the parameter of the location
        GLES30.glEnableVertexAttribArray(aTextureCoordinateLocation)

        // 指定a_textureCoordinate所使用的顶点数据
        // Specify the data of a_textureCoordinate
        GLES30.glVertexAttribPointer(aTextureCoordinateLocation, TEXTURE_COORDINATE_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, textureCoordinateDataBuffer)

        // 绑定纹理并设置u_texture参数
        // Bind the texture and set the u_texture parameter
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        val uTextureLocation = GLES30.glGetUniformLocation(programId, "u_texture")
        GLES30.glUniform1i(uTextureLocation, 0)

    }

    private fun loadImageIntoTexture(imageFileName: String, texture: Int) {

        // 设置纹理参数
        // Set texture parameters
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

        // 解码图片并加载到纹理中
        // Decode the image and load it into texture
        val bitmap = Util.decodeBitmapFromAssets(imageFileName)
        val b = ByteBuffer.allocate(bitmap.width * bitmap.height * 4)
        bitmap.copyPixelsToBuffer(b)
        b.position(0)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap.width, bitmap.height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, b)

    }

    private fun render() {

        // 设置视口，这里设置为整个GLSurfaceView区域
        // Set the viewport to the full GLSurfaceView
        GLES30.glViewport(0, 0, glSurfaceViewWidth, glSurfaceViewHeight)

        // 启用颜色混合
        // Enable color blend
        GLES30.glEnable(GLES30.GL_BLEND)

        // 设置混合方式
        // Set blend functions
        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE)

        // 调用draw方法用TRIANGLES的方式执行渲染，顶点数量为6个
        // Call the draw method with GL_TRIANGLES to render 6 vertices
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)

    }

}