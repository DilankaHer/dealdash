package com.example.dealdash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class ImageFullScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_full_screen)
        val imageURL = intent.getStringExtra("position")

        val close = findViewById<MaterialButton>(R.id.close)
        val imageView = findViewById<ImageView>(R.id.fullscreenImage)
        Glide.with(this).load(imageURL).into(imageView)

        close.setOnClickListener {
            finish()
        }

    }
}