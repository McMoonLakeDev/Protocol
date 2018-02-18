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

package com.mcmoonlake.protocol.packet.login

import com.mcmoonlake.protocol.packet.PacketAbstract
import com.mcmoonlake.protocol.packet.PacketBuffer
import com.mcmoonlake.protocol.packet.PacketClient
import com.mcmoonlake.protocol.util.CryptUtils
import java.security.PrivateKey
import java.util.*

data class CPacketEncryptionResponse(
        var sharedKey: ByteArray,
        var verifyToken: ByteArray
) : PacketAbstract(),
        PacketClient {

    constructor() : this(byteArrayOf(), byteArrayOf())

    fun decryptSharedKey(privateKey: PrivateKey): ByteArray
            = CryptUtils.decrypt(privateKey, sharedKey)
    fun decryptVerifyToken(privateKey: PrivateKey): ByteArray
            = CryptUtils.decrypt(privateKey, verifyToken)

    override fun read(data: PacketBuffer) {
        sharedKey = data.readBytes(data.readVarInt())
        verifyToken = data.readBytes(data.readVarInt())
    }

    override fun write(data: PacketBuffer) {
        data.writeVarInt(sharedKey.size)
        data.writeBytes(sharedKey)
        data.writeVarInt(verifyToken.size)
        data.writeBytes(verifyToken)
    }

    override fun equals(other: Any?): Boolean {
        if(other === this)
            return true
        if(other is CPacketEncryptionResponse)
            return super.equals(other) && Arrays.equals(sharedKey, other.sharedKey) && Arrays.equals(verifyToken, other.verifyToken)
        return false
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + Arrays.hashCode(sharedKey)
        result = 31 * result + Arrays.hashCode(verifyToken)
        return result
    }
}
