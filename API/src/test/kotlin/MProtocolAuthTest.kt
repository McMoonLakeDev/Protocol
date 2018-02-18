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

import com.google.gson.GsonBuilder
import com.mcmoonlake.protocol.auth.GameProfile
import com.mcmoonlake.protocol.fromJson
import com.mcmoonlake.protocol.util.UUIDSerializer
import org.junit.Test
import java.util.*

class MProtocolAuthTest {

    @Test
    fun testGameProfile() {
        val gson = GsonBuilder()
                .registerTypeAdapter(UUID::class.java, UUIDSerializer())
                .setPrettyPrinting()
                .create()

        val json = """
                {
                    "isLegacy": true,
                    "id": "3336203d1da74e8aa0e1e18c8a214519",
                    "name": "lgou2w",
                    "properties": [
                        {
                            "name": "test",
                            "value": "value"
                        }
                    ]
                }
            """
        val profile: GameProfile = gson.fromJson(json)
        println(profile)
    }
}
