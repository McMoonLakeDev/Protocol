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


package com.minecraft.moonlake.protocol.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class NBTBase implements Cloneable {

    private String name;

    public NBTBase(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public abstract Object getValue();

    public abstract int getTypeId();

    public abstract void read(DataInput input) throws IOException;

    public abstract void write(DataOutput output) throws IOException;

    @Override
    public abstract NBTBase clone();

    public boolean isNumber() {
        int id = getTypeId();
        return id == 1 || id == 2 || id == 3 || id == 4 || id == 5 || id == 6;
    }

    public static NBTBase createTag(byte typeId) {
        return createTag(typeId, "");
    }

    public static NBTBase createTag(byte typeId, String name) {
        switch (typeId) {
            case 1:
                return new NBTTagByte(name);
            case 2:
                return new NBTTagShort(name);
            case 3:
                return new NBTTagInteger(name);
            case 4:
                return new NBTTagLong(name);
            case 5:
                return new NBTTagFloat(name);
            case 6:
                return new NBTTagDouble(name);
            case 7:
                return new NBTTagByteArray(name);
            case 8:
                return new NBTTagString(name);
            case 9:
                return new NBTTagList<>(name);
            case 10:
                return new NBTTagCompound(name);
            case 11:
                return new NBTTagIntegerArray(name);
            default:
                return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(obj instanceof NBTBase) {
            NBTBase other = (NBTBase) obj;
            return name.equals(other.name) && getTypeId() == other.getTypeId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getTypeId();
    }
}