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

import com.mcmoonlake.protocol.api.Minecraft
import com.mcmoonlake.protocol.api.Properties
import com.mcmoonlake.protocol.chat.ChatComponent
import com.mcmoonlake.protocol.packet.Packet
import com.mcmoonlake.protocol.packet.PacketProtocol
import java.net.Proxy
import java.net.SocketAddress

interface MConnection : Properties<String, Any> {

    val host: String

    val port: Int

    val localAddress: SocketAddress?

    val remoteAddress: SocketAddress?

    val protocol: PacketProtocol

    var compressionThreshold: Int

    var connectTimeout: Int

    var readTimeout: Int

    var writeTimeout: Int

    val isConnected: Boolean

    fun connect(wait: Boolean = true)

    fun disconnect(reason: ChatComponent, wait: Boolean = false, cause: Throwable? = null)

    fun disconnect(reason: String, wait: Boolean = false, cause: Throwable? = null)

    fun sendPacket(packet: Packet)

    fun callEvent(event: ProtocolEvent)

    fun addListener(listener: MConnectionListener)

    fun removeListener(listener: MConnectionListener)
}

interface MConnectionFactory {

    fun createClientConnection(mc: Minecraft): MConnection

    fun createServerConnection(mc: Minecraft): MConnection
}

abstract class MConnectionFactoryAbstract(val proxy: Proxy?) : MConnectionFactory
