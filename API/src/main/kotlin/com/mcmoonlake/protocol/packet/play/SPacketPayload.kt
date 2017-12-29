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

package com.mcmoonlake.protocol.packet.play

import com.mcmoonlake.protocol.packet.PacketAbstract
import com.mcmoonlake.protocol.packet.PacketBuffer
import com.mcmoonlake.protocol.packet.PacketServer

data class SPacketPayload(
        var channel: String,
        var data: PacketBuffer) : PacketAbstract(), PacketServer {

    constructor() : this("MoonLake", PacketBuffer.EMPTY)

    override fun read(data: PacketBuffer) {
        channel = data.readString()
        this.data = PacketBuffer(data.readBytes(data.readableBytes()))
    }

    override fun write(data: PacketBuffer) {
        data.writeString(channel)
        data.writeBytes(data)
    }
}
