package com.example.areyuhere

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
// This is the loading time of the splash screen
private const val SPLASH_TIME_OUT:Long = 2000 // 1 sec
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // This delays the main activity starting until the timer expires
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))

            // close this activity (splashscreen)
            finish()
        }, SPLASH_TIME_OUT)
    }

}