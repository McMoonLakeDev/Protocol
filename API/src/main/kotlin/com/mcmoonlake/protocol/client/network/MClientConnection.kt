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

package com.mcmoonlake.protocol.client.network

import com.mcmoonlake.protocol.api.Minecraft
import com.mcmoonlake.protocol.chat.ChatComponent
import com.mcmoonlake.protocol.chat.ChatSerializer
import com.mcmoonlake.protocol.client.MClient
import com.mcmoonlake.protocol.network.MConnectionAbstract
import com.mcmoonlake.protocol.packet.PacketCodecHandler
import com.mcmoonlake.protocol.packet.PacketEncryptionHandler
import com.mcmoonlake.protocol.packet.PacketSizerHandler
import com.mcmoonlake.protocol.packet.play.CPacketChatMessage
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.oio.OioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.Proxy
import java.util.*
import javax.naming.directory.InitialDirContext

open class MClientConnection(
        override val host: String,
        override val port: Int,
        override val protocol: MClientProtocol,
        protected val client: MClient,
        private val proxy: Proxy?) : MConnectionAbstract(host, port, protocol) {

    private var group: EventLoopGroup? = null

    override fun initializeProperties() {
        super.initializeProperties()
        setProperty(Minecraft.KEY_AUTH_PROXY, proxy ?: Proxy.NO_PROXY)
        setProperty(Minecraft.KEY_PROTOCOL_VER, protocol.version)
        setProperty(Minecraft.KEY_PROFILE, protocol.profile)
    }

    fun chat(message: String)
            = chat(ChatSerializer.fromRaw(message))

    fun chat(message: ChatComponent)
            = sendPacket(CPacketChatMessage(message))

    override fun connect(wait: Boolean) {
        super.connect(wait)
        if(isDisconnected) throw IllegalStateException("The connection has been disconnected.")
        else if(group != null) return
        try {
            val bootstrap = Bootstrap()
            if(proxy != null) {
                group = OioEventLoopGroup()
                bootstrap.channelFactory(MClientChannelFactory(proxy))
            } else {
                group = NioEventLoopGroup()
                bootstrap.channel(NioSocketChannel::class.java)
            }
            bootstrap.handler(object: ChannelInitializer<Channel>() {
                override fun initChannel(ch: Channel) {
                    ch.config().setOption(ChannelOption.IP_TOS, 0x18)
                    ch.config().setOption(ChannelOption.TCP_NODELAY, false)
                    val pipeline = ch.pipeline()
                    refreshReadTimeoutHandler(ch)
                    refreshWriteTimeoutHandler(ch)
                    pipeline.addLast("encryption", PacketEncryptionHandler(this@MClientConnection))
                    pipeline.addLast("sizer", PacketSizerHandler(this@MClientConnection))
                    pipeline.addLast("codec", PacketCodecHandler(this@MClientConnection))
                    pipeline.addLast("manager", this@MClientConnection)
                }
            }).group(group).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout * 1000)

            val connectTask = Runnable {
                try {
                    var host = host
                    var port = port
                    try {
                        val environment = Hashtable<String, String>()
                        environment.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory")
                        environment.put("java.naming.provider.url", "dns:")
                        val result = InitialDirContext(environment)
                                .getAttributes("_minecraft._tcp.$host", arrayOf("SRV"))
                                .get("srv").get().toString().split(" ", limit = 4)
                        host = result[3]
                        port = result[2].toInt()
                    } catch(e: Throwable) {
                    }
                    val future = bootstrap.remoteAddress(host, port).connect().sync()
                    if(future.isSuccess) while (!isConnected && !isDisconnected) try {
                        Thread.sleep(5)
                    } catch(e: InterruptedException) {
                    }
                } catch(e: Throwable) {
                    exceptionCaught(null, e)
                }
            }
            if(wait)
                connectTask.run()
            else
                Thread(connectTask).start()
        } catch(e: Exception) {
            exceptionCaught(null, e)
        }
    }

    override fun disconnect(reason: String, wait: Boolean, cause: Throwable?) {
        super.disconnect(reason, wait, cause)
        if(group != null) {
            val future = group?.shutdownGracefully()
            if(wait) try {
                future?.await()
            } catch(e: InterruptedException) {
            }
            group = null
        }
    }
}
