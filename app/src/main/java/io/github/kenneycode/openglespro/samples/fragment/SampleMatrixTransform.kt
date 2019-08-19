package io.github.kenneycode.openglespro.samples.fragment

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.kenneycode.openglespro.R
import io.github.kenneycode.openglespro.samples.renderer.OnParameterChangeCallback
import io.github.kenneycode.openglespro.samples.renderer.SampleMatrixTransformRenderer
import kotlinx.android.synthetic.main.fragment_sample_matrix_transform.view.*
import kotlinx.android.synthetic.main.item_parameter_list.view.*

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

class SampleMatrixTransform : Fragment() {

    private lateinit var glSurfaceView: GLSurfaceView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_sample_matrix_transform, container,  false)
        glSurfaceView = rootView.findViewById(R.id.glsurfaceview)
        // 设置GL版本，这里设置为3.0
        // Set GL version, here I set it to 3.0
        glSurfaceView.setEGLContextClientVersion(3)
        // 设置RGBA颜色缓冲、深度缓冲及stencil缓冲大小
        // Set the size of RGBA、depth and stencil vertexDataBuffer
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 8, 0)

        // 设置对应sample的渲染器
        // Set the corresponding sample renderer
        val renderer = SampleMatrixTransformRenderer()
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        glSurfaceView.post {
            val parameters = getParameterItems()
            val layoutManager = LinearLayoutManager(activity)
            layoutManager.orientation = RecyclerView.VERTICAL
            rootView.parameterList.layoutManager = layoutManager
            val adapter = Adapter(parameters)
            adapter.onParameterChangeCallback = renderer
            rootView.parameterList.adapter = adapter

            rootView.resetButton.setOnClickListener {
                renderer.onParameterReset()
                adapter.parameters = getParameterItems()
                adapter.notifyDataSetChanged()
                glSurfaceView.requestRender()
            }
        }
        return rootView
    }

    private fun getParameterItems(): Array<ParameterItem> {
        return arrayOf(
                ParameterItem("translateX", 0f),
                ParameterItem("translateY", 0f),
                ParameterItem("translateZ", 0f),
                ParameterItem("rotateX", 0f),
                ParameterItem("rotateY", 0f),
                ParameterItem("rotateZ", 0f),
                ParameterItem("scaleX", 1f),
                ParameterItem("scaleY", 1f),
                ParameterItem("scaleZ", 1f),
                ParameterItem("cameraPositionX", 0f),
                ParameterItem("cameraPositionY", 0f),
                ParameterItem("cameraPositionZ", 5f),
                ParameterItem("lookAtX", 0f),
                ParameterItem("lookAtY", 0f),
                ParameterItem("lookAtZ", 0f),
                ParameterItem("cameraUpX", 0f),
                ParameterItem("cameraUpY", 1f),
                ParameterItem("cameraUpZ", 0f),
                ParameterItem("nearPlaneLeft", -1f),
                ParameterItem("nearPlaneRight", 1f),
                ParameterItem("nearPlaneBottom", - glSurfaceView.height.toFloat() / glSurfaceView.width),
                ParameterItem("nearPlaneTop", glSurfaceView.height.toFloat() / glSurfaceView.width),
                ParameterItem("nearPlane", 2f),
                ParameterItem("farPlane", 100f)
        )
    }

    inner class Adapter(var parameters: Array<ParameterItem>) : RecyclerView.Adapter<VH>() {

        lateinit var onParameterChangeCallback: OnParameterChangeCallback

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VH {
            return VH(LayoutInflater.from(p0.context).inflate(R.layout.item_parameter_list, null, false))
        }

        override fun getItemCount(): Int {
            return parameters.size
        }

        override fun onBindViewHolder(vh: VH, index: Int) {
            vh.parameterKey.text = parameters[index].key
            vh.parameterValue.text = String.format("%.2f", parameters[index].value)
            vh.reduceButton.setOnClickListener {
                val oldValue = vh.parameterValue.text.toString().toFloat()
                val newValue = oldValue - if (parameters[index].key.startsWith("scale")) { 0.1f } else { 1f }
                vh.parameterValue.text = String.format("%.2f", newValue)
                onParameterChangeCallback.onParameterChange(parameters[index].key, newValue)
                glSurfaceView.requestRender()
            }
            vh.addButton.setOnClickListener {
                val oldValue = vh.parameterValue.text.toString().toFloat()
                val newValue = oldValue + if (parameters[index].key.startsWith("scale")) { 0.1f } else { 1f }
                vh.parameterValue.text = String.format("%.2f", newValue)
                onParameterChangeCallback.onParameterChange(parameters[index].key, newValue)
                glSurfaceView.requestRender()
            }
        }

    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parameterKey = itemView.parameterKey
        val parameterValue = itemView.parameterValue
        val reduceButton = itemView.reduceButton
        val addButton = itemView.addButton
    }

    inner class ParameterItem(val key: String, val value: Float)
}