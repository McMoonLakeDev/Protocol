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

package com.mcmoonlake.protocol.packet.handshake

import com.mcmoonlake.protocol.packet.PacketAbstract
import com.mcmoonlake.protocol.packet.PacketBuffer
import com.mcmoonlake.protocol.packet.PacketClient

data class CPacketHandshake(
        var version: Int,
        var host: String,
        var port: Int,
        var nextState: Int) : PacketAbstract(), PacketClient {

    constructor() : this(-1, "127.0.0.1", 25565, -1)
    constructor(version: Int, host: String, port: Int, nextState: HandshakeIntent) : this(version, host, port, nextState.value)

    override fun read(data: PacketBuffer) {
        version = data.readVarInt()
        host = data.readString()
        port = data.readUnsignedShort()
        nextState = data.readVarInt()
    }

    override fun write(data: PacketBuffer) {
        data.writeVarInt(version)
        data.writeString(host)
        data.writeShort(port)
        data.writeVarInt(nextState)
    }

    override val isPriority: Boolean
        get() = true
}

enum class HandshakeIntent(val value: Int) {

    STATUS(1),
    LOGIN(2),
    ;
}
