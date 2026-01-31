package com.twelfth.data.local

import kotlin.random.Random

/**
 * 访客ID管理器（跨平台）
 * 
 * 负责：
 * - FUN-001: 检测访客ID存在性
 * - FUN-002: 生成访客ID
 */
class GuestIdManager(private val localStorage: ILocalStorage) {
    
    companion object {
        // 存储键
        private const val STORAGE_KEY = "GUEST_ID"
        
        // 访客ID格式正则：GUEST_<13位时间戳>_<8位随机字符>
        private val ID_PATTERN = Regex("^GUEST_\\d{13}_[a-z0-9]{8}$")
        
        // 随机字符集
        private const val RANDOM_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789"
    }
    
    /**
     * FUN-001: 检测访客ID存在性
     * 
     * 从本地存储读取访客ID，验证格式，如果格式无效则清除并返回 null
     * 
     * @return 有效的访客ID，或 null（不存在或格式无效）
     */
    fun detectGuestId(): String? {
        return try {
            val guestId = localStorage.getString(STORAGE_KEY)
            
            // 如果不存在，返回 null
            if (guestId.isNullOrEmpty()) {
                return null
            }
            
            // 验证格式
            if (validateGuestId(guestId)) {
                guestId
            } else {
                // 格式无效，清除数据
                localStorage.remove(STORAGE_KEY)
                null
            }
        } catch (e: Exception) {
            // 读取失败，记录日志并返回 null
            println("Error detecting guest ID: ${e.message}")
            null
        }
    }
    
    /**
     * FUN-002: 生成访客ID
     * 
     * 生成格式为 GUEST_<timestamp>_<random> 的访客ID，并立即存储到本地
     * 
     * @return 新生成的访客ID
     * @throws StorageException 存储失败时抛出
     */
    fun generateGuestId(): String {
        // 生成时间戳（13位毫秒）
        val timestamp = getCurrentTimestamp()
        
        // 生成8位随机字符串
        val random = generateRandomString(8)
        
        // 组合访客ID
        val guestId = "GUEST_${timestamp}_$random"
        
        // 立即存储
        try {
            localStorage.setString(STORAGE_KEY, guestId)
        } catch (e: Exception) {
            throw StorageException("Failed to store guest ID", e)
        }
        
        return guestId
    }
    
    /**
     * 验证访客ID格式
     * 
     * @param id 待验证的访客ID
     * @return 格式是否有效
     */
    fun validateGuestId(id: String): Boolean {
        return ID_PATTERN.matches(id)
    }
    
    /**
     * 生成随机字符串
     * 
     * @param length 字符串长度
     * @return 随机字符串（字符集：[a-z0-9]）
     */
    private fun generateRandomString(length: Int): String {
        return (1..length)
            .map { Random.nextInt(0, RANDOM_CHARS.length) }
            .map(RANDOM_CHARS::get)
            .joinToString("")
    }
    
    /**
     * 获取当前时间戳（13位毫秒）
     * 
     * @return 毫秒级时间戳
     */
    private fun getCurrentTimestamp(): Long {
        return currentTimeMillis()
    }
}

/**
 * 获取当前时间戳（跨平台）
 * 需要在各平台实现
 */
expect fun currentTimeMillis(): Long
