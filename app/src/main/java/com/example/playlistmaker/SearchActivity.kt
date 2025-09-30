package com.example.playlistmaker

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
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: TrackAdapter
    private val networkClient = NetworkClient()
    private val repository = TrackRepository(networkClient)
    private var searchText: String = ""
    private var isSearchInProgress = false

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
        private const val TAG = "SearchActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupRecyclerView()
        setupViews()
        setupClickListeners()
        setupTextWatcher()
        setupClearHistoryButton()

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            showEmptyState()
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
        progressBar = findViewById(R.id.progress_bar)

    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter()
        adapter.onItemClick = { track ->
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = this@SearchActivity.adapter
            setHasFixedSize(true)
        }

        Log.d(TAG, "setupRecyclerView: RecyclerView configured")
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
            Log.d(TAG, "Toolbar navigation clicked")
            onBackPressedDispatcher.onBackPressed()
        }

        hideAllPlaceholders()

        Log.d(TAG, "setupViews: Views configured")
    }

    private fun setupClickListeners() {
        iwClear.setOnClickListener {
            Log.d(TAG, "Clear button clicked")
            clearSearchQuery()
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Log.d(TAG, "Keyboard Done button pressed")
                performSearch()
                true
            } else {
                false
            }
        }

        findViewById<View>(R.id.update).setOnClickListener {
            Log.d(TAG, "Update button clicked")
            if (repository.getLastSearchQuery().isNotEmpty()) {
                performSearch(repository.getLastSearchQuery())
            }
        }
    }

    private fun setupTextWatcher() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s?.toString() ?: ""
                updateClearButtonVisibility(s)

                if (s.isNullOrEmpty()) {
                    showEmptyState()
                } else if (s.length >= 3) {
                    performSearch()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClearHistoryButton() {
        findViewById<android.widget.Button>(R.id.clear_history_button)?.setOnClickListener {
            Log.d(TAG, "Clear history button clicked")
            showEmptyState()
        }
    }

    private fun performSearch(query: String? = null) {
        val searchQuery = query ?: etSearch.text.toString().trim()
        Log.d(TAG, "performSearch: Searching for '$searchQuery'")

        if (searchQuery.isEmpty() || isSearchInProgress) return

        hideKeyboard()
        showLoading()
        isSearchInProgress = true

        repository.searchTracks(
            query = searchQuery,
            onSuccess = { tracks ->
                isSearchInProgress = false
                Log.d(TAG, "performSearch: Found ${tracks.size} tracks for query '$searchQuery'")

                if (tracks.isEmpty()) {
                    showNothingFound()
                } else {
                    showSearchResults(tracks)
                }
            },
            onError = { error ->
                isSearchInProgress = false
                Log.e(TAG, "performSearch: Search error: $error")
                showNoConnection()
            }
        )
    }

    private fun showSearchResults(tracks: List<Track>) {
        Log.d(TAG, "showSearchResults: Displaying ${tracks.size} tracks")
        tracks.take(3).forEachIndexed { index, track ->
            Log.d(TAG, "showSearchResults: Track $index - ${track.trackName} - ${track.artistName}")
        }

        adapter.submitList(tracks)
        recyclerView.visibility = View.VISIBLE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.GONE
        searchHistoryLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showNothingFound() {
        Log.d(TAG, "showNothingFound: No tracks found")
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.VISIBLE
        placeholderNoConnection.visibility = View.GONE
        searchHistoryLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showNoConnection() {
        Log.w(TAG, "showNoConnection: Showing no connection placeholder")
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
        searchHistoryLayout.visibility = View.VISIBLE
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
        Log.d(TAG, "updateClearButtonVisibility: Clear button visible = $isVisible")
    }

    private fun clearSearchQuery() {
        etSearch.setText("")
        searchText = ""
        hideKeyboard()
        etSearch.clearFocus()
        showEmptyState()
    }

    private fun hideKeyboard() {
        Log.d(TAG, "hideKeyboard: Hiding keyboard")
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = getSystemService(android.net.ConnectivityManager::class.java)
            val isAvailable = connectivityManager.activeNetwork != null
            Log.d(TAG, "isNetworkAvailable: Network available = $isAvailable")
            isAvailable
        } catch (e: Exception) {
            Log.e(TAG, "isNetworkAvailable: Error checking network", e)
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
            performSearch()
        } else {
            showEmptyState()
        }
    }

    override fun onResume() {
        super.onResume()
        updateClearButtonVisibility(etSearch.text)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }
}