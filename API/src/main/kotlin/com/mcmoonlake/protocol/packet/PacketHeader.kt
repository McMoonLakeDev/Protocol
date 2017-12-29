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

import java.io.IOException

interface PacketHeader {

    val isVariableLength: Boolean

    val lengthSize: Int

    fun getLengthSize(length: Int): Int

    @Throws(IOException::class)
    fun readLength(buffer: PacketBuffer, available: Int): Int

    @Throws(IOException::class)
    fun writeLength(buffer: PacketBuffer, length: Int)

    @Throws(IOException::class)
    fun readPacketId(buffer: PacketBuffer): Int

    @Throws(IOException::class)
    fun writePacketId(buffer: PacketBuffer, packetId: Int)
}

class PacketHeaderDefault : PacketHeader {

    override val isVariableLength: Boolean
        get() = true

    override val lengthSize: Int
        get() = 5

    override fun getLengthSize(length: Int): Int = when {
        length and -128 == 0 -> 1
        length and -16384 == 0 -> 2
        length and -2097152 == 0 -> 3
        length and -268435456 == 0 -> 4
        else -> 5
    }

    override fun readLength(buffer: PacketBuffer, available: Int): Int
            = buffer.readVarInt()

    override fun writeLength(buffer: PacketBuffer, length: Int)
            { buffer.writeVarInt(length) }

    override fun readPacketId(buffer: PacketBuffer): Int
            = buffer.readVarInt()

    override fun writePacketId(buffer: PacketBuffer, packetId: Int)
            { buffer.writeVarInt(packetId) }
}
