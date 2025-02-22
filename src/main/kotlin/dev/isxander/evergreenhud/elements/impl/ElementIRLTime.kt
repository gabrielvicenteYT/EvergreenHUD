/*
 * EvergreenHUD - A mod to improve your heads-up-display.
 * Copyright (c) isXander [2019 - 2022].
 *
 * This work is licensed under the GPL-3 License.
 * To view a copy of this license, visit https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package dev.isxander.evergreenhud.elements.impl

import dev.isxander.evergreenhud.elements.type.SimpleTextElement
import dev.isxander.evergreenhud.utils.elementmeta.ElementMeta
import dev.isxander.settxi.impl.boolean
import java.text.SimpleDateFormat
import java.util.*

@ElementMeta(id = "evergreenhud:irl_time", name = "IRL Time", description = "Show the current time in real life.", category = "Miscallaneous")
class ElementIRLTime : SimpleTextElement("Time") {
    var twelveHour by boolean(false) {
        name = "Twelve Hour"
        category = "Time"
        description = "If the clock should display AM or PM or go into 13:00+"
    }

    var seconds by boolean(false) {
        name = "Seconds"
        category = "Time"
        description = "Show the seconds."
    }

    override fun calculateValue(): String =
        SimpleDateFormat(String.format(if (twelveHour) "hh:mm%s a" else "HH:mm%s", if (seconds) ":ss" else ""))
            .format(Calendar.getInstance().time).uppercase()
}
