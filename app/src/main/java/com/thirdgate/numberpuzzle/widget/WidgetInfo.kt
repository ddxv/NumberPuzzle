package com.thirdgate.numberpuzzle.widget

import kotlinx.serialization.Serializable

@Serializable
data class WidgetInfo(
    val games: Int = 0,
    val wins: Int = 0,
)


