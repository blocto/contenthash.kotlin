package com.portto.ethereum.contenthash

import io.ipfs.multicodec.VarInt
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Kotlin Implementation of multiformats/multicodec (https://github.com/multiformats/multicodec)
 * @author by Kihon(portto)@2022/05/27
 *
 * Original Java Implementation
 * @author Aliabbas Merchant
 * @version 1.0
 * @since 2019-02-01
 *
 */
object Multicodec {
    /*
    A map to store the codec as key(string) and the corresponding prefix as value(integer)
     */
    private val nameTable = mutableMapOf<String, Int>()

    /*
    A map to store the prefix as key(integer) and the corresponding codec as value(string)
     */
    private val codeTable = mutableMapOf<Int, String>()

    /**
     * Returns the prefix(multicodec identifier) corresponding to the multicodec name
     *
     * @param multicodec The multicodec name
     * @return The prefix, encoded in [VarInt] format
     * @throws IllegalArgumentException If an unsupported multicodec name is passed
     */
    @Throws(IllegalArgumentException::class)
    fun getPrefix(multicodec: String): ByteBuffer {
        return if (nameTable.containsKey(multicodec)) {
            VarInt.encodeVarInt(nameTable[multicodec]!!)
        } else {
            throw IllegalArgumentException("The $multicodec is not supported")
        }
    }

    /**
     * Checks if the codec is a valid/supported multicodec or not
     *
     * @param codecName The multicodec name
     * @return True if the codecName is a supported multicodec, else false
     */
    fun isCodec(codecName: String): Boolean {
        return nameTable.containsKey(codecName)
    }

    /**
     * Returns the prefix(multicodec identifier) of the prefixed data
     *
     * @param prefixedData Data prefixed with the multicodec identifier
     * @return The prefix, encoded in [VarInt] format
     */
    fun extractPrefix(prefixedData: ByteBuffer): ByteBuffer {
        return VarInt.encodeVarInt(extractPrefixInt(prefixedData))
    }

    /**
     * Returns the prefix(multicodec identifier) of the prefixed data
     *
     * @param prefixedData Data prefixed with the multicodec identifier
     * @return The prefix, encoded in integer format
     */
    private fun extractPrefixInt(prefixedData: ByteBuffer): Int {
        return VarInt.decodeVarInt(prefixedData)
    }

    /**
     * Returns the data, after prefixing it with the multicodec identifier
     *
     * @param multicodec Name of the multicodec with which the data has to be prefixed
     * @param data       The data, which needs prefixing
     * @return The prefixed data
     * @throws IllegalArgumentException If an unsupported multicodec name is passed
     */
    @Throws(IllegalArgumentException::class)
    fun addPrefix(multicodec: String, data: ByteBuffer): ByteBuffer {
        return if (nameTable.containsKey(multicodec)) {
            val varInt =
                VarInt.encodeVarInt(nameTable[multicodec]!!)
            var b = ByteBuffer.allocate(data.limit() + varInt.limit())
            // b.put(varInt);
            for (i in 0 until varInt.limit()) {
                b = b.put(varInt[i])
            }
            // b.put(data);
            for (i in 0 until data.limit()) {
                b = b.put(data[i])
            }
            b
        } else {
            throw IllegalArgumentException("The $multicodec is not supported")
        }
    }

    /**
     * Returns the data after removing the multicodec identifier
     *
     * @param prefixedData The data, prefixed with a multicodec identifier
     * @return The prefix-removed data
     */
    fun removePrefix(prefixedData: ByteBuffer): ByteBuffer {
        val prefix = extractPrefix(prefixedData)
        val ans = ByteBuffer.allocate(prefixedData.limit() - prefix.limit())
        for (i in prefix.limit() until prefixedData.limit()) {
            ans.put(prefixedData[i])
        }
        return ans
    }

    /**
     * Returns the multicodec name, with which the data has been prefixed
     *
     * @param prefixedData The data, prefixed with a multicodec identifier
     * @return The multicodec name
     * @throws IllegalArgumentException If the data was prefixed with an unsupported multicodec
     */
    @Throws(IllegalArgumentException::class)
    fun getCodec(prefixedData: ByteBuffer): String {
        val code = extractPrefixInt(prefixedData)
        return codeTable[code]
            ?: throw IllegalArgumentException("The code $code is not found in the Codec Table")
    }

    /*
    Static block to read the codecs from the 'table.csv' file and store them in the maps
     */
    init {
        try {
            val inputStream = Multicodec::class.java.classLoader.getResourceAsStream("table.csv")
            inputStream?.let {
                for (line in inputStream.reader().readLines()) {
                    if (line.trim { it <= ' ' }.isNotEmpty()) {
                        val attributes = line.split(",")
                        val codec: String =
                            attributes[0].trim { it <= ' ' }
                        if (attributes[2].trim { it <= ' ' }.startsWith("0x")) {
                            val code: Int = attributes[2].trim { it <= ' ' }
                                .substring(2).toInt(16)
                            nameTable[codec] = code
                            codeTable[code] = codec
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}