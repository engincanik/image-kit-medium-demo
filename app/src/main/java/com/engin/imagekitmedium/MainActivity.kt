package com.engin.imagekitmedium

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.engin.imagekitmedium.ui.FilterActivity
import com.engin.imagekitmedium.ui.RenderActivity

class MainActivity : AppCompatActivity() {
    lateinit var renderBtn: Button
    lateinit var visionBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        renderBtn = findViewById(R.id.renderBtn)
        renderBtn.setOnClickListener {
            var intent = Intent(this, RenderActivity::class.java)
            startActivity(intent)
        }
        visionBtn = findViewById(R.id.visionBtn)
        visionBtn.setOnClickListener {
            var intent = Intent(this, FilterActivity::class.java)
            startActivity(intent)
        }
    }
}