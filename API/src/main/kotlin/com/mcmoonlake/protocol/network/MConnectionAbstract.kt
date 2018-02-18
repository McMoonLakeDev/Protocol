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

import com.mcmoonlake.protocol.chat.ChatComponent
import com.mcmoonlake.protocol.packet.Packet
import com.mcmoonlake.protocol.packet.PacketCompressionHandler
import com.mcmoonlake.protocol.packet.PacketProtocol
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ConnectTimeoutException
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.ReadTimeoutException
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutException
import io.netty.handler.timeout.WriteTimeoutHandler
import java.net.ConnectException
import java.net.SocketAddress
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue

abstract class MConnectionAbstract(
        override val host: String,
        override val port: Int,
        override val protocol: PacketProtocol
) : SimpleChannelInboundHandler<Packet>(),
        MConnection {

    protected var isDisconnected: Boolean = false

    private val listeners: MutableList<MConnectionListener> = CopyOnWriteArrayList()
    private val packets: BlockingQueue<Packet> = LinkedBlockingQueue()
    private val properties0: MutableMap<String, Any> = HashMap()
    private var packetHandleThread: Thread? = null
    private var channel: Channel? = null

    private var compressionThreshold0 = -1
    private var connectTimeout0 = 30
    private var readTimeout0 = 30
    private var writeTimeout0 = 0

    override val properties: Map<String, Any>
        get() = HashMap(properties0)

    override fun getProperty(key: String): Any?
            = properties0[key]

    override fun setProperty(key: String, value: Any)
            { properties0.put(key, value) }

    protected open fun initializeProperties() { }

    override val localAddress: SocketAddress?
        get() = channel?.localAddress()

    override val remoteAddress: SocketAddress?
        get() = channel?.remoteAddress()

    override var compressionThreshold: Int
        get() = compressionThreshold0
        set(value) {
            compressionThreshold0 = value
            refreshCompressionHandler(channel)
        }

    protected open fun refreshCompressionHandler(channel: Channel?) {
        if(channel == null) return
        if(compressionThreshold0 >= 0) {
            if(channel.pipeline().get("compression") == null)
                channel.pipeline().addBefore("codec", "compression", PacketCompressionHandler(this))
        } else if(channel.pipeline().get("compression") != null) {
            channel.pipeline().remove("compression")
        }
    }

    override var connectTimeout: Int
        get() = connectTimeout0
        set(value) { connectTimeout0 = value }

    override var readTimeout: Int
        get() = readTimeout0
        set(value) {
            readTimeout0 = value
            refreshReadTimeoutHandler(channel)
        }

    override var writeTimeout: Int
        get() = writeTimeout0
        set(value) {
            writeTimeout0 = value
            refreshWriteTimeoutHandler(channel)
        }

    protected open fun refreshReadTimeoutHandler(channel: Channel?) {
        if(channel == null) return
        if(readTimeout0 <= 0) {
            if(channel.pipeline().get("readTimeout") != null)
                channel.pipeline().remove("readTimeout")
        } else {
            if(channel.pipeline().get("readTimeout") == null)
                channel.pipeline().addFirst("readTimeout", ReadTimeoutHandler(readTimeout0))
            else
                channel.pipeline().replace("readTimeout", "readTimeout", ReadTimeoutHandler(readTimeout0))
        }
    }

    protected open fun refreshWriteTimeoutHandler(channel: Channel?) {
        if(channel == null) return
        if(writeTimeout0 <= 0) {
            if(channel.pipeline().get("writeTimeout") != null)
                channel.pipeline().remove("writeTimeout")
        } else {
            if(channel.pipeline().get("writeTimeout") == null)
                channel.pipeline().addFirst("writeTimeout", WriteTimeoutHandler(writeTimeout0))
            else
                channel.pipeline().replace("writeTimeout", "writeTimeout", WriteTimeoutHandler(writeTimeout0))
        }
    }

    override val isConnected: Boolean
        get() = channel != null && channel!!.isOpen && !isDisconnected

    override fun connect(wait: Boolean) {
        initializeProperties()
    }

    override fun disconnect(reason: ChatComponent, wait: Boolean, cause: Throwable?)
            = disconnect(reason.toRaw(false), wait, cause)

    override fun disconnect(reason: String, wait: Boolean, cause: Throwable?) {
        if(isDisconnected) return
        isDisconnected = true
        if(packetHandleThread != null) {
            packetHandleThread?.interrupt()
            packetHandleThread = null
        }
        if(channel != null && channel!!.isOpen) {
            val future = channel!!.flush().close().addListener { _ ->
                callEvent(DisconnectedEvent(this, reason, cause))
            }
            if(wait) try {
                future.await()
            } catch(e: InterruptedException) {
            }
        } else {
            callEvent(DisconnectedEvent(this, reason, cause))
        }
        channel = null
    }

    override fun sendPacket(packet: Packet) {
        if(channel == null) return
        val future = channel!!.writeAndFlush(packet).addListener {
            if(it.isSuccess)
                callEvent(PacketSendingEvent(this, packet))
            else
                exceptionCaught(null, it.cause())
        }
        if(packet.isPriority) try {
            future.await()
        } catch(e: InterruptedException) {
        }
    }

    override fun callEvent(event: ProtocolEvent) = try {
        listeners.forEach { event.call(it) }
    } catch(e: Throwable) {
        exceptionCaught(null, e)
    }

    override fun addListener(listener: MConnectionListener)
            { listeners.add(listener) }

    override fun removeListener(listener: MConnectionListener)
            { listeners.remove(listener) }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        val message =
                if(cause is ConnectTimeoutException || (cause is ConnectException
                        && cause.message?.contains("Connection timed out") == true))
                    "Connection timed out."
                else if(cause is ReadTimeoutException)
                    "Read timed out."
                else if(cause is WriteTimeoutException)
                    "Write timed out."
                else
                    cause.toString()
        disconnect(message, cause = cause)
    }

    override fun channelActive(ctx: ChannelHandlerContext?) {
        if(isDisconnected || channel != null) {
            ctx?.channel()?.close()
            return
        }
        channel = ctx?.channel()
        packetHandleThread = Thread({
            try {
                var packet: Packet? = null
                while(packets.take().apply { packet = this } != null)
                    callEvent(PacketReceivingEvent(this, packet!!))
            } catch(e: InterruptedException) {
            } catch(e: Throwable) {
                exceptionCaught(null, e)
            }
        })
        packetHandleThread?.start()
        callEvent(ConnectedEvent(this))
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        if(ctx?.channel() == channel)
            disconnect("Connection is closed.")
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, packet: Packet?) {
        if(packet == null) return
        if(!packet.isPriority)
            packets.add(packet)
    }
}
