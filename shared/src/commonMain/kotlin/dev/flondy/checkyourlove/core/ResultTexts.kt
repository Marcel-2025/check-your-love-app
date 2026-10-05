package dev.flondy.checkyourlove.core

import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import org.jetbrains.compose.resources.StringResource

/** Zuordnung der Ergebnisbereiche zu Sprachressourcen. */
object ResultTexts {
    fun title(range: ResultRange): StringResource = when (range) {
        ResultRange.R0 -> Res.string.result_title_0
        ResultRange.R1 -> Res.string.result_title_1
        ResultRange.R2 -> Res.string.result_title_2
        ResultRange.R3 -> Res.string.result_title_3
        ResultRange.R4 -> Res.string.result_title_4
        ResultRange.R5 -> Res.string.result_title_5
    }

    private val texts = mapOf(
        ResultRange.R0 to listOf(Res.string.result_text_0_0, Res.string.result_text_0_1, Res.string.result_text_0_2),
        ResultRange.R1 to listOf(Res.string.result_text_1_0, Res.string.result_text_1_1, Res.string.result_text_1_2),
        ResultRange.R2 to listOf(Res.string.result_text_2_0, Res.string.result_text_2_1, Res.string.result_text_2_2),
        ResultRange.R3 to listOf(Res.string.result_text_3_0, Res.string.result_text_3_1, Res.string.result_text_3_2),
        ResultRange.R4 to listOf(Res.string.result_text_4_0, Res.string.result_text_4_1, Res.string.result_text_4_2),
        ResultRange.R5 to listOf(Res.string.result_text_5_0, Res.string.result_text_5_1, Res.string.result_text_5_2),
    )

    fun text(range: ResultRange, variant: Int): StringResource {
        val list = texts.getValue(range)
        return list[variant.mod(list.size)]
    }
}
