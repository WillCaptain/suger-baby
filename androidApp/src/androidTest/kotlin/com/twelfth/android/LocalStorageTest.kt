package com.twelfth.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.twelfth.data.local.LocalStorage
import com.twelfth.data.local.StorageException
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android 平台 LocalStorage 集成测试
 * 
 * 测试 SharedPreferences 实现的正确性
 */
@RunWith(AndroidJUnit4::class)
class LocalStorageTest {
    
    private lateinit var context: Context
    private lateinit var localStorage: LocalStorage
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        localStorage = LocalStorage(context)
        
        // 清理测试数据
        localStorage.remove("TEST_KEY")
    }
    
    @After
    fun cleanup() {
        // 清理测试数据
        try {
            localStorage.remove("TEST_KEY")
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
    
    /**
     * 测试 SharedPreferences 写入和读取
     */
    @Test
    fun test_write_and_read_string() {
        // Given
        val key = "TEST_KEY"
        val value = "test_value_123"
        
        // When: 写入
        localStorage.setString(key, value)
        
        // Then: 读取成功
        val retrieved = localStorage.getString(key)
        assertEquals(value, retrieved)
    }
    
    /**
     * 测试读取不存在的键
     */
    @Test
    fun test_read_nonexistent_key_returns_null() {
        // When
        val result = localStorage.getString("NONEXISTENT_KEY")
        
        // Then
        assertNull(result)
    }
    
    /**
     * 测试移除键值对
     */
    @Test
    fun test_remove_key() {
        // Given: 先写入数据
        val key = "TEST_KEY"
        localStorage.setString(key, "value")
        assertNotNull(localStorage.getString(key))
        
        // When: 移除
        localStorage.remove(key)
        
        // Then: 读取返回 null
        assertNull(localStorage.getString(key))
    }
    
    /**
     * 测试覆盖已有数据
     */
    @Test
    fun test_overwrite_existing_value() {
        // Given
        val key = "TEST_KEY"
        localStorage.setString(key, "old_value")
        
        // When: 写入新值
        localStorage.setString(key, "new_value")
        
        // Then: 读取到新值
        val result = localStorage.getString(key)
        assertEquals("new_value", result)
    }
    
    /**
     * 测试存储访客ID格式的数据
     */
    @Test
    fun test_store_guest_id_format() {
        // Given: 访客ID格式
        val guestId = "GUEST_1706543210123_a3f9c2e1"
        
        // When
        localStorage.setString("GUEST_ID", guestId)
        
        // Then
        val retrieved = localStorage.getString("GUEST_ID")
        assertEquals(guestId, retrieved)
    }
    
    /**
     * 测试持久化（重新创建 LocalStorage 实例后数据仍存在）
     */
    @Test
    fun test_data_persistence() {
        // Given: 写入数据
        val key = "TEST_KEY"
        val value = "persistent_value"
        localStorage.setString(key, value)
        
        // When: 重新创建 LocalStorage 实例（模拟应用重启）
        val newLocalStorage = LocalStorage(context)
        val retrieved = newLocalStorage.getString(key)
        
        // Then: 数据仍然存在
        assertEquals(value, retrieved)
    }
}
