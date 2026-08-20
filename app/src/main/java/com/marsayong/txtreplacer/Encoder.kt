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
            val ch = try {
                CharsetICU.forNameICU(name)
            } catch (e: Exception) {
                null
            }
            if (ch != null) {
                ch.encode(text).array().copyOf()
            } else {
                text.toByteArray(java.nio.charset.Charset.forName(name))
            }
        } catch (e: Exception) {
            // 最后回退 UTF-8
            text.toByteArray(Charsets.UTF_8)
        }
    }

    /** 把字节按指定编码解码成字符串 */
    fun decode(bytes: ByteArray, display: String): String {
        val name = charsetName(display)
        return try {
            val ch = try {
                CharsetICU.forNameICU(name)
            } catch (e: Exception) {
                null
            }
            if (ch != null) {
                ch.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
            } else {
                String(bytes, java.nio.charset.Charset.forName(name))
            }
        } catch (e: Exception) {
            String(bytes, Charsets.UTF_8)
        }
    }
}
