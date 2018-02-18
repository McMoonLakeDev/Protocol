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

import com.mcmoonlake.protocol.chat.ChatComponent
import com.mcmoonlake.protocol.chat.ChatSerializer
import com.mcmoonlake.protocol.packet.PacketAbstract
import com.mcmoonlake.protocol.packet.PacketBuffer
import com.mcmoonlake.protocol.packet.PacketClient

data class CPacketChatMessage(
        var message: ChatComponent
) : PacketAbstract(),
        PacketClient {

    constructor() : this(ChatComponent.NULL)
    constructor(message: String) : this(ChatSerializer.fromRaw(message))

    override fun read(data: PacketBuffer) {
        message = ChatSerializer.fromJsonLenient(data.readString())
    }

    override fun write(data: PacketBuffer) {
        data.writeString(message.toJson())
    }
}
