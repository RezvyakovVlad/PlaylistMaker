package com.example.playlistmaker.presentation.search

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.Creator
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.presentation.player.AudioPlayerActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val searchInteractor by lazy { Creator.provideSearchTracksInteractor(this) }
    private val historyInteractor by lazy { Creator.provideManageSearchHistoryInteractor(this) }

    private var searchText: String = ""
    private var isSearchInProgress = false
    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    private val searchDebounceDelay = 2000L
    private val clickDebounceDelay = 1000L

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

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
    }

    private fun setupRecyclerView() {
        searchAdapter = TrackAdapter()
        searchAdapter.onItemClick = { track ->
            clickDebounce {
                historyInteractor.addTrack(track)
                val intent = Intent(this@SearchActivity, AudioPlayerActivity::class.java)
                intent.putExtra("TRACK", track)
                startActivity(intent)
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchAdapter
        }
    }

    private fun setupHistoryRecyclerView() {
        historyAdapter = TrackAdapter()
        historyAdapter.onItemClick = { track ->
            clickDebounce {
                val intent = Intent(this@SearchActivity, AudioPlayerActivity::class.java)
                intent.putExtra("TRACK", track)
                startActivity(intent)
            }
        }

        searchHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = historyAdapter
        }
    }

    private fun setupViews() {
        iwClear.visibility = View.GONE
        toolbar.setNavigationOnClickListener { finish() }
        searchHistoryText.text = getString(R.string.your_history)
        hideAllPlaceholders()
    }

    private fun setupClickListeners() {
        iwClear.setOnClickListener { clearSearchQuery() }

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
            clickDebounce {
                historyInteractor.clearHistory()
                showInitialState()
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
                    handler.removeCallbacksAndMessages(null)
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
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ performSearch() }, searchDebounceDelay)
    }

    private fun clickDebounce(action: () -> Unit) {
        if (isClickAllowed) {
            isClickAllowed = false
            action.invoke()
            handler.postDelayed({ isClickAllowed = true }, clickDebounceDelay)
        }
    }

    private fun showInitialState() {
        try {
            val history = historyInteractor.getHistory()
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
        historyAdapter.submitList(history)
        searchHistoryLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showEmptySearchState() {
        searchHistoryLayout.visibility = View.GONE
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun performSearch(query: String? = null) {
        val searchQuery = query ?: etSearch.text.toString().trim()
        if (searchQuery.isEmpty() || isSearchInProgress) return

        showLoading()
        isSearchInProgress = true

        ioScope.launch {
            try {
                val tracks = searchInteractor.execute(searchQuery)

                withContext(Dispatchers.Main) {
                    isSearchInProgress = false
                    searchHistoryLayout.visibility = View.GONE

                    if (tracks.isEmpty()) {
                        showNothingFound()
                    } else {
                        showSearchResults(tracks)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isSearchInProgress = false
                    showNoConnection()
                }
            }
        }
    }

    private fun showSearchResults(tracks: List<Track>) {
        searchAdapter.submitList(tracks)
        recyclerView.visibility = View.VISIBLE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showNothingFound() {
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.VISIBLE
        placeholderNoConnection.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showNoConnection() {
        recyclerView.visibility = View.GONE
        placeholderNothing.visibility = View.GONE
        placeholderNoConnection.visibility = View.VISIBLE
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
        handler.removeCallbacksAndMessages(null)
    }
}
