/*
 * EvergreenHUD - A mod to improve your heads-up-display.
 * Copyright (c) isXander [2019 - 2022].
 *
 * This work is licensed under the GPL-3 License.
 * To view a copy of this license, visit https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package dev.isxander.evergreenhud.utils

import java.text.DecimalFormat

private val decimalFormat: (Int, Boolean, Boolean) -> DecimalFormat = { places: Int, trailingZeroes: Boolean, percentage: Boolean ->
    var pattern = "0"
    if (places > 0)
        pattern += "." + (if (trailingZeroes) "0" else "#").repeat(places)
    if (percentage)
        pattern += "%"

    DecimalFormat(pattern)
}.memoize()

fun decimalFormat(places: Int, trailingZeroes: Boolean, percentage: Boolean = false): DecimalFormat {
    return decimalFormat.invoke(places, trailingZeroes, percentage)
}
