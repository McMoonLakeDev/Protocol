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

import com.mcmoonlake.protocol.network.MConnectionListener
import com.mcmoonlake.protocol.network.ProtocolEvent
import com.mcmoonlake.protocol.wrapper.ServerInfo

interface MClientConnectionEvent : ProtocolEvent {

    fun call(listener: MClientConnectionListener)
}

abstract class MClientConnectionEventAbstract(
        val connection: MClientConnection
) : MClientConnectionEvent {

    override fun call(listener: MConnectionListener) {
        if(listener is MClientConnectionListener)
            call(listener)
    }
}

class ConnectedServerEvent(
        connection: MClientConnection
) : MClientConnectionEventAbstract(connection),
        MClientConnectionEvent {

    override fun call(listener: MClientConnectionListener) {
        listener.onConnectedServer(this)
    }
}

class ServerPingEvent(
        connection: MClientConnection,
        val info: ServerInfo
) : MClientConnectionEventAbstract(connection),
        MClientConnectionEvent {

    override fun call(listener: MClientConnectionListener) {
        listener.onServerPingEvent(this)
    }
}
