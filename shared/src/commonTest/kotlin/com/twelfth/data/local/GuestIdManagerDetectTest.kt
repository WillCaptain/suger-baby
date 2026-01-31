package com.twelfth.data.local

import kotlin.test.*

/**
 * FUN-001: 检测访客ID存在性 - 契约测试
 * 
 * 测试覆盖：
 * - AC-01: 有效访客ID存在
 * - AC-02: 无访客ID
 * - AC-03: 格式无效
 * - AC-04: 存储异常
 */
class GuestIdManagerDetectTest {
    
    private lateinit var mockStorage: MockLocalStorage
    private lateinit var guestIdManager: GuestIdManager
    
    @BeforeTest
    fun setup() {
        mockStorage = MockLocalStorage()
        guestIdManager = GuestIdManager(mockStorage)
    }
    
    /**
     * AC-01: Given 本地存储中存在有效访客ID，When 调用 detectGuestId()，Then 返回该访客ID
     */
    @Test
    fun test_FUN_001_AC_01_valid_guest_id_exists() {
        // Given: 存储中有有效的访客ID
        val validGuestId = "GUEST_1706543210123_a3f9c2e1"
        mockStorage.setString("GUEST_ID", validGuestId)
        
        // When: 调用检测方法
        val result = guestIdManager.detectGuestId()
        
        // Then: 返回该访客ID
        assertEquals(validGuestId, result)
    }
    
    /**
     * AC-02: Given 本地存储中不存在访客ID，When 调用 detectGuestId()，Then 返回 null
     */
    @Test
    fun test_FUN_001_AC_02_no_guest_id() {
        // Given: 存储为空（默认状态）
        
        // When: 调用检测方法
        val result = guestIdManager.detectGuestId()
        
        // Then: 返回 null
        assertNull(result)
    }
    
    /**
     * AC-03: Given 本地存储中访客ID格式无效，When 调用 detectGuestId()，Then 返回 null 并清除无效数据
     */
    @Test
    fun test_FUN_001_AC_03_invalid_format() {
        // Given: 存储中有格式无效的ID
        val invalidGuestId = "invalid_id_format"
        mockStorage.setString("GUEST_ID", invalidGuestId)
        
        // When: 调用检测方法
        val result = guestIdManager.detectGuestId()
        
        // Then: 返回 null
        assertNull(result)
        
        // And: 无效数据已被清除
        assertNull(mockStorage.getString("GUEST_ID"))
    }
    
    /**
     * AC-04: Given 读取本地存储发生异常，When 调用 detectGuestId()，Then 返回 null
     */
    @Test
    fun test_FUN_001_AC_04_storage_error() {
        // Given: 存储会抛出异常
        mockStorage.simulateReadError = true
        
        // When: 调用检测方法
        val result = guestIdManager.detectGuestId()
        
        // Then: 返回 null（不崩溃）
        assertNull(result)
    }
    
    /**
     * 边缘情况：空字符串
     */
    @Test
    fun test_empty_string_returns_null() {
        // Given: 存储中是空字符串
        mockStorage.setString("GUEST_ID", "")
        
        // When
        val result = guestIdManager.detectGuestId()
        
        // Then
        assertNull(result)
    }
}

/**
 * Mock LocalStorage 实现
 */
class MockLocalStorage : ILocalStorage {
    private val storage = mutableMapOf<String, String>()
    var simulateReadError = false
    var simulateWriteError = false
    
    override fun getString(key: String): String? {
        if (simulateReadError) {
            throw RuntimeException("Simulated read error")
        }
        return storage[key]
    }
    
    override fun setString(key: String, value: String) {
        if (simulateWriteError) {
            throw StorageException("Simulated write error")
        }
        storage[key] = value
    }
    
    override fun remove(key: String) {
        storage.remove(key)
    }
}
