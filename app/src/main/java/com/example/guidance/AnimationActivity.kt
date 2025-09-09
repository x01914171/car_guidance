package com.example.guidance

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import com.airbnb.lottie.LottieAnimationView

class AnimationActivity : ComponentActivity() {
    lateinit var animationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_animation)
        
        animationView = findViewById(R.id.animation_view)
        
        // 播放动画
        animationView.setAnimation("animation.json")
        animationView.playAnimation()
        
        // 3秒后跳转到登录页面
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}