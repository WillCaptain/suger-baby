package com.twelfth.feature

import com.twelfth.data.local.GuestIdManager
import com.twelfth.data.local.ILocalStorage
import com.twelfth.data.local.StorageException
import kotlin.test.*

/**
 * FTR-001: 访客ID生成与管理 - Feature 级别测试
 * 
 * 测试覆盖 Feature 验收标准（FAC）：
 * - FAC-01: 首次访问自动生成访客ID
 * - FAC-02: 访客ID可用于访问功能
 * - FAC-03: 重复访问复用访客ID
 * - FAC-04: 清除数据后生成新访客ID
 * - FAC-05: 无效访客ID处理
 * - FAC-06: 跨平台一致性
 */
class FTR001GuestIdGenerationTest {
    
    private lateinit var mockStorage: MockLocalStorage
    private lateinit var guestIdManager: GuestIdManager
    
    @BeforeTest
    fun setup() {
        mockStorage = MockLocalStorage()
        guestIdManager = GuestIdManager(mockStorage)
    }
    
    /**
     * FAC-01: Given 用户首次打开应用，When 检测本地存储无访客ID，Then 自动生成唯一访客ID并存储
     */
    @Test
    fun test_FAC_01_first_visit_generates_guest_id() {
        // Given: 首次访问，无访客ID
        assertNull(mockStorage.getString("GUEST_ID"))
        
        // When: 检测访客ID
        val detectedId = guestIdManager.detectGuestId()
        assertNull(detectedId, "First visit should not detect any ID")
        
        // When: 生成访客ID
        val generatedId = guestIdManager.generateGuestId()
        
        // Then: 访客ID已生成并存储
        assertNotNull(generatedId)
        assertTrue(generatedId.startsWith("GUEST_"))
        assertEquals(generatedId, mockStorage.getString("GUEST_ID"))
    }
    
    /**
     * FAC-02: Given 访客ID已生成，When 访客使用该ID，Then 系统正常响应
     * （此测试验证访客ID格式有效性，实际API调用在集成测试中验证）
     */
    @Test
    fun test_FAC_02_guest_id_is_valid_for_usage() {
        // Given: 生成访客ID
        val guestId = guestIdManager.generateGuestId()
        
        // Then: 访客ID格式有效
        assertTrue(guestIdManager.validateGuestId(guestId))
        
        // And: 可以从存储中读取
        val storedId = mockStorage.getString("GUEST_ID")
        assertEquals(guestId, storedId)
    }
    
    /**
     * FAC-03: Given 用户再次打开应用，When 本地存储中已有访客ID，Then 复用该ID，不重新生成
     */
    @Test
    fun test_FAC_03_repeat_visit_reuses_guest_id() {
        // Given: 首次访问生成了访客ID
        val originalId = guestIdManager.generateGuestId()
        
        // When: 模拟应用重启，创建新的 GuestIdManager
        val newGuestIdManager = GuestIdManager(mockStorage)
        val detectedId = newGuestIdManager.detectGuestId()
        
        // Then: 检测到原来的ID，无需重新生成
        assertNotNull(detectedId)
        assertEquals(originalId, detectedId)
    }
    
    /**
     * FAC-04: Given 用户清除应用数据，When 再次打开应用，Then 生成新的访客ID
     */
    @Test
    fun test_FAC_04_cleared_data_generates_new_id() {
        // Given: 首次访问生成了访客ID
        val originalId = guestIdManager.generateGuestId()
        
        // When: 用户清除应用数据
        mockStorage.remove("GUEST_ID")
        
        // And: 再次打开应用
        val detectedId = guestIdManager.detectGuestId()
        assertNull(detectedId, "After clearing data, no ID should be detected")
        
        // When: 生成新的访客ID
        val newId = guestIdManager.generateGuestId()
        
        // Then: 新ID与原ID不同
        assertNotEquals(originalId, newId)
    }
    
    /**
     * FAC-05: Given 用户使用无效格式的访客ID，When 检测访客ID，Then 返回 null 并清除无效数据
     */
    @Test
    fun test_FAC_05_invalid_guest_id_is_rejected() {
        // Given: 本地存储中有无效格式的ID（可能是数据损坏）
        mockStorage.setString("GUEST_ID", "INVALID_FORMAT_123")
        
        // When: 检测访客ID
        val detectedId = guestIdManager.detectGuestId()
        
        // Then: 无效ID被拒绝
        assertNull(detectedId)
        
        // And: 无效数据已被清除
        assertNull(mockStorage.getString("GUEST_ID"))
    }
    
    /**
     * FAC-06: Given 共享模块在不同平台运行，When 执行访客ID生成和检测，Then 行为一致
     * （此测试在 commonTest 中运行，验证跨平台逻辑一致性）
     */
    @Test
    fun test_FAC_06_cross_platform_consistency() {
        // Given & When: 生成访客ID
        val guestId = guestIdManager.generateGuestId()
        
        // Then: 格式一致
        val pattern = Regex("^GUEST_\\d{13}_[a-z0-9]{8}$")
        assertTrue(pattern.matches(guestId), "Guest ID format should be consistent across platforms")
        
        // And: 可以被检测到
        val detectedId = guestIdManager.detectGuestId()
        assertEquals(guestId, detectedId)
    }
    
    /**
     * 完整用户流程测试
     */
    @Test
    fun test_complete_user_journey() {
        // 1. 用户首次打开应用
        val firstDetect = guestIdManager.detectGuestId()
        assertNull(firstDetect, "First time should not detect any ID")
        
        // 2. 系统生成访客ID
        val generatedId = guestIdManager.generateGuestId()
        assertNotNull(generatedId)
        
        // 3. 用户关闭应用，再次打开
        val secondDetect = guestIdManager.detectGuestId()
        assertEquals(generatedId, secondDetect, "Should reuse the same ID")
        
        // 4. 用户清除应用数据
        mockStorage.remove("GUEST_ID")
        
        // 5. 用户再次打开应用
        val thirdDetect = guestIdManager.detectGuestId()
        assertNull(thirdDetect, "After clearing, should not detect any ID")
        
        // 6. 系统生成新的访客ID
        val newId = guestIdManager.generateGuestId()
        assertNotEquals(generatedId, newId, "Should generate a new ID")
    }
}

/**
 * Mock LocalStorage for testing
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
