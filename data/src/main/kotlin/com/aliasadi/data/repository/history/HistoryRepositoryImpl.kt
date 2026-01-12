package com.aliasadi.data.repository.history

import com.aliasadi.data.db.qrcode.QRCodeDAO
import com.aliasadi.domain.entities.QRCodeEntity
import com.aliasadi.domain.repository.history.HistoryRepository

class HistoryRepositoryImpl(private val qrCodeEntityDAO: QRCodeDAO) : HistoryRepository