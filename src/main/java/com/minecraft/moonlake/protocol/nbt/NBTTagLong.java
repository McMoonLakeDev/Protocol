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

public class NBTTagLong extends NBTBase {

    private long value;

    public NBTTagLong(String name) {
        this(name, 0);
    }

    public NBTTagLong(String name, long value) {
        super(name);
        this.value = value;
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public int getTypeId() {
        return 4;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public void read(DataInput input) throws IOException {
        this.value = input.readLong();
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeLong(value);
    }

    @Override
    public NBTTagLong clone() {
        return new NBTTagLong(getName(), getValue());
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj)) {
            NBTTagLong other = (NBTTagLong) obj;
            return value == other.value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ (int) (value ^ value >>> 32);
    }

    @Override
    public String toString() {
        return value + "L";
    }
}
