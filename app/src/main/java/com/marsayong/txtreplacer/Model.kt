package com.marsayong.txtreplacer

import kotlinx.serialization.Serializable

/** 一条替换规则：把 from 替换为 to */
@Serializable
data class Rule(
    val from: String = "",
    val to: String = ""
)

/** 一个命名规则库（配置），如「戒色1号库」，内含若干条替换规则 */
@Serializable
data class RuleLibrary(
    var name: String = "未命名库",
    var rules: MutableList<Rule> = mutableListOf()
)
