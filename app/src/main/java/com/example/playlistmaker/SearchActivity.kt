package com.example.playlistmaker

import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class SearchActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var iwClear: ImageView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderNothing: LinearLayout
    private lateinit var placeholderNoConnection: LinearLayout
    private lateinit var searchHistoryLayout: View
    private lateinit var searchHistoryRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var searchAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private val networkClient = NetworkClient()
    private val repository = TrackRepository(networkClient)
    private lateinit var searchHistory: SearchHistory

    private var searchText: String = ""
    private var isSearchInProgress = false

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
        private const val TAG = "SearchActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val sharedPreferences = getSharedPreferences("playlist_maker", MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPreferences)

        initViews()
        setupRecyclerView()
        setupHistoryRecyclerView()
        setupViews()
        setupClickListeners()
        setupTextWatcher()
        setupFocusListener()

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            updateHistoryVisibility()
        }
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        iwClear = findViewById(R.id.iwClear)
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recycler_view)
        placeholderNothing = findViewById(R.id.placeholder_nothing)
        placeholderNoConnection = findViewById(R.id.placeholder_no_connection)
        searchHistoryLayout = findViewById(R.id.search_history_layout)
        searchHistoryRecyclerView = findViewById(R.id.search_history_recycler_view)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupRecyclerView() {
        searchAdapter = TrackAdapter()
        searchAdapter.onItemClick = { track ->
            searchHistory.addTrack(track)

        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupHistoryRecyclerView() {
        historyAdapter = TrackAdapter()
        historyAdapter.onItemClick = { track ->
            searchHistory.addTrack(track)
        }

        searchHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = historyAdapter
            setHasFixedSize(true)
        }

        loadSearchHistory()
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

        hideAllPlaceholders()
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

        findViewById<View>(R.id.update).setOnClickListener {
            if (repository.getLastSearchQuery().isNotEmpty()) {
                performSearch(repository.getLastSearchQuery())
            }
        }

        findViewById<View>(R.id.clear_history_button).setOnClickListener {
            searchHistory.clearHistory()
            loadSearchHistory()
            updateHistoryVisibility()
        }
    }

    private fun setupTextWatcher() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s?.toString() ?: ""
                updateClearButtonVisibility(s)

                if (s.isNullOrEmpty()) {
                    updateHistoryVisibility()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFocusListener() {
        etSearch.setOnFocusChangeListener { _, hasFocus ->
            updateHistoryVisibility()
        }
    }

    private fun updateHistoryVisibility() {
        val hasFocus = etSearch.hasFocus()
        val isEmpty = etSearch.text.isNullOrEmpty()
        val hasHistory = searchHistory.hasHistory()

        if (hasFocus && isEmpty && hasHistory) {
            showHistory()
        } else {
            hideHistory()
        }
    }

    private fun loadSearchHistory() {
        val history = searchHistory.getHistory()
        historyAdapter.submitList(history)
    }

    private fun showHistory() {
        searchHistoryLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun hideHistory() {
        searchHistoryLayout.visibility = View.GONE
    }

    private fun performSearch(query: String? = null) {
        val searchQuery = query ?: etSearch.text.toString().trim()
        if (searchQuery.isEmpty() || isSearchInProgress) return

        showLoading()
        isSearchInProgress = true

        if (!isNetworkAvailable()) {
            isSearchInProgress = false
            showNoConnection()
            return
        }

        repository.searchTracks(
            query = searchQuery,
            onSuccess = { tracks ->
                isSearchInProgress = false
                if (tracks.isEmpty()) {
                    showNothingFound()
                } else {
                    showSearchResults(tracks)
                }
            },
            onError = { error ->
                isSearchInProgress = false
                showNoConnection()
            }
        )
    }

    private fun showSearchResults(tracks: List<Track>) {
        searchAdapter.submitList(tracks)
        recyclerView.visibility = View.VISIBLE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.GONE
        searchHistoryLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showNothingFound() {
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.VISIBLE
        placeholderNoConnection.visibility = View.GONE
        searchHistoryLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showNoConnection() {
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.VISIBLE
        searchHistoryLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showLoading() {
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.GONE
        searchHistoryLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.GONE
        searchHistoryLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun hideAllPlaceholders() {
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.GONE
        searchHistoryLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun updateClearButtonVisibility(text: CharSequence?) {
        val isVisible = !text.isNullOrEmpty()
        iwClear.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun clearSearchQuery() {
        etSearch.setText("")
        searchText = ""
        hideKeyboard()
        etSearch.clearFocus()
        updateHistoryVisibility()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = getSystemService(android.net.ConnectivityManager::class.java)
            connectivityManager.activeNetwork != null
        } catch (e: Exception) {
            false
        }
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
        } else {
            updateHistoryVisibility()
        }
    }

    override fun onResume() {
        super.onResume()
        updateClearButtonVisibility(etSearch.text)
        loadSearchHistory()
        updateHistoryVisibility()
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }
}