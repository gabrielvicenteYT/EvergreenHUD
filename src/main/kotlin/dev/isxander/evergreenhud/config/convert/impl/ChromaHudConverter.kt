/*
 * EvergreenHUD - A mod to improve your heads-up-display.
 * Copyright (c) isXander [2019 - 2022].
 *
 * This work is licensed under the GPL-3 License.
 * To view a copy of this license, visit https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package dev.isxander.evergreenhud.config.convert.impl

import dev.isxander.evergreenhud.EvergreenHUD
import dev.isxander.evergreenhud.config.convert.ConfigConverter
import dev.isxander.evergreenhud.elements.Element
import dev.isxander.evergreenhud.elements.impl.ElementCoordinates
import dev.isxander.evergreenhud.elements.impl.ElementCps
import dev.isxander.evergreenhud.elements.impl.ElementIRLTime
import dev.isxander.evergreenhud.elements.impl.ElementText
import dev.isxander.evergreenhud.elements.type.TextElement
import dev.isxander.evergreenhud.utils.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import java.io.File

object ChromaHudConverter : ConfigConverter {
    override val name = "ChromaHUD"
    val file = File(mc.runDirectory, "config/ChromaHUD.cfg")

    private val ids = mapOf(
        "CORDS" to "evergreenhud:coordinates",
        "PING" to "evergreenhud:ping",
        "DIRECTION" to "evergreenhud:direction",
        "CPS" to "evergreenhud:cps",
        "FPS" to "evergreenhud:fps",
        "TEXT" to "evergreenhud:text",
        "TIME" to "evergreenhud:irl_time",
        "ARMOUR_HUD" to "evergreenhud:armour_hud"
    )

    override fun process(): String? {
        if (!file.exists() || file.isDirectory) return "Invalid ChromaHUD config."

        val config = json.decodeFromString<JsonObject>(file.readText())
        EvergreenHUD.elementManager.enabled = config.decode("enabled")!!

        for (elementJson in config.decode<List<JsonObject>>("elements")!!) {
            val position = OriginedPosition.scaledPositioning(
                x = elementJson.decode("x")!!,
                y = elementJson.decode("y")!!,
                scale = elementJson.decode("scale")!!,
            )

            var textColor = Color(elementJson.decode<Int>("color")!!)
            if (elementJson.decode<Boolean>("rgb") == true)
                textColor = Color(
                    elementJson.decode("red") ?: 255,
                    elementJson.decode("green") ?: 255,
                    elementJson.decode("blue") ?: 255,
                    255
                )

            val chroma = elementJson.decode("chroma") ?: false
            textColor = textColor.withChroma(Color.ChromaProperties(chroma))

            val shadow = elementJson.decode("shadow") ?: false
            val useBg = elementJson.decode("highlighted") ?: false

            var i = 0
            val changeY = (mc.textRenderer.fontHeight + 4) / mc.window.scaledHeight
            for (item in elementJson.decode<List<JsonObject>>("items") ?: emptyList()) {
                val id = ids[item.decode("type") ?: ""] ?: continue
                val element = EvergreenHUD.elementManager.getNewElementInstance<Element>(id) ?: continue

                element.position = position
                element.position.rawY += i * changeY

                if (element is TextElement) {
                    element.textColor = textColor
                    element.textStyle = if (shadow) TextElement.TextStyle.SHADOW else TextElement.TextStyle.NORMAL

                    if (useBg) {
                        element.paddingLeft = 1f
                        element.paddingRight = 1f
                        element.paddingTop = 1f
                        element.paddingBottom = 1f
                    } else {
                        element.backgroundColor = Color.none
                    }
                }

                when (element) {
                    is ElementCoordinates -> element.accuracy = item.decode("precision") ?: 0
                    is ElementText -> element.text = item.decode("text") ?: "Sample Text"
                    is ElementIRLTime -> element.seconds = true
                    is ElementCps -> element.button = ElementCps.MouseButton.LEFT
                }

                EvergreenHUD.elementManager.addElement(element)

                i++
            }
        }

        return null
    }

    override fun detect(): Boolean {
        return file.exists()
    }
}
