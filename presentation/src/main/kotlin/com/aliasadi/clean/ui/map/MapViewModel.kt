package com.aliasadi.clean.ui.map

import androidx.lifecycle.ViewModel
import com.aliasadi.clean.ui.base.BaseViewModel
import com.aliasadi.clean.ui.feed.FeedUiState
import com.aliasadi.clean.ui.feed.usecase.GetMoviesWithSeparators
import com.aliasadi.domain.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MapViewModel  @Inject constructor(
    val networkMonitor: NetworkMonitor,
    getMoviesWithSeparators: GetMoviesWithSeparators,
) : BaseViewModel(){
    private val _uiState = MutableStateFlow<MapUIState>(MapUIState())
    val uiState = _uiState.asStateFlow()

}
