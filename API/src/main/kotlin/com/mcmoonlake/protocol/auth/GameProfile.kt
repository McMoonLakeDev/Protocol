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

package com.mcmoonlake.protocol.auth

import java.util.*

// TODO Minecraft Auth

data class GameProfile(
        val id: UUID?,
        val name: String?) {

    val properties: MutableList<Property> = ArrayList()
    val isLegacy: Boolean = false

    constructor(id: String?, name: String)
            : this(if(id != null && id != "") UUID.fromString(id) else null, name)

    fun getProperty(name: String): Property?
            = properties.find { it.name == name }

    override fun toString(): String {
        return "GameProfile(id=$id, name=$name, properties=$properties, isLegacy=$isLegacy)"
    }
}
