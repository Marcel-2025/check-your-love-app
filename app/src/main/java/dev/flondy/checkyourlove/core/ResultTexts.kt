package dev.flondy.checkyourlove.core

import androidx.annotation.StringRes
import dev.flondy.checkyourlove.R

/** Zuordnung der Ergebnisbereiche zu Sprachressourcen. */
object ResultTexts {
    @StringRes
    fun title(range: ResultRange): Int = when (range) {
        ResultRange.R0 -> R.string.result_title_0
        ResultRange.R1 -> R.string.result_title_1
        ResultRange.R2 -> R.string.result_title_2
        ResultRange.R3 -> R.string.result_title_3
        ResultRange.R4 -> R.string.result_title_4
        ResultRange.R5 -> R.string.result_title_5
    }

    private val texts = mapOf(
        ResultRange.R0 to intArrayOf(R.string.result_text_0_0, R.string.result_text_0_1, R.string.result_text_0_2),
        ResultRange.R1 to intArrayOf(R.string.result_text_1_0, R.string.result_text_1_1, R.string.result_text_1_2),
        ResultRange.R2 to intArrayOf(R.string.result_text_2_0, R.string.result_text_2_1, R.string.result_text_2_2),
        ResultRange.R3 to intArrayOf(R.string.result_text_3_0, R.string.result_text_3_1, R.string.result_text_3_2),
        ResultRange.R4 to intArrayOf(R.string.result_text_4_0, R.string.result_text_4_1, R.string.result_text_4_2),
        ResultRange.R5 to intArrayOf(R.string.result_text_5_0, R.string.result_text_5_1, R.string.result_text_5_2),
    )

    @StringRes
    fun text(range: ResultRange, variant: Int): Int {
        val list = texts.getValue(range)
        return list[Math.floorMod(variant, list.size)]
    }
}
