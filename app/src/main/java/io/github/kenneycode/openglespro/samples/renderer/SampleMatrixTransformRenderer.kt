package io.github.kenneycode.openglespro.samples.renderer

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
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
 *      这是一个演示矩阵变换的例子，包括模型矩阵、视图矩阵和投影矩阵
 *      This sample demonstrates matrix transform, including model matrix, view matrix and project matrix
 *
 **/

class SampleMatrixTransformRenderer : GLSurfaceView.Renderer, OnParameterChangeCallback {
    
    private val vertexShaderCode =
            "#version 300 es\n" +
            "precision mediump float;\n" +
            "layout(location = 0) in vec4 a_position;\n" +
            "layout(location = 1) in vec2 a_textureCoordinate;\n" +
            "layout(location = 2) uniform mat4 u_mvp;\n" +
            "out vec2 v_textureCoordinate;\n" +
            "void main() {\n" +
            "    v_textureCoordinate = a_textureCoordinate;\n" +
            "    gl_Position = u_mvp * a_position;\n" +
            "}"

    private val fragmentShaderCode =
            "#version 300 es\n" +
            "precision mediump float;\n" +
            "precision mediump sampler2D;\n" +
            "layout(location = 0) out vec4 fragColor;\n" +
            "layout(location = 0) uniform sampler2D u_texture;\n" +
            "in vec2 v_textureCoordinate;\n" +
            "void main() {\n" +
            "    fragColor = texture(u_texture, v_textureCoordinate);\n" +
            "}"

    // GLSurfaceView的宽高
    // The width and height of GLSurfaceView
    private var glSurfaceViewWidth = 0
    private var glSurfaceViewHeight = 0

    // 三角形顶点数据
    // The vertex data of a triangle
    private val vertexData = floatArrayOf(-1f, -1f, -1f, 1f, 1f, 1f, -1f, -1f, 1f, 1f, 1f, -1f)
    private val VERTEX_COMPONENT_COUNT = 2
    private lateinit var vertexDataBuffer : FloatBuffer

    // 纹理坐标
    // The texture coordinate
    private val textureCoordinateData = floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)
    private val TEXTURE_COORDINATE_COMPONENT_COUNT = 2
    private lateinit var textureCoordinateDataBuffer : FloatBuffer

    // 要渲染的图片纹理
    // The texture of the image to be rendered
    private var imageTexture = 0

    // a_position、a_textureCoordinate和u_texture的位置，与shader中写的对应
    // The location of a_position、a_textureCoordinate and u_texture, corresponding with which in shader
    private val LOCATION_ATTRIBUTE_POSITION = 0
    private val LOCATION_ATTRIBUTE_TEXTURE_COORDINATE = 1
    private val LOCATION_UNIFORM_MVP = 2
    private val LOCATION_UNIFORM_TEXTURE = 0

    private var translateX = 0f
    private var translateY = 0f
    private var translateZ = 0f
    private var rotateX = 0f
    private var rotateY = 0f
    private var rotateZ = 0f
    private var scaleX = 1f
    private var scaleY = 1f
    private var scaleZ = 1f
    private var cameraPositionX = 0f
    private var cameraPositionY = 0f
    private var cameraPositionZ = 2f
    private var lookAtX = 0f
    private var lookAtY = 0f
    private var lookAtZ = 0f
    private var cameraUpX = 0f
    private var cameraUpY = 1f
    private var cameraUpZ = 2f

    override fun onDrawFrame(gl: GL10?) {

        // 设置清屏颜色
        // Set the color which the screen will be cleared to
        GLES30.glClearColor(0.9f, 0.9f, 0.9f, 1f)

        // 清屏
        // Clear the screen
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // 设置视口，这里设置为整个GLSurfaceView区域
        // Set the viewport to the full GLSurfaceView
        GLES30.glViewport(0, 0, glSurfaceViewWidth, glSurfaceViewHeight)

        // 设置好状态，准备渲染
        // Set the status before rendering
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_POSITION)
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_POSITION, VERTEX_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, vertexDataBuffer)
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE)
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE, TEXTURE_COORDINATE_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, textureCoordinateDataBuffer)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)

        val mvpMatrix = getIdentity()
        val translateMatrix = getIdentity()
        val rotateMatrix = getIdentity()
        val scaleMatrix = getIdentity()
        val modelMatrix = getIdentity()
        val viewMatrix = getIdentity()
        val projectMatrix = getIdentity()

        // 模型矩阵计算
        // Calculate the Model matrix
        Matrix.translateM(translateMatrix, 0, translateX, translateY, translateZ)
        Matrix.rotateM(rotateMatrix, 0, rotateX, 1f, 0f, 0f)
        Matrix.rotateM(rotateMatrix, 0, rotateY, 0f, 1f, 0f)
        Matrix.rotateM(rotateMatrix, 0, rotateZ, 0f, 0f, 1f)
        Matrix.scaleM(scaleMatrix, 0, scaleX, scaleY, scaleZ)
        Matrix.multiplyMM(modelMatrix, 0, rotateMatrix, 0, scaleMatrix, 0)
        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, translateMatrix, 0)

        // 视图矩阵计算
        // Calculate the View matrix
        Matrix.setLookAtM(viewMatrix, 0, cameraPositionX, cameraPositionY, cameraPositionZ, lookAtX, lookAtY, lookAtZ, cameraUpX, cameraUpY, cameraUpZ)

        // 投影矩阵计算
        // Calculate the Project matrix
        Matrix.frustumM(projectMatrix, 0, -1f, 1f, -1f, 1f, 1f, 100f)

        // MVP矩阵计算
        // Calculate the MVP matrix
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, mvpMatrix, 0)

        GLES30.glUniformMatrix4fv(LOCATION_UNIFORM_MVP, 1, false, mvpMatrix, 0)

        // 调用draw方法用TRIANGLES的方式执行渲染，顶点数量为3个
        // Call the draw method with GL_TRIANGLES to render 3 vertices
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexData.size / VERTEX_COMPONENT_COUNT)
        
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        // 记录GLSurfaceView的宽高
        // Record the width and height of the GLSurfaceView
        glSurfaceViewWidth = width
        glSurfaceViewHeight = height

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

        // 应用GL程序
        // Use the GL program
        GLES30.glUseProgram(programId)

        // 将三角形顶点数据放入buffer中
        // Put the triangle vertex data into the vertexDataBuffer
        vertexDataBuffer = ByteBuffer.allocateDirect(vertexData.size * java.lang.Float.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexDataBuffer.put(vertexData)
        vertexDataBuffer.position(0)

        // 启动对应位置的参数，这里直接使用LOCATION_ATTRIBUTE_POSITION，而无需像OpenGL 2.0那样需要先获取参数的location
        // Enable the parameter of the location. Here we can simply use LOCATION_ATTRIBUTE_POSITION, while in OpenGL 2.0 we have to query the location of the parameter
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_POSITION)

        // 指定a_position所使用的顶点数据
        // Specify the data of a_position
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_POSITION, VERTEX_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, vertexDataBuffer)

        // 将纹理坐标数据放入buffer中
        // Put the texture coordinates into the textureCoordinateDataBuffer
        textureCoordinateDataBuffer = ByteBuffer.allocateDirect(textureCoordinateData.size * java.lang.Float.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        textureCoordinateDataBuffer.put(textureCoordinateData)
        textureCoordinateDataBuffer.position(0)

        // 启动对应位置的参数，这里直接使用LOCATION_ATTRIBUTE_TEXTURE_COORDINATE，而无需像OpenGL 2.0那样需要先获取参数的location
        // Enable the parameter of the location. Here we can simply use LOCATION_ATTRIBUTE_TEXTURE_COORDINATE, while in OpenGL 2.0 we have to query the location of the parameter
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE)

        // 指定a_textureCoordinate所使用的顶点数据
        // Specify the data of a_textureCoordinate
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE, TEXTURE_COORDINATE_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, textureCoordinateDataBuffer)

        // 创建图片纹理
        // Create texture for image
        val textures = IntArray(1)
        GLES30.glGenTextures(textures.size, textures, 0)
        imageTexture = textures[0]

        // 将图片解码并加载到纹理中
        // Decode image and load it into texture
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
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap.width,
            bitmap.height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, b)
                bitmap.recycle()
        
        // 启动对应位置的参数，这里直接使用LOCATION_UNIFORM_TEXTURE，而无需像OpenGL 2.0那样需要先获取参数的location
        // Enable the parameter of the location. Here we can simply use LOCATION_UNIFORM_TEXTURE, while in OpenGL 2.0 we have to query the location of the parameter
        GLES30.glUniform1i(LOCATION_UNIFORM_TEXTURE, 0)

        GLES30.glEnable(GLES30.GL_DEPTH)
        
    }

    fun getIdentity(): FloatArray {
        return floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
        )
    }

    override fun onParameterChange(parameterKey: String, parameterValue: Float) {
        when (parameterKey) {
            "translateX" -> { translateX = parameterValue }
            "translateY" -> { translateY = parameterValue }
            "translateZ" -> { translateZ = parameterValue }
            "rotateX" -> { rotateX = parameterValue }
            "rotateY" -> { rotateY = parameterValue }
            "rotateZ" -> { rotateZ = parameterValue }
            "scaleX" -> { scaleX = parameterValue }
            "scaleY" -> { scaleY = parameterValue }
            "scaleZ" -> { scaleZ = parameterValue }
            "cameraPositionX" -> { cameraPositionX = parameterValue }
            "cameraPositionY" -> { cameraPositionY = parameterValue }
            "cameraPositionZ" -> { cameraPositionZ = parameterValue }
            "lookAtX" -> { lookAtX = parameterValue }
            "lookAtY" -> { lookAtY = parameterValue }
            "lookAtZ" -> { lookAtZ = parameterValue }
            "cameraUpX" -> { cameraUpX = parameterValue }
            "cameraUpY" -> { cameraUpY = parameterValue }
            "cameraUpZ" -> { cameraUpZ = parameterValue }
        }
    }
    
    override fun onParameterReset() {
        translateX = 0f
        translateY = 0f
        translateZ = 0f
        rotateX = 0f
        rotateY = 0f
        rotateZ = 0f
        scaleX = 1f
        scaleY = 1f
        scaleZ = 1f
        cameraPositionX = 0f
        cameraPositionY = 0f
        cameraPositionZ = 2f
        lookAtX = 0f
        lookAtY = 0f
        lookAtZ = 0f
        cameraUpX = 0f
        cameraUpY = 1f
        cameraUpZ = 2f
    }

}

interface OnParameterChangeCallback {
    fun onParameterChange(parameterKey: String, parameterValue: Float)
    fun onParameterReset()
}