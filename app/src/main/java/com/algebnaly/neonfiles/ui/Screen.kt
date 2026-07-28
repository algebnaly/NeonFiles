package com.algebnaly.neonfiles.ui

import kotlinx.serialization.Serializable

sealed interface Screen {
    // 列表主界面
    @Serializable
    data object FileBrowser : Screen

    // 存储位置表单界面：locationId = null 表示新增，传递具体 Int 表示编辑
    @Serializable
    data class NfsLocation(val locationId: Int? = null) : Screen
}