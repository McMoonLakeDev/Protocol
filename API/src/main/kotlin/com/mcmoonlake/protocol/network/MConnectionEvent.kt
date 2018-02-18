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

package com.mcmoonlake.protocol.network

import com.mcmoonlake.protocol.packet.Packet
import com.mcmoonlake.protocol.packet.PacketBuffer

interface ProtocolEvent {

    fun call(listener: MConnectionListener)
}

class PacketReceivingEvent(
        val connection: MConnection,
        val packet: Packet
) : ProtocolEvent {

    override fun call(listener: MConnectionListener) {
        listener.onReceiving(this)
    }
}

class PacketSendingEvent(
        val connection: MConnection,
        val packet: Packet
) : ProtocolEvent {

    override fun call(listener: MConnectionListener) {
        listener.onSending(this)
    }
}

enum class PacketDirection {

    IN, OUT
}

class PacketPayloadEvent(
        val connection: MConnection,
        val direction: PacketDirection,
        val channel: String,
        val data: PacketBuffer
) : ProtocolEvent {

    override fun call(listener: MConnectionListener) {
        listener.onPayload(this)
    }
}

class ConnectedEvent(
        val connection: MConnection
) : ProtocolEvent {

    override fun call(listener: MConnectionListener) {
        listener.onConnected(this)
    }
}

class DisconnectedEvent(
        val connection: MConnection,
        val reason: String,
        val cause: Throwable?
) : ProtocolEvent {

    override fun call(listener: MConnectionListener) {
        listener.onDisconnected(this)
    }
}
