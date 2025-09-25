package com.example.playlistmaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.google.android.material.appbar.MaterialToolbar

class SearchActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var iwClear: ImageView
    private lateinit var toolbar: MaterialToolbar

    private var searchText: String = ""

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupViews()
        setupClickListeners()
        setupTextWatcher()

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        }
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        iwClear = findViewById(R.id.iwClear)
        toolbar = findViewById(R.id.toolbar)
    }

    private fun setupViews() {
        etSearch.apply {
            hint = getString(R.string.c_search)
            maxLines = 1
            imeOptions = EditorInfo.IME_ACTION_DONE
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            isSingleLine = true
        }

        iwClear.visibility = View.GONE

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClickListeners() {
        iwClear.setOnClickListener {
            clearSearchQuery()
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun setupTextWatcher() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s?.toString() ?: ""
                updateClearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun updateClearButtonVisibility(text: CharSequence?) {
        if (text.isNullOrEmpty()) {
            iwClear.visibility = View.GONE
        } else {
            iwClear.visibility = View.VISIBLE
        }
    }

    private fun clearSearchQuery() {
        etSearch.setText("")
        searchText = ""
        hideKeyboard()
        etSearch.clearFocus()
    }

    private fun performSearch() {
        val query = etSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreState(savedInstanceState)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        val savedSearchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        if (savedSearchText.isNotEmpty()) {
            etSearch.setText(savedSearchText)
            searchText = savedSearchText
            updateClearButtonVisibility(savedSearchText)
        }
    }

    override fun onResume() {
        super.onResume()
        updateClearButtonVisibility(etSearch.text)
    }
}