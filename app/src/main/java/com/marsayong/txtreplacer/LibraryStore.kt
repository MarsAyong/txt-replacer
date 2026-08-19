package com.marsayong.txtreplacer

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 本地持久化：多个命名规则库
 * 用 SharedPreferences 存 JSON 字符串，重启不丢
 */
object LibraryStore {
    private const val PREFS = "libraries_prefs"
    private const val KEY_LIBS = "libraries_json"
    private const val KEY_CURRENT = "current_library_index"

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    fun loadLibraries(context: Context): MutableList<RuleLibrary> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_LIBS, null) ?: return defaultLibraries()
        return try {
            json.decodeFromString<MutableList<RuleLibrary>>(raw)
        } catch (e: Exception) {
            defaultLibraries()
        }
    }

    fun saveLibraries(context: Context, libs: List<RuleLibrary>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LIBS, json.encodeToString(libs)).apply()
    }

    fun currentIndex(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CURRENT, 0)
    }

    fun saveCurrentIndex(context: Context, index: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_CURRENT, index).apply()
    }

    /** 首次启动的示例库 */
    private fun defaultLibraries(): MutableList<RuleLibrary> {
        return mutableListOf(
            RuleLibrary(
                name = "戒色1号库",
                rules = mutableListOf(
                    Rule("jiese", "戒色"),
                    Rule("mogui", "魔鬼"),
                    Rule("weisuo", "猥琐"),
                    Rule("xieyin", "邪淫")
                )
            ),
            RuleLibrary(name = "空白示例库", rules = mutableListOf())
        )
    }
}
