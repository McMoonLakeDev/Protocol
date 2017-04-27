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
import java.util.*;

public class NBTTagList<T extends NBTBase> extends NBTBase implements Iterable<T> {

    private List<T> value;
    private int valueTypeId = 0;

    public NBTTagList(String name) {
        this(name, new ArrayList<>());
    }

    public NBTTagList(String name, List<T> list) {
        super(name);
        for(T value : list) {
            if(value == null)
                throw new IllegalArgumentException("List 列表值不能拥有 null 值.");
            if(valueTypeId == 0)
                this.valueTypeId = value.getTypeId();
            else if(value.getTypeId() != valueTypeId)
                throw new IllegalArgumentException("NBTTag 列表不是同等的值类型.");
        }
        this.value = new ArrayList<>(list);
    }

    @Override
    public List<T> getValue() {
        return new ArrayList<>(value);
    }

    public void setValue(List<T> value) {
        for(T nbt : value) {
            if(nbt == null)
                throw new IllegalArgumentException("List 列表值不能拥有 null 值.");
            if(valueTypeId == 0)
                this.valueTypeId = nbt.getTypeId();
            else if(nbt.getTypeId() != valueTypeId)
                throw new IllegalArgumentException("NBTTag 列表不是同等的值类型.");
        }
        this.value = new ArrayList<>(value);
    }

    @Override
    public int getTypeId() {
        return 9;
    }

    public int getValueTypeId() {
        return valueTypeId;
    }

    public boolean add(T t) {
        if(valueTypeId == 0)
            this.valueTypeId = t.getTypeId();
        else if(t.getTypeId() != valueTypeId)
            throw new IllegalArgumentException("NBTTag 列表不是同等的值类型.");
        return value.add(t);
    }

    public boolean remove(T t) {
        return value.remove(t);
    }

    public T get(int index) {
        return value.get(index);
    }

    public int size() {
        return value.size();
    }

    public void clear() {
        this.value.clear();
        this.valueTypeId = 0; // 重置列表值类型 id
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(DataInput input) throws IOException {
        int typeId = input.readUnsignedByte();
        this.valueTypeId = typeId;
        this.value = new ArrayList<>();
        int length = input.readInt();
        for(int i = 0; i < length; i++) {
            NBTBase nbt = createTag((byte) typeId);
            nbt.read(input);
            add((T) nbt);
        }
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeByte(value.isEmpty() ? 0 : valueTypeId);
        output.writeInt(value.size());
        for(T t : value)
            t.write(output);
    }

    @Override
    @SuppressWarnings("unchecked")
    public NBTTagList<T> clone() {
        List<T> newList = new ArrayList<>();
        for(T value : value)
            newList.add((T) value.clone());
        return new NBTTagList<>(getName(), newList);
    }

    @Override
    public Iterator<T> iterator() {
        return value.iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj)) {
            NBTTagList other = (NBTTagList) obj;
            if(valueTypeId == other.valueTypeId)
                return value.equals(other.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ value.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for(int i = 0; i < size(); i++) {
            if(i != 0)
                builder.append(",");
            builder.append(i).append(":").append(value.get(i));
        }
        return builder.append("]").toString();
    }
}
