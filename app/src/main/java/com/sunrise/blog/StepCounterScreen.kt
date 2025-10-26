// StepCounterScreen.kt
package com.sunrise.blog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sunrise.blog.model.StepCounterData
import com.sunrise.blog.viewmodel.StepCounterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepCounterScreen(navController: NavController) {
    val viewModel: StepCounterViewModel = viewModel()
    val stepData by viewModel.stepData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // 顶部栏
        TopAppBar(
            title = {
                Text(
                    "跑步计步器",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 步数显示区域
        StepDisplay(stepData = stepData)

        Spacer(modifier = Modifier.height(48.dp))

        // 统计数据
        RunningStats(stepData = stepData)

        Spacer(modifier = Modifier.height(48.dp))

        // 控制按钮
        ControlButtons(
            isRunning = stepData.isRunning,
            onStart = { viewModel.startRunning() },
            onStop = { viewModel.stopRunning() },
            onReset = { viewModel.reset() }
        )
    }
}

@Composable
fun StepDisplay(stepData: StepCounterData) {
    // 简单的脉冲动画
    val scale = remember { Animatable(1f) }

    LaunchedEffect(stepData.steps) {
        if (stepData.isRunning) {
            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            scale.animateTo(1f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 步数圆圈
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .scale(scale.value),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stepData.steps.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 48.sp
                )
                Text(
                    "步数",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 状态指示
        Text(
            text = if (stepData.isRunning) "跑步中..." else "准备开始",
            style = MaterialTheme.typography.titleMedium,
            color = if (stepData.isRunning) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RunningStats(stepData: StepCounterData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 距离
        StatCard(
            value = String.format("%.2f", stepData.distance),
            unit = "公里",
            label = "距离",
            icon = Icons.Default.DirectionsWalk
        )

        // 卡路里
        StatCard(
            value = String.format("%.0f", stepData.calories),
            unit = "卡路里",
            label = "消耗",
            icon = Icons.Default.LocalFireDepartment
        )
    }
}

@Composable
fun StatCard(value: String, unit: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ControlButtons(
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (!isRunning) {
            // 开始按钮
            Button(
                onClick = onStart,
                modifier = Modifier
                    .width(120.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "开始")
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始跑步")
            }
        } else {
            // 停止按钮
            Button(
                onClick = onStop,
                modifier = Modifier
                    .width(120.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = "停止")
                Spacer(modifier = Modifier.width(8.dp))
                Text("停止")
            }
        }

        // 重置按钮
        Button(
            onClick = onReset,
            modifier = Modifier
                .width(120.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "重置")
            Spacer(modifier = Modifier.width(8.dp))
            Text("重置")
        }
    }
}

// 简单的设置对话框
@Composable
fun SimpleSettingsDialog(
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var stepGoal by remember { mutableStateOf("10000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置步数目标") },
        text = {
            Column {
                OutlinedTextField(
                    value = stepGoal,
                    onValueChange = { stepGoal = it },
                    label = { Text("每日目标步数") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "设置您每天想要达到的步数目标",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(stepGoal.toIntOrNull() ?: 10000)
                onDismiss()
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}