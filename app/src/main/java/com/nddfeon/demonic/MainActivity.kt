package com.nddfeon.demonic

import android.app.PictureInPictureParams
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.nddfeon.demonic.ui.home.HomeScreen
import com.nddfeon.demonic.ui.login.LoginScreen
import com.nddfeon.demonic.ui.room.RoomScreen
import com.nddfeon.demonic.ui.theme.DEMONICTheme
import com.nddfeon.demonic.ui.theme.DemonicBackground
import com.nddfeon.demonic.viewmodel.HomeViewModel
import com.nddfeon.demonic.viewmodel.LoginViewModel
import com.nddfeon.demonic.viewmodel.RoomViewModel

class MainActivity : ComponentActivity() {

    private var activeInRoom = false
    private var navController: NavHostController? = null

    private fun extractRoomCodeFromUri(uri: Uri?): String? {
        if (uri == null) return null
        try {
            // Match demonic://room/{roomCode}
            if (uri.scheme.equals("demonic", ignoreCase = true) && uri.host.equals("room", ignoreCase = true)) {
                return uri.lastPathSegment?.trim()?.uppercase()
            }
            // Match https://demonic.app/room/{roomCode} or http://demonic.app/room/{roomCode}
            if ((uri.scheme.equals("https", ignoreCase = true) || uri.scheme.equals("http", ignoreCase = true)) &&
                uri.host.equals("demonic.app", ignoreCase = true)) {
                val segments = uri.pathSegments
                if (segments.size >= 2 && segments[0].equals("room", ignoreCase = true)) {
                    return segments[1].trim().uppercase()
                }
            }
        } catch (_: Exception) {}
        return null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val handled = navController?.handleDeepLink(intent) ?: false
        if (!handled) {
            val roomCode = extractRoomCodeFromUri(intent.data)
            if (!roomCode.isNullOrBlank()) {
                navController?.navigate("room/$roomCode") {
                    launchSingleTop = true
                }
            }
        }
    }

    fun setActiveInRoom(active: Boolean) {
        activeInRoom = active
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .setAutoEnterEnabled(active)
                    .build()
                setPictureInPictureParams(params)
            } catch (_: Exception) {}
        }
    }

    private val isInPipMode = androidx.compose.runtime.mutableStateOf(false)

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode.value = isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (activeInRoom && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (_: Exception) {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        setContent {
            DEMONICTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DemonicBackground
                ) {
                    val navController = rememberNavController()
                    this@MainActivity.navController = navController
                    val context = LocalContext.current
                    val app = context.applicationContext as DemonicApp

                    val startDestination = if (app.authRepository.currentUser != null) "home" else "login"

                    LaunchedEffect(Unit) {
                        val initialUri = intent?.data
                        val roomCode = extractRoomCodeFromUri(initialUri)
                        if (!roomCode.isNullOrBlank()) {
                            if (navController.currentDestination?.route?.startsWith("room") != true) {
                                navController.navigate("room/$roomCode") {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("login") {
                            val loginViewModel: LoginViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return LoginViewModel(app.authRepository) as T
                                    }
                                }
                            )
                            LoginScreen(
                                viewModel = loginViewModel,
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            val homeViewModel: HomeViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return HomeViewModel(app.authRepository, app.roomRepository, app.recentRoomsManager) as T
                                    }
                                }
                            )
                            HomeScreen(
                                viewModel = homeViewModel,
                                ownerConfigManager = app.ownerConfigManager,
                                onNavigateToRoom = { roomCode ->
                                    navController.navigate("room/$roomCode")
                                },
                                onSignOut = {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "room/{roomCode}",
                            arguments = listOf(
                                navArgument("roomCode") {
                                    type = NavType.StringType
                                }
                            ),
                            deepLinks = listOf(
                                navDeepLink { uriPattern = "demonic://room/{roomCode}" },
                                navDeepLink { uriPattern = "https://demonic.app/room/{roomCode}" },
                                navDeepLink { uriPattern = "http://demonic.app/room/{roomCode}" }
                            )
                        ) { backStackEntry ->
                            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
                            val roomViewModel: RoomViewModel = viewModel(
                                key = "room_$roomCode",
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        val savedStateHandle = SavedStateHandle(mapOf("roomCode" to roomCode))
                                        return RoomViewModel(
                                            savedStateHandle = savedStateHandle,
                                            roomRepository = app.roomRepository,
                                            authRepository = app.authRepository,
                                            playerManager = app.youTubePlayerManager,
                                            searchManager = app.youTubeSearchManager
                                        ) as T
                                    }
                                }
                            )

                            DisposableEffect(Unit) {
                                setActiveInRoom(true)
                                onDispose {
                                    setActiveInRoom(false)
                                }
                            }

                            RoomScreen(
                                viewModel = roomViewModel,
                                searchManager = app.youTubeSearchManager,
                                isInPip = isInPipMode.value,
                                onNavigateBack = {
                                    val currentRoute = navController.currentDestination?.route
                                    if (currentRoute?.startsWith("room") == true) {
                                        val targetDest = if (app.authRepository.currentUser != null) "home" else "login"
                                        val popped = navController.popBackStack(targetDest, inclusive = false)
                                        if (!popped) {
                                            navController.navigate(targetDest) {
                                                popUpTo(0) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}