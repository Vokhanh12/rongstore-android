package com.aliasadi.domain.usecase

import com.aliasadi.domain.model.entities.QRCodeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetQRCodeByRowIdFlowUseCase @Inject constructor(
) {
    operator fun invoke(input: Int): Flow<QRCodeEntity?> = flow {}
}
