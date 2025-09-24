package com.example.playlistmaker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.net.Uri
import android.view.View
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<View>(R.id.settings).setOnClickListener {
            finish()
        }


        findViewById<View>(R.id.share_app).setOnClickListener {
            shareApp()
        }

        findViewById<View>(R.id.write_support).setOnClickListener {
            contactSupport()
        }

    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Привет, Android разработка — это круто!")
        startActivity(Intent.createChooser(shareIntent, "Поделиться приложением"))
    }

    private fun contactSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:yourEmail@ya.ru")
        }
        startActivity(emailIntent)
    }
}