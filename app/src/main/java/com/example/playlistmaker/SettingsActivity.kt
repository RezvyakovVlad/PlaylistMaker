package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var shareApp: TextView
    private lateinit var writeSupport: TextView
    private lateinit var termsOfUse: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        setupToolbar()
        setupClickListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        shareApp = findViewById(R.id.share_app)
        writeSupport = findViewById(R.id.write_support)
        termsOfUse = findViewById(R.id.terms_of_use)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClickListeners() {
        shareApp.setOnClickListener {
            shareApp()
        }

        writeSupport.setOnClickListener {
            writeToSupport()
        }

        termsOfUse.setOnClickListener {
            openTermsOfUse()
        }
    }

    private fun shareApp() {
        try {
            val shareMessage = getString(R.string.url_of_Practicum)
            val shareText = "Смотри какое крутое приложение я нашёл! Курс по Android-разработке в Практикуме: $shareMessage"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.c_share))
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            val chooserIntent = Intent.createChooser(shareIntent, getString(R.string.c_share))

            if (shareIntent.resolveActivity(packageManager) != null) {
                startActivity(chooserIntent)
            } else {
                showToast("Нет приложений для отправки сообщений")
            }
        } catch (e: Exception) {
            showToast("Ошибка при попытке поделиться")
        }
    }

    private fun writeToSupport() {
        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.myEmail)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.theme_support))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.message_support))
            }

            if (emailIntent.resolveActivity(packageManager) != null) {
                startActivity(emailIntent)
            } else {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.myEmail)))
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.theme_support))
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.message_support))
                }

                val chooserIntent = Intent.createChooser(fallbackIntent, getString(R.string.c_w_support))

                if (fallbackIntent.resolveActivity(packageManager) != null) {
                    startActivity(chooserIntent)
                } else {
                    showToast("Установите почтовое приложение")
                }
            }
        } catch (e: Exception) {
            showToast("Ошибка при открытии почтового клиента")
        }
    }

    private fun openTermsOfUse() {
        try {
            val termsUrl = getString(R.string.terms_of_use)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl))

            if (browserIntent.resolveActivity(packageManager) != null) {
                startActivity(browserIntent)
            } else {
                showToast("Установите браузер для просмотра соглашения")
            }
        } catch (e: Exception) {
            showToast("Ошибка при открытии браузера")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}