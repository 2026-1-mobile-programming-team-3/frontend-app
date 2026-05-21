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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.siheunggagae.data.repository.AuthRepository
import com.example.siheunggagae.data.repository.UserRepository
import com.example.siheunggagae.data.repository.NotificationRepository
import com.example.siheunggagae.ui.viewmodel.AuthViewModel
import com.example.siheunggagae.ui.viewmodel.MyViewModel
import com.example.siheunggagae.ui.viewmodel.NotificationViewModel
import com.example.siheunggagae.ui.viewmodel.PetAddViewModel
import com.example.siheunggagae.ui.viewmodel.PetListViewModel
import com.example.siheunggagae.ui.viewmodel.ProfileEditViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.siheunggagae.ui.screen.AutoSplashScreen
import com.example.siheunggagae.ui.screen.ChatScreen
import com.example.siheunggagae.ui.screen.StartScreen
import com.example.siheunggagae.ui.screen.HomeScreen
import com.example.siheunggagae.ui.screen.LoginScreen
import com.example.siheunggagae.ui.screen.MapScreen
import com.example.siheunggagae.ui.screen.MatchingDetailScreen
import com.example.siheunggagae.ui.screen.MatchingPublicDetailScreen
import com.example.siheunggagae.ui.screen.MatchingScreen
import com.example.siheunggagae.ui.screen.MyRequestsScreen
import com.example.siheunggagae.ui.screen.MyScreen
import com.example.siheunggagae.ui.screen.ProfileEditScreen
import com.example.siheunggagae.ui.screen.NewsDetailScreen
import com.example.siheunggagae.ui.screen.NewsScreen
import com.example.siheunggagae.ui.screen.NotificationScreen
import com.example.siheunggagae.ui.screen.PetAddScreen
import com.example.siheunggagae.ui.screen.PetListScreen
import com.example.siheunggagae.ui.screen.PlaceDetailScreen
import com.example.siheunggagae.ui.screen.RequestFlowScreen
import com.example.siheunggagae.ui.screen.SettingsScreen
import com.example.siheunggagae.ui.screen.SignUpScreen
import com.example.siheunggagae.ui.screen.BlockManageScreen
import com.example.siheunggagae.ui.screen.FavoriteStoresScreen
import com.example.siheunggagae.ui.screen.HelpScreen
import com.example.siheunggagae.ui.screen.PrivacyPolicyScreen
import com.example.siheunggagae.ui.screen.VolunteerApplyScreen
import com.example.siheunggagae.ui.screen.VolunteerBadgeListScreen
import com.example.siheunggagae.ui.screen.VolunteerHistoryScreen
import com.example.siheunggagae.ui.viewmodel.BlockManageViewModel
import com.example.siheunggagae.ui.viewmodel.FavoriteStoresViewModel
import com.example.siheunggagae.ui.viewmodel.AccountSettingsViewModel
import com.example.siheunggagae.ui.viewmodel.LocationSettingsViewModel
import com.example.siheunggagae.ui.viewmodel.NotificationSettingsViewModel
import com.example.siheunggagae.ui.viewmodel.VolunteerBadgeViewModel
import com.example.siheunggagae.ui.viewmodel.VolunteerHistoryViewModel
import com.example.siheunggagae.ui.theme.Brown40
import com.example.siheunggagae.ui.theme.Gray10
import com.example.siheunggagae.ui.theme.Gray40
import com.example.siheunggagae.ui.theme.Gray80
import com.example.siheunggagae.ui.theme.Gray95
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// ─── 라우트 정의 ───────────────────────────────────────────────────────────────

sealed class Screen(val route: String) {
    object AutoSplash   : Screen("auto_splash")  // 재호-1: 로고 표시 + 토큰 확인
    object Splash       : Screen("splash")        // 재호-2: 시작 화면 (로그인/회원가입)
    object Login        : Screen("login")
    object SignUp       : Screen("signup")
    object Home         : Screen("home")
    object Notification : Screen("notification")
    object Matching     : Screen("matching")
    object RequestFlow : Screen("request_flow?requestId={requestId}") {
        fun createRoute(requestId: Int) = "request_flow?requestId=$requestId"
    }
    object MyRequests   : Screen("my_requests")
    object Map          : Screen("map")
    object News         : Screen("news")
    object PlaceDetail  : Screen("place_detail/{placeId}") {
        fun createRoute(placeId: Int) = "place_detail/$placeId"
    }
    object My              : Screen("my")
    object Settings        : Screen("settings")
    object PetList         : Screen("pet_list")
    object PetAdd          : Screen("pet_add?petId={petId}") {
        fun editRoute(petId: Int) = "pet_add?petId=$petId"
    }
    object VolunteerApply      : Screen("volunteer_apply")
    object VolunteerBadgeList  : Screen("volunteer_badge_list")
    object VolunteerHistory    : Screen("volunteer_history")
    object ProfileEdit     : Screen("profile_edit")
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
        fun createRoute(newsId: String) = "news_detail/$newsId"
    }
    object FavoriteStores  : Screen("favorite_stores")
    object BlockManage     : Screen("block_manage")
    object Help            : Screen("help")
    object Privacy         : Screen("privacy")
}

// ─── 공유 BottomNavigationBar ──────────────────────────────────────────────────

private data class BottomNavEntry(
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
    val label: String,
    val route: String,
)

private val bottomNavEntries = listOf(
    BottomNavEntry(iconRes = R.drawable.ic_home,      label = "홈",   route = Screen.Home.route),
    BottomNavEntry(iconRes = R.drawable.ic_handshake, label = "매칭", route = Screen.Matching.route),
    BottomNavEntry(iconRes = R.drawable.ic_map,       label = "지도", route = Screen.Map.route),
    BottomNavEntry(iconRes = R.drawable.ic_newsmode,  label = "소식", route = Screen.News.route),
    BottomNavEntry(iconRes = R.drawable.ic_person,    label = "마이", route = Screen.My.route),
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (entry.icon != null) {
                            Icon(imageVector = entry.icon, contentDescription = entry.label, tint = Color.White, modifier = Modifier.size(18.dp))
                        } else if (entry.iconRes != null) {
                            Icon(painter = painterResource(entry.iconRes), contentDescription = entry.label, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
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
                        if (entry.icon != null) {
                            Icon(imageVector = entry.icon, contentDescription = entry.label, tint = Color(0xFFC4A882), modifier = Modifier.size(22.dp))
                        } else if (entry.iconRes != null) {
                            Icon(painter = painterResource(entry.iconRes), contentDescription = entry.label, tint = Color(0xFFC4A882), modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── NavHost ───────────────────────────────────────────────────────────────────

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    val app = LocalContext.current.applicationContext as SiheungGagaeApp
    LaunchedEffect(Unit) {
        app.sessionExpiredChannel.receiveAsFlow().collect {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.AutoSplash.route,
    ) {
        // 재호-1: 로고 스플래시 + 토큰 확인 → 홈 or 시작 화면
        composable(Screen.AutoSplash.route) {
            AutoSplashScreen(
                onHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onStartScreen = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Splash.route) {
            StartScreen(
                onLogin = { navController.navigate(Screen.Login.route) },
                onSignup = { navController.navigate(Screen.SignUp.route) },
            )
        }

        composable(Screen.SignUp.route) {
            val signUpContext = LocalContext.current
            val signUpRepo = remember {
                val a = signUpContext.applicationContext as SiheungGagaeApp
                AuthRepository(a.tokenManager, a.fcmTokenManager)
            }
            val signUpViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(signUpRepo))

            SignUpScreen(
                viewModel = signUpViewModel,
                onBack = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Login.route) {
            val context = LocalContext.current
            val authRepository = remember {
                val a = context.applicationContext as SiheungGagaeApp
                AuthRepository(a.tokenManager, a.fcmTokenManager)
            }
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(authRepository))

            LoginScreen(
                viewModel = authViewModel,
                onBack = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLogin = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Home.route) {
            val homeNotifRepo = remember { NotificationRepository() }
            val unreadCountState = remember { mutableStateOf(0) }
            val homeLifecycle = LocalLifecycleOwner.current.lifecycle
            LaunchedEffect(homeLifecycle) {
                homeLifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    runCatching { homeNotifRepo.getUnreadCount() }.onSuccess { resp ->
                        if (resp.isSuccessful) unreadCountState.value = resp.body()?.unreadCount ?: 0
                    }
                }
            }
            HomeScreen(
                unreadCount = unreadCountState.value,
                onNotificationClick = { navController.navigate(Screen.Notification.route) },
                onNavigate = { route -> navController.navigateTab(route) },
                onPlaceDetailClick = { placeId -> navController.navigate(Screen.PlaceDetail.createRoute(placeId)) },
                onNewsDetailClick = { newsId -> navController.navigate(Screen.NewsDetail.createRoute(newsId)) },
            )
        }

        composable(Screen.Notification.route) {
            val notifApp = LocalContext.current.applicationContext as SiheungGagaeApp
            val notifViewModel: NotificationViewModel = viewModel(
                factory = NotificationViewModel.Factory(
                    NotificationRepository(notifApp.localNotificationStore)
                )
            )
            NotificationScreen(
                viewModel = notifViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Matching.route) {
            // 뷰모델 생성 및 주입
            val context = LocalContext.current
            val app = context.applicationContext as SiheungGagaeApp
            val api = com.example.siheunggagae.data.network.RetrofitClient.api // Retrofit 설정에 맞게 수정 필요
            val repository = remember { com.example.siheunggagae.data.repository.MatchRepository(api) }
            val viewModel: com.example.siheunggagae.ui.viewmodel.MatchingViewModel = viewModel(
                factory = com.example.siheunggagae.ui.viewmodel.MatchingViewModel.Factory(repository)
            )

            MatchingScreen(
                viewModel = viewModel, // 추가된 부분!
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

            //뷰모델 생성 부분 추가
            val api = com.example.siheunggagae.data.network.RetrofitClient.api
            val detailViewModel: com.example.siheunggagae.ui.viewmodel.MatchDetailViewModel = viewModel(
                factory = com.example.siheunggagae.ui.viewmodel.MatchDetailViewModel.Factory(api)
            )

            MatchingDetailScreen(
                requestId = requestId,
                viewModel = detailViewModel, //생성한 뷰모델을 화면에 주입.
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


        composable(
            route = Screen.RequestFlow.route, // 이제 "request_flow?requestId={requestId}"가 됨
            arguments = listOf(navArgument("requestId") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getInt("requestId") ?: 0

            val api = com.example.siheunggagae.data.network.RetrofitClient.api
            val requestViewModel: com.example.siheunggagae.ui.viewmodel.RequestViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = com.example.siheunggagae.ui.viewmodel.RequestViewModel.Factory(api)
            )

            RequestFlowScreen(
                viewModel = requestViewModel,
                matchId = requestId, // 여기서 추출한 ID를 넘겨줍니다!
                onBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() },
                onAddPet = { navController.navigate(Screen.PetAdd.route) }
            )
        }

        composable(Screen.MyRequests.route) {
            val api = com.example.siheunggagae.data.network.RetrofitClient.api
            val myRequestsViewModel: com.example.siheunggagae.ui.viewmodel.MyRequestsViewModel = viewModel(
                factory = com.example.siheunggagae.ui.viewmodel.MyRequestsViewModel.Factory(api)
            )

            MyRequestsScreen(
                viewModel = myRequestsViewModel,
                onBack = { navController.popBackStack() },
                onCardClick = { requestId ->
                    // 카드를 클릭하면 해당 요청의 상세 화면으로 이동합니다.
                    navController.navigate(Screen.MatchingDetail.createRoute(requestId))
                }
            )
        }

        composable(
            route = "${Screen.Map.route}?volunteerMode={volunteerMode}",
            arguments = listOf(navArgument("volunteerMode") {
                type = NavType.BoolType
                defaultValue = false
            }),
        ) { backStackEntry ->
            val volunteerMode = backStackEntry.arguments?.getBoolean("volunteerMode") ?: false
            MapScreen(
                onNavigate = { route ->
                    if (route == Screen.Home.route || route == Screen.Matching.route ||
                        route.startsWith(Screen.Map.route) || route == Screen.News.route ||
                        route == Screen.My.route
                    ) navController.navigateTab(route)
                    else navController.navigate(route)
                },
                startVolunteerMode = volunteerMode,
            )
        }

        composable(Screen.News.route) {
            val newsNotifRepo = remember { NotificationRepository() }
            val newsUnreadCount = remember { mutableStateOf(0) }
            val newsLifecycle = LocalLifecycleOwner.current.lifecycle
            LaunchedEffect(newsLifecycle) {
                newsLifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    runCatching { newsNotifRepo.getUnreadCount() }.onSuccess { resp ->
                        if (resp.isSuccessful) newsUnreadCount.value = resp.body()?.unreadCount ?: 0
                    }
                }
            }
            NewsScreen(
                unreadCount = newsUnreadCount.value,
                onNotificationClick = { navController.navigate(Screen.Notification.route) },
                onNewsDetailClick = { newsId ->
                    navController.navigate(Screen.NewsDetail.createRoute(newsId))
                },
                onPlaceDetailClick = { placeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(placeId))
                },
                onNavigate = { route -> navController.navigateTab(route) }
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
            val myContext = LocalContext.current
            val myApp = myContext.applicationContext as SiheungGagaeApp
            val myScope = rememberCoroutineScope()
            val myAuthRepo = remember { AuthRepository(myApp.tokenManager, myApp.fcmTokenManager) }
            val myViewModel: MyViewModel = viewModel(factory = MyViewModel.Factory(UserRepository()))
            val myLifecycle = LocalLifecycleOwner.current.lifecycle
            val localImageUri by myApp.tokenManager.localProfileImageUri.collectAsState()

            // 다른 화면에서 돌아올 때마다 데이터 새로고침
            LaunchedEffect(myLifecycle) {
                myLifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    myViewModel.fetchData()
                }
            }

            MyScreen(
                viewModel = myViewModel,
                localImageUri = localImageUri,
                onNavigate = { route -> navController.navigateTab(route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onPetListClick = { navController.navigate(Screen.PetList.route) },
                onBadgeListClick = { navController.navigate(Screen.VolunteerBadgeList.route) },
                onVolunteerHistoryClick = { navController.navigate(Screen.VolunteerHistory.route) },
                onFavoriteStoresClick = { navController.navigate(Screen.FavoriteStores.route) },
                onEditProfileClick = { navController.navigate(Screen.ProfileEdit.route) },
                onVolunteerApplyClick = { navController.navigate(Screen.VolunteerApply.route) },
                onLogout = {
                    myScope.launch { myAuthRepo.logout() }
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.ProfileEdit.route) {
            val peContext = LocalContext.current
            val peApp = peContext.applicationContext as SiheungGagaeApp
            val peViewModel: ProfileEditViewModel = viewModel(
                factory = ProfileEditViewModel.Factory(UserRepository(), peApp.tokenManager)
            )
            ProfileEditScreen(
                viewModel = peViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Settings.route) {
            val settingsContext = LocalContext.current
            val settingsApp = settingsContext.applicationContext as SiheungGagaeApp
            val settingsScope = rememberCoroutineScope()
            val settingsAuthRepo = remember { AuthRepository(settingsApp.tokenManager, settingsApp.fcmTokenManager) }
            val notifViewModel: NotificationSettingsViewModel = viewModel(
                factory = NotificationSettingsViewModel.Factory(UserRepository()),
            )
            val locationViewModel: LocationSettingsViewModel = viewModel(
                factory = LocationSettingsViewModel.Factory(UserRepository()),
            )
            val accountViewModel: AccountSettingsViewModel = viewModel(
                factory = AccountSettingsViewModel.Factory(UserRepository()),
            )

            SettingsScreen(
                onBack = { navController.popBackStack() },
                onProfileEditClick = { navController.navigate(Screen.ProfileEdit.route) },
                onPetListClick = { navController.navigate(Screen.PetList.route) },
                onVolunteerHistoryClick = { navController.navigate(Screen.VolunteerHistory.route) },
                onBlockManageClick = { navController.navigate(Screen.BlockManage.route) },
                onHelpClick = { navController.navigate(Screen.Help.route) },
                onAccountDeleted = {
                    settingsScope.launch { settingsAuthRepo.logout() }
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                notifViewModel = notifViewModel,
                locationViewModel = locationViewModel,
                accountViewModel = accountViewModel,
            )
        }


        composable(Screen.PetList.route) {
            val petListViewModel: PetListViewModel = viewModel(
                factory = PetListViewModel.Factory(UserRepository())
            )
            val petListLifecycle = LocalLifecycleOwner.current.lifecycle
            LaunchedEffect(petListLifecycle) {
                petListLifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    petListViewModel.fetchPets()
                }
            }
            PetListScreen(
                viewModel = petListViewModel,
                onBack = { navController.popBackStack() },
                onAddPet = { navController.navigate(Screen.PetAdd.route) },
                onEditPet = { petId -> navController.navigate(Screen.PetAdd.editRoute(petId)) },
            )
        }

        composable(
            route = Screen.PetAdd.route,
            arguments = listOf(navArgument("petId") {
                type = NavType.IntType
                defaultValue = -1
            }),
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getInt("petId")?.takeIf { it != -1 }
            val petAddContext = LocalContext.current
            val petAddViewModel: PetAddViewModel = viewModel(
                factory = PetAddViewModel.Factory(
                    petAddContext.applicationContext as android.app.Application,
                    UserRepository(),
                    petId,
                    (petAddContext.applicationContext as SiheungGagaeApp).localNotificationStore,
                )
            )
            PetAddScreen(
                viewModel = petAddViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.VolunteerApply.route) {
            val volunteerApplyViewModel: com.example.siheunggagae.ui.viewmodel.VolunteerApplyViewModel = viewModel(
                factory = com.example.siheunggagae.ui.viewmodel.VolunteerApplyViewModel.Factory(UserRepository())
            )
            VolunteerApplyScreen(
                viewModel = volunteerApplyViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.VolunteerBadgeList.route) {
            val badgeViewModel: VolunteerBadgeViewModel = viewModel(
                factory = VolunteerBadgeViewModel.Factory(UserRepository())
            )
            VolunteerBadgeListScreen(
                viewModel = badgeViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.VolunteerHistory.route) {
            val historyViewModel: VolunteerHistoryViewModel = viewModel(
                factory = VolunteerHistoryViewModel.Factory(UserRepository())
            )
            VolunteerHistoryScreen(
                viewModel = historyViewModel,
                onBack = { navController.popBackStack() },
                onMatchClick = { matchId ->
                    navController.navigate(Screen.MatchingPublicDetail.createRoute(matchId))
                },
                onVolunteerApplyClick = { navController.navigate(Screen.VolunteerApply.route) },
            )
        }

        composable(
            route = Screen.NewsDetail.route,
            arguments = listOf(navArgument("newsId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId") ?: ""
            NewsDetailScreen(
                newsId = newsId,
                onBack = { navController.popBackStack() },
                onRelatedClick = { id -> navController.navigate(Screen.NewsDetail.createRoute(id)) },
            )
        }

        composable(Screen.FavoriteStores.route) {
            val favViewModel: FavoriteStoresViewModel = viewModel(
                factory = FavoriteStoresViewModel.Factory(UserRepository())
            )
            FavoriteStoresScreen(
                viewModel = favViewModel,
                onBack = { navController.popBackStack() },
                onPlaceDetailClick = { storeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(storeId))
                },
            )
        }

        composable(Screen.BlockManage.route) {
            val blockViewModel: BlockManageViewModel = viewModel(
                factory = BlockManageViewModel.Factory(UserRepository())
            )
            BlockManageScreen(
                viewModel = blockViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Help.route) {
            HelpScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Privacy.route) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
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
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
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
fun AppNavGraphPreview() {
    SiheungGagaeTheme { AppNavGraph() }
}
