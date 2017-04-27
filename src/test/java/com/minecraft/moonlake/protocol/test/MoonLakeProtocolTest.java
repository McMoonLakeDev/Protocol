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

import com.minecraft.moonlake.protocol.nbt.*;
import com.minecraft.moonlake.protocol.util.Util;

import java.io.File;
import java.io.IOException;

public class MoonLakeProtocolTest {

    public static void main(String[] args) {
        // 测试写出和读取
        testWriteItem();
        testReadItem();
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
