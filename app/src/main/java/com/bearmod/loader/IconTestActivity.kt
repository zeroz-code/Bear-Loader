package com.bearmod.loader

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Test activity to verify icon implementation
 * This activity displays the app icon and provides debugging information
 */
class IconTestActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create simple layout programmatically
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        // Title
        val title = TextView(this).apply {
            text = "PUBG Mobile Icon Test"
            textSize = 24f
            setTextColor(ContextCompat.getColor(this@IconTestActivity, android.R.color.white))
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)
        
        // Icon display
        val iconView = ImageView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(200, 200)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        
        // Try to load the app icon
        try {
            val packageManager = packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val icon: Drawable = packageManager.getApplicationIcon(applicationInfo)
            iconView.setImageDrawable(icon)
            
            val info = TextView(this).apply {
                text = "✅ App icon loaded successfully\n" +
                       "Package: $packageName\n" +
                       "Icon type: ${icon.javaClass.simpleName}"
                setTextColor(ContextCompat.getColor(this@IconTestActivity, android.R.color.white))
                setPadding(0, 32, 0, 0)
            }
            layout.addView(info)
            
        } catch (e: PackageManager.NameNotFoundException) {
            val error = TextView(this).apply {
                text = "❌ Failed to load app icon: ${e.message}"
                setTextColor(ContextCompat.getColor(this@IconTestActivity, android.R.color.holo_red_light))
                setPadding(0, 32, 0, 0)
            }
            layout.addView(error)
        }
        
        layout.addView(iconView)
        
        // Additional icon tests
        val adaptiveTest = TextView(this).apply {
            val hasAdaptiveIcon = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try {
                    val drawable = ContextCompat.getDrawable(this@IconTestActivity, R.mipmap.ic_launcher)
                    drawable is android.graphics.drawable.AdaptiveIconDrawable
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }
            
            text = if (hasAdaptiveIcon) {
                "✅ Adaptive icon supported and detected"
            } else {
                "ℹ️ Using fallback icon (Android < 8.0 or adaptive icon not detected)"
            }
            setTextColor(ContextCompat.getColor(this@IconTestActivity, android.R.color.white))
            setPadding(0, 16, 0, 0)
        }
        layout.addView(adaptiveTest)
        
        // Background color
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_background))
        
        setContentView(layout)
    }
}
