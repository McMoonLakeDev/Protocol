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

import com.mcmoonlake.protocol.isOrLater
import com.mcmoonlake.protocol.network.MProtocolVersion
import com.mcmoonlake.protocol.ofValuable
import com.mcmoonlake.protocol.ofValuableNotNull
import com.mcmoonlake.protocol.packet.PacketAbstract
import com.mcmoonlake.protocol.packet.PacketBuffer
import com.mcmoonlake.protocol.packet.PacketServer
import com.mcmoonlake.protocol.packet.PacketVersion
import com.mcmoonlake.protocol.wrapper.Difficulty
import com.mcmoonlake.protocol.wrapper.Environment
import com.mcmoonlake.protocol.wrapper.GameMode
import com.mcmoonlake.protocol.wrapper.WorldType

data class SPacketJoinGame(
        var entityId: Int,
        var gameMode: GameMode,
        var hardCore: Boolean,
        var dimension: Environment,
        var difficulty: Difficulty,
        var maxPlayer: Int,
        var worldType: WorldType,
        var reducedDebug: Boolean
) : PacketAbstract(), PacketServer, PacketVersion {

    constructor() : this(-1, GameMode.SURVIVAL, false, Environment.NORMAL, Difficulty.EASY, 20, WorldType.DEFAULT, false)

    override fun read(data: PacketBuffer, version: MProtocolVersion) {
        entityId = data.readInt()
        val flag = data.readUnsignedByte().toInt()
        val isCombatOrLaterVer = version.isOrLater(MProtocolVersion.V1_9)
        hardCore = (flag and 8) == 8
        gameMode = ofValuableNotNull(if(!isCombatOrLaterVer) flag else flag and -9)
        dimension = ofValuableNotNull(if(!isCombatOrLaterVer) data.readByte().toInt() else data.readInt())
        difficulty = ofValuableNotNull(data.readUnsignedByte().toInt())
        maxPlayer = data.readUnsignedByte().toInt()
        worldType = ofValuable(data.readString()) ?: WorldType.DEFAULT
        reducedDebug = data.readBoolean()
    }

    override fun write(data: PacketBuffer, version: MProtocolVersion) {
        data.writeInt(entityId)
        var flag = gameMode.value()
        val isCombatOrLaterVer = version.isOrLater(MProtocolVersion.V1_9)
        if(hardCore)
            flag = flag or 8
        data.writeByte(flag)
        if(!isCombatOrLaterVer) data.writeByte(dimension.value())
        else data.writeInt(dimension.value())
        data.writeByte(difficulty.value())
        data.writeByte(maxPlayer)
        data.writeString(worldType.value())
        data.writeBoolean(reducedDebug)
    }
}
