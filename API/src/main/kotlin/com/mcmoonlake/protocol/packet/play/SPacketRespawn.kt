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

import com.mcmoonlake.protocol.ofValuable
import com.mcmoonlake.protocol.ofValuableNotNull
import com.mcmoonlake.protocol.packet.PacketAbstract
import com.mcmoonlake.protocol.packet.PacketBuffer
import com.mcmoonlake.protocol.packet.PacketServer
import com.mcmoonlake.protocol.wrapper.Difficulty
import com.mcmoonlake.protocol.wrapper.Environment
import com.mcmoonlake.protocol.wrapper.GameMode
import com.mcmoonlake.protocol.wrapper.WorldType

data class SPacketRespawn(
        var dimension: Environment,
        var difficulty: Difficulty,
        var gameMode: GameMode,
        var worldType: WorldType
) : PacketAbstract(),
        PacketServer {

    constructor() : this(Environment.NORMAL, Difficulty.EASY, GameMode.SURVIVAL, WorldType.DEFAULT)

    override fun read(data: PacketBuffer) {
        dimension = ofValuableNotNull(data.readInt())
        difficulty = ofValuableNotNull(data.readUnsignedByte().toInt())
        gameMode = ofValuableNotNull(data.readUnsignedByte().toInt())
        worldType = ofValuable(data.readString()) ?: WorldType.DEFAULT
    }

    override fun write(data: PacketBuffer) {
        data.writeInt(dimension.value())
        data.writeByte(difficulty.value())
        data.writeByte(gameMode.value())
        data.writeString(worldType.value())
    }
}
