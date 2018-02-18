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

import com.mcmoonlake.protocol.api.Valuable
import com.mcmoonlake.protocol.packet.*
import com.mcmoonlake.protocol.packet.handshake.CPacketHandshake
import com.mcmoonlake.protocol.packet.login.CPacketEncryptionResponse
import com.mcmoonlake.protocol.packet.login.CPacketLoginStart
import com.mcmoonlake.protocol.packet.login.SPacketEncryptionRequest
import com.mcmoonlake.protocol.packet.login.SPacketSetCompression
import com.mcmoonlake.protocol.packet.play.*
import com.mcmoonlake.protocol.packet.status.CPacketStatusPing
import com.mcmoonlake.protocol.packet.status.CPacketStatusStart
import com.mcmoonlake.protocol.packet.status.SPacketStatusPong
import com.mcmoonlake.protocol.packet.status.SPacketStatusResponse
import com.mcmoonlake.protocol.util.ComparisonChain
import java.security.GeneralSecurityException
import java.security.Key

open class MProtocol(
        type: MProtocolType,
        val version: MProtocolVersion
) : PacketProtocol() {

    private var type0: MProtocolType = type
    private var encryption0: PacketEncryption? = null
    private val header0 = PacketHeaderDefault()

    override val header: PacketHeader
        get() = header0

    override val encryption: PacketEncryption?
        get() = encryption0

    @Throws(Throwable::class)
    fun enableEncryption(key: Key) = try {
        encryption0 = PacketEncryptionAES(key)
    } catch (e: GeneralSecurityException) {
        throw Error("Could not enable encryption protocol.", e)
    }

    val type: MProtocolType
        get() = type0

    fun setProtocolType(type: MProtocolType, connection: MConnection, isClient: Boolean) {
        clearPackets()
        when(type) {
            MProtocolType.HANDSHAKE -> initHandshake(connection, isClient)
            MProtocolType.STATUS -> initStatus(connection, isClient)
            MProtocolType.LOGIN -> initLogin(connection, isClient)
            MProtocolType.PLAY -> initPlay(connection, isClient)
        }
        type0 = type
    }

    private fun initHandshake(connection: MConnection, isClient: Boolean) {
        registerOutgoingPacket(0x00, CPacketHandshake::class.java)
    }
    private fun initStatus(connection: MConnection, isClient: Boolean) {
        registerIncomingPacket(0x00, SPacketStatusResponse::class.java)
        registerIncomingPacket(0x01, SPacketStatusPong::class.java)
        registerOutgoingPacket(0x00, CPacketStatusStart::class.java)
        registerOutgoingPacket(0x01, CPacketStatusPing::class.java)
    }
    private fun initLogin(connection: MConnection, isClient: Boolean) {
        registerIncomingPacket(0x00, SPacketKickDisconnect::class.java)
        registerIncomingPacket(0x01, SPacketEncryptionRequest::class.java)
        registerIncomingPacket(0x02, SPacketLoginSuccess::class.java)
        registerIncomingPacket(0x03, SPacketSetCompression::class.java)
        registerOutgoingPacket(0x00, CPacketLoginStart::class.java)
        registerOutgoingPacket(0x01, CPacketEncryptionResponse::class.java)
    }
    private fun initPlay(connection: MConnection, isClient: Boolean) {
        // TODO Play
        registerIncomingPacket(0x18, SPacketPayload::class.java)
        registerIncomingPacket(0x23, SPacketJoinGame::class.java)
        registerIncomingPacket(0x35, SPacketRespawn::class.java)
        registerOutgoingPacket(0x02, CPacketChatMessage::class.java)
        registerOutgoingPacket(0x09, CPacketPayload::class.java)
    }
}

enum class MProtocolType {

    HANDSHAKE,
    STATUS,
    LOGIN,
    PLAY,
    ;
}

class MProtocolVersion private constructor(
        val major: Int,
        val minor: Int,
        val build: Int,
        private val protocol: Int
) : Comparable<MProtocolVersion>,
        Valuable<Int> {

    override fun value(): Int
            = protocol

    override fun compareTo(other: MProtocolVersion): Int {
        return ComparisonChain.start()
                .compare(major, other.major)
                .compare(minor, other.minor)
                .compare(build, other.build)
                .result
    }

    override fun hashCode(): Int {
        var result = major.hashCode()
        result = 31 * result + minor.hashCode()
        result = 31 * result + build.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if(other === this)
            return true
        if(other is MProtocolVersion)
            return major == other.major && minor == other.minor && build == other.build
        return false
    }

    override fun toString(): String {
        return "MProtocolVersion(mc=$major.$minor.$build, protocol=$protocol)"
    }

    companion object {

        @JvmField val V1_12_2 = MProtocolVersion(1, 12, 2, 340)
        @JvmField val V1_12_1 = MProtocolVersion(1, 12, 1, 338)
        @JvmField val V1_12 = MProtocolVersion(1, 12, 0, 335)

        @JvmField val V1_11_2 = MProtocolVersion(1, 11, 2, 316)
        @JvmField val V1_11_1 = MProtocolVersion(1, 11, 1, 316)
        @JvmField val V1_11 = MProtocolVersion(1, 11, 0, 315)

        @JvmField val V1_10_2 = MProtocolVersion(1, 10, 2, 210)
        @JvmField val V1_10_1 = MProtocolVersion(1, 10, 1, 210)
        @JvmField val V1_10 = MProtocolVersion(1, 10, 0, 210)

        @JvmField val V1_9_4 = MProtocolVersion(1, 9, 4, 110)
        @JvmField val V1_9_3 = MProtocolVersion(1, 9, 3, 110)
        @JvmField val V1_9_2 = MProtocolVersion(1, 9, 2, 109)
        @JvmField val V1_9_1 = MProtocolVersion(1, 9, 1, 108)
        @JvmField val V1_9 = MProtocolVersion(1, 9, 0, 107)

        @JvmField val V1_8_9 = MProtocolVersion(1, 8, 9, 47)
        @JvmField val V1_8_8 = MProtocolVersion(1, 8, 8, 47)
        @JvmField val V1_8_7 = MProtocolVersion(1, 8, 7, 47)
        @JvmField val V1_8_6 = MProtocolVersion(1, 8, 6, 47)
        @JvmField val V1_8_5 = MProtocolVersion(1, 8, 5, 47)
        @JvmField val V1_8_4 = MProtocolVersion(1, 8, 4, 47)
        @JvmField val V1_8_3 = MProtocolVersion(1, 8, 3, 47)
        @JvmField val V1_8_2 = MProtocolVersion(1, 8, 2, 47)
        @JvmField val V1_8_1 = MProtocolVersion(1, 8, 1, 47)
        @JvmField val V1_8 = MProtocolVersion(1, 8, 0, 47)
    }
}
