package com.sunrise.blog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlogAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    MainApp()
                }
            }
        }
    }
}

@Composable
fun BlogAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}

// 定义底部导航项数据类
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val contentDescription: String
)

@Composable
fun MainApp() {
    val navController = rememberNavController()

    // 定义导航项
    val navItems = listOf(
        BottomNavItem(
            route = "home",
            title = "首页",
            icon = Icons.Default.Home,
            contentDescription = "首页"
        ),
        BottomNavItem(
            route = "tools",
            title = "工具",
            icon =Icons.Default.Build,
            contentDescription = "工具"
        ),
        BottomNavItem(
            route = "search",
            title = "搜索",
            icon = Icons.Default.Search,
            contentDescription = "搜索"
        ),
        BottomNavItem(
            route = "favorites",
            title = "收藏",
            icon = Icons.Default.Favorite,
            contentDescription = "收藏"
        ),
        BottomNavItem(
            route = "profile",
            title = "我的",
            icon = Icons.Default.Person,
            contentDescription = "个人资料"
        )
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = navItems
            )
        }
    ) { innerPadding ->
        NavigationHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // 在详情页面隐藏底部导航栏
    val skipRoutes = setOf(
        "post_detail",
        "password_manager",
        "about",
        "metronome",
        "file_manager",
        "microphone_permission"
    )

// 当前路由是否属于上述任一前缀
    val skipBottomBar = skipRoutes.any { currentRoute?.startsWith(it) == true }
    if (skipBottomBar) return
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // 清除返回栈直到首页，避免堆叠过多页面
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription
                    )
                },
                label = {
                    Text(text = item.title)
                }
            )
        }
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(navController = navController)
//            ClassicRefreshAutoLoadSample()
        }
        composable("tools"){
            ToolsScreen(navController = navController)
        }
        composable("json_formatter") {
            JsonFormatterScreen(navController = navController)
        }
        composable("base64_tool") {
            Base64ToolScreen(navController = navController)
        }
        composable("timestamp_tool") {
            TimestampToolScreen(navController = navController)
        }
        composable("password_manager") {
            PasswordManagerScreen(navController = navController)
        }
        composable("metronome") {
            MetronomeScreen(navController = navController)
//            StepCounterScreen(navController = navController)
        }
        composable("file_manager") {
            FileManagerScreen(navController = navController)
        }
        
        composable("root_file_manager") {
            RootFileManagerScreen(navController = navController)
        }

        composable("microphone_permission") {
            MicrophonePermissionScreen(navController = navController)
        }
//        composable("color_picker") {
//            ToolDetailScreen(navController, "颜色选择器", "颜色选择器功能")
//        }
//        composable("qr_generator") {
//            ToolDetailScreen(navController, "二维码生成", "二维码生成功能")
//        }
//        composable("markdown_editor") {
//            ToolDetailScreen(navController, "Markdown 编辑器", "Markdown 编辑器功能")
//        }
//        composable("unit_converter") {
//            ToolDetailScreen(navController, "单位换算", "单位换算功能")
//        }
//        composable("regex_tester") {
//            ToolDetailScreen(navController, "正则表达式测试", "正则表达式测试功能")
//        }
        composable("search") {
            SearchScreen()
        }
        composable("favorites") {
            FavoritesScreen()
        }
        composable("profile") {
            ProfileScreen(navController = navController)
        }
        composable("about") {
            AboutScreen(navController = navController)
        }

        composable(
            "post_detail/{postUuid}",
            arguments = listOf(navArgument("postUuid") { type = NavType.StringType })
        ) { backStackEntry ->
            val postUuid = backStackEntry.arguments?.getString("postUuid") ?: "01111111"
            PostDetailScreen(
                postUuid = postUuid,
                navController = navController
            )
        }
    }
}

// 其他页面组件（搜索、收藏、个人资料）
@Composable
fun SearchScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "搜索文章",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun FavoritesScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "我的收藏",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
