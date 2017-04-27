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

public class NBTTagByte extends NBTBase {

    private byte value;

    public NBTTagByte(String name) {
        this(name, (byte) 0);
    }

    public NBTTagByte(String name, byte value) {
        super(name);
        this.value = value;
    }

    @Override
    public Byte getValue() {
        return value;
    }

    @Override
    public int getTypeId() {
        return 1;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    @Override
    public void read(DataInput input) throws IOException {
        this.value = input.readByte();
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeByte(value);
    }

    @Override
    public NBTTagByte clone() {
        return new NBTTagByte(getName(), getValue());
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj)) {
            NBTTagByte other = (NBTTagByte) obj;
            return value == other.value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ value;
    }

    @Override
    public String toString() {
        return value + "b";
    }
}
