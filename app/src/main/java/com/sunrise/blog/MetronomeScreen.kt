// MetronomeScreen.kt
package com.sunrise.blog

import androidx.compose.animation.core.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sunrise.blog.model.StepSide
import com.sunrise.blog.viewmodel.AudioTrackMetronomeViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomeScreen(navController: NavController) {
    val viewModel: AudioTrackMetronomeViewModel = viewModel()
    val metronomeData by viewModel.metronomeData.collectAsState()

    var showVolumeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部栏
        TopAppBar(
            title = {
                Text(
                    "高精度跑步步频器",
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
                // 音量控制按钮
                IconButton(onClick = { showVolumeDialog = true }) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "音量设置")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 步频显示和控制
            TempoControl(
                tempo = metronomeData.tempo,
                actualTempo = metronomeData.actualTempo,
                onIncrease = { viewModel.increaseTempo() },
                onDecrease = { viewModel.decreaseTempo() },
                modifier = Modifier.fillMaxWidth()
            )

            // 脚步动画显示
            FootAnimation(
                currentStep = metronomeData.currentStep,
                isRunning = metronomeData.isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // 控制按钮
            ControlButtons(
                isRunning = metronomeData.isRunning,
                onStart = { viewModel.startMetronome() },
                onStop = { viewModel.stopMetronome() },
                modifier = Modifier.fillMaxWidth()
            )

            // 步频说明
//            TempoGuide(modifier = Modifier.fillMaxWidth())
        }
    }

    // 音量控制对话框
    if (showVolumeDialog) {
        VolumeControlDialog(
            currentVolume = metronomeData.volume,
            onVolumeChange = { newVolume ->
                viewModel.setVolume(newVolume)
            },
            onDismiss = { showVolumeDialog = false }
        )
    }
}

@Composable
fun VolumeControlDialog(
    currentVolume: Int,
    onVolumeChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tempVolume by remember { mutableStateOf(currentVolume) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("音量设置") },
        text = {
            Column {
                Text("音量: $tempVolume%", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = tempVolume.toFloat(),
                    onValueChange = { tempVolume = it.toInt() },
                    valueRange = 0f..100f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", style = MaterialTheme.typography.bodySmall)
                    Text("100%", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "使用 AudioTrack 高精度音频引擎",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onVolumeChange(tempVolume)
                onDismiss()
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}



@Composable
fun TempoControl(
    tempo: Int,
    actualTempo: Double,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "目标步频",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 减少按钮
                IconButton(
                    onClick = onDecrease,
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "降低步频",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // 步频显示
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$tempo",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "步/分钟",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 显示实际步频
                    if (actualTempo > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "实际: ${"%.1f".format(actualTempo)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (abs(actualTempo - tempo) > 5)
                                Color(0xFFF44336)
                            else
                                Color(0xFF4CAF50)
                        )
                    }
                }

                // 增加按钮
                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "提高步频",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 步频调节滑块
            var sliderValue by remember { mutableStateOf(tempo.toFloat()) }
            val viewModel: AudioTrackMetronomeViewModel = viewModel()

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = sliderValue,
                    onValueChange = { newValue ->
                        sliderValue = newValue
                        viewModel.setTempo(newValue.toInt())
                    },
                    valueRange = 40f..240f,
                    steps = 39,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("40", style = MaterialTheme.typography.bodySmall)
                    Text("240", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
@Composable
fun FootAnimation(
    currentStep: StepSide,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    // 脚步动画
    val leftFootScale = remember { Animatable(1f) }
    val rightFootScale = remember { Animatable(1f) }

    LaunchedEffect(currentStep, isRunning) {
        if (isRunning) {
            if (currentStep == StepSide.LEFT) {
                // 左脚激活动画
                leftFootScale.animateTo(1.2f, animationSpec = tween(durationMillis = 100))
                leftFootScale.animateTo(1f, animationSpec = tween(durationMillis = 100))
                rightFootScale.animateTo(1f)
            } else {
                // 右脚激活动画
                rightFootScale.animateTo(1.2f, animationSpec = tween(durationMillis = 100))
                rightFootScale.animateTo(1f, animationSpec = tween(durationMillis = 100))
                leftFootScale.animateTo(1f)
            }
        } else {
            // 停止时重置
            leftFootScale.animateTo(1f)
            rightFootScale.animateTo(1f)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左脚
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            if (currentStep == StepSide.LEFT && isRunning)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                        .scale(leftFootScale.value),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "左脚",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStep == StepSide.LEFT && isRunning)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 脚印指示器
                if (currentStep == StepSide.LEFT && isRunning) {
                    Text(
                        "👣",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 32.sp
                    )
                } else {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // 右脚
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            if (currentStep == StepSide.RIGHT && isRunning)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                        .scale(rightFootScale.value),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "右脚",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStep == StepSide.RIGHT && isRunning)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 脚印指示器
                if (currentStep == StepSide.RIGHT && isRunning) {
                    Text(
                        "👣",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 32.sp
                    )
                } else {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // 中间的连接线
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(4.dp)
//                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
//        )
    }
}

@Composable
fun ControlButtons(
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 调试信息
    LaunchedEffect(isRunning) {
        println("ControlButtons - isRunning: $isRunning")
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        if (!isRunning) {
            // 开始按钮 - 绿色
            Button(
                onClick = onStart,
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // 绿色
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "开始")
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始步频器", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            // 停止按钮 - 红色，确保始终可用
            Button(
                onClick = onStop,
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336) // 红色
                ),
                enabled = true // 明确设置为可用
            ) {
                Icon(Icons.Default.Stop, contentDescription = "停止")
                Spacer(modifier = Modifier.width(8.dp))
                Text("停止", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
@Composable
fun TempoGuide(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "步频参考指南",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "• 慢跑: 150-165 步/分钟\n" +
                        "• 轻松跑: 165-175 步/分钟\n" +
                        "• 节奏跑: 175-185 步/分钟\n" +
                        "• 比赛配速: 180+ 步/分钟",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "提示：根据这个节奏调整您的步频，配合滴答声练习",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

//@Composable
//fun VolumeControlDialog(
//    currentVolume: Int,
//    onVolumeChange: (Int) -> Unit,
//    onDismiss: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("音量设置") },
//        text = {
//            Column {
//                Text("音量: $currentVolume%", style = MaterialTheme.typography.bodyMedium)
//                Spacer(modifier = Modifier.height(16.dp))
//                Slider(
//                    value = currentVolume.toFloat(),
//                    onValueChange = { onVolumeChange(it.toInt()) },
//                    valueRange = 0f..100f,
//                    steps = 9,
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text("0%", style = MaterialTheme.typography.bodySmall)
//                    Text("100%", style = MaterialTheme.typography.bodySmall)
//                }
//            }
//        },
//        confirmButton = {
//            Button(onClick = onDismiss) {
//                Text("确定")
//            }
//        }
//    )
//}