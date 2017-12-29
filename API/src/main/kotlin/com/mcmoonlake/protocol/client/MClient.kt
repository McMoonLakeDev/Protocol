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

package com.mcmoonlake.protocol.client

import com.mcmoonlake.protocol.api.Minecraft
import com.mcmoonlake.protocol.client.network.MClientConnection
import com.mcmoonlake.protocol.client.network.MClientConnectionFactory
import com.mcmoonlake.protocol.client.network.MClientProtocol

class MClient(
        override val host: String,
        override val port: Int,
        override val protocol: MClientProtocol,
        factory: MClientConnectionFactory) : Minecraft {

    private val clientConnection = factory.createClientConnection(this)

    override val connection: MClientConnection
        get() = clientConnection as MClientConnection
}
