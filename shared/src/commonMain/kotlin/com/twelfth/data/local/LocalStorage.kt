package com.twelfth.data.local

/**
 * 本地存储平台实现（expect/actual 模式）
 * 
 * Android 实现: SharedPreferences
 * iOS 实现: UserDefaults (P2)
 */
expect class LocalStorage : ILocalStorage {
    override fun getString(key: String): String?
    override fun setString(key: String, value: String)
    override fun remove(key: String)
}
