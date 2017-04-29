/*
 * Copyright (C) 2017 The MoonLake Authors
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


package com.minecraft.moonlake.protocol.mc;

import com.minecraft.moonlake.protocol.data.GameProfile;
import com.minecraft.moonlake.protocol.data.MinecraftProtocolType;
import com.minecraft.moonlake.protocol.data.handshake.HandshakeIntent;
import com.minecraft.moonlake.protocol.mc.connect.ClientConnection;
import com.minecraft.moonlake.protocol.mc.connect.ClientConnectionService;
import com.minecraft.moonlake.protocol.mc.connect.event.ConnectionListenerAdapter;
import com.minecraft.moonlake.protocol.mc.connect.event.ConnectedEvent;
import com.minecraft.moonlake.protocol.mc.connect.event.PacketReceivedEvent;
import com.minecraft.moonlake.protocol.mc.connect.event.client.ConnectedServerEvent;
import com.minecraft.moonlake.protocol.mc.connect.event.client.ServerPingEvent;
import com.minecraft.moonlake.protocol.packet.handshake.HandshakePacket;
import com.minecraft.moonlake.protocol.packet.login.client.EncryptionResponsePacket;
import com.minecraft.moonlake.protocol.packet.login.client.LoginStartPacket;
import com.minecraft.moonlake.protocol.packet.login.server.EncryptionRequestPacket;
import com.minecraft.moonlake.protocol.packet.login.server.LoginDisconnectPacket;
import com.minecraft.moonlake.protocol.packet.login.server.LoginSetCompressionPacket;
import com.minecraft.moonlake.protocol.packet.login.server.LoginSuccessPacket;
import com.minecraft.moonlake.protocol.packet.play.client.ClientKeepAlivePacket;
import com.minecraft.moonlake.protocol.packet.play.server.ServerDisconnectPacket;
import com.minecraft.moonlake.protocol.packet.play.server.ServerKeepAlivePacket;
import com.minecraft.moonlake.protocol.packet.play.server.ServerSetCompressionPacket;
import com.minecraft.moonlake.protocol.packet.status.client.StatusPingPacket;
import com.minecraft.moonlake.protocol.packet.status.client.StatusQueryPacket;
import com.minecraft.moonlake.protocol.packet.status.server.StatusPongPacket;
import com.minecraft.moonlake.protocol.packet.status.server.StatusResponsePacket;
import com.minecraft.moonlake.protocol.util.CryptUtil;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.Proxy;
import java.security.PublicKey;

public class MinecraftClientListener extends ConnectionListenerAdapter {

    //
    // Minecraft 客户端协议监听器: 处理和服务端数据包相互交互的数据
    // 更详细的 Minecraft 协议实现可以查看维基百科: http://wiki.vg/Protocol
    ///

    @Override
    public void onReceived(PacketReceivedEvent event) {
        // 处理客户端接收到服务器的数据包事件
        ClientConnection connection = (ClientConnection) event.getConnection();
        MinecraftClientProtocol protocol = (MinecraftClientProtocol) event.getConnection().getPacketProtocol();

        if(protocol.getProtocolType() == MinecraftProtocolType.LOGIN) {
            // 如果数据包协议类型为: 登录
            if(event.getPacket() instanceof EncryptionRequestPacket) {
                // 处理正版登录的加密登录请求数据包
                // 暂时不详细解释说明, 因为正版登录验证还没实现 2333 // TODO Auth Service
                EncryptionRequestPacket encryptionRequestPacket = (EncryptionRequestPacket) event.getPacket();
                SecretKey secretKey = CryptUtil.generateSharedKey();
                Proxy proxy = connection.getProperty(MinecraftClientProtocol.AUTH_PROXY_KEY);
                if(proxy == null) proxy = Proxy.NO_PROXY;
                PublicKey publicKey = encryptionRequestPacket.getPublicKey();
                GameProfile profile = connection.getProperty(MinecraftClientProtocol.PROFILE_KEY);
                String serverHash = new BigInteger(CryptUtil.getServerIdHash(encryptionRequestPacket.getServerId(), publicKey, secretKey)).toString(16);
                String accessToken = connection.getProperty(MinecraftClientProtocol.ACCESS_TOKEN_KEY);
                try {
                    new ClientConnectionService(proxy).joinServer(profile, accessToken, serverHash);
                } catch (Exception e) {
                    connection.disconnect("登录失败: 异常信息: " + e.getMessage(), e);
                    return;
                }
                connection.sendPacket(new EncryptionResponsePacket(secretKey, publicKey, encryptionRequestPacket.getVerifyToken()));
                protocol.enableEncryption(secretKey);

            } else if(event.getPacket() instanceof LoginSuccessPacket) {
                // 客户端成功登录到服务器的数据包
                // 设置客户端的游戏信息 GameProfile 以及设置当前协议状态为 PLAY 游戏中
                LoginSuccessPacket loginSuccessPacket = (LoginSuccessPacket) event.getPacket();
                connection.setProperty(MinecraftClientProtocol.PROFILE_KEY, loginSuccessPacket.getProfile());
                protocol.setProtocolType(MinecraftProtocolType.PLAY, true, connection);
                connection.callClientEvent(new ConnectedServerEvent(connection)); // 触发连接到服务器事件

            } else if(event.getPacket() instanceof LoginDisconnectPacket) {
                // 登录断开连接数据包则将会话断开连接
                LoginDisconnectPacket loginDisconnectPacket = (LoginDisconnectPacket) event.getPacket();
                connection.disconnect(loginDisconnectPacket.getMessage());

            } else if(event.getPacket() instanceof LoginSetCompressionPacket) {
                // 登录设置压缩数据包则将会话的压缩阈值进行修改
                LoginSetCompressionPacket loginSetCompressionPacket = (LoginSetCompressionPacket) event.getPacket();
                connection.setCompressionThreshold(loginSetCompressionPacket.getThreshold());

            }
        } else if(protocol.getProtocolType() == MinecraftProtocolType.STATUS) {
            // 如果数据包协议类型为: 状态
            if(event.getPacket() instanceof StatusResponsePacket) {
                // 客户端收到服务器的状态响应数据包就说明 STATUS 准备完成, 则发送ping进行应答
                // 流程结构: [-> 捂手] [-> 状态查询] [<- 状态响应] [-> 状态ping] [<- 状态pong]
                StatusResponsePacket statusResponsePacket = (StatusResponsePacket) event.getPacket();
                connection.callClientEvent(new ServerPingEvent(connection, statusResponsePacket.getStatusInfo())); // 触发客户端请求服务器ping事件
                connection.sendPacket(new StatusPingPacket(System.currentTimeMillis()));

            } else if(event.getPacket() instanceof StatusPongPacket) {
                // 客户端收到这个数据包就说明 STATUS 的会话全部完成, 结束即可
                // 流程结构: [-> 捂手] [-> 状态查询] [<- 状态响应] [-> 状态ping] [<- 状态pong]
                connection.disconnect("状态会话已完成.");
            }
        } else if(protocol.getProtocolType() == MinecraftProtocolType.PLAY) {
            // 如果数据包协议类型为: 游戏中
            if(event.getPacket() instanceof ServerKeepAlivePacket) {
                // 服务器心跳包则将会话进行回复心跳包保持在线
                // 心跳包里面有一个 int 类型的 id 值, 接收到客户端必须回应对应的
                ServerKeepAlivePacket serverKeepAlivePacket = (ServerKeepAlivePacket) event.getPacket();
                connection.sendPacket(new ClientKeepAlivePacket(serverKeepAlivePacket.getPingId()));

            } else if(event.getPacket() instanceof ServerDisconnectPacket) {
                // 服务器断开连接数据包则将会话断开连接
                ServerDisconnectPacket serverDisconnectPacket = (ServerDisconnectPacket) event.getPacket();
                connection.disconnect(serverDisconnectPacket.getReason());

            } else if(event.getPacket() instanceof ServerSetCompressionPacket) {
                // 服务器设置压缩数据包则将会话的压缩阈值进行修改
                // 这个压缩阈值是在服务器的 server.properties 内的 network-compression-threshold 值
                // 默认是 256, 这个值表示客户端和服务器通信时数据包数据的压缩阈值, 必须相同, 否则解压缩错误
                ServerSetCompressionPacket serverSetCompressionPacket = (ServerSetCompressionPacket) event.getPacket();
                connection.setCompressionThreshold(serverSetCompressionPacket.getThreshold());
            }
        }
    }

    @Override
    public void onConnected(ConnectedEvent event) {
        // 处理客户端或服务端连接的事件
        // 当这个事件触发, 说明客户端和服务器的通道已经建立, 可以发送数据包
        ClientConnection connection = (ClientConnection) event.getConnection();
        MinecraftClientProtocol protocol = (MinecraftClientProtocol) event.getConnection().getPacketProtocol();

        if(protocol.getProtocolType() == MinecraftProtocolType.LOGIN) {
            // 如果数据包协议类型为: 登录
            // 则开始将会话发送握手数据包和登录开始数据包并设置协议类型为登录
            // 流程结构: [-> 捂手] [-> 登录开始] [<- 设置压缩阈值] [<- 登录成功] [<- 加入游戏] [<- 游戏数据包...]
            GameProfile profile = connection.getProperty(MinecraftClientProtocol.PROFILE_KEY);
            protocol.setProtocolType(MinecraftProtocolType.HANDSHAKE, true, connection);
            connection.sendPacket(new HandshakePacket(protocol.getProtocolVersion().value(), connection.getHost(), connection.getPort(), HandshakeIntent.LOGIN));
            protocol.setProtocolType(MinecraftProtocolType.LOGIN, true, connection);
            connection.sendPacket(new LoginStartPacket(profile != null ? profile.getName() : MinecraftClientProtocol.DEF_NAME)); // 如果游戏简介为 null 则使用默认用户名

        } else if(protocol.getProtocolType() == MinecraftProtocolType.STATUS) {
            // 如果数据包协议类型为: 状态
            // 则开始将会话发送握手数据包并设置协议类型为状态
            // 流程结构: [-> 捂手] [-> 状态查询] [<- 状态响应] [-> 状态ping] [<- 状态pong]
            protocol.setProtocolType(MinecraftProtocolType.HANDSHAKE, true, connection);
            connection.sendPacket(new HandshakePacket(protocol.getProtocolVersion().value(), connection.getHost(), connection.getPort(), HandshakeIntent.STATUS));
            protocol.setProtocolType(MinecraftProtocolType.STATUS, true, connection);
            connection.sendPacket(new StatusQueryPacket());
        }
    }
}
