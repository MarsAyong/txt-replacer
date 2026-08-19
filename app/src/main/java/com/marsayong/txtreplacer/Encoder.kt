package com.marsayong.txtreplacer

import com.ibm.icu.charset.CharsetICU

/**
 * 缂栫爜宸ュ叿锛氫紭鍏堢敤 ICU4J锛堟敮鎸?GBK/GB2312/Big5锛夛紝鍥為€€鍒板钩鍙?Charset锛圲TF-8 绛夛級
 */
object Encoder {
    fun charsetName(display: String): String = when (display) {
        "UTF-16LE" -> "UTF-16LE"
        else -> display
    }

    /** 鎶婁竴涓瓧绗︿覆鎸夋寚瀹氱紪鐮佺紪鐮佹垚瀛楄妭 */
    fun encode(text: String, display: String): ByteArray {
        val name = charsetName(display)
        return try {
            // ICU 浼樺厛锛岃兘澶勭悊 GBK/GB2312/Big5
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
            // 鏈€鍚庡洖閫€ UTF-8
            text.toByteArray(Charsets.UTF_8)
        }
    }

    /** 鎶婂瓧鑺傛寜鎸囧畾缂栫爜瑙ｇ爜鎴愬瓧绗︿覆 */
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
