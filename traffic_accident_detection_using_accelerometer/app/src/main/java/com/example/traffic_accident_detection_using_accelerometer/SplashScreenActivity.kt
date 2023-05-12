package com.example.traffic_accident_detection_using_accelerometer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        val bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)
        val middleAnimation = AnimationUtils.loadAnimation(this, R.anim.middle_animation)
        val first = findViewById<View>(R.id.green)
        val second = findViewById<View>(R.id.blue)
        val third = findViewById<View>(R.id.greay)
        val fourth = findViewById<View>(R.id.red)
        val fifth = findViewById<View>(R.id.blue1)
        val sixth = findViewById<View>(R.id.green1)
        val seventh = findViewById<View>(R.id.greay1)
        val title = findViewById<ImageView>(R.id.title)
        val slogen = findViewById<TextView>(R.id.name)
        first.animation = topAnimation
        second.animation = topAnimation
        third.animation = topAnimation
        fourth.animation = topAnimation
        fifth.animation = topAnimation
        sixth.animation = topAnimation
        seventh.animation = topAnimation
        title.animation = middleAnimation
        slogen.animation = bottomAnimation


        Handler().postDelayed({
            val startAct = Intent(this, MainActivity::class.java)
            startActivity(startAct)
            finish()
        }, 2000)

    }
}