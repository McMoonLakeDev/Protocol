/*
 * Copyright (C) 2016-Present The MoonLake (mcmoonlake@hotmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mcmoonlake.protocol.packet

abstract class PacketProtocol {

    private val incoming: MutableMap<Int, Class<out Packet>> = HashMap()
    private val outgoing: MutableMap<Class<out Packet>, Int> = HashMap()

    abstract val header: PacketHeader

    abstract val encryption: PacketEncryption?

    fun clearPackets() {
        incoming.clear()
        outgoing.clear()
    }

    fun registerPacket(packetId: Int, clazz: Class<out Packet>) {
        registerIncomingPacket(packetId, clazz)
        registerOutgoingPacket(packetId, clazz)
    }

    @Throws(IllegalArgumentException::class)
    fun registerIncomingPacket(packetId: Int, clazz: Class<out Packet>) {
        incoming.put(packetId, clazz)
        try {
            createIncomingPacket(packetId)
        } catch(e: IllegalStateException) {
            incoming.remove(packetId)
            throw IllegalArgumentException(e.message, e.cause)
        }
    }

    fun registerOutgoingPacket(packetId: Int, clazz: Class<out Packet>) {
        outgoing.put(clazz, packetId)
    }

    fun createIncomingPacket(packetId: Int): Packet {
        if(packetId < 0 || !incoming.containsKey(packetId))
            throw IllegalArgumentException("Invalid packet id: $packetId.")
        val clazz = incoming[packetId]
        try {
            val constructor = clazz?.getDeclaredConstructor()
            if(constructor?.isAccessible == false)
                constructor.isAccessible = true
            return constructor?.newInstance()!!
        } catch(e: NoSuchMethodError) {
            throw IllegalStateException("Packet ${clazz?.name ?: packetId} has no parameter less constructor.")
        } catch(e: Exception) {
            throw IllegalStateException("Create packet ${clazz?.name ?: packetId} instance failed.")
        }
    }

    fun getOutgoingPacketId(clazz: Class<out Packet>): Int {
        if(!outgoing.containsKey(clazz))
            throw IllegalArgumentException("Unregistered output packet class: ${clazz.name}.")
        return outgoing[clazz] ?: -1
    }
}
