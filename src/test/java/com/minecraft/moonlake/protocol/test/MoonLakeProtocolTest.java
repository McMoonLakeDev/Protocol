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


package com.minecraft.moonlake.protocol.test;

import com.minecraft.moonlake.protocol.data.MinecraftProtocolType;
import com.minecraft.moonlake.protocol.data.play.entity.player.PositionFlag;
import com.minecraft.moonlake.protocol.mc.MinecraftClient;
import com.minecraft.moonlake.protocol.mc.MinecraftConnection;
import com.minecraft.moonlake.protocol.mc.MinecraftProtocol;
import com.minecraft.moonlake.protocol.mc.connect.DefaultConnectionFactory;
import com.minecraft.moonlake.protocol.mc.connect.event.DisconnectedEvent;
import com.minecraft.moonlake.protocol.mc.connect.event.PacketReceivedEvent;
import com.minecraft.moonlake.protocol.mc.connect.event.PacketSentEvent;
import com.minecraft.moonlake.protocol.mc.connect.event.client.ClientConnectionListenerAdapter;
import com.minecraft.moonlake.protocol.mc.connect.event.client.ConnectedServerEvent;
import com.minecraft.moonlake.protocol.nbt.NBTTagCompound;
import com.minecraft.moonlake.protocol.nbt.NBTTagList;
import com.minecraft.moonlake.protocol.packet.play.client.ClientChatMessagePacket;
import com.minecraft.moonlake.protocol.packet.play.client.player.ClientPlayerPositionRotationPacket;
import com.minecraft.moonlake.protocol.packet.play.client.world.ClientTeleportConfirmPacket;
import com.minecraft.moonlake.protocol.packet.play.server.ServerChatMessagePacket;
import com.minecraft.moonlake.protocol.packet.play.server.entity.player.ServerPlayerPositionAndLookPacket;
import com.minecraft.moonlake.protocol.util.Util;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class MoonLakeProtocolTest {

    static boolean debug = true;
    static double posX;
    static double posY;
    static double posZ;
    static float rotationYaw;
    static float rotationPitch;
    static double prevPosX;
    static double prevPosY;
    static double prevPosZ;
    static float prevYaw;
    static float prevPitch;

    public static void main(String[] args) {
        testClientConnect("localhost", 29999, MinecraftProtocolType.LOGIN);
    }

    static void testClientConnect(String host, int port, MinecraftProtocolType protocolType) {
        // 创建一个 Minecraft 客户端对象并添加数据监听器
        MinecraftClient client = new MinecraftClient(host, port, new MinecraftProtocol(protocolType == null ? MinecraftProtocolType.STATUS : protocolType), new DefaultConnectionFactory());
        client.getConnection().addListener(new ClientConnectionListenerAdapter() {
            @Override
            public void onReceived(PacketReceivedEvent event) {
                // 处理数据包接收事件

//                if(debug && !(event.getPacket() instanceof ServerChunkDataPacket))
//                    System.out.println("[Debug][PacketIn]: " + event.getPacket().toString());

                // 如果数据包为服务器聊天消息则打印在控制台
                if(event.getPacket() instanceof ServerChatMessagePacket)
                    System.out.println(((ServerChatMessagePacket) event.getPacket()).getMessage());

                // 如果数据包为服务器玩具位置和看数据包则设置数据并处理
                if(event.getPacket() instanceof ServerPlayerPositionAndLookPacket) {
                    ServerPlayerPositionAndLookPacket sppalp = (ServerPlayerPositionAndLookPacket) event.getPacket();
                    if(debug)
                        System.out.println("[Debug][PacketIn][Move]: " + sppalp.toString());
                    double x = sppalp.getX();
                    double y = sppalp.getY();
                    double z = sppalp.getZ();
                    float yaw = sppalp.getYaw();
                    float pitch = sppalp.getPitch();
                    if(sppalp.getFlags().contains(PositionFlag.X))
                        x += posX;
                    if(sppalp.getFlags().contains(PositionFlag.Y))
                        y += posY;
                    if(sppalp.getFlags().contains(PositionFlag.Z))
                        z += posZ;
                    if(sppalp.getFlags().contains(PositionFlag.YAW))
                        yaw += rotationYaw;
                    if(sppalp.getFlags().contains(PositionFlag.PITCH))
                        pitch += rotationPitch;
                    setPositionAndRotation(x, y, z, yaw, pitch);
                    event.getConnection().sendPacket(new ClientTeleportConfirmPacket(sppalp.getTeleportId()));
                    event.getConnection().sendPacket(new ClientPlayerPositionRotationPacket(true, posX, posY, posZ, rotationYaw, rotationPitch));
                }
            }

            @Override
            public void onConnectedServer(ConnectedServerEvent event) {
                // 处理连接到服务器事件
                event.getConnection().sendPacket(new ClientChatMessagePacket("你好服务器, 我是mc协议假人."));
                event.getConnection().sendPacket(new ClientChatMessagePacket("MoonLakeProtocol By Month_Light"));
            }

            @Override
            public void onSent(PacketSentEvent event) {
                // 处理数据包发送事件
                if(debug)
                    System.out.println("[Debug][PacketOut]: " + event.getPacket().toString());
            }

            @Override
            public void onDisconnected(DisconnectedEvent event) {
                // 处理连接断开事件: 打印原因和异常信息
                System.err.println("连接断开: " + event.getReason());
                if(event.getCause() != null)
                    event.getCause().printStackTrace();
            }
        });
        client.getConnection().connect(); // 开始向目标服务器连接

        JFrame jFrame = new JFrame("MoonLakeProtocolTest");
        jFrame.setSize(300, 150);
        jFrame.add(new JLabel("试试 WASD 按键控制移动", SwingConstants.CENTER));
        jFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        posZ += 0.5d;
                        setPosition(posX, posY, posZ);
                        sendMove(client.getConnection());
                        break;
                    case KeyEvent.VK_S:
                        posZ -= 0.5d;
                        setPosition(posX, posY, posZ);
                        sendMove(client.getConnection());
                        break;
                    case KeyEvent.VK_A:
                        posX += 0.5d;
                        setPosition(posX, posY, posZ);
                        sendMove(client.getConnection());
                        break;
                    case KeyEvent.VK_D:
                        posX -= 0.5d;
                        setPosition(posX, posY, posZ);
                        sendMove(client.getConnection());
                        break;
                    case KeyEvent.VK_ESCAPE:
                        client.getConnection().disconnect("exit"); // 关闭客户端连接
                        jFrame.dispose();
                        break;
                    default:
                        break;
                }
            }
        });
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    static void sendMove(MinecraftConnection session) {
        session.sendPacket(new ClientPlayerPositionRotationPacket(true, posX, posY, posZ, rotationYaw, rotationPitch));
    }

    static void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
        posX = x;
        posY = y;
        posZ = z;
        rotationYaw = yaw;
        rotationPitch = pitch;
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        prevYaw = rotationYaw;
        prevPitch = rotationPitch;
        double angle = (double) (prevYaw - yaw);
        if(angle < -180d)
            prevYaw += 360f;
        if(angle >= 180d)
            prevPitch -= 360f;
        setPosition(posX, posY, posZ);
        setRotation(yaw, pitch);
    }

    static void setPosition(double x, double y, double z) {
        posX = x;
        posY = y;
        posZ = z;
    }

    static void setRotation(float yaw, float pitch) {
        rotationYaw = yaw;
        rotationPitch = pitch;
    }

    static void testWriteItem() {
        NBTTagCompound root = new NBTTagCompound("");
        root.setString("id", "minecraft:iron_sword");
        root.setByte("Count", 1);
        root.setShort("Damage", 0);
        NBTTagCompound tag = new NBTTagCompound("tag");
        NBTTagList<NBTTagCompound> ench = new NBTTagList<>("ench");
        NBTTagCompound ench0 = new NBTTagCompound("");
        ench0.setShort("id", 16);
        ench0.setShort("lvl", 300);
        ench.add(ench0);
        tag.put(ench);
        root.put(tag);

        File file = new File("src\\test\\item.dat");
        try {
            Util.writeNBTTagToFile(root, file);
            System.out.println("done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void testReadItem() {
        File file = new File("src\\test\\item.dat");
        try {
            NBTTagCompound compound = Util.readNBTTagFromFile(file);
            System.out.println("id: " + compound.getString("id"));
            System.out.println("count: " + compound.getInteger("Count")); // Count 是个 short 类型, 但是可以强转为 int 类型
            System.out.println(compound);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
