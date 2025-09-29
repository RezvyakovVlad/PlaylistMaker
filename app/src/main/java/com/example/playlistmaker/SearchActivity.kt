package com.example.playlistmaker

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
    private val repository = TrackRepository()

    private var searchText: String = ""

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
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
            loadMockTracks()
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
            println("Track clicked: ${track.trackName} by ${track.artistName}")
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = this@SearchActivity.adapter
            setHasFixedSize(true)
        }
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
            loadMockTracks()
        }
    }

    private fun setupTextWatcher() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s?.toString() ?: ""
                updateClearButtonVisibility(s)

                if (s.isNullOrEmpty()) {
                    loadMockTracks()
                } else {
                    performSearch()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun setupClearHistoryButton() {
        findViewById<android.widget.Button>(R.id.clear_history_button)?.setOnClickListener {
            loadMockTracks()
        }
    }

    private fun loadMockTracks() {
        showLoading()

        etSearch.postDelayed({
            if (!isNetworkAvailable()) {
                showNoConnection()
                return@postDelayed
            }

            val tracks = repository.getMockTracks()
            if (tracks.isEmpty()) {
                showNothingFound()
            } else {
                showSearchResults(tracks)
            }
        }, 500)
    }

    private fun performSearch() {
        val query = etSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            hideKeyboard()
            showLoading()

            etSearch.postDelayed({
                if (!isNetworkAvailable()) {
                    showNoConnection()
                    return@postDelayed
                }

                val filteredTracks = repository.searchTracks(query)
                if (filteredTracks.isEmpty()) {
                    showNothingFound()
                } else {
                    showSearchResults(filteredTracks)
                }
            }, 500)
        } else {
            loadMockTracks()
        }
    }

    private fun showSearchResults(tracks: List<Track>) {
        println("Showing ${tracks.size} tracks in search results")
        tracks.take(3).forEachIndexed { index, track ->
            println("Track $index: ${track.trackName} - ${track.artistName}")
        }

        adapter.submitList(tracks)
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

    private fun hideAllPlaceholders() {
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.GONE
        searchHistoryLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
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
        loadMockTracks()
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
            performSearch()
        } else {
            loadMockTracks()
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