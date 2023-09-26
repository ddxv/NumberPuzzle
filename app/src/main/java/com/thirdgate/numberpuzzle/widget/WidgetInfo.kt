package com.thirdgate.numberpuzzle.widget

import kotlinx.serialization.Serializable

@Serializable
data class WidgetInfo(
    val games: Int = 0,
    val wins: Int = 0,
    val rows: Int = 3,
    val columns: Int = 3,
)


