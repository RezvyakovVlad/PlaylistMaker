package com.example.playlistmaker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var searchHistoryLayout: ScrollView
    private lateinit var searchHistoryRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var clearHistoryButton: TextView
    private lateinit var searchHistoryText: TextView

    private lateinit var searchAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private val networkClient = NetworkClient()
    private val repository = TrackRepository(networkClient)
    private lateinit var searchHistory: SearchHistory

    private var searchText: String = ""
    private var isSearchInProgress = false

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
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

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            showInitialState()
        }
    }

    private fun initViews() {
        try {
            etSearch = findViewById(R.id.etSearch)
            iwClear = findViewById(R.id.iwClear)
            toolbar = findViewById(R.id.toolbar)
            recyclerView = findViewById(R.id.recycler_view)
            placeholderNothing = findViewById(R.id.placeholder_nothing)
            placeholderNoConnection = findViewById(R.id.placeholder_no_connection)
            searchHistoryLayout = findViewById(R.id.search_history_layout)
            searchHistoryRecyclerView = findViewById(R.id.search_history_recycler_view)
            progressBar = findViewById(R.id.progress_bar)
            clearHistoryButton = findViewById(R.id.clear_history_button)
            searchHistoryText = findViewById(R.id.search_history_text)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun setupRecyclerView() {
        searchAdapter = TrackAdapter()
        searchAdapter.onItemClick = { track ->
            searchHistory.addTrack(track)
            val intent = Intent(this@SearchActivity, AudioPlayerActivity::class.java)
            intent.putExtra("TRACK", track)
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchAdapter
        }
    }

    private fun setupHistoryRecyclerView() {
        historyAdapter = TrackAdapter()
        historyAdapter.onItemClick = { track ->
            val intent = Intent(this@SearchActivity, AudioPlayerActivity::class.java)
            intent.putExtra("TRACK", track)
            startActivity(intent)
        }

        searchHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = historyAdapter
        }
    }

    private fun setupViews() {
        iwClear.visibility = View.GONE

        toolbar.setNavigationOnClickListener {
            finish()
        }

        searchHistoryText.text = getString(R.string.your_history)

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
            if (searchText.isNotEmpty()) {
                performSearch(searchText)
            }
        }

        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            showInitialState()
        }
    }

    private fun setupTextWatcher() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s?.toString() ?: ""
                updateClearButtonVisibility(s)

                if (s.isNullOrEmpty()) {
                    showInitialState()
                } else if (s.length >= 2) {
                    performDebouncedSearch()
                } else {
                    hideAllPlaceholders()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performDebouncedSearch() {
        etSearch.removeCallbacks(searchRunnable)
        etSearch.postDelayed(searchRunnable, 1000)
    }

    private val searchRunnable = Runnable {
        performSearch()
    }

    private fun showInitialState() {
        try {
            val history = searchHistory.getHistory()
            if (history.isNotEmpty()) {
                showSearchHistory(history)
            } else {
                showEmptySearchState()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showEmptySearchState()
        }
    }

    private fun showSearchHistory(history: List<Track>) {
        try {
            historyAdapter.submitList(history)
            searchHistoryLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            placeholderNothing.visibility = View.GONE
            placeholderNoConnection.visibility = View.GONE
            progressBar.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showEmptySearchState() {
        try {
            searchHistoryLayout.visibility = View.GONE
            recyclerView.visibility = View.GONE
            placeholderNothing.visibility = View.GONE
            placeholderNoConnection.visibility = View.GONE
            progressBar.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

        try {
            repository.searchTracks(
                query = searchQuery,
                onSuccess = { tracks ->
                    isSearchInProgress = false
                    searchHistoryLayout.visibility = View.GONE

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
        } catch (e: Exception) {
            e.printStackTrace()
            isSearchInProgress = false
            showNoConnection()
        }
    }

    private fun showSearchResults(tracks: List<Track>) {
        try {
            searchAdapter.submitList(tracks)
            recyclerView.visibility = View.VISIBLE
            placeholderNothing.visibility = View.GONE
            placeholderNoConnection.visibility = View.GONE
            progressBar.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNothingFound() {
        try {
            recyclerView.visibility = View.GONE
            placeholderNothing.visibility = View.VISIBLE
            placeholderNoConnection.visibility = View.GONE
            progressBar.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNoConnection() {
        try {
            recyclerView.visibility = View.GONE
            placeholderNothing.visibility = View.GONE
            placeholderNoConnection.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showLoading() {
        try {
            recyclerView.visibility = View.GONE
            placeholderNothing.visibility = View.GONE
            placeholderNoConnection.visibility = View.GONE
            searchHistoryLayout.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideAllPlaceholders() {
        try {
            recyclerView.visibility = View.GONE
            placeholderNothing.visibility = View.GONE
            placeholderNoConnection.visibility = View.GONE
            searchHistoryLayout.visibility = View.GONE
            progressBar.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateClearButtonVisibility(text: CharSequence?) {
        val isVisible = !text.isNullOrEmpty()
        iwClear.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun clearSearchQuery() {
        etSearch.setText("")
        searchText = ""
        hideKeyboard()
        showInitialState()
    }

    private fun hideKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            performSearch(savedSearchText)
        } else {
            showInitialState()
        }
    }

    override fun onResume() {
        super.onResume()
        updateClearButtonVisibility(etSearch.text)
        if (etSearch.text.isNullOrEmpty()) {
            showInitialState()
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
        etSearch.removeCallbacks(searchRunnable)
    }
}