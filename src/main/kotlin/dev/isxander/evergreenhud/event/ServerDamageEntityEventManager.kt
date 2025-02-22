/*
 * EvergreenHUD - A mod to improve your heads-up-display.
 * Copyright (c) isXander [2019 - 2022].
 *
 * This work is licensed under the GPL-3 License.
 * To view a copy of this license, visit https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package dev.isxander.evergreenhud.event

import dev.isxander.evergreenhud.utils.mc
import net.minecraft.entity.Entity
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket

class ServerDamageEntityEventManager(private val eventBus: EventBus) {
    private var attacker: Entity? = null
    private var targetId = -1

    private val packetHandleListener by eventBus.register<HandlePacketEvent<*>> {
        if (it.packet is EntityStatusS2CPacket) {
            if (it.packet.status.toInt() != 2) return@register

            val target = it.packet.getEntity(mc.world) ?: return@register
            if (attacker != null && targetId == target.id) {
                eventBus.post(ServerDamageEntityEvent(attacker!!, target))
                attacker = null
                targetId = -1
            }
        }
    }

    private val clientDamageEntityListener by eventBus.register<ClientDamageEntityEvent> {
        this.attacker = it.attacker
        targetId = it.victim.id
    }
}
