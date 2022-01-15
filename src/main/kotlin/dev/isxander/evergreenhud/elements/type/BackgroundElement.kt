/*
 * EvergreenHUD - A mod to improve your heads-up-display.
 * Copyright (c) isXander [2019 - 2022].
 *
 * This work is licensed under the GPL-3 License.
 * To view a copy of this license, visit https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package dev.isxander.evergreenhud.elements.type

import dev.isxander.evergreenhud.elements.Element
import dev.isxander.evergreenhud.elements.RenderOrigin
import dev.isxander.evergreenhud.utils.HitBox2D
import dev.isxander.evergreenhud.utils.drawBorderLines
import dev.isxander.evergreenhud.utils.fill
import dev.isxander.settxi.impl.*
import net.minecraft.client.util.math.MatrixStack
import java.awt.Color

abstract class BackgroundElement : Element() {
    var backgroundColor: Color by color(Color(0, 0, 0, 100)) {
        name = "Color"
        category = "Background"
        description = "The color of the background."
    }

    var outlineEnabled by boolean(false) {
        name = "Enabled"
        category = "Outline"
        description = "If the background is rendered."

        set { enabled ->
            val new = if (enabled) Color(outlineColor.red, outlineColor.green, outlineColor.blue, 255)
            else Color(outlineColor.red, outlineColor.green, outlineColor.blue, 0)
            if (outlineColor != new) outlineColor = new

            return@set enabled
        }
    }

    var outlineColor: Color by color(Color(0, 0, 0, 0)) {
        name = "Color"
        category = "Outline"
        description = "The color of the outline."

        set {
            val enabled = it.alpha != 0
            if (outlineEnabled != enabled) outlineEnabled = enabled
            return@set it
        }
    }

    var outlineThickness by float(1f) {
        name = "Thickness"
        category = "Outline"
        description = "How thick the outline is."
        range = 0.5f..8f
    }

    var paddingLeft by float(4f) {
        name = "Padding (Left)"
        category = "Background"
        description = "How far the background extends to the left."
        range = 0f..12f
    }

    var paddingRight by float(4f) {
        name = "Padding (Right)"
        category = "Background"
        description = "How far the background extends to the right."
        range = 0f..12f
    }

    var paddingTop by float(4f) {
        name = "Padding (Top)"
        category = "Background"
        description = "How far the background extends to the top."
        range = 0f..12f
    }

    var paddingBottom by float(4f) {
        name = "Padding (Bottom)"
        category = "Background"
        description = "How far the background extends to the bottom."
        range = 0f..12f
    }

    var minWidth by float(0f) {
        name = "Minimum Width"
        category = "Background"
        description = "The minimum width of the background."
        range = 0f..20f
    }

    var minHeight by float(0f) {
        name = "Minimum Height"
        category = "Background"
        description = "The minimum height of the background."
        range = 0f..20f
    }

    var cornerRadius by float(0f) {
        name = "Corner Radius"
        category = "Background"
        description = "How rounded the edges of the background are."
        range = 0f..6f
    }

    override fun render(matrices: MatrixStack, renderOrigin: RenderOrigin) {
        val bgCol = backgroundColor
        val outlineCol = outlineColor

        val scale = position.scale
        val hitbox = calculateHitBox(1f, scale)

        if (backgroundColor.alpha > 0) {
            matrices.fill(hitbox.x1, hitbox.y1, hitbox.x1 + hitbox.width, hitbox.y1 + hitbox.height, bgCol.rgb)
        }
        if (outlineEnabled) {
            matrices.drawBorderLines(hitbox.x1, hitbox.y1, hitbox.x1 + hitbox.width, hitbox.height, outlineThickness, outlineCol.rgb)
        }
    }

    override fun calculateHitBox(glScale: Float, drawScale: Float): HitBox2D {
        val width = hitboxWidth.coerceAtLeast(minWidth) * drawScale
        val height = hitboxHeight.coerceAtLeast(minHeight) * drawScale

        val top = paddingTop * drawScale
        val bottom = paddingBottom * drawScale
        val left = paddingLeft * drawScale
        val right = paddingRight * drawScale

        val x = position.rawX / glScale
        val y = position.rawY / glScale

        return HitBox2D(x - left, y - top, width + left + right, height + top + bottom)
    }
}
