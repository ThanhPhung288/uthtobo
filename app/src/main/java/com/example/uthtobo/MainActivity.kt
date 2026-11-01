package com.example.uthtobo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =====================================================================
// 1. OOP: MODEL
// =====================================================================

enum class TaskCategory(val label: String) {
    STUDY("Học tập"),
    LIFE("Cuộc sống")
}

data class TodoTask(
    val id: Int,
    val title: String,
    val category: TaskCategory,
    val isDone: Boolean = false
)

/** Một trang trong màn hình Onboarding */
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val buttonText: String
)

/** Navigation: dùng sealed class thay vì chuỗi rời rạc */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
}

// =====================================================================
// 2. OOP: REPOSITORY (quản lý dữ liệu)
// =====================================================================

class OnboardingRepository {
    fun getPages(): List<OnboardingPage> = listOf(
        OnboardingPage(
            title = "Mọi việc, gọn trong một chỗ",
            description = "Ghi lại bài tập, việc nhà, lịch hẹn. Không còn quên việc nào.",
            icon = Icons.Default.Checklist,
            buttonText = "Tiếp tục"
        ),
        OnboardingPage(
            title = "Chia theo Học tập và Cuộc sống",
            description = "Mỗi việc có nhóm riêng, nhìn là biết hôm nay cần làm gì.",
            icon = Icons.Default.Category,
            buttonText = "Tiếp tục"
        ),
        OnboardingPage(
            title = "Nhắc đúng lúc",
            description = "Bật thông báo để UTH Todo nhắc bạn trước hạn chót.",
            icon = Icons.Default.Notifications,
            buttonText = "Bắt đầu ngay"
        )
    )
}

class TodoRepository {
    private val _tasks = mutableStateListOf(
        TodoTask(1, "Làm bài tập Navigation + OOP", TaskCategory.STUDY),
        TodoTask(2, "Dọn phòng", TaskCategory.LIFE)
    )
    val tasks: List<TodoTask> get() = _tasks

    private var nextId = 3

    fun add(title: String, category: TaskCategory) {
        _tasks.add(TodoTask(nextId++, title, category))
    }

    fun toggle(id: Int) {
        val index = _tasks.indexOfFirst { it.id == id }
        if (index >= 0) _tasks[index] = _tasks[index].copy(isDone = !_tasks[index].isDone)
    }

    fun remove(id: Int) {
        _tasks.removeAll { it.id == id }
    }
}

// =====================================================================
// 3. ACTIVITY + NAVIGATION
// =====================================================================

private val UthBlue = Color(0xFF2F5BEA)

/** Logo UTH Todo vẽ bằng code, giống thiết kế Figma */
@Composable
fun UthLogo(
    size: Dp,
    background: Color = UthBlue,
    main: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val soft = main.copy(alpha = 0.6f)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(size * 0.1f)) {
            // Hàng 1: vòng tròn có dấu tick + thanh ngang
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(size * 0.2f).clip(CircleShape).background(main),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = background, modifier = Modifier.size(size * 0.14f))
                }
                Spacer(Modifier.width(size * 0.06f))
                Box(Modifier.width(size * 0.3f).height(size * 0.08f).clip(CircleShape).background(main))
            }
            // Hàng 2: vòng tròn rỗng + thanh ngang
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(size * 0.2f)
                        .clip(CircleShape)
                        .border(size * 0.04f, soft, CircleShape)
                )
                Spacer(Modifier.width(size * 0.06f))
                Box(Modifier.width(size * 0.22f).height(size * 0.08f).clip(CircleShape).background(soft))
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val todoRepository = remember { TodoRepository() }
    val onboardingRepository = remember { OnboardingRepository() }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController, onboardingRepository.getPages())
        }
        composable(Screen.Home.route) {
            HomeScreen(todoRepository)
        }
    }
}

// =====================================================================
// 4. SCREENS
// =====================================================================

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate(Screen.Onboarding.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(UthBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UthLogo(size = 96.dp, background = Color.White, main = UthBlue)
            Spacer(Modifier.height(16.dp))
            Text("UTH Todo", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Việc học, việc nhà – gọn gàng mỗi ngày", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavHostController, pages: List<OnboardingPage>) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    fun goHome() {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Header: logo + Bỏ qua
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UthLogo(size = 32.dp)
                Spacer(Modifier.width(8.dp))
                Text("UTH ", color = Color(0xFF1C1C1E), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Todo", color = UthBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            TextButton(onClick = { goHome() }) { Text("Bỏ qua", color = Color.Gray) }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            val page = pages[index]
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFEEF1FB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(page.icon, null, tint = UthBlue, modifier = Modifier.size(72.dp))
                }
                Spacer(Modifier.height(32.dp))
                Text(page.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(page.description, color = Color.Gray, fontSize = 14.sp)
            }
        }

        // Dấu chấm chỉ trang
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            repeat(pages.size) { i ->
                val selected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .height(6.dp)
                        .width(if (selected) 20.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) UthBlue else Color.LightGray)
                )
            }
        }

        val current = pages[pagerState.currentPage]
        Button(
            onClick = {
                if (pagerState.currentPage < pages.lastIndex) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    goHome()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UthBlue)
        ) {
            Text(current.buttonText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repository: TodoRepository) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("UTH Todo") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = UthBlue) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(repository.tasks, key = { it.id }) { task ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = task.isDone, onCheckedChange = { repository.toggle(task.id) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, fontWeight = FontWeight.Medium)
                            Text(task.category.label, fontSize = 12.sp, color = UthBlue)
                        }
                        TextButton(onClick = { repository.remove(task.id) }) { Text("Xóa") }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddTaskDialog(
            onDismiss = { showDialog = false },
            onAdd = { title, category ->
                repository.add(title, category)
                showDialog = false
            }
        )
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, TaskCategory) -> Unit) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(TaskCategory.STUDY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm công việc") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tên công việc") })
                Spacer(Modifier.height(8.dp))
                Row {
                    TaskCategory.values().forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c.label) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onAdd(title.trim(), category) }) { Text("Thêm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}