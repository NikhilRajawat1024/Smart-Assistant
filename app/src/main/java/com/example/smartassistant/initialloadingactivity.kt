package com.example.smartassistant

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

class initialloadingactivity : AppCompatActivity() {
    lateinit var gifloader: GifImageView
    private var isMainActivityStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initialloadingactivity)

        gifloader = findViewById(R.id.gifloader)
        gifloader.setImageResource(R.drawable.loadingio)

        val gifDrawable = gifloader.drawable as GifDrawable

        gifDrawable.addAnimationListener {
            if (!isMainActivityStarted) {
                isMainActivityStarted = true

                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this@initialloadingactivity, startactivity()::class.java)
                    startActivity(intent)
                    finish()
                }, 2000)
            }
        }
    }
}
