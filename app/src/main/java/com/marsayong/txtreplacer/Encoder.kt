package com.marsayong.txtreplacer

import com.ibm.icu.charset.CharsetICU

/**
 * 编码工具：优先用 ICU4J（支持 GBK/GB2312/Big5），回退到平台 Charset（UTF-8 等）
 */
object Encoder {
    fun charsetName(display: String): String = when (display) {
        "UTF-16LE" -> "UTF-16LE"
        else -> display
    }

    /** 把一个字符串按指定编码编码成字节 */
    fun encode(text: String, display: String): ByteArray {
        val name = charsetName(display)
        return try {
            // ICU 优先，能处理 GBK/GB2312/Big5
            charsetICU(name).encode(text).array().copyOf()
        } catch (e: Exception) {
            // 回退到平台
            text.toByteArray(Charsets.UTF_8)
        }
    }

    /** 把字节按指定编码解码成字符串 */
    fun decode(bytes: ByteArray, display: String): String {
        val name = charsetName(display)
        return try {
            charsetICU(name).decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        } catch (e: Exception) {
            try {
                String(bytes, java.nio.charset.Charset.forName(name))
            } catch (e2: Exception) {
                String(bytes, Charsets.UTF_8)
            }
        }
    }

    private fun charsetICU(name: String): CharsetICU {
        return com.ibm.icu.charset.Charset.forNameICU(name) as CharsetICU
    }
}
