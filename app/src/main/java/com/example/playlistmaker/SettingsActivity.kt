package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.appbar.MaterialToolbar
import android.content.ActivityNotFoundException

class SettingsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var themeSwitcher: SwitchCompat
    private lateinit var shareApp: TextView
    private lateinit var writeSupport: TextView
    private lateinit var termsOfUse: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        setupToolbar()
        setupThemeSwitcher()
        setupClickListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        themeSwitcher = findViewById(R.id.theme_switcher)
        shareApp = findViewById(R.id.share_app)
        writeSupport = findViewById(R.id.write_support)
        termsOfUse = findViewById(R.id.terms_of_use)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupThemeSwitcher() {
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        val isDarkTheme = when (currentNightMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                val isSystemDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                isSystemDark
            }
        }

        themeSwitcher.isChecked = isDarkTheme

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            toggleTheme(isChecked)
        }
    }

    private fun toggleTheme(isDarkTheme: Boolean) {
        val newMode = if (isDarkTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

        AppCompatDelegate.setDefaultNightMode(newMode)

        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        sharedPreferences.edit().putInt("NightMode", newMode).apply()
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
            val practicumUrl = getString(R.string.url_of_Practicum)
            val messagePrefix = getString(R.string.messege_practicum)
            val shareText = "$messagePrefix $practicumUrl"

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

            if (termsUrl.isBlank()) {
                showToast("Ссылка на соглашение не настроена")
                return
            }

            val uri = Uri.parse(termsUrl)
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)

            try {
                startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                val chooserIntent = Intent.createChooser(browserIntent, "Выберите браузер")
                try {
                    startActivity(chooserIntent)
                } catch (e: ActivityNotFoundException) {
                    showToast("Установите браузер для просмотра соглашения")
                }
            }
        } catch (e: Exception) {
            showToast("Ошибка при открытии соглашения")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}