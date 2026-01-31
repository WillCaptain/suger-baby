package com.twelfth.data.local

/**
 * 存储异常
 * 当本地存储操作失败时抛出
 */
class StorageException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
