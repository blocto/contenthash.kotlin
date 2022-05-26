/*
 * Copyright (C) 2022 portto Co., Ltd.
 *
 * Created by Kihon on 2022/5/27
 */

package com.portto.ethereum.contenthash

import io.ipfs.cid.Cid
import io.ipfs.multibase.Base58
import io.ipfs.multibase.Multibase
import io.ipfs.multihash.Multihash
import io.matthewnelson.component.encoding.base32.encodeBase32
import org.komputing.khex.extensions.hexToByteArray
import org.komputing.khex.extensions.toHexString
import org.komputing.khex.extensions.toNoPrefixHexString
import org.komputing.khex.model.HexString
import java.nio.ByteBuffer

class ContentHash {

    companion object {
        /**
         * Decode a Content Hash.
         * @param {String} hash an hex string containing a content hash
         * @return {String} the decoded content
         */
        fun decode(contentHash: String): String {
            val buffer = HexString(contentHash).hexToByteArray()
            val codec = Multicodec.getCodec(ByteBuffer.wrap(buffer))
            val value = Multicodec.removePrefix(ByteBuffer.wrap(buffer))
            val profile = getProfile(codec)
            return profile.decode(value.array())
        }

        /**
         * General purpose encoding function
         * @param {String} codec
         * @param {String} value
         */
        fun encode(codec: String, value: String): String {
            val profile = getProfile(codec)
            val encodedValue = profile.encode(value)
            return Multicodec.addPrefix(codec, ByteBuffer.wrap(encodedValue)).array()
                .toNoPrefixHexString()
        }

        private fun getProfile(codec: String) = when (codec) {
            "swarm-ns" -> SwarmNs
            "ipfs-ns", "ipns-ns" -> IpfsNs
            else -> Default
        }

        /**
         * Extract the codec of a content hash
         * @param {String} hash hex string containing a content hash
         * @return {String} the extracted codec
         */
        fun getCodec(hash: String): String? {
            val buffer = HexString(hash).hexToByteArray()
            return Multicodec.getCodec(ByteBuffer.wrap(buffer))
        }

        /**
         * Take any ipfsHash and convert it to a CID v1 encoded in base32.
         * @param {String} ipfsHash a regular ipfs hash either a cid v0 or v1 (v1 will remain unchanged)
         * @return {String} the resulting ipfs hash as a cid v1
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun cidV0ToV1Base32(ipfsHash: String): String {
            val cid = Cid.decode(ipfsHash)
            if (cid.version == 0L) {
                val multihash = Base58.decode(ipfsHash)
                multihash.toHexString()
                val result =
                    (ubyteArrayOf(0x1u, cid.codec.type.toUByte()) + multihash.toUByteArray()).toByteArray()
//                cid = Cid.buildCidV1(cid.codec, cid.type, multihash)
                return Multibase.encode(Multibase.Base.Base32, result)
            }
            return cid.toBytes().encodeBase32()
        }
    }

    /**
     * list of known encoding/decoding for a given codec,
     * `encode` should be chosen among the `encodes` functions
     * `decode` should be chosen among the `decodes` functions
     */
    private sealed class Profiles {

        /**
         * list of known decoding,
         * decoding should be a function that takes a `ByteArray` input,
         * and return a `String` result
         */
        fun decode(value: ByteArray): String {
            return when (this) {
                is SwarmNs -> hexMultiHash(value)
                is IpfsNs, is IpnsNs -> b58MultiHash(value)
                is Default -> utf8(value)
            }
        }

        /**
         * list of known encoding,
         * encoding should be a function that takes a `String` input,
         * and return a `ByteArray` result
         */
        fun encode(value: String): ByteArray {
            return when (this) {
                is SwarmNs -> {
                    val multihash =
                        Multihash(Multihash.Type.keccak_256, HexString(value).hexToByteArray())
                    // swarm-manifes [0xfa]
                    // Cid.build(1, Cid.Codec.EthereumTx, multihash).toBytes()
                    TODO()
                }
                is IpfsNs, is IpnsNs -> {
                    val multihash = Multihash.fromBase58(value)
                    Cid.build(1, Cid.Codec.DagProtobuf, multihash).toBytes()
                }
                is Default -> value.toByteArray()
            }
        }

        private fun hexMultiHash(value: ByteArray): String {
            val cid = Cid.deserialize(value)
            return cid.hash.toHexString()
        }

        private fun b58MultiHash(value: ByteArray): String {
            val cid = Cid.cast(value)
            val multihash = Multicodec.removePrefix(ByteBuffer.wrap(cid.toBytes().drop(1).toByteArray()))
            return Base58.encode(multihash.array())
        }

        private  fun utf8(value: ByteArray): String {
            return value.toString()
        }

    }

    private object SwarmNs : Profiles()
    private object IpfsNs : Profiles()
    private object IpnsNs : Profiles()
    private object Default : Profiles()

}