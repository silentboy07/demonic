package com.nddfeon.demonic

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
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

    fun setActiveInRoom(active: Boolean) {
        activeInRoom = active
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
        setContent {
            DEMONICTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DemonicBackground
                ) {
                    val navController = rememberNavController()
                    val context = LocalContext.current
                    val app = context.applicationContext as DemonicApp

                    NavHost(
                        navController = navController,
                        startDestination = "login"
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
                                        return HomeViewModel(app.authRepository, app.roomRepository) as T
                                    }
                                }
                            )
                            HomeScreen(
                                viewModel = homeViewModel,
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
                                navDeepLink { uriPattern = "https://demonic.app/room/{roomCode}" }
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
                                            playerManager = app.youTubePlayerManager
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
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}