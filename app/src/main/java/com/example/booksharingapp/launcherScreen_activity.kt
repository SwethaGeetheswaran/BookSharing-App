package com.example.booksharingapp

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import kotlinx.android.synthetic.main.activity_launcher_screen_activity.*
import java.nio.channels.FileLock

//Reference:https://www.youtube.com/watch?v=afc2LFk9YrQ
class launcherScreen_activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher_screen_activity)

        launcherAnimationImage()

        app_start_button.setOnClickListener {
            startActivity(MainActivity.getLaunchIntent(this))
        }
    }

    // Add animations for image
    private fun launcherAnimationImage() {
        val animator = ValueAnimator.ofFloat(0f,1f).setDuration(3000)
        animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener{
            override fun onAnimationUpdate(p0: ValueAnimator?) {
                app_image.progress = p0?.animatedValue as Float
            }

        })

        if(app_image.progress == 0f){
            animator.repeatCount = 5
            animator.start()
        } else{
            app_image.progress = 0f
        }
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, launcherScreen_activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}
