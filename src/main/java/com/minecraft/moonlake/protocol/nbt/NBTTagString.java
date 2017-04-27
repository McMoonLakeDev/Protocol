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

public class NBTTagString extends NBTBase {

    private String value;

    public NBTTagString(String name) {
        this(name, "");
    }

    public NBTTagString(String name, String value) {
        super(name);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int getTypeId() {
        return 8;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void read(DataInput input) throws IOException {
        this.value = input.readUTF();
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeUTF(value);
    }

    @Override
    public NBTTagString clone() {
        return new NBTTagString(getName(), getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj)) {
            NBTTagString other = (NBTTagString) obj;
            return value != null ? value.equals(other.value) : other.value == null;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ value.hashCode();
    }

    @Override
    public String toString() {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }
}
