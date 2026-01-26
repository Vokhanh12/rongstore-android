package com.aliasadi.clean.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliasadi.clean.ui.base.BaseViewModel
import com.aliasadi.clean.ui.feed.FeedUiState
import com.aliasadi.clean.ui.feed.usecase.GetMoviesWithSeparators
import com.aliasadi.data.remote.http.RemoteException
import com.aliasadi.domain.model.commands.StoreOwnerMutateCommand
import com.aliasadi.domain.model.valueobjects.Location
import com.aliasadi.domain.model.valueobjects.Tile
import com.aliasadi.domain.usecase.MutateStoreOwnerUseCase
import com.aliasadi.domain.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject
@HiltViewModel
class MapViewModel @Inject constructor(
    private val mutateStoreOwnerUseCase: MutateStoreOwnerUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MapUIState())
    val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()

    fun saveStores(points: List<LatLng>) {
        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true, error = null) }

            val commands = points.map {
                StoreOwnerMutateCommand.Create(
                    location = Location(
                        lat = it.latitude,
                        lng = it.longitude
                    ),
                    tile = Tile.fromLatLng(it.latitude, it.longitude),
                    createBy = 1
                )
            }

            try {
                val results = mutateStoreOwnerUseCase(commands)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = results
                    )
                }

            } catch (e: RemoteException.Network) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = MapError.Network
                    )
                }

            } catch (e: RemoteException.NotFound) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = MapError.NotFound
                    )
                }

            } catch (e: RemoteException.Unauthorized) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = MapError.Unauthorized
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = MapError.Unknown
                    )
                }
            }
        }
    }
}
