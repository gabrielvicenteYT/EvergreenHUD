/*
 * EvergreenHUD - A mod to improve on your heads-up-display.
 * Copyright (C) isXander [2019 - 2021]
 *
 * This program comes with ABSOLUTELY NO WARRANTY
 * This is free software, and you are welcome to redistribute it
 * under the certain conditions that can be found here
 * https://www.gnu.org/licenses/lgpl-2.1.en.html
 *
 * If you have any questions or concerns, please create
 * an issue on the github page that can be found here
 * https://github.com/isXander/EvergreenHUD
 *
 * If you have a private concern, please contact
 * isXander @ business.isxander@gmail.com
 */

package dev.isxander.evergreenhud.api.fabric11701.events

import dev.deamsy.eventbus.api.listener.EventListener
import dev.isxander.evergreenhud.EvergreenHUD
import dev.isxander.evergreenhud.api.fabric11701.impl.EntityImpl
import dev.isxander.evergreenhud.api.fabric11701.mc
import dev.isxander.evergreenhud.api.impl.UEntity
import dev.isxander.evergreenhud.event.ClientDamageEntity
import dev.isxander.evergreenhud.event.ServerDamageEntity
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket

class ServerDamageEntityEventManager {
    private var attacker: UEntity? = null
    private var targetId = -1

    init {
        PacketEvent.EVENT.register(object : PacketEvent {
            override fun invoke(packet: Packet<*>) {
                onPacket(packet)
            }
        })
    }

    fun onPacket(packet: Packet<*>) {
        if (packet is EntityStatusS2CPacket) {
            if (packet.status.toInt() != 2) return

            val target = packet.getEntity(mc.world) ?: return
            if (attacker != null && targetId == target.id) {
                EvergreenHUD.eventBus.post(ServerDamageEntity(attacker!!, EntityImpl(target)))
                attacker = null
                targetId = -1
            }
        }
    }

    @EventListener
    fun onAttack(event: ClientDamageEntity) {
        attacker = event.attacker
        targetId = event.victim.id
    }
}