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

package com.mcmoonlake.protocol.client.network

import com.mcmoonlake.protocol.api.Minecraft
import com.mcmoonlake.protocol.auth.GameProfile
import com.mcmoonlake.protocol.network.*
import com.mcmoonlake.protocol.packet.handshake.CPacketHandshake
import com.mcmoonlake.protocol.packet.handshake.HandshakeIntent
import com.mcmoonlake.protocol.packet.login.CPacketLoginStart
import com.mcmoonlake.protocol.packet.login.SPacketSetCompression
import com.mcmoonlake.protocol.packet.play.*
import com.mcmoonlake.protocol.packet.status.CPacketStatusPing
import com.mcmoonlake.protocol.packet.status.CPacketStatusStart
import com.mcmoonlake.protocol.packet.status.SPacketStatusPong
import com.mcmoonlake.protocol.packet.status.SPacketStatusResponse

interface MClientConnectionListener : MConnectionListener {

    fun onConnectedServer(event: ConnectedServerEvent)

    fun onServerPingEvent(event: ServerPingEvent)
}

open class MClientConnectionListenerAdapter : MConnectionListenerAdapter(), MClientConnectionListener {

    override fun onConnectedServer(event: ConnectedServerEvent) { }

    override fun onServerPingEvent(event: ServerPingEvent) { }
}

class MClientConnectionListenerDefault : MClientConnectionListenerAdapter() {

    override fun onReceiving(event: PacketReceivingEvent) {
        val connection = event.connection as MClientConnection
        val protocol = connection.protocol
        val packet = event.packet

        if(packet is SPacketPayload) {
            val payloadEvent = PacketPayloadEvent(connection, PacketDirection.IN, packet.channel, packet.data)
            connection.callEvent(payloadEvent)
        }
        when(protocol.type) {
            MProtocolType.STATUS -> when(packet) {
                is SPacketStatusResponse -> {
                    connection.callEvent(ServerPingEvent(connection, packet.info))
                    connection.sendPacket(CPacketStatusPing(System.currentTimeMillis()))
                }
                is SPacketStatusPong -> connection.disconnect("Status session completed.")
            }
            MProtocolType.LOGIN -> when(packet) {
                // TODO Encryption
                is SPacketLoginSuccess -> {
                    connection.setProperty(Minecraft.KEY_PROFILE, packet.profile)
                    protocol.setProtocolType(MProtocolType.PLAY, connection, true)
                    connection.callEvent(ConnectedServerEvent(connection))
                }
                is SPacketKickDisconnect -> connection.disconnect(packet.message)
                is SPacketSetCompression -> connection.compressionThreshold = packet.threshold
            }
            MProtocolType.PLAY -> when(packet) {
                is SPacketKeepAlive -> connection.sendPacket(CPacketKeepAlive(packet.id))
                is SPacketKickDisconnect -> connection.disconnect(packet.message)
                is SPacketSetCompression -> connection.compressionThreshold = packet.threshold
            }
            else -> { }
        }
    }

    override fun onSending(event: PacketSendingEvent) {
        val connection = event.connection as MClientConnection
        val packet = event.packet

        if(packet is CPacketPayload) {
            val payloadEvent = PacketPayloadEvent(connection, PacketDirection.OUT, packet.channel, packet.data)
            connection.callEvent(payloadEvent)
        }
    }

    override fun onConnected(event: ConnectedEvent) {
        val connection = event.connection as MClientConnection
        val protocol = connection.protocol

        when(protocol.type) {
            MProtocolType.STATUS -> {
                protocol.setProtocolType(MProtocolType.HANDSHAKE, connection, true)
                connection.sendPacket(CPacketHandshake(protocol.version.value, connection.host, connection.port, HandshakeIntent.STATUS))
                protocol.setProtocolType(MProtocolType.STATUS, connection, true)
                connection.sendPacket(CPacketStatusStart())
            }
            MProtocolType.LOGIN -> {
                val profile = connection.getPropertyAs<GameProfile>(Minecraft.KEY_PROFILE)
                        ?: throw IllegalStateException("Invalid profile when logging in to the session.")
                protocol.setProtocolType(MProtocolType.HANDSHAKE, connection, true)
                connection.sendPacket(CPacketHandshake(protocol.version.value, connection.host, connection.port, HandshakeIntent.LOGIN))
                protocol.setProtocolType(MProtocolType.LOGIN, connection, true)
                connection.sendPacket(CPacketLoginStart(profile))
            }
            else -> { }
        }
    }
}
