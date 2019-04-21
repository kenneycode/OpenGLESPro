package com.kenneycode

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.kenneycode.samples.fragment.*

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *      Sample展示类
 *      This sample activity
 *
 **/

class SimpleActivity : AppCompatActivity() {

    private val samples =
        arrayOf(
            SampleShader(),
            SampleTextureArray(),
            SampleBinaryProgram(),
            SampleFenceSync(),
            SampleMultiRenderTarget()
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        title = intent.getStringExtra(GlobalConstants.KEY_SAMPLE_NAME)
        val sampleIndex = intent.getIntExtra(GlobalConstants.KEY_SAMPLE_INDEX, -1)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content, samples[sampleIndex])
        transaction.commit()
    }

}
