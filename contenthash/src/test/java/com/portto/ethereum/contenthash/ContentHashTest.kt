package com.portto.ethereum.contenthash

import org.junit.Assert
import org.junit.Test

class ContentHashTest {

    @Test
    fun decodeContentHash() {
        val rawContentHash =
            "e30101701220c27f5a54fefc77ff1b2980461286628736f3f410f7e446da3266cdfff3d049c6"
        val decodedContentHash = ContentHash.decode(rawContentHash)
        Assert.assertEquals(decodedContentHash, "QmbRtS9dp2zqARv7v7ak2reJp3zE5NRkvEpHsc48Hjo9MF")
        val type = ContentHash.getCodec(rawContentHash)
        Assert.assertEquals(type, "ipfs-ns")
        val v1Base32 = ContentHash.cidV0ToV1Base32(decodedContentHash)
        Assert.assertEquals(v1Base32, "bafybeigcp5nfj7x4o77rwkmaiyjimyuhg3z7iehx4rdnumtgzx77hucjyy")
    }

    @Test
    fun encodeContentHash() {
        val ipfs = "QmRAQB6YaCyidP37UdDnjFY5vQuiBrcqdyoW1CuDgwxkD4"
        val hex = ContentHash.encode("ipfs-ns", ipfs)
        Assert.assertEquals(hex,
            "e3010170122029f2d17be6139079dc48696d1f582a8530eb9805b561eda517e22a892c7e3f1f")
    }
}