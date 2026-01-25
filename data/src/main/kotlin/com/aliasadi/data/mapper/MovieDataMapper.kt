package com.aliasadi.data.mapper

import com.aliasadi.data.entities.MovieDbData
import com.aliasadi.domain.model.entities.MovieEntity

/**
 * Created by Ali Asadi on 13/05/2020
 **/

fun MovieEntity.toDbData() = MovieDbData(
    id = id,
    image = image,
    description = description,
    title = title,
    category = category,
    backgroundUrl = backgroundUrl
)
