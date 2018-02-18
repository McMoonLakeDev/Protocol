/*
 * Copyright (C) 2016-Present The MoonLake (mcmoonlake@hotmail.com)
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

package com.mcmoonlake.protocol.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.*

class UUIDSerializer : TypeAdapter<UUID>() {

    override fun write(jsonWriter: JsonWriter?, value: UUID?) {
        jsonWriter?.value(fromUUID(value))
    }

    override fun read(jsonReader: JsonReader?): UUID? {
        return fromString(jsonReader?.nextString())
    }

    companion object {

        /**
         * Converts the specified UUID object to a 32-bit string UUID.
         *
         * @param value UUID
         * @return 32-bit string UUID
         */
        @JvmStatic
        @JvmName("fromUUID")
        fun fromUUID(value: UUID?): String {
            return value?.toString()?.replace("-", "") ?: ""
        }

        /**
         * Converts the specified string UUID object to a UUID object.
         *
         * @param value String UUID
         * @return UUID object
         */
        @JvmStatic
        @JvmName("fromString")
        fun fromString(value: String?): UUID? {
            return if (value == null || value == "") null
            else UUID.fromString(value.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(), "$1-$2-$3-$4-$5"))
        }
    }
}
