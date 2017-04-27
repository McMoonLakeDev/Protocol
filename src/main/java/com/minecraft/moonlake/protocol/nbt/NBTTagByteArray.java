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
import java.util.Arrays;

public class NBTTagByteArray extends NBTBase {

    private byte[] value;

    public NBTTagByteArray(String name) {
        this(name, new byte[0]);
    }

    public NBTTagByteArray(String name, byte[] value) {
        super(name);
        this.value = value;
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public int getTypeId() {
        return 7;
    }

    public void setValue(byte[] value) {
        if(value == null)
            return;
        this.value = value.clone();
    }

    public byte getValue(int index) {
        return value[index];
    }

    public void setValue(int index, byte value) {
        this.value[index] = value;
    }

    @Override
    public void read(DataInput input) throws IOException {
        this.value = new byte[input.readInt()];
        input.readFully(value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeInt(value.length);
        output.write(value);
    }

    @Override
    public NBTTagByteArray clone() {
        return new NBTTagByteArray(getName(), getValue());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && Arrays.equals(value, ((NBTTagByteArray) obj).value);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(value);
    }

    @Override
    public String toString() {
        return "[" + value.length + " bytes]";
    }
}
