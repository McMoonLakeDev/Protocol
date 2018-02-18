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

import java.security.PublicKey
import java.security.Signature
import java.util.*

data class Property(
        val name: String,
        val value: String,
        val signature: String?) {

    fun hasSignature()
            = signature != null

    fun verifySignature(publicKey: PublicKey): Boolean {
        if(signature == null)
            return false
        return try {
            val sign = Signature.getInstance(publicKey.algorithm)
            sign.initVerify(publicKey)
            sign.update(value.toByteArray())
            sign.verify(Base64.getDecoder().decode(signature.toByteArray(Charsets.UTF_8)))
        } catch(e: Exception) {
            false
        }
    }
}
