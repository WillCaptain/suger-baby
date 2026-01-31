package com.twelfth.data.local

/**
 * 本地存储接口（用于跨平台抽象和测试）
 */
interface ILocalStorage {
    /**
     * 读取字符串
     * @param key 存储键
     * @return 存储的值，不存在则返回 null
     */
    fun getString(key: String): String?
    
    /**
     * 保存字符串
     * @param key 存储键
     * @param value 存储值
     * @throws StorageException 存储失败时抛出
     */
    fun setString(key: String, value: String)
    
    /**
     * 移除键值对
     * @param key 存储键
     */
    fun remove(key: String)
}
