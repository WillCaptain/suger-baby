package com.twelfth.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twelfth.android.ui.theme.TangXiaoNuanTheme
import com.twelfth.data.local.GuestIdManager
import com.twelfth.data.local.LocalStorage

/**
 * 糖小暖 - 主界面
 * 
 * 实现 FTR-001：访客ID生成与管理
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var guestIdManager: GuestIdManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化访客ID管理器
        val localStorage = LocalStorage(this)
        guestIdManager = GuestIdManager(localStorage)
        
        // 检测或生成访客ID
        val guestId = initializeGuestId()
        
        setContent {
            TangXiaoNuanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(guestId = guestId)
                }
            }
        }
    }
    
    /**
     * 初始化访客ID（FTR-001 核心流程）
     * 
     * 1. 检测本地是否存在访客ID（FUN-001）
     * 2. 如果不存在，生成新的访客ID（FUN-002）
     * 3. 返回可用的访客ID
     */
    private fun initializeGuestId(): String {
        // FUN-001: 检测访客ID
        var guestId = guestIdManager.detectGuestId()
        
        if (guestId == null) {
            // FUN-002: 生成访客ID
            guestId = guestIdManager.generateGuestId()
            println("Generated new guest ID: $guestId")
        } else {
            println("Detected existing guest ID: $guestId")
        }
        
        return guestId
    }
}

/**
 * 主屏幕 UI
 */
@Composable
fun HomeScreen(guestId: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "欢迎使用糖小暖",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "访客模式已启用",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "访客 ID",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = guestId,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "您可以立即使用以下功能：",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        listOf("血糖记录", "饮食记录", "运动记录", "数字人对话").forEach { feature ->
            Text(
                text = "• $feature",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
