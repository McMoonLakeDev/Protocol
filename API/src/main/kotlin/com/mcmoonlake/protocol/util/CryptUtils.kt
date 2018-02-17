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

import java.io.UnsupportedEncodingException
import java.security.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object CryptUtils {

    @JvmStatic
    @JvmName("generateSharedKey")
    fun generateSharedKey(): SecretKey = try {
        val gen = KeyGenerator.getInstance("AES")
        gen.init(128)
        gen.generateKey()
    } catch(e: NoSuchAlgorithmException) {
        throw Error("Unable to generate shared secret.", e.cause ?: e)
    }

    @JvmStatic
    @JvmName("encrypt")
    fun encrypt(key: Key, data: ByteArray): ByteArray
            = runEncryption(Cipher.ENCRYPT_MODE, key, data)

    @JvmStatic
    @JvmName("decrypt")
    fun decrypt(key: Key, data: ByteArray): ByteArray
            = runEncryption(Cipher.DECRYPT_MODE, key, data)

    @JvmStatic
    @JvmName("generateServerIdHash")
    fun generateServerIdHash(serverId: String, publicKey: PublicKey, secretKey: SecretKey): ByteArray = try {
        encrypt("SHA-1", serverId.toByteArray(Charsets.ISO_8859_1), secretKey.encoded, publicKey.encoded)
    } catch(e: UnsupportedEncodingException) {
        throw Error("Unable to generate server id hash value.", e.cause ?: e)
    }

    private fun encrypt(algorithm: String, vararg data: ByteArray): ByteArray = try {
        val digest = MessageDigest.getInstance(algorithm)
        data.forEach { digest.update(it) }
        digest.digest()
    } catch(e: NoSuchAlgorithmException) {
        throw Error("Unable to encrypt data.", e.cause ?: e)
    }

    private fun runEncryption(mode: Int, key: Key, data: ByteArray): ByteArray = try {
        val cipher = Cipher.getInstance(key.algorithm)
        cipher.init(mode, key)
        cipher.doFinal(data)
    } catch(e: GeneralSecurityException) {
        throw Error("Unable to run encryption.", e.cause ?: e)
    }
}
