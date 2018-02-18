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

package com.mcmoonlake.protocol.api

import com.mcmoonlake.protocol.network.MConnection
import com.mcmoonlake.protocol.network.MProtocol
import java.util.*

interface Minecraft {

    val host: String

    val port: Int

    val protocol: MProtocol

    val connection: MConnection

    companion object {
        val DEF_UUID = UUID.fromString("E2B3CA04-5500-4EBA-8F17-CCA27F58918A")
        const val DEF_NAME = "MoonLakeProtocol"
        const val KEY_PROFILE = "profile"
        const val KEY_AUTH_PROXY = "auth-proxy"
        const val KEY_ACCESS_TOKEN = "access-token"
        const val KEY_PROTOCOL_VER = "protocol-version"
    }
}

interface MinecraftServer : Minecraft {

}
