package io.github.kenneycode.openglespro

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *
 **/

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Util.context = applicationContext
        val samplesList = findViewById<RecyclerView>(R.id.list)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        samplesList.layoutManager = layoutManager
        samplesList.adapter = MyAdapter()
    }

    inner class MyAdapter : RecyclerView.Adapter<VH>() {

        private val sampleNames =
            arrayOf(resources.getString(R.string.sample_0),
                    resources.getString(R.string.sample_1),
                    resources.getString(R.string.sample_2),
                    resources.getString(R.string.sample_3),
                    resources.getString(R.string.sample_4),
                    resources.getString(R.string.sample_5),
                    resources.getString(R.string.sample_6),
                    resources.getString(R.string.sample_7),
                    resources.getString(R.string.sample_8),
                    resources.getString(R.string.sample_9))

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VH {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.layout_sample_list_item, p0, false)
            return VH(view)
        }

        override fun getItemCount(): Int {
            return sampleNames.size
        }

        override fun onBindViewHolder(p0: VH, p1: Int) {
            p0.button.text = sampleNames[p1]
            p0.button.setOnClickListener {
                val intent = Intent(this@MainActivity, SimpleActivity::class.java)
                intent.putExtra(GlobalConstants.KEY_SAMPLE_INDEX, p1)
                intent.putExtra(GlobalConstants.KEY_SAMPLE_NAME, sampleNames[p1])
                this@MainActivity.startActivity(intent)
            }
        }

    }

    inner class VH(itemView : View) : RecyclerView.ViewHolder(itemView) {
        var button : Button = itemView.findViewById(R.id.button)
    }
}
