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

import com.google.gson.JsonObject
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL
import java.util.*

object Authentication {

    const val MOJANG_JOIN = "https://sessionserver.mojang.com/session/minecraft/join"

    @JvmStatic
    @JvmName("joinServerRequest")
    @JvmOverloads
    fun joinServerRequest(proxy: Proxy = Proxy.NO_PROXY, url: String = MOJANG_JOIN, accessToken: String, id: UUID, serverId: String) {
        val post = makeJsonObject(accessToken, id, serverId)
        val postData = post.toString().toByteArray(Charsets.UTF_8)
        val connection = URL(url).openConnection(proxy) as HttpURLConnection
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.setRequestProperty("Content-Length", postData.size.toString())
        connection.setRequestProperty("User-agent", "MoonLake Protocol by lgou2w.")
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.useCaches = false
        connection.doInput = true
        connection.doOutput = true
        connection.outputStream.use { it.write(postData) }
        connection.responseCode == 204
    }

    private fun makeJsonObject(accessToken: String, id: UUID, serverId: String): JsonObject {
        val json = JsonObject()
        json.addProperty("accessToken", accessToken)
        json.addProperty("selectedProfile", id.toString().replace("-", ""))
        json.addProperty("serverId", serverId)
        return json
    }
}
