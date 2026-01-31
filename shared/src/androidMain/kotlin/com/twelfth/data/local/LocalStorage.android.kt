package com.twelfth.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * LocalStorage Android 实现（使用 SharedPreferences）
 */
actual class LocalStorage(context: Context) : ILocalStorage {
    
    companion object {
        private const val PREFS_NAME = "twelfth_prefs"
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * 读取字符串
     */
    actual override fun getString(key: String): String? {
        return try {
            sharedPreferences.getString(key, null)
        } catch (e: Exception) {
            throw StorageException("Failed to read from SharedPreferences: $key", e)
        }
    }
    
    /**
     * 保存字符串
     */
    actual override fun setString(key: String, value: String) {
        try {
            val editor = sharedPreferences.edit()
            editor.putString(key, value)
            
            // commit() 同步写入，确保立即生效
            if (!editor.commit()) {
                throw StorageException("Failed to commit SharedPreferences: $key")
            }
        } catch (e: StorageException) {
            throw e
        } catch (e: Exception) {
            throw StorageException("Failed to write to SharedPreferences: $key", e)
        }
    }
    
    /**
     * 移除键值对
     */
    actual override fun remove(key: String) {
        try {
            val editor = sharedPreferences.edit()
            editor.remove(key)
            editor.commit()
        } catch (e: Exception) {
            throw StorageException("Failed to remove from SharedPreferences: $key", e)
        }
    }
}
