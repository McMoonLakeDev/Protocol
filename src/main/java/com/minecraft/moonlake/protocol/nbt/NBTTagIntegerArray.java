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

public class NBTTagIntegerArray extends NBTBase {

    private int[] value;

    public NBTTagIntegerArray(String name) {
        this(name, new int[0]);
    }

    public NBTTagIntegerArray(String name, int[] value) {
        super(name);
        this.value = value;
    }

    @Override
    public int[] getValue() {
        return value;
    }

    @Override
    public int getTypeId() {
        return 11;
    }

    public void setValue(int[] value) {
        if(value == null)
            return;
        this.value = value.clone();
    }

    public int getValue(int index) {
        return value[index];
    }

    public void setValue(int index, int value) {
        this.value[index] = value;
    }

    @Override
    public void read(DataInput input) throws IOException {
        this.value = new int[input.readInt()];
        for(int i = 0; i < value.length; i++)
            this.value[i] = input.readInt();
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeInt(value.length);
        for(int i = 0; i < value.length; i++)
            output.writeInt(value[i]);
    }

    @Override
    public NBTTagIntegerArray clone() {
        return new NBTTagIntegerArray(getName(), getValue());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && Arrays.equals(value, ((NBTTagIntegerArray) obj).value);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for(int i = 0; i < value.length; i++)
            builder.append(value[i]).append(",");
        return builder.append("]").toString();
    }
}
