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

import com.mcmoonlake.protocol.api.Minecraft
import com.mcmoonlake.protocol.network.MConnection
import com.mcmoonlake.protocol.network.MProtocolVersion
import com.mcmoonlake.protocol.network.PacketReceivingEvent
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageCodec
import io.netty.handler.codec.CorruptedFrameException
import io.netty.handler.codec.DecoderException
import java.util.zip.Deflater
import java.util.zip.Inflater



class PacketCompressionHandler(
        val connection: MConnection) : ByteToMessageCodec<ByteBuf>() {

    companion object {
        @JvmStatic
        private val MAX_COMPRESSED_SIZE = 2097152
    }

    private val buffer = ByteArray(8192)
    private val deflater = Deflater()
    private val inflater = Inflater()

    override fun encode(ctx: ChannelHandlerContext?, msg: ByteBuf?, out: ByteBuf?) {
        if(msg == null || out == null) return
        val readable = msg.readableBytes()
        val outBuffer = PacketBuffer(out)
        if(readable < connection.compressionThreshold) {
            outBuffer.writeVarInt(0)
            out.writeBytes(msg)
        } else {
            val bytes = ByteArray(readable)
            msg.readBytes(bytes)
            outBuffer.writeVarInt(bytes.size)
            deflater.setInput(bytes, 0, readable)
            deflater.finish()
            while(!deflater.finished()) {
                val length = deflater.deflate(buffer)
                outBuffer.writeBytes(buffer, length)
            }
            deflater.reset()
        }
    }

    override fun decode(ctx: ChannelHandlerContext?, msg: ByteBuf?, out: MutableList<Any>?) {
        if(msg == null || out == null) return
        if(msg.readableBytes() != 0) {
            val inBuffer = PacketBuffer(msg)
            val size = inBuffer.readVarInt()
            if(size == 0) {
                out.add(msg.readBytes(msg.readableBytes()))
            } else {
                if(size < connection.compressionThreshold)
                    throw DecoderException("Bad compression packet: size $size, current compression threshold: ${connection.compressionThreshold}.")
                if(size > MAX_COMPRESSED_SIZE)
                    throw DecoderException("Bad compression packet: size $size, greater than the maximum: $MAX_COMPRESSED_SIZE.")
                val bytes = ByteArray(msg.readableBytes())
                inBuffer.readBytes(bytes)
                inflater.setInput(bytes)
                val inflated = ByteArray(size)
                inflater.inflate(inflated)
                out.add(Unpooled.wrappedBuffer(inflated))
                inflater.reset()
            }
        }
    }
}

class PacketEncryptionHandler(
        val connection: MConnection) : ByteToMessageCodec<ByteBuf>() {

    private var decryptedBuffer = ByteArray(0)
    private var encryptedBuffer = ByteArray(0)

    override fun encode(ctx: ChannelHandlerContext?, msg: ByteBuf?, out: ByteBuf?) {
        if(msg == null || out == null) return
        if(connection.protocol.encryption != null) {
            val encryption = connection.protocol.encryption!!
            val length = msg.readableBytes()
            val bytes = readBytes(msg)
            val outLength = encryption.getEncryptOutputSize(length)
            if(encryptedBuffer.size < outLength)
                encryptedBuffer = ByteArray(outLength)
            val finalLength = encryption.encrypt(bytes, 0, length, encryptedBuffer, 0)
            out.writeBytes(encryptedBuffer, 0, finalLength)
        } else {
            out.writeBytes(msg)
        }
    }

    override fun decode(ctx: ChannelHandlerContext?, msg: ByteBuf?, out: MutableList<Any>?) {
        if(msg == null || out == null) return
        if(ctx != null && connection.protocol.encryption != null) {
            val encryption = connection.protocol.encryption!!
            val length = msg.readableBytes()
            val bytes = readBytes(msg)
            val inLength = encryption.getDecryptOutputSize(length)
            val byteBuf = ctx.alloc().heapBuffer(inLength)
            val finalLength = encryption.decrypt(bytes, 0, length, byteBuf.array(), byteBuf.arrayOffset())
            byteBuf.writerIndex(finalLength)
            out.add(byteBuf)
        } else {
            out.add(msg.readBytes(msg.readableBytes()))
        }
    }

    private fun readBytes(byteBuf: ByteBuf): ByteArray {
        val length = byteBuf.readableBytes()
        if(decryptedBuffer.size < length)
            decryptedBuffer = ByteArray(length)
        byteBuf.readBytes(decryptedBuffer, 0, length)
        return decryptedBuffer
    }
}

class PacketSizerHandler(
        val connection: MConnection) : ByteToMessageCodec<ByteBuf>() {

    override fun encode(ctx: ChannelHandlerContext?, msg: ByteBuf?, out: ByteBuf?) {
        if(msg == null || out == null) return
        val length = msg.readableBytes()
        out.ensureWritable(connection.protocol.header.getLengthSize(length) + length)
        connection.protocol.header.writeLength(PacketBuffer(out), length)
        out.writeBytes(msg)
    }

    override fun decode(ctx: ChannelHandlerContext?, msg: ByteBuf?, out: MutableList<Any>?) {
        if(msg == null || out == null) return
        val length = connection.protocol.header.lengthSize
        if(length > 0) {
            msg.markReaderIndex()
            val bytes = ByteArray(length)
            for(index in 0 until bytes.size) {
                if(!msg.isReadable) {
                    msg.resetReaderIndex()
                    return
                }
                bytes[index] = msg.readByte()
                if((connection.protocol.header.isVariableLength && bytes[index] >= 0) || index == length - 1) {
                    val newLength = connection.protocol.header.readLength(PacketBuffer(Unpooled.wrappedBuffer(bytes)), msg.readableBytes())
                    if(msg.readableBytes() < newLength) {
                        msg.resetReaderIndex()
                        return
                    }
                    out.add(msg.readBytes(newLength))
                    return
                }
            }
            throw CorruptedFrameException("Packet data is too long.")
        } else {
            out.add(msg.readBytes(msg.readableBytes()))
        }
    }
}

class PacketCodecHandler(
        val connection: MConnection) : ByteToMessageCodec<Packet>() {

    private val protocolVer: MProtocolVersion by lazy {
        connection.getPropertyAs<MProtocolVersion>(Minecraft.KEY_PROTOCOL_VER)
                ?: throw IllegalStateException("Protocol session invalid protocol version property.") }

    override fun encode(ctx: ChannelHandlerContext?, msg: Packet?, out: ByteBuf?) {
        if(msg == null || out == null) return
        val buffer = PacketBuffer(out)
        val packetId = connection.protocol.getOutgoingPacketId(msg::class.java)
        connection.protocol.header.writePacketId(buffer, packetId)
        if(msg is PacketVersion) {
            msg.write(buffer, protocolVer)
        } else {
            msg.write(buffer)
        }
    }

    override fun decode(ctx: ChannelHandlerContext?, msg: ByteBuf?, out: MutableList<Any>?) {
        if(msg == null || out == null) return
        val initial = msg.readerIndex()
        val buffer = PacketBuffer(msg)
        val packetId = connection.protocol.header.readPacketId(buffer)
        if(packetId == -1) {
            msg.readerIndex(initial)
            return
        }
        val packet = connection.protocol.createIncomingPacket(packetId)
        if(packet is PacketVersion) {
            packet.read(buffer, protocolVer)
        } else {
            packet.read(buffer)
        }
        if(msg.readableBytes() > 0)
            throw IllegalStateException("Packet ${packet::class.java.name} is not fully read.")
        if(packet.isPriority)
            connection.callEvent(PacketReceivingEvent(connection, packet))
        out.add(packet)
    }
}
