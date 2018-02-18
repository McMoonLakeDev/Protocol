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

import com.mcmoonlake.protocol.api.Minecraft
import com.mcmoonlake.protocol.client.MClient
import com.mcmoonlake.protocol.network.MConnection
import com.mcmoonlake.protocol.network.MConnectionFactoryAbstract
import java.net.Proxy

open class MClientConnectionFactory(
        proxy: Proxy?
) : MConnectionFactoryAbstract(proxy) {

    override fun createClientConnection(mc: Minecraft): MConnection
            = MClientConnection(mc.host, mc.port, (mc as MClient).protocol, mc, proxy)

    override fun createServerConnection(mc: Minecraft): MConnection
            = throw UnsupportedOperationException()
}
