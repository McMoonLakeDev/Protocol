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

@file:JvmName("ProtocolAPI")

package com.mcmoonlake.protocol

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import com.mcmoonlake.protocol.api.Valuable
import com.mcmoonlake.protocol.chat.ChatColor
import com.mcmoonlake.protocol.util.Enums
import java.io.Reader

@JvmOverloads
fun <T> T?.notNull(message: String = "验证的对象值为 null 时异常."): T
        = this ?: throw IllegalArgumentException(message)

fun <T, C: Comparable<T>> C.isLater(other: T): Boolean
        = compareTo(other) > 0

fun <T, C: Comparable<T>> C.isOrLater(other: T): Boolean
        = compareTo(other) >= 0

fun <T, C: Comparable<T>> C.isRange(min: T, max: T): Boolean
        = compareTo(min) > 0 && compareTo(max) < 0

fun <T, C: Comparable<T>> C.isOrRange(min: T, max: T): Boolean
        = compareTo(min) >= 0 && compareTo(max) <= 0

@JvmOverloads
inline fun <V, reified T> ofValuable(value: V?, def: T? = null): T? where T: Enum<T>, T: Valuable<V>
        = Enums.ofValuable(T::class.java, value, def)

@Throws(IllegalArgumentException::class)
inline fun <V, reified T> ofValuableNotNull(value: V?): T where T: Enum<T>, T: Valuable<V>
        = ofValuable(value) ?: throw IllegalArgumentException("未知的枚举 ${T::class.java.canonicalName} 类型值: $value")

/** Gson Extension function */

inline fun <reified T> Gson.fromJson(json: String): T
        = fromJson(json, T::class.java)

inline fun <reified T> Gson.fromJson(reader: Reader): T
        = fromJson(reader, T::class.java)

inline fun <reified T> Gson.fromJson(jsonReader: JsonReader): T
        = fromJson(jsonReader, T::class.java)

inline fun <reified T> Gson.fromJson(jsonElement: JsonElement): T
        = fromJson(jsonElement, T::class.java)

fun String.toColor(): String
        = ChatColor.translateAlternateColorCodes('&', this)

fun String.toColor(altColorChar: Char): String
        = ChatColor.translateAlternateColorCodes(altColorChar, this)

fun Array<out String>.toColor(): Array<out String>
        = toList().map { it.toColor() }.toTypedArray()

fun Array<out String>.toColor(altColorChar: Char): Array<out String>
        = toList().map { it.toColor(altColorChar) }.toTypedArray()

fun Iterable<String>.toColor(): List<String>
        = map { it.toColor() }.let { ArrayList(it) }

fun Iterable<String>.toColor(altColorChar: Char): List<String>
        = map { it.toColor(altColorChar) }

fun String.stripColor(): String
        = ChatColor.stripColor(this)

fun Array<out String>.stripColor(): Array<out String>
        = toList().map { it.stripColor() }.toTypedArray()

fun Iterable<String>.stripColor(): List<String>
        = map { it.stripColor() }
