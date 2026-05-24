package com.example.siheunggagae

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.example.siheunggagae.data.repository.NotificationRepository
import com.example.siheunggagae.data.repository.PetHotelRepository
import com.example.siheunggagae.data.repository.UserRepository
import com.example.siheunggagae.ui.screen.PetHotelCompareScreen
import com.example.siheunggagae.ui.screen.PetHotelMatrixCompareScreen
import com.example.siheunggagae.ui.viewmodel.PetHotelCompareViewModel
import com.example.siheunggagae.ui.viewmodel.PetHotelMatrixCompareViewModel
import com.example.siheunggagae.ui.viewmodel.AuthViewModel
import com.example.siheunggagae.ui.viewmodel.MyViewModel
import com.example.siheunggagae.ui.viewmodel.NotificationViewModel
import com.example.siheunggagae.ui.viewmodel.PetAddViewModel
import com.example.siheunggagae.ui.viewmodel.PetListViewModel
import com.example.siheunggagae.ui.viewmodel.ProfileEditViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.core.animateDpAsState
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.example.siheunggagae.ui.screen.MapPinPickerScreen
import com.example.siheunggagae.ui.screen.MyStoreRequestsScreen
import com.example.siheunggagae.ui.screen.StoreRequestDetailScreen
import com.example.siheunggagae.ui.screen.StoreRequestFormScreen
import com.example.siheunggagae.ui.screen.VolunteerApplyScreen
import com.example.siheunggagae.ui.screen.VolunteerBadgeListScreen
import com.example.siheunggagae.ui.screen.VolunteerHistoryScreen
import com.example.siheunggagae.ui.screen.MatchReviewScreen
import com.example.siheunggagae.ui.viewmodel.BlockManageViewModel
import com.example.siheunggagae.ui.viewmodel.MapPinPickerViewModel
import com.example.siheunggagae.ui.viewmodel.MyStoreRequestsViewModel
import com.example.siheunggagae.ui.viewmodel.StoreRequestDetailViewModel
import com.example.siheunggagae.ui.viewmodel.StoreRequestFormViewModel
import com.example.siheunggagae.data.model.StoreRequestType
import com.example.siheunggagae.data.repository.StoreRequestRepository
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
import com.example.siheunggagae.ui.viewmodel.MatchDetailViewModel
import com.example.siheunggagae.ui.viewmodel.MatchReviewViewModel
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
    object Map          : Screen("map") {
        fun createRoute(focusLat: Double, focusLng: Double, focusStoreId: Int = 0) =
            "map?volunteerMode=false&focusLat=$focusLat&focusLng=$focusLng&focusStoreId=$focusStoreId"
    }
    object News         : Screen("news")
    object PlaceDetail  : Screen("place_detail/{placeId}?lat={lat}&lng={lng}") {
        fun createRoute(placeId: Int, lat: Double = 0.0, lng: Double = 0.0) =
            "place_detail/$placeId?lat=$lat&lng=$lng"
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
    object Chat             : Screen("chat/{matchId}/{applicationId}") {
        fun createRoute(matchId: Int, applicationId: Int) = "chat/$matchId/$applicationId"
    }
    object NewsDetail      : Screen("news_detail/{newsId}") {
        fun createRoute(newsId: String) = "news_detail/$newsId"
    }
    object MatchReview : Screen("match_review?matchId={matchId}&status={status}&isViewOnly={isViewOnly}&canEdit={canEdit}") {
        fun createRoute(matchId: Int, status: String, isViewOnly: Boolean = false, canEdit: Boolean = false) =
            "match_review?matchId=$matchId&status=$status&isViewOnly=$isViewOnly&canEdit=$canEdit"
    }
    object PetHotelCompare : Screen("pet_hotel_compare?lat={lat}&lng={lng}&radius={radius}") {
        const val ARG_LAT = "lat"
        const val ARG_LNG = "lng"
        const val ARG_RADIUS = "radius"

        fun createRoute(lat: Double? = null, lng: Double? = null, radius: Int? = null): String {
            val parts = buildList {
                lat?.let { add("lat=$it") }
                lng?.let { add("lng=$it") }
                radius?.let { add("radius=$it") }
            }
            return if (parts.isEmpty()) "pet_hotel_compare"
            else "pet_hotel_compare?" + parts.joinToString("&")
        }
    }
    object PetHotelMatrixCompare : Screen("pet_hotel_matrix_compare?lat={lat}&lng={lng}&radius={radius}") {
        const val ARG_LAT = "lat"
        const val ARG_LNG = "lng"
        const val ARG_RADIUS = "radius"

        fun createRoute(lat: Double, lng: Double, radius: Int? = null): String {
            val parts = mutableListOf("lat=$lat", "lng=$lng")
            radius?.let { parts += "radius=$it" }
            return "pet_hotel_matrix_compare?" + parts.joinToString("&")
        }
    }
    object FavoriteStores  : Screen("favorite_stores")
    object BlockManage     : Screen("block_manage")
    object Help            : Screen("help")
    object Privacy         : Screen("privacy")

    object MyStoreRequests : Screen("my_store_requests")

    object StoreRequestForm : Screen(
        "store_request_form?type={type}&storeId={storeId}&requestId={requestId}",
    ) {
        const val ARG_TYPE = "type"
        const val ARG_STORE_ID = "storeId"
        const val ARG_REQUEST_ID = "requestId"

        fun createRoute(
            type: com.example.siheunggagae.data.model.StoreRequestType,
            storeId: Int? = null,
            requestId: Int? = null,
        ): String {
            val sb = StringBuilder("store_request_form?type=$type")
            storeId?.let { sb.append("&storeId=$it") }
            requestId?.let { sb.append("&requestId=$it") }
            return sb.toString()
        }
    }

    object StoreRequestDetail : Screen("store_request_detail/{requestId}") {
        const val ARG_REQUEST_ID = "requestId"
        fun createRoute(requestId: Int): String = "store_request_detail/$requestId"
    }

    object MapPinPicker : Screen("map_pin_picker?lat={lat}&lng={lng}") {
        const val ARG_LAT = "lat"
        const val ARG_LNG = "lng"
        const val RESULT_LAT = "picked_lat"
        const val RESULT_LNG = "picked_lng"
        const val RESULT_ADDRESS = "picked_address"

        fun createRoute(lat: Double? = null, lng: Double? = null): String {
            if (lat == null && lng == null) return "map_pin_picker"
            val parts = buildList {
                lat?.let { add("lat=$it") }
                lng?.let { add("lng=$it") }
            }
            return "map_pin_picker?" + parts.joinToString("&")
        }
    }
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

// ─── 애니메이션 튜닝 상수 ─────────────────────────────────────────────────────
// iOS 표준 ease-out (cubic-bezier(0.32, 0.72, 0, 1)) — 빠르게 시작해서 부드럽게 정착.
// Apple Human Interface Guidelines 의 system curve 와 같은 특성.
private val AppleEaseOut = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
// iOS 표준 ease-in-out — push/pop 전환의 자연스러운 가속/감속.
private val AppleEaseInOut = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

// 화면 전환 시간 (Apple 느낌의 충분히 긴 듀레이션)
private const val SCREEN_ENTER_MS = 380
private const val SCREEN_EXIT_MS = 320
private const val SCREEN_FADE_MS = 280

// 도크 morph 듀레이션. 화면 전환과 동시에 끝나도록 살짝 짧게 잡는다.
// spring 대신 tween — overshoot 없이 정확하게 정착해서 전환 끝난 뒤 흔들림 제거.
private const val DOCK_MORPH_MS = 320
private val DockColorAnim = tween<Color>(DOCK_MORPH_MS, easing = AppleEaseOut)
private val DockDpAnim = tween<Dp>(DOCK_MORPH_MS, easing = AppleEaseOut)
private val DockFloatAnim = tween<Float>(DOCK_MORPH_MS, easing = AppleEaseOut)
private val DockSizeAnim = tween<androidx.compose.ui.unit.IntSize>(
    DOCK_MORPH_MS,
    easing = AppleEaseOut,
)

// 도크 아이콘 크기 — 선택/미선택 모두 동일하게 유지해서 통일감 확보.
private val DockIconSize = 22.dp

// LookaheadScope 가 자식 bounds 변화를 사전 측정해 위치를 매끄럽게 보간하도록 하는 BoundsTransform.
// SpaceEvenly 가 픽셀 단위로 위치를 재계산할 때 생기는 미세한 점프(stutter)를 제거한다.
@OptIn(ExperimentalSharedTransitionApi::class)
private val DockBoundsTransform = BoundsTransform { _, _ ->
    tween(DOCK_MORPH_MS, easing = AppleEaseOut)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppBottomBar(currentRoute: String, onNavigate: (String) -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 6.dp),
    ) {
        LookaheadScope {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(50.dp))
                    .clip(RoundedCornerShape(50.dp))
                    // 살짝 frosted — Material3 가 native blur 를 ModalBottomSheet 외엔 노출하지 않으므로 alpha + 강화된 elevation 으로 iOS Liquid Glass 톤 흉내.
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                bottomNavEntries.forEach { entry ->
                    BottomNavItem(
                        modifier = Modifier.animateBounds(
                            lookaheadScope = this@LookaheadScope,
                            boundsTransform = DockBoundsTransform,
                        ),
                        entry = entry,
                        selected = currentRoute == entry.route,
                        onClick = { onNavigate(entry.route) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    modifier: Modifier = Modifier,
    entry: BottomNavEntry,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF1A1A1A) else Color.Transparent,
        animationSpec = DockColorAnim,
        label = "dockBg",
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFFC4A882),
        animationSpec = DockColorAnim,
        label = "dockIcon",
    )
    val hPadding by animateDpAsState(
        targetValue = if (selected) 14.dp else 10.dp,
        animationSpec = DockDpAnim,
        label = "dockPad",
    )
    // 선택 시 아이콘 약간 키워서 "weight" 변화처럼 — Material Symbols Extended 가 weight axis 를 노출하지 않아 size morph 로 대체.
    val iconSize by animateDpAsState(
        targetValue = if (selected) 24.dp else 22.dp,
        animationSpec = DockDpAnim,
        label = "dockIconSize",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = hPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (entry.icon != null) {
            Icon(
                imageVector = entry.icon,
                contentDescription = entry.label,
                tint = iconColor,
                modifier = Modifier.size(iconSize),
            )
        } else if (entry.iconRes != null) {
            Icon(
                painter = painterResource(entry.iconRes),
                contentDescription = entry.label,
                tint = iconColor,
                modifier = Modifier.size(iconSize),
            )
        }
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(animationSpec = DockSizeAnim) +
                fadeIn(animationSpec = DockFloatAnim),
            exit = shrinkHorizontally(animationSpec = DockSizeAnim) +
                fadeOut(animationSpec = DockFloatAnim),
        ) {
            Text(
                text = entry.label,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp,
                color = Color.White,
                maxLines = 1,
            )
        }
    }
}

// ─── 화면 전환 애니메이션 헬퍼 ─────────────────────────────────────────────────

private fun isTopLevelTabRoute(route: String?): Boolean {
    if (route == null) return false
    return route == Screen.Home.route ||
        route == Screen.Matching.route ||
        route.startsWith(Screen.Map.route) ||
        route == Screen.News.route ||
        route == Screen.My.route
}

private fun isTopLevelTab(entry: NavBackStackEntry): Boolean =
    isTopLevelTabRoute(entry.destination.route)

// 실제 라우트(예: "map?volunteerMode=false")를 탭 슬롯과 매칭되도록 정규화.
private fun normalizeTabRoute(route: String?): String =
    when {
        route == null -> ""
        route.startsWith(Screen.Map.route) -> Screen.Map.route
        else -> route
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

    // 단일 공유 BottomBar — 탭 라우트일 때만 표시하고, 라우트가 바뀔 때만
    // morph 애니메이션이 일어나도록 NavHost 위에 한 번만 mount.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = isTopLevelTabRoute(currentRoute)
    val tabRoute = normalizeTabRoute(currentRoute)

    Box(modifier = Modifier.fillMaxSize()) {
    NavHost(
        navController = navController,
        startDestination = Screen.AutoSplash.route,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            if (isTopLevelTab(initialState) && isTopLevelTab(targetState)) {
                // Shared Axis Z: 같은 레벨 탭 전환 — fade + 살짝 줌인
                fadeIn(animationSpec = tween(SCREEN_FADE_MS, easing = AppleEaseOut)) +
                    scaleIn(initialScale = 0.94f, animationSpec = tween(SCREEN_ENTER_MS, easing = AppleEaseOut))
            } else {
                // Shared Axis X: 푸시 — 오른쪽에서 슬라이드 인 + fade
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(SCREEN_ENTER_MS, easing = AppleEaseInOut),
                ) + fadeIn(animationSpec = tween(SCREEN_FADE_MS, easing = AppleEaseOut))
            }
        },
        exitTransition = {
            if (isTopLevelTab(initialState) && isTopLevelTab(targetState)) {
                fadeOut(animationSpec = tween(SCREEN_FADE_MS, easing = AppleEaseOut)) +
                    scaleOut(targetScale = 1.04f, animationSpec = tween(SCREEN_EXIT_MS, easing = AppleEaseOut))
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(SCREEN_EXIT_MS, easing = AppleEaseInOut),
                ) + fadeOut(animationSpec = tween(SCREEN_FADE_MS, easing = AppleEaseOut))
            }
        },
        popEnterTransition = {
            if (isTopLevelTab(initialState) && isTopLevelTab(targetState)) {
                fadeIn(animationSpec = tween(SCREEN_FADE_MS, easing = AppleEaseOut)) +
                    scaleIn(initialScale = 0.94f, animationSpec = tween(SCREEN_ENTER_MS, easing = AppleEaseOut))
            } else {
                // 뒤로가기: 왼쪽에서 슬라이드 인
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(SCREEN_ENTER_MS, easing = AppleEaseInOut),
                ) + fadeIn(animationSpec = tween(SCREEN_FADE_MS, easing = AppleEaseOut))
            }
        },
        popExitTransition = {
            if (isTopLevelTab(initialState) && isTopLevelTab(targetState)) {
                fadeOut(animationSpec = tween(SCREEN_FADE_MS, easing = AppleEaseOut)) +
                    scaleOut(targetScale = 1.04f, animationSpec = tween(SCREEN_EXIT_MS, easing = AppleEaseOut))
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(SCREEN_EXIT_MS, easing = AppleEaseInOut),
                ) + fadeOut(animationSpec = tween(SCREEN_FADE_MS, easing = AppleEaseOut))
            }
        },
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
                onPlaceDetailClick = { placeId, lat, lng -> navController.navigate(Screen.PlaceDetail.createRoute(placeId, lat, lng)) },
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
                onItemClick = { item ->
                    handleNotificationDeeplink(item.link, navController)
                },
            )
        }

        composable(Screen.Matching.route) {
            val context = LocalContext.current
            val locationProvider = remember { com.example.siheunggagae.data.location.LocationProvider(context) }
            val locationPermission = com.example.siheunggagae.ui.util.rememberLocationPermissionState()
            val currentUserStore = remember { com.example.siheunggagae.data.local.CurrentUserStore(context) }
            val currentUserId = currentUserStore.userId()
            val currentUserNickname = currentUserStore.nickname()

            val api = com.example.siheunggagae.data.network.RetrofitClient.api

            // EffectiveCenter: GPS 가 시흥 안이면 GPS, 밖이면 사용자 등록 동네, 그것도 없으면 시청.
            // 비동기로 채워서 runBlocking ANR 회피. 결과는 EffectiveCenterCache 에 3분간 캐싱.
            val effectiveCenter = remember {
                mutableStateOf<com.example.siheunggagae.data.location.EffectiveCenter?>(null)
            }
            LaunchedEffect(locationPermission.hasPermission) {
                // 권한이 변경되면 캐시를 무효화하고 다시 판정.
                com.example.siheunggagae.data.location.EffectiveCenterCache.invalidate()
                effectiveCenter.value = com.example.siheunggagae.data.location
                    .resolveEffectiveCenter(api, locationProvider)
            }

            val repository = remember { com.example.siheunggagae.data.repository.MatchRepository(api) }
            val viewModel: com.example.siheunggagae.ui.viewmodel.MatchingViewModel = viewModel(
                factory = com.example.siheunggagae.ui.viewmodel.MatchingViewModel.Factory(
                    repository = repository,
                    isCurrentUserVolunteer = { currentUserStore.isVolunteer() },
                    getCurrentLocation = { effectiveCenter.value?.latLng },
                )
            )

            // 첫 fetch 는 effectiveCenter 미도착(null)으로 위치 없이 실행됨 → 도착 후 한 번 refresh.
            // 이후에는 좌표가 실제로 바뀐 경우에만 refresh — source 변경(GPS→프로필)은 재호출 불필요.
            val prevCenterLatLng = remember { mutableStateOf<Pair<Double, Double>?>(null) }
            LaunchedEffect(effectiveCenter.value) {
                val current = effectiveCenter.value ?: return@LaunchedEffect
                if (current.latLng != prevCenterLatLng.value) {
                    prevCenterLatLng.value = current.latLng
                    viewModel.refresh()
                }
            }

            MatchingScreen(
                viewModel = viewModel,
                onMyRequestsClick = { navController.navigate(Screen.MyRequests.route) },
                onRequestFlowClick = { navController.navigate(Screen.RequestFlow.createRoute(0)) },
                onCardClick = { matchId, isMine ->
                    if (isMine) {
                        navController.navigate(Screen.MatchingDetail.createRoute(matchId))
                    } else {
                        navController.navigate(Screen.MatchingPublicDetail.createRoute(matchId))
                    }
                },
                onVolunteerApplyClick = { navController.navigate(Screen.VolunteerApply.route) },
                onSearchClick = { /* no-op */ },
                locationAvailable = effectiveCenter.value != null,
                centerFallback = effectiveCenter.value?.takeIf { it.isFallback },
                currentUserId = currentUserId,
                currentUserNickname = currentUserNickname,
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

            // 1. 뷰모델 프로바이더 팩토리를 이용해 뷰모델 인스턴스를 생성합니다.
            val matchDetailViewModel: MatchDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = MatchDetailViewModel.Factory(com.example.siheunggagae.data.network.RetrofitClient.api)
            )

            MatchingPublicDetailScreen(
                requestId = requestId,
                viewModel = matchDetailViewModel, // 2. 👈 여기에 방금 만든 뷰모델을 쏙 넣어줍니다!
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) },
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("matchId") { type = NavType.IntType },
                navArgument("applicationId") { type = NavType.IntType }
            ),
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getInt("matchId") ?: 0
            val applicationId = backStackEntry.arguments?.getInt("applicationId") ?: 0

            val context = LocalContext.current
            val app = context.applicationContext as SiheungGagaeApp
            val api = com.example.siheunggagae.data.network.RetrofitClient.api

            val wsManager = remember(applicationId) {
                com.example.siheunggagae.data.network.ChatWebSocketManager(
                    tokenManager = app.tokenManager,
                    refreshToken = {
                        try {
                            val resp = api.getMe()
                            resp.isSuccessful
                        } catch (e: Exception) {
                            false
                        }
                    }
                )
            }

            val chatViewModel: com.example.siheunggagae.ui.viewmodel.ChatViewModel = viewModel(
                factory = com.example.siheunggagae.ui.viewmodel.ChatViewModel.Factory(api, wsManager)
            )

            ChatScreen(
                matchId = matchId,
                applicationId = applicationId,
                viewModel = chatViewModel,
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

        // NavGraph.kt의 MyRequests 컴포저블 구역을 아래 코드로 예쁘게 이어주세요!

        composable(Screen.MyRequests.route) {
            val api = com.example.siheunggagae.data.network.RetrofitClient.api
            val myRequestsViewModel: com.example.siheunggagae.ui.viewmodel.MyRequestsViewModel = viewModel(
                factory = com.example.siheunggagae.ui.viewmodel.MyRequestsViewModel.Factory(api)
            )

            MyRequestsScreen(
                viewModel = myRequestsViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToCreate = {
                    // 9.1 새 요청 클릭 시 폼 작성 화면으로 전환
                    navController.navigate("request_flow?requestId=0")
                },
                onCardClick = { requestId ->
                    // 9.5 카드를 클릭하면 상세 화면(요청자 시점)으로 매끄럽게 연결
                    navController.navigate(Screen.MatchingDetail.createRoute(requestId))
                }
            )
        }
        composable(
            route = Screen.MatchReview.route,
            arguments = listOf(
                navArgument("matchId") { type = NavType.IntType; defaultValue = -1 },
                navArgument("status") { type = NavType.StringType; defaultValue = "" },
                navArgument("isViewOnly") { type = NavType.BoolType; defaultValue = false },
                navArgument("canEdit") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getInt("matchId") ?: -1
            val status = backStackEntry.arguments?.getString("status") ?: ""
            val isViewOnly = backStackEntry.arguments?.getBoolean("isViewOnly") ?: false
            val canEdit = backStackEntry.arguments?.getBoolean("canEdit") ?: false
            val api = com.example.siheunggagae.data.network.RetrofitClient.api

            val reviewViewModel: MatchReviewViewModel = viewModel(
                factory = MatchReviewViewModel.Factory(api)
            )

            MatchReviewScreen(
                matchId = matchId,
                matchStatus = status,
                isViewOnly = isViewOnly,
                canEdit = canEdit,
                viewModel = reviewViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Screen.Map.route}?volunteerMode={volunteerMode}&focusLat={focusLat}&focusLng={focusLng}&focusStoreId={focusStoreId}",
            arguments = listOf(
                navArgument("volunteerMode") { type = NavType.BoolType; defaultValue = false },
                navArgument("focusLat") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("focusLng") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("focusStoreId") { type = NavType.IntType; defaultValue = 0 },
            ),
        ) { backStackEntry ->
            val volunteerMode = backStackEntry.arguments?.getBoolean("volunteerMode") ?: false
            val focusLat = backStackEntry.arguments?.getString("focusLat")?.toDoubleOrNull() ?: 0.0
            val focusLng = backStackEntry.arguments?.getString("focusLng")?.toDoubleOrNull() ?: 0.0
            val focusStoreId = backStackEntry.arguments?.getInt("focusStoreId") ?: 0
            MapScreen(
                onNavigate = { route ->
                    if (route == Screen.Home.route || route == Screen.Matching.route ||
                        route.startsWith(Screen.Map.route) || route == Screen.News.route ||
                        route == Screen.My.route
                    ) navController.navigateTab(route)
                    else navController.navigate(route)
                },
                startVolunteerMode = volunteerMode,
                focusLat = focusLat,
                focusLng = focusLng,
                focusStoreId = focusStoreId,
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
            arguments = listOf(
                navArgument("placeId") { type = NavType.IntType },
                navArgument("lat") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("lng") { type = NavType.StringType; defaultValue = "0.0" },
            ),
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getInt("placeId") ?: 0
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            PlaceDetailScreen(
                placeId = placeId,
                initialLat = lat,
                initialLng = lng,
                onBack = { navController.popBackStack() },
                onNavigateToMap = { lat, lng, storeId ->
                    navController.navigate(Screen.Map.createRoute(lat, lng, storeId)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onEditRequestClick = { sid ->
                    navController.navigate(
                        Screen.StoreRequestForm.createRoute(
                            type = StoreRequestType.UPDATE,
                            storeId = sid,
                        ),
                    )
                },
            )
        }

        composable(
            route = Screen.PetHotelCompare.route,
            arguments = listOf(
                navArgument(Screen.PetHotelCompare.ARG_LAT) {
                    type = NavType.StringType; nullable = true; defaultValue = null
                },
                navArgument(Screen.PetHotelCompare.ARG_LNG) {
                    type = NavType.StringType; nullable = true; defaultValue = null
                },
                navArgument(Screen.PetHotelCompare.ARG_RADIUS) {
                    type = NavType.IntType; defaultValue = 5000
                },
            ),
        ) { backStackEntry ->
            val lat = backStackEntry.arguments
                ?.getString(Screen.PetHotelCompare.ARG_LAT)?.toDoubleOrNull()
                ?: 37.3799   // 시흥시청 폴백
            val lng = backStackEntry.arguments
                ?.getString(Screen.PetHotelCompare.ARG_LNG)?.toDoubleOrNull()
                ?: 126.8030
            val radius = backStackEntry.arguments
                ?.getInt(Screen.PetHotelCompare.ARG_RADIUS)
                ?.takeIf { it > 0 }
                ?: PetHotelCompareViewModel.RADIUS_DEFAULT_M
            val vm: PetHotelCompareViewModel = viewModel(
                factory = PetHotelCompareViewModel.Factory(
                    PetHotelRepository(),
                    UserRepository(),
                    lat,
                    lng,
                    radius,
                ),
            )
            PetHotelCompareScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onStoreClick = { storeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(storeId))
                },
                onCompareClick = {
                    navController.navigate(
                        Screen.PetHotelMatrixCompare.createRoute(lat = lat, lng = lng, radius = radius),
                    )
                },
            )
        }

        composable(
            route = Screen.PetHotelMatrixCompare.route,
            arguments = listOf(
                navArgument(Screen.PetHotelMatrixCompare.ARG_LAT) {
                    type = NavType.StringType; nullable = true; defaultValue = null
                },
                navArgument(Screen.PetHotelMatrixCompare.ARG_LNG) {
                    type = NavType.StringType; nullable = true; defaultValue = null
                },
                navArgument(Screen.PetHotelMatrixCompare.ARG_RADIUS) {
                    type = NavType.IntType; defaultValue = 5000
                },
            ),
        ) { entry ->
            val lat = entry.arguments?.getString(Screen.PetHotelMatrixCompare.ARG_LAT)?.toDoubleOrNull()
                ?: 37.3799
            val lng = entry.arguments?.getString(Screen.PetHotelMatrixCompare.ARG_LNG)?.toDoubleOrNull()
                ?: 126.8030
            val radius = entry.arguments?.getInt(Screen.PetHotelMatrixCompare.ARG_RADIUS)?.takeIf { it > 0 }
                ?: PetHotelCompareViewModel.RADIUS_DEFAULT_M
            val vm: PetHotelMatrixCompareViewModel = viewModel(
                factory = PetHotelMatrixCompareViewModel.Factory(
                    PetHotelRepository(), lat, lng, radius,
                ),
            )
            PetHotelMatrixCompareScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onStoreClick = { storeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(storeId))
                },
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

            // CurrentUserStore 캐싱 — 매칭 화면에서 본인 식별 / 봉사자 안내 트리거에 사용
            val myUserStore = remember { com.example.siheunggagae.data.local.CurrentUserStore(myContext) }
            val myUiState by myViewModel.uiState.collectAsState()
            LaunchedEffect(myUiState) {
                val me = (myUiState as? com.example.siheunggagae.ui.viewmodel.MyUiState.Success)?.user
                if (me != null) myUserStore.save(me)
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
                onMyStoreRequestsClick = { navController.navigate(Screen.MyStoreRequests.route) },
                onEditProfileClick = { navController.navigate(Screen.ProfileEdit.route) },
                onVolunteerApplyClick = { navController.navigate(Screen.VolunteerApply.route) },
                onNotifSettingsClick = { navController.navigate("${Screen.Settings.route}?section=notifications") },
                onLocationSettingsClick = { navController.navigate("${Screen.Settings.route}?section=location") },
                onPrivacyClick = { navController.navigate("${Screen.Settings.route}?section=privacy") },
                onHelpClick = { navController.navigate(Screen.Help.route) },
                onLogout = {
                    myScope.launch {
                        myAuthRepo.logout()
                        com.example.siheunggagae.data.local.CurrentUserStore(myContext).clear()
                    }
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

        composable(
            route = "${Screen.Settings.route}?section={section}",
            arguments = listOf(navArgument("section") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }),
        ) { backStackEntry ->
            val section = backStackEntry.arguments?.getString("section")
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
                    settingsScope.launch {
                        settingsAuthRepo.logout()
                        com.example.siheunggagae.data.local.CurrentUserStore(settingsContext).clear()
                    }
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                notifViewModel = notifViewModel,
                locationViewModel = locationViewModel,
                accountViewModel = accountViewModel,
                initialSection = section,
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

        composable(Screen.MyStoreRequests.route) {
            val vm: MyStoreRequestsViewModel = viewModel(
                factory = MyStoreRequestsViewModel.Factory(StoreRequestRepository()),
            )
            val myStoreRequestsLifecycle = LocalLifecycleOwner.current.lifecycle
            LaunchedEffect(myStoreRequestsLifecycle) {
                myStoreRequestsLifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    vm.refresh()
                }
            }
            MyStoreRequestsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) },
            )
        }

        composable(
            route = Screen.StoreRequestForm.route,
            arguments = listOf(
                navArgument(Screen.StoreRequestForm.ARG_TYPE) {
                    type = NavType.StringType
                    defaultValue = "ADD"
                },
                navArgument(Screen.StoreRequestForm.ARG_STORE_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(Screen.StoreRequestForm.ARG_REQUEST_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                },
            ),
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString(Screen.StoreRequestForm.ARG_TYPE) ?: "ADD"
            val storeId = backStackEntry.arguments?.getInt(Screen.StoreRequestForm.ARG_STORE_ID, -1)
                ?.takeIf { it >= 0 }
            val reqId = backStackEntry.arguments?.getInt(Screen.StoreRequestForm.ARG_REQUEST_ID, -1)
                ?.takeIf { it >= 0 }
            val mode = StoreRequestType.valueOf(typeStr)
            val vm: StoreRequestFormViewModel = viewModel(
                factory = StoreRequestFormViewModel.Factory(
                    StoreRequestRepository(), mode, storeId, reqId,
                ),
            )

            // 위치 선택기 결과 핸들링 (SavedStateHandle StateFlow 방식)
            val handle = backStackEntry.savedStateHandle
            val pickedLat by handle.getStateFlow<Double?>(Screen.MapPinPicker.RESULT_LAT, null)
                .collectAsState()
            LaunchedEffect(pickedLat) {
                val lat = pickedLat ?: return@LaunchedEffect
                val lng = handle.get<Double>(Screen.MapPinPicker.RESULT_LNG) ?: return@LaunchedEffect
                val addr = handle.get<String>(Screen.MapPinPicker.RESULT_ADDRESS)
                vm.applyPickedLocation(lat, lng, addr)
                handle.remove<Double>(Screen.MapPinPicker.RESULT_LAT)
                handle.remove<Double>(Screen.MapPinPicker.RESULT_LNG)
                handle.remove<String>(Screen.MapPinPicker.RESULT_ADDRESS)
            }

            StoreRequestFormScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onPickLocation = { lat, lng ->
                    navController.navigate(Screen.MapPinPicker.createRoute(lat, lng))
                },
                onSubmitted = { id ->
                    navController.popBackStack()
                    navController.navigate(Screen.StoreRequestDetail.createRoute(id))
                },
            )
        }

        composable(
            route = Screen.StoreRequestDetail.route,
            arguments = listOf(
                navArgument(Screen.StoreRequestDetail.ARG_REQUEST_ID) {
                    type = NavType.IntType
                },
            ),
        ) { backStackEntry ->
            val reqId = backStackEntry.arguments?.getInt(Screen.StoreRequestDetail.ARG_REQUEST_ID)
                ?: return@composable
            val vm: StoreRequestDetailViewModel = viewModel(
                factory = StoreRequestDetailViewModel.Factory(StoreRequestRepository(), reqId),
            )
            StoreRequestDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSeeStore = { storeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(storeId))
                },
                onRetryRequest = { sourceId, type ->
                    navController.navigate(
                        Screen.StoreRequestForm.createRoute(type = type, requestId = sourceId),
                    )
                },
            )
        }

        composable(
            route = Screen.MapPinPicker.route,
            arguments = listOf(
                navArgument(Screen.MapPinPicker.ARG_LAT) {
                    type = NavType.StringType; nullable = true; defaultValue = null
                },
                navArgument(Screen.MapPinPicker.ARG_LNG) {
                    type = NavType.StringType; nullable = true; defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val argLat = backStackEntry.arguments?.getString(Screen.MapPinPicker.ARG_LAT)?.toDoubleOrNull()
            val argLng = backStackEntry.arguments?.getString(Screen.MapPinPicker.ARG_LNG)?.toDoubleOrNull()
            val initLat = argLat ?: 37.3799   // 시흥시청
            val initLng = argLng ?: 126.8030
            val vm: MapPinPickerViewModel = viewModel(
                factory = MapPinPickerViewModel.Factory(initLat, initLng),
            )
            MapPinPickerScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onConfirm = { lat, lng, addr ->
                    val handle = navController.previousBackStackEntry?.savedStateHandle
                    handle?.set(Screen.MapPinPicker.RESULT_LAT, lat)
                    handle?.set(Screen.MapPinPicker.RESULT_LNG, lng)
                    handle?.set(Screen.MapPinPicker.RESULT_ADDRESS, addr)
                    navController.popBackStack()
                },
            )
        }
    }

        AnimatedVisibility(
            visible = showBottomBar,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(SCREEN_ENTER_MS, easing = AppleEaseOut),
            ) + fadeIn(animationSpec = tween(SCREEN_FADE_MS, easing = AppleEaseOut)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(SCREEN_EXIT_MS, easing = AppleEaseInOut),
            ) + fadeOut(animationSpec = tween(SCREEN_FADE_MS, easing = AppleEaseOut)),
        ) {
            AppBottomBar(
                currentRoute = tabRoute,
                onNavigate = { route -> navController.navigateTab(route) },
            )
        }
    }
}

private fun NavHostController.navigateTab(route: String) {
    if (isTopLevelTabRoute(route)) {
        navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Screen.Home.route) { saveState = true }
        }
    } else {
        navigate(route)
    }
}

/** 알림 link 필드의 deeplink를 파싱해 적절한 화면으로 이동한다.
 *  처리된 경우 true, 알 수 없는 link이면 false 반환. */
private fun handleNotificationDeeplink(link: String?, navController: NavHostController): Boolean {
    if (link.isNullOrBlank()) return false
    // 매장 요청 상세: siheunggagae://store-request/<requestId>
    if (link.startsWith("siheunggagae://store-request/")) {
        val id = link.substringAfterLast("/").toIntOrNull() ?: return false
        navController.navigate(Screen.StoreRequestDetail.createRoute(id))
        return true
    }
    // 매칭 상세: siheunggagae://matching/<requestId>
    if (link.startsWith("siheunggagae://matching/")) {
        val id = link.substringAfterLast("/").toIntOrNull() ?: return false
        navController.navigate(Screen.MatchingDetail.createRoute(id))
        return true
    }
    // 채팅: siheunggagae://chat/<matchId>/<applicationId>
    if (link.startsWith("siheunggagae://chat/")) {
        val parts = link.removePrefix("siheunggagae://chat/").split("/")
        val matchId = parts.getOrNull(0)?.toIntOrNull() ?: return false
        val appId = parts.getOrNull(1)?.toIntOrNull() ?: return false
        navController.navigate(Screen.Chat.createRoute(matchId, appId))
        return true
    }
    return false
}

// ─── MyRequestsScreen (placeholder) ───────────────────────────────────────────

@Composable
fun MyRequestsScreen(
    viewModel: com.example.siheunggagae.ui.viewmodel.MyRequestsViewModel,
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
