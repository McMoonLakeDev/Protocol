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

package com.mcmoonlake.protocol.packet

import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

interface PacketEncryption {

    fun getDecryptOutputSize(length: Int): Int

    fun getEncryptOutputSize(length: Int): Int

    @Throws(Exception::class)
    fun decrypt(input: ByteArray, inOffset: Int, inLength: Int, output: ByteArray, outOffset: Int): Int

    @Throws(Exception::class)
    fun encrypt(input: ByteArray, inOffset: Int, inLength: Int, output: ByteArray, outOffset: Int): Int
}

class PacketEncryptionAES(
        key: Key
) : PacketEncryption {

    private val inCipher = Cipher.getInstance("AES/CFB8/NoPadding")
    private val outCipher = Cipher.getInstance("AES/CFB8/NoPadding")

    init {
        inCipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(key.encoded))
        outCipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(key.encoded))
    }

    override fun getDecryptOutputSize(length: Int): Int
            = inCipher.getOutputSize(length)

    override fun getEncryptOutputSize(length: Int): Int
            = outCipher.getOutputSize(length)

    override fun decrypt(input: ByteArray, inOffset: Int, inLength: Int, output: ByteArray, outOffset: Int): Int
            = inCipher.update(input, inOffset, inLength, output, outOffset)

    override fun encrypt(input: ByteArray, inOffset: Int, inLength: Int, output: ByteArray, outOffset: Int): Int
            = outCipher.update(input, inOffset, inLength, output, outOffset)
}
