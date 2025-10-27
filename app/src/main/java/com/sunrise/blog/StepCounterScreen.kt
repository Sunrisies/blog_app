// StepCounterScreen.kt
package com.sunrise.blog

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sunrise.blog.model.StepCounterData
import com.sunrise.blog.model.StepCounterSettings
import com.sunrise.blog.viewmodel.StepCounterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepCounterScreen(navController: NavController) {
    val viewModel: StepCounterViewModel = viewModel()
    val stepData by viewModel.stepData.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
            actions = {
                IconButton(onClick = { showSettingsDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "设置")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 步频显示和动画
            PaceDisplay(
                currentPace = stepData.currentPace,
                targetPace = stepData.targetPace,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
            )

            // 步数显示
            StepDisplay(
                stepData = stepData,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
            )

            // 统计数据
            RunningStats(stepData = stepData, modifier = Modifier.fillMaxWidth())

            // 控制按钮
            ControlButtons(
                isRunning = stepData.isRunning,
                onStart = { viewModel.startRunning() },
                onStop = { viewModel.stopRunning() },
                onReset = { viewModel.reset() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // 设置对话框
    if (showSettingsDialog) {
        StepCounterSettingsDialog(
            stepData = stepData,
            settings = settings,
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun PaceDisplay(
    currentPace: Int,
    targetPace: Int,
    modifier: Modifier = Modifier
) {
    // 根据步频匹配情况决定动画效果
    val isOnPace = currentPace in (targetPace - 5)..(targetPace + 5) && currentPace > 0
    val isTooFast = currentPace > targetPace + 5
    val isTooSlow = currentPace < targetPace - 5 && currentPace > 0

    // 脉冲动画
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(isOnPace) {
        if (isOnPace) {
            pulseScale.animateTo(
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseScale.animateTo(1f)
        }
    }

    Card(
        modifier = modifier
            .padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOnPace -> Color(0xFF4CAF50).copy(alpha = 0.2f) // 绿色
                isTooFast -> Color(0xFFF44336).copy(alpha = 0.2f) // 红色
                isTooSlow -> Color(0xFFFF9800).copy(alpha = 0.2f) // 橙色
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "当前步频",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$currentPace",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    isOnPace -> Color(0xFF4CAF50)
                    isTooFast -> Color(0xFFF44336)
                    isTooSlow -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.scale(pulseScale.value)
            )

            Text(
                "步/分钟",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "目标: $targetPace 步/分钟",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // 步频提示
            Text(
                text = when {
                    isOnPace -> "✓ 完美步频！"
                    isTooFast -> "↓ 稍微慢一点"
                    isTooSlow -> "↑ 加快步频"
                    else -> "开始跑步以检测步频"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isOnPace -> Color(0xFF4CAF50)
                    isTooFast -> Color(0xFFF44336)
                    isTooSlow -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun StepDisplay(stepData: StepCounterData, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(stepData.steps) {
        if (stepData.isRunning && stepData.steps > 0) {
            scale.animateTo(1.05f, tween(durationMillis = 100))
            scale.animateTo(1f, tween(durationMillis = 200))
        }
    }

    Card(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
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
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "步数",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (stepData.isRunning) "跑步中..." else "准备开始",
                style = MaterialTheme.typography.bodyMedium,
                color = if (stepData.isRunning) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RunningStats(stepData: StepCounterData, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCard(
            value = String.format("%.2f", stepData.distance),
            unit = "公里",
            label = "距离",
            icon = Icons.Default.DirectionsWalk
        )

        StatCard(
            value = String.format("%.0f", stepData.calories),
            unit = "卡路里",
            label = "消耗",
            icon = Icons.Default.LocalFireDepartment
        )

        StatCard(
            value = stepData.targetPace.toString(),
            unit = "步/分钟",
            label = "目标步频",
            icon = Icons.Default.Speed
        )
    }
}

@Composable
fun StatCard(value: String, unit: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (!isRunning) {
            Button(
                onClick = onStart,
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "开始")
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始")
            }
        } else {
            Button(
                onClick = onStop,
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = "停止")
                Spacer(modifier = Modifier.width(8.dp))
                Text("停止")
            }
        }

        Button(
            onClick = onReset,
            modifier = Modifier
                .width(120.dp)
                .height(50.dp),
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

// 在 StepCounterSettingsDialog 中添加音量控制
// 在 StepCounterSettingsDialog 中添加音量控制
@Composable
fun StepCounterSettingsDialog(
    stepData: StepCounterData,
    settings: StepCounterSettings,
    viewModel: StepCounterViewModel,
    onDismiss: () -> Unit
) {
    var targetPace by remember { mutableStateOf(stepData.targetPace.toString()) }
    var enableSound by remember { mutableStateOf(settings.enableSound) }
    var enableVibration by remember { mutableStateOf(settings.enableVibration) }
    var enableBluetooth by remember { mutableStateOf(settings.enableBluetooth) }
    var soundVolume by remember { mutableStateOf(50) } // 音量 0-100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("计步器设置") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 目标步频设置
                Column {
                    Text("目标步频", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetPace,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                targetPace = it
                            }
                        },
                        label = { Text("步/分钟") },
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text("步/分钟") }
                    )
                    Text(
                        "常见步频范围: 150-180 步/分钟",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 提示设置
                Column {
                    Text("提示设置", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = enableSound,
                            onCheckedChange = { enableSound = it }
                        )
                        Text("声音提示", modifier = Modifier.clickable {
                            enableSound = !enableSound
                        })
                    }

                    if (enableSound) {
                        Column {
                            Text("音量: $soundVolume%", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = soundVolume.toFloat(),
                                onValueChange = { soundVolume = it.toInt() },
                                valueRange = 0f..100f,
                                steps = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = enableVibration,
                            onCheckedChange = { enableVibration = it }
                        )
                        Text("震动提示", modifier = Modifier.clickable {
                            enableVibration = !enableVibration
                        })
                    }
                }

                // 蓝牙设置
                Column {
                    Text("蓝牙设置", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = enableBluetooth,
                            onCheckedChange = {
                                enableBluetooth = it
                                if (it) {
                                    viewModel.connectBluetooth()
                                } else {
                                    viewModel.disconnectBluetooth()
                                }
                            }
                        )
                        Text("蓝牙音频播放", modifier = Modifier.clickable {
                            enableBluetooth = !enableBluetooth
                            if (!enableBluetooth) {
                                viewModel.connectBluetooth()
                            } else {
                                viewModel.disconnectBluetooth()
                            }
                        })
                    }

                    if (enableBluetooth) {
                        Text(
                            "连接蓝牙设备播放语音提示",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 测试声音按钮
                if (enableSound) {
                    Button(
                        onClick = {
                            // 这里可以添加测试声音的功能
                            // 由于在Composable中无法直接调用ViewModel，我们需要其他方式
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("测试提示音")
                    }
                }

                // 使用说明
                Text(
                    "注意：此版本计步器需要在应用前台运行。当应用进入后台时，计步将暂停。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newPace = targetPace.toIntOrNull() ?: 160
                viewModel.updateTargetPace(newPace)
                viewModel.updateSettings(StepCounterSettings(
                    targetPace = newPace,
                    enableSound = enableSound,
                    enableVibration = enableVibration,
                    enableBluetooth = enableBluetooth
                ))
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