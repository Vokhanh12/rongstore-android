package com.aliasadi.clean.ui.scanqr

import com.aliasadi.clean.ui.base.BaseViewModel
import com.aliasadi.clean.ui.feed.usecase.GetMoviesWithSeparators
import com.aliasadi.domain.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ScanQrViewModel  @Inject constructor(
    val networkMonitor: NetworkMonitor,
    getMoviesWithSeparators: GetMoviesWithSeparators,
) : BaseViewModel(){
    private val _uiState = MutableStateFlow<MapUIState>(MapUIState())
    val uiState = _uiState.asStateFlow()

}
