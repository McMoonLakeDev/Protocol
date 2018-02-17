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

import com.mcmoonlake.protocol.packet.*
import com.mcmoonlake.protocol.packet.handshake.CPacketHandshake
import com.mcmoonlake.protocol.packet.login.CPacketEncryptionResponse
import com.mcmoonlake.protocol.packet.login.CPacketLoginStart
import com.mcmoonlake.protocol.packet.login.SPacketEncryptionRequest
import com.mcmoonlake.protocol.packet.login.SPacketSetCompression
import com.mcmoonlake.protocol.packet.play.CPacketChatMessage
import com.mcmoonlake.protocol.packet.play.SPacketKickDisconnect
import com.mcmoonlake.protocol.packet.play.SPacketLoginSuccess
import com.mcmoonlake.protocol.packet.status.CPacketStatusPing
import com.mcmoonlake.protocol.packet.status.CPacketStatusStart
import com.mcmoonlake.protocol.packet.status.SPacketStatusPong
import com.mcmoonlake.protocol.packet.status.SPacketStatusResponse
import java.security.GeneralSecurityException
import java.security.Key

open class MProtocol(
        type: MProtocolType,
        val version: MProtocolVersion) : PacketProtocol() {

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
        registerOutgoingPacket(0x02, CPacketChatMessage::class.java)
    }
}

enum class MProtocolType {

    HANDSHAKE,
    STATUS,
    LOGIN,
    PLAY,
    ;
}

enum class MProtocolVersion(val value: Int) {

    V1_12_2(340),
    V1_12_1(338),
    V1_12(335),

    V1_11_2(316),
    V1_11_1(316),
    V1_11(315),

    V1_10_2(210),
    V1_10_1(210),
    V1_10(210),

    V1_9_4(110),
    V1_9_3(110),
    V1_9_2(109),
    V1_9_1(108),
    V1_9(107),

    V1_8_9(47),
    V1_8_8(47),
    V1_8_7(47),
    V1_8_6(47),
    V1_8_5(47),
    V1_8_4(47),
    V1_8_3(47),
    V1_8_2(47),
    V1_8_1(47),
    V1_8(47),
    ;
}
