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

import com.minecraft.moonlake.protocol.util.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.*;

public class NBTTagCompound extends NBTBase implements Iterable<NBTBase> {

    private Map<String, NBTBase> value;

    public NBTTagCompound(String name) {
        //this(name, new LinkedHashMap<>());
        this(name, new HashMap<>());
    }

    public NBTTagCompound(String name, Map<String, NBTBase> value) {
        super(name);
        this.value = value;
    }

    @Override
    public Map<String, NBTBase> getValue() {
        return new HashMap<>(value);
    }

    public void setValue(Map<String, NBTBase> value) {
        this.value = new HashMap<>(value);
    }

    public NBTBase get(String name) {
        return value.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends NBTBase> T put(T nbt) {
        return (T) value.put(nbt.getName(), nbt);
    }

    public void set(String name, NBTBase nbt) {
        this.value.put(name, nbt);
    }

    public void setByte(String name, byte value) {
        this.value.put(name, new NBTTagByte(name, value));
    }

    public void setByte(String name, int value) {
        setByte(name, (byte) value);
    }

    public void setShort(String name, short value) {
        this.value.put(name, new NBTTagShort(name, value));
    }

    public void setShort(String name, int value) {
        setShort(name, (short) value);
    }

    public void setInteger(String name, int value) {
        this.value.put(name, new NBTTagInteger(name, value));
    }

    public void setLong(String name, long value) {
        this.value.put(name, new NBTTagLong(name, value));
    }

    public void setFloat(String name, float value) {
        this.value.put(name, new NBTTagFloat(name, value));
    }

    public void setDouble(String name, double value) {
        this.value.put(name, new NBTTagDouble(name, value));
    }
    
    public void setByteArray(String name, byte[] value) {
        this.value.put(name, new NBTTagByteArray(name, value));
    }

    public void setString(String name, String value) {
        this.value.put(name, new NBTTagString(name, value));
    }

    public void setBoolean(String name, boolean value) {
        setByte(name, value ? (byte) 1 : 0);
    }

    protected Number getNumber(String name) {
        NBTBase nbt = get(name);
        if(nbt != null && nbt.isNumber()) {
            if(nbt instanceof NBTTagByte)
                return ((NBTTagByte) nbt).getValue();
            else if(nbt instanceof NBTTagShort)
                return ((NBTTagShort) nbt).getValue();
            else if(nbt instanceof NBTTagInteger)
                return ((NBTTagInteger) nbt).getValue();
            else if(nbt instanceof NBTTagLong)
                return ((NBTTagLong) nbt).getValue();
            else if(nbt instanceof NBTTagFloat)
                return ((NBTTagFloat) nbt).getValue();
            else if(nbt instanceof NBTTagDouble)
                return ((NBTTagDouble) nbt).getValue();
        }
        return null;
    }

    public byte getByte(String name) {
        return getByte(name, 0);
    }

    public byte getByte(String name, int def) {
        return getByte(name, (byte) def);
    }

    public byte getByte(String name, byte def) {
        Number value = getNumber(name);
        return value != null ? value.byteValue() : def;
    }

    public short getShort(String name) {
        return getShort(name, 0);
    }

    public short getShort(String name, int def) {
        return getShort(name, (byte) def);
    }

    public short getShort(String name, short def) {
        Number value = getNumber(name);
        return value != null ? value.shortValue() : def;
    }

    public int getInteger(String name) {
        return getInteger(name, 0);
    }

    public int getInteger(String name, int def) {
        Number value = getNumber(name);
        return value != null ? value.intValue() : def;
    }

    public long getLong(String name) {
        return getLong(name, 0);
    }

    public long getLong(String name, long def) {
        Number value = getNumber(name);
        return value != null ? value.longValue() : def;
    }

    public float getFloat(String name) {
        return getFloat(name, 0);
    }

    public float getFloat(String name, float def) {
        Number value = getNumber(name);
        return value != null ? value.floatValue() : def;
    }

    public double getDouble(String name) {
        return getDouble(name, 0);
    }

    public double getDouble(String name, double def) {
        Number value = getNumber(name);
        return value != null ? value.doubleValue() : def;
    }

    public byte[] getByteArray(String name) {
        return getByteArray(name, new byte[0]);
    }

    public byte[] getByteArray(String name, byte[] def) {
        NBTBase nbt = get(name);
        return nbt != null && nbt instanceof NBTTagByteArray ? ((NBTTagByteArray) nbt).getValue() : def;
    }

    public int[] getIntegerArray(String name) {
        return getIntegerArray(name, new int[0]);
    }

    public int[] getIntegerArray(String name, int[] def) {
        NBTBase nbt = get(name);
        return nbt != null && nbt instanceof NBTTagIntegerArray ? ((NBTTagIntegerArray) nbt).getValue() : def;
    }

    public NBTTagList<?> getList(String name) {
        NBTBase nbt = get(name);
        if(nbt != null && nbt instanceof NBTTagList)
            return (NBTTagList<?>) nbt;
        return null;
    }

    public NBTTagCompound getCompound(String name) {
        NBTBase nbt = get(name);
        if(nbt != null && nbt instanceof NBTTagCompound)
            return ((NBTTagCompound) nbt);
        return null;
    }

    public String getString(String name) {
        NBTBase nbt = get(name);
        return nbt != null && nbt instanceof NBTTagString ? ((NBTTagString) nbt).getValue() : null;
    }

    public boolean getBoolean(String name) {
        return getByte(name) != 0;
    }

    public NBTBase remove(String name) {
        return value.remove(name);
    }

    @Override
    public int getTypeId() {
        return 10;
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public boolean contains(String name) {
        return value.containsKey(name);
    }

    public int size() {
        return value.size();
    }

    public void clear() {
        this.value.clear();
    }

    public Set<String> keySet() {
        return value.keySet();
    }

    public Collection<NBTBase> values() {
        return value.values();
    }

    @Override
    public void read(DataInput input) throws IOException {
        List<NBTBase> tagList = new ArrayList<>();
        try {
            NBTBase nbt;
            while((nbt = Util.readNBTTag(input)) != null)
                tagList.add(nbt);
        } catch (EOFException e) {
            throw new IOException("关闭标签 EndTag 不存在.");
        }
        for(NBTBase nbt : tagList)
            put(nbt);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        for(NBTBase nbt : value.values())
            Util.writeNBTTag(output, nbt);
        output.writeByte(0);
    }

    @Override
    public NBTTagCompound clone() {
        Map<String, NBTBase> newMap = new HashMap<>();
        for(Map.Entry<String, NBTBase> entry : value.entrySet())
            newMap.put(entry.getKey(), entry.getValue());
        return new NBTTagCompound(getName(), newMap);
    }

    @Override
    public Iterator<NBTBase> iterator() {
        return values().iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj)) {
            NBTTagCompound other = (NBTTagCompound) obj;
            return value.entrySet().equals(other.value.entrySet());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ value.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        for(Iterator<String> iterator = value.keySet().iterator(); iterator.hasNext();) {
            String key = iterator.next();
            if(builder.length() != 1)
                builder.append(",");
            builder.append(key).append(":").append(value.get(key));
        }
        return builder.append("}").toString();
    }
}
