package com.example.siheunggagae

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.siheunggagae.ui.theme.Brown40
import com.example.siheunggagae.ui.theme.Gray10
import com.example.siheunggagae.ui.theme.Gray40
import com.example.siheunggagae.ui.theme.Gray80
import com.example.siheunggagae.ui.theme.Gray95
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// ─── 라우트 정의 ───────────────────────────────────────────────────────────────

sealed class Screen(val route: String) {
    object Splash       : Screen("splash")
    object Home         : Screen("home")
    object Notification : Screen("notification")
    object Matching     : Screen("matching")
    object RequestFlow  : Screen("request_flow")
    object MyRequests   : Screen("my_requests")
    object Map          : Screen("map")
    object News         : Screen("news")
    object PlaceDetail  : Screen("place_detail/{placeId}") {
        fun createRoute(placeId: Int) = "place_detail/$placeId"
    }
    object My              : Screen("my")
    object Settings        : Screen("settings")
    object PetList         : Screen("pet_list")
    object PetAdd          : Screen("pet_add")
    object VolunteerApply  : Screen("volunteer_apply")
    object MatchingDetail       : Screen("matching_detail/{requestId}") {
        fun createRoute(requestId: Int) = "matching_detail/$requestId"
    }
    object MatchingPublicDetail : Screen("matching_public_detail/{requestId}") {
        fun createRoute(requestId: Int) = "matching_public_detail/$requestId"
    }
    object Chat             : Screen("chat/{userId}") {
        fun createRoute(userId: Int) = "chat/$userId"
    }
    object NewsDetail      : Screen("news_detail/{newsId}") {
        fun createRoute(newsId: Int) = "news_detail/$newsId"
    }
}

// ─── 공유 BottomNavigationBar ──────────────────────────────────────────────────

private data class BottomNavEntry(
    val icon: ImageVector,
    val label: String,
    val route: String,
)

private val bottomNavEntries = listOf(
    BottomNavEntry(Icons.Default.Home,                "홈",   Screen.Home.route),
    BottomNavEntry(Icons.Default.Handshake,           "매칭", Screen.Matching.route),
    BottomNavEntry(Icons.Default.Map,                 "지도", Screen.Map.route),
    BottomNavEntry(Icons.AutoMirrored.Filled.Article, "소식", Screen.News.route),
    BottomNavEntry(Icons.Default.AccountCircle,       "마이", Screen.My.route),
)

@Composable
fun AppBottomBar(currentRoute: String, onNavigate: (String) -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(50.dp))
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            bottomNavEntries.forEach { entry ->
                val selected = currentRoute == entry.route
                if (selected) {
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF1A1A1A))
                            .clickable { }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = entry.icon,
                            contentDescription = entry.label,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = entry.label,
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp,
                            color = Color.White,
                        )
                    }
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { onNavigate(entry.route) },
                    ) {
                        Icon(
                            imageVector = entry.icon,
                            contentDescription = entry.label,
                            tint = Color(0xFFC4A882),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

// ─── NavHost ───────────────────────────────────────────────────────────────────

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onLogin = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onSignup = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNotificationClick = { navController.navigate(Screen.Notification.route) },
                onNavigate = { route -> navController.navigateTab(route) },
                onPlaceDetailClick = { placeId -> navController.navigate(Screen.PlaceDetail.createRoute(placeId)) },
            )
        }

        composable(Screen.Notification.route) {
            NotificationScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Matching.route) {
            MatchingScreen(
                onMyRequests = { navController.navigate(Screen.MyRequests.route) },
                onRequestFlowClick = { navController.navigate(Screen.RequestFlow.route) },
                onCardClick = { requestId -> navController.navigate(Screen.MatchingPublicDetail.createRoute(requestId)) },
                onNavigate = { route -> navController.navigateTab(route) },
            )
        }

        composable(
            route = Screen.MatchingDetail.route,
            arguments = listOf(navArgument("requestId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getInt("requestId") ?: 0
            MatchingDetailScreen(
                requestId = requestId,
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) },
            )
        }

        composable(
            route = Screen.MatchingPublicDetail.route,
            arguments = listOf(navArgument("requestId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getInt("requestId") ?: 0
            MatchingPublicDetailScreen(
                requestId = requestId,
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) },
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            ChatScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) },
            )
        }

        composable(Screen.RequestFlow.route) {
            RequestFlowScreen(
                onBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() },
                onAddPet = { navController.navigate(Screen.PetAdd.route) },
            )
        }

        composable(Screen.MyRequests.route) {
            MyRequestsScreen(
                onBack = { navController.popBackStack() },
                onCardClick = { requestId -> navController.navigate(Screen.MatchingDetail.createRoute(requestId)) },
            )
        }

        composable(Screen.Map.route) {
            MapScreen(onNavigate = { route -> navController.navigateTab(route) })
        }

        composable(Screen.News.route) {
            NewsScreen(
                onNewsDetailClick = { newsId ->
                    navController.navigate(Screen.NewsDetail.createRoute(newsId))
                },
                onPlaceDetailClick = { placeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(placeId))
                },
                onNavigate = { route -> navController.navigateTab(route) },
            )
        }

        composable(
            route = Screen.PlaceDetail.route,
            arguments = listOf(navArgument("placeId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getInt("placeId") ?: 0
            PlaceDetailScreen(
                placeId = placeId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.My.route) {
            MyScreen(
                onNavigate = { route -> navController.navigateTab(route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onPetListClick = { navController.navigate(Screen.PetList.route) },
                onVolunteerApplyClick = { navController.navigate(Screen.VolunteerApply.route) },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onPetListClick = { navController.navigate(Screen.PetList.route) },
            )
        }

        composable(Screen.PetList.route) {
            PetListScreen(
                onBack = { navController.popBackStack() },
                onAddPet = { navController.navigate(Screen.PetAdd.route) },
            )
        }

        composable(Screen.PetAdd.route) {
            PetAddScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.VolunteerApply.route) {
            VolunteerApplyScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.NewsDetail.route,
            arguments = listOf(navArgument("newsId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getInt("newsId") ?: 1
            NewsDetailScreen(newsId = newsId, onBack = { navController.popBackStack() })
        }
    }
}

private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(Screen.Home.route) { saveState = true }
    }
}

// ─── SplashScreen ──────────────────────────────────────────────────────────────

@Composable
fun SplashScreen(onLogin: () -> Unit = {}, onSignup: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "🐾", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "시흥가개",
                fontFamily = PretendardFamily,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 48.sp,
                color = Color(0xFF8A6E58),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "우리 동네 반려동물을 위한 따뜻한 발걸음",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Gray40,
            )
            Spacer(Modifier.height(64.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF8A6E58))
                    .clickable { onLogin() }
                    .padding(vertical = 14.dp),
            ) {
                Text(
                    text = "로그인하기",
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFEFEFE))
                    .clickable { onSignup() }
                    .padding(vertical = 14.dp),
            ) {
                Text(
                    text = "회원가입하기",
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                    color = Color(0xFF1E120A),
                )
            }
        }

        Text(
            text = "v3.0.0",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Gray80,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        )
    }
}

// ─── MyRequestsScreen (placeholder) ───────────────────────────────────────────

@Composable
fun MyRequestsScreen(
    onBack: () -> Unit = {},
    onCardClick: (requestId: Int) -> Unit = {},
) {
    Scaffold(
        containerColor = Gray95,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.White)
                    .padding(vertical = 8.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로",
                        tint = Gray10,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = "내 봉사 요청 목록",
                    fontFamily = PretendardFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 32.sp,
                    color = Gray10,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "준비 중입니다",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = Gray40,
            )
        }
    }
}

// ─── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SiheungGagaeTheme { SplashScreen() }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppNavGraphPreview() {
    SiheungGagaeTheme { AppNavGraph() }
}
