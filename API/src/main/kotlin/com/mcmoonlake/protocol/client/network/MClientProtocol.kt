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

package com.mcmoonlake.protocol.client.network

import com.mcmoonlake.protocol.api.Minecraft.Companion.DEF_NAME
import com.mcmoonlake.protocol.api.Minecraft.Companion.DEF_UUID
import com.mcmoonlake.protocol.auth.GameProfile
import com.mcmoonlake.protocol.network.MProtocol
import com.mcmoonlake.protocol.network.MProtocolType
import com.mcmoonlake.protocol.network.MProtocolVersion

class MClientProtocol(
        type: MProtocolType,
        version: MProtocolVersion,
        val profile: GameProfile) : MProtocol(type, version) {

    constructor(type: MProtocolType, version: MProtocolVersion, username: String) : this(type, version, GameProfile(null, username))
    constructor(type: MProtocolType, version: MProtocolVersion) : this(type, version, GameProfile(DEF_UUID, DEF_NAME))
}
