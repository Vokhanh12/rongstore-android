package com.aliasadi.data.repository.user

import com.aliasadi.domain.entities.UserSettingData
import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    val userSettingData: Flow<UserSettingData>

    suspend fun updateSoundSetting(isEnableSound: Boolean)

    suspend fun isEnableSound(): Boolean

    suspend fun updateVibrateSetting(isEnableVibrate: Boolean)

    suspend fun isEnableVibrate(): Boolean

    suspend fun updatePremiumSetting(isPremium: Boolean)
    suspend fun isPremium(): Boolean
    suspend fun updateKeepScanningSetting(isKeepScanning: Boolean)
    suspend fun isKeepScanning(): Boolean
}