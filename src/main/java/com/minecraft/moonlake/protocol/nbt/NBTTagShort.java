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

public class NBTTagShort extends NBTBase {

    private short value;

    public NBTTagShort(String name) {
        this(name, (short) 0);
    }

    public NBTTagShort(String name, short value) {
        super(name);
        this.value = value;
    }

    @Override
    public Short getValue() {
        return value;
    }

    @Override
    public int getTypeId() {
        return 2;
    }

    public void setValue(short value) {
        this.value = value;
    }

    @Override
    public void read(DataInput input) throws IOException {
        this.value = input.readShort();
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeShort(value);
    }

    @Override
    public NBTTagShort clone() {
        return new NBTTagShort(getName(), getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj)) {
            NBTTagShort other = (NBTTagShort) obj;
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
        return value + "s";
    }
}
