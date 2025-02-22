/*
 * EvergreenHUD - A mod to improve your heads-up-display.
 * Copyright (c) isXander [2019 - 2022].
 *
 * This work is licensed under the GPL-3 License.
 * To view a copy of this license, visit https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package dev.isxander.evergreenhud.utils

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*
import io.ktor.serialization.kotlinx.json.*
import net.minecraft.client.MinecraftClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("EvergreenHUD")

val mc: MinecraftClient
    get() = MinecraftClient.getInstance()

val tickDelta: Float
    get() = mc.tickDelta

val http = HttpClient(Apache) {
    install(ContentNegotiation) {
        json(json)
    }
}
