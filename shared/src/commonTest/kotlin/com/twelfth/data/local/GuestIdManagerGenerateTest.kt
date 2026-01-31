package com.twelfth.data.local

import kotlin.test.*

/**
 * FUN-002: 生成访客ID - 契约测试
 * 
 * 测试覆盖：
 * - AC-01: 格式正确
 * - AC-02: 已存储
 * - AC-03: 唯一性
 * - AC-04: 格式验证
 * - AC-05: 存储异常
 */
class GuestIdManagerGenerateTest {
    
    private lateinit var mockStorage: MockLocalStorage
    private lateinit var guestIdManager: GuestIdManager
    
    @BeforeTest
    fun setup() {
        mockStorage = MockLocalStorage()
        guestIdManager = GuestIdManager(mockStorage)
    }
    
    /**
     * AC-01: Given 调用 generateGuestId()，When 生成成功，Then 返回格式为 GUEST_<timestamp>_<random> 的字符串
     */
    @Test
    fun test_FUN_002_AC_01_valid_format() {
        // When: 生成访客ID
        val guestId = guestIdManager.generateGuestId()
        
        // Then: 格式正确
        assertTrue(guestId.startsWith("GUEST_"))
        assertTrue(guestIdManager.validateGuestId(guestId))
    }
    
    /**
     * AC-02: Given 调用 generateGuestId()，When 生成成功，Then 访客ID 已保存到本地存储
     */
    @Test
    fun test_FUN_002_AC_02_stored_in_cache() {
        // When: 生成访客ID
        val guestId = guestIdManager.generateGuestId()
        
        // Then: 已存储到本地
        val stored = mockStorage.getString("GUEST_ID")
        assertEquals(guestId, stored)
    }
    
    /**
     * AC-03: Given 短时间内多次调用 generateGuestId()，When 生成多个ID，Then 每个ID唯一
     */
    @Test
    fun test_FUN_002_AC_03_uniqueness() {
        // When: 生成10个访客ID
        val ids = mutableSetOf<String>()
        repeat(10) {
            mockStorage = MockLocalStorage() // 每次重新创建存储
            guestIdManager = GuestIdManager(mockStorage)
            val id = guestIdManager.generateGuestId()
            ids.add(id)
        }
        
        // Then: 10个ID互不相同
        assertEquals(10, ids.size, "Generated IDs should be unique")
    }
    
    /**
     * AC-04: Given 生成的访客ID，When 验证格式，Then timestamp 为13位数字，random 为8位小写字母或数字
     */
    @Test
    fun test_FUN_002_AC_04_format_validation() {
        // When: 生成访客ID
        val guestId = guestIdManager.generateGuestId()
        
        // Then: 格式验证
        val pattern = Regex("^GUEST_(\\d{13})_([a-z0-9]{8})$")
        val matchResult = pattern.matchEntire(guestId)
        
        assertNotNull(matchResult, "Guest ID format should match pattern")
        
        val timestamp = matchResult.groupValues[1]
        val random = matchResult.groupValues[2]
        
        assertEquals(13, timestamp.length, "Timestamp should be 13 digits")
        assertEquals(8, random.length, "Random part should be 8 characters")
        assertTrue(random.all { it.isLowerCase() || it.isDigit() }, "Random part should be lowercase or digit")
    }
    
    /**
     * AC-05: Given 本地存储写入失败，When 调用 generateGuestId()，Then 抛出 StorageException
     */
    @Test
    fun test_FUN_002_AC_05_storage_error() {
        // Given: 存储会抛出异常
        mockStorage.simulateWriteError = true
        
        // When & Then: 抛出 StorageException
        assertFailsWith<StorageException> {
            guestIdManager.generateGuestId()
        }
    }
    
    /**
     * 验证 validateGuestId 方法
     */
    @Test
    fun test_validateGuestId_valid_formats() {
        // Valid cases
        assertTrue(guestIdManager.validateGuestId("GUEST_1706543210123_a3f9c2e1"))
        assertTrue(guestIdManager.validateGuestId("GUEST_9999999999999_zzzzz000"))
        
        // Invalid cases
        assertFalse(guestIdManager.validateGuestId("GUEST_123_abc"))           // timestamp too short
        assertFalse(guestIdManager.validateGuestId("GUEST_1706543210123_ABC")) // uppercase
        assertFalse(guestIdManager.validateGuestId("GUEST_1706543210123_abc")) // random too short
        assertFalse(guestIdManager.validateGuestId("invalid_format"))
        assertFalse(guestIdManager.validateGuestId(""))
    }
}
