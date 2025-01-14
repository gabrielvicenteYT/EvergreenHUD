/*
 * EvergreenHUD - A mod to improve your heads-up-display.
 * Copyright (c) isXander [2019 - 2022].
 *
 * This work is licensed under the GPL-3 License.
 * To view a copy of this license, visit https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package dev.isxander.evergreenhud.elements.impl

import dev.isxander.evergreenhud.elements.type.SimpleTextElement
import dev.isxander.evergreenhud.utils.decimalFormat
import dev.isxander.evergreenhud.utils.elementmeta.ElementMeta
import dev.isxander.evergreenhud.utils.mc
import dev.isxander.settxi.impl.boolean
import dev.isxander.settxi.impl.int
import net.minecraft.util.math.MathHelper
import java.text.DecimalFormat

@ElementMeta(id = "evergreenhud:yaw", name = "Yaw", description = "Displays the player's yaw.", category = "Player")
class ElementYaw : SimpleTextElement("Yaw", 0) {
    var accuracy by int(2) {
        range = 0..8
        name = "Accuracy"
        category = "Speed"
        description = "How many decimal places to show the speed to."
    }

    var trailingZeros by boolean(true) {
        name = "Trailing Zeros"
        category = "Speed"
        description = "Show zeros to match the accuracy."
    }

    override fun calculateValue(): String {
        return decimalFormat(accuracy, trailingZeros).format(mc.player?.yaw?.let(MathHelper::wrapDegrees) ?: 0f)
    }
}
