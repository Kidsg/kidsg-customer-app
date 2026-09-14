package com.kidsg.feature.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kidsg.core.designsystem.KidsGBottomBar
import com.kidsg.core.designsystem.KidsGNavTab
import com.kidsg.core.designsystem.KidsGTheme
import com.kidsg.core.navigation.Screen
import com.kidsg.data.mock.KidsGMockData
import com.kidsg.data.repository.MockCartRepository
import com.kidsg.data.repository.MockConfigRepository
import com.kidsg.data.repository.MockProductRepository
import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.Product
import com.kidsg.feature.auth.AuthScreen
import com.kidsg.feature.bag.SchoolBagScreen
import com.kidsg.feature.discovery.DiscoveryScreen
import com.kidsg.feature.home.KidsGDeskHomeScreen
import com.kidsg.feature.onboarding.OnboardingScreen
import com.kidsg.feature.onboarding.StudentSetupScreen
import com.kidsg.feature.product.ProductDeskViewScreen
import com.kidsg.feature.splash.SplashScreen

import com.kidsg.data.repository.RepositoryProvider

/**
 * KidsG Main Application Entry Point
 * Hosts the 5 Hero Experiences with clean state navigation and bottom bar.
 */
@Composable
fun KidsGApp(
    modifier: Modifier = Modifier
) {
    val productRepository = remember { RepositoryProvider.productRepository }
    val cartRepository = remember { RepositoryProvider.cartRepository }
    val configRepository = remember { RepositoryProvider.configRepository }
    val authRepository = remember { RepositoryProvider.authRepository }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var currentTab by remember { mutableStateOf(KidsGNavTab.HOME) }
    val screenStack = remember { mutableStateListOf<Screen>() }

    fun navigateTo(screen: Screen) {
        screenStack.add(currentScreen)
        currentScreen = screen
    }

    fun navigateBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            currentScreen = screenStack.removeAt(screenStack.lastIndex)
            return true
        }
        return false
    }

    val cart by cartRepository.cartState.collectAsState()
    val currentUser by authRepository.currentUser.collectAsState()

    KidsGTheme {
        Scaffold(
            bottomBar = {
                // Show bottom bar on primary exploration screens
                if (currentScreen is Screen.Home || currentScreen is Screen.Discovery || currentScreen is Screen.Bag) {
                    KidsGBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            currentTab = tab
                            when (tab) {
                                KidsGNavTab.HOME -> currentScreen = Screen.Home
                                KidsGNavTab.CATEGORIES -> currentScreen = Screen.Discovery(null)
                                KidsGNavTab.BAG -> currentScreen = Screen.Bag
                                KidsGNavTab.ORDERS -> currentScreen = Screen.Home
                                KidsGNavTab.PROFILE -> currentScreen = Screen.Home
                            }
                        },
                        bagItemCount = cart.itemCount
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (val screen = currentScreen) {
                    is Screen.Splash -> {
                        SplashScreen(
                            onSplashFinished = {
                                currentScreen = Screen.Onboarding
                            }
                        )
                    }
                    is Screen.Onboarding -> {
                        OnboardingScreen(
                            onGetStarted = {
                                navigateTo(Screen.Auth)
                            },
                            onExploreGuest = {
                                currentScreen = Screen.Home
                                currentTab = KidsGNavTab.HOME
                            }
                        )
                    }
                    is Screen.Auth -> {
                        AuthScreen(
                            authRepository = authRepository,
                            onAuthSuccess = {
                                navigateTo(Screen.StudentSetup)
                            },
                            onBack = {
                                navigateBack()
                            }
                        )
                    }
                    is Screen.StudentSetup -> {
                        StudentSetupScreen(
                            authRepository = authRepository,
                            initialProfile = currentUser,
                            onSetupComplete = {
                                currentScreen = Screen.Home
                                currentTab = KidsGNavTab.HOME
                            }
                        )
                    }
                    is Screen.Home -> {
                        KidsGDeskHomeScreen(
                            productRepository = productRepository,
                            cartRepository = cartRepository,
                            onNavigateToDiscovery = { mode ->
                                currentTab = KidsGNavTab.CATEGORIES
                                navigateTo(Screen.Discovery(mode))
                            },
                            onNavigateToSearch = {
                                currentTab = KidsGNavTab.CATEGORIES
                                navigateTo(Screen.Discovery(null))
                            },
                            onProductClick = { product ->
                                navigateTo(Screen.ProductDetail(product))
                            },
                            onAddToCart = { product ->
                                kotlinx.coroutines.runBlocking {
                                    cartRepository.addToCart(product, 1)
                                }
                            },
                            onIncreaseQuantity = { product ->
                                val item = cart.items.find { it.product.id == product.id }
                                val qty = (item?.quantity ?: 0) + 1
                                kotlinx.coroutines.runBlocking {
                                    cartRepository.updateQuantity(product.id, qty)
                                }
                            },
                            onDecreaseQuantity = { product ->
                                val item = cart.items.find { it.product.id == product.id }
                                val qty = (item?.quantity ?: 0) - 1
                                kotlinx.coroutines.runBlocking {
                                    cartRepository.updateQuantity(product.id, qty)
                                }
                            }
                        )
                    }
                    is Screen.Discovery -> {
                        DiscoveryScreen(
                            productRepository = productRepository,
                            cartRepository = cartRepository,
                            initialMode = screen.initialMode,
                            onProductClick = { product ->
                                navigateTo(Screen.ProductDetail(product))
                            },
                            onBackToHome = {
                                currentTab = KidsGNavTab.HOME
                                currentScreen = Screen.Home
                            },
                            onAddToCart = { product ->
                                kotlinx.coroutines.runBlocking {
                                    cartRepository.addToCart(product, 1)
                                }
                            },
                            onIncreaseQuantity = { product ->
                                val item = cart.items.find { it.product.id == product.id }
                                val qty = (item?.quantity ?: 0) + 1
                                kotlinx.coroutines.runBlocking {
                                    cartRepository.updateQuantity(product.id, qty)
                                }
                            },
                            onDecreaseQuantity = { product ->
                                val item = cart.items.find { it.product.id == product.id }
                                val qty = (item?.quantity ?: 0) - 1
                                kotlinx.coroutines.runBlocking {
                                    cartRepository.updateQuantity(product.id, qty)
                                }
                            }
                        )
                    }
                    is Screen.ProductDetail -> {
                        ProductDeskViewScreen(
                            product = screen.product,
                            cartRepository = cartRepository,
                            onBack = { navigateBack() },
                            onGoToBag = {
                                currentTab = KidsGNavTab.BAG
                                currentScreen = Screen.Bag
                            }
                        )
                    }
                    is Screen.Bag -> {
                        SchoolBagScreen(
                            cartRepository = cartRepository,
                            configRepository = configRepository,
                            onProceedToCheckout = {
                                // Transition to checkout review
                            },
                            onStartShopping = {
                                currentTab = KidsGNavTab.HOME
                                currentScreen = Screen.Home
                            }
                        )
                    }
                    else -> {
                        KidsGDeskHomeScreen(
                            productRepository = productRepository,
                            cartRepository = cartRepository,
                            onNavigateToDiscovery = { mode -> currentScreen = Screen.Discovery(mode) },
                            onNavigateToSearch = { currentScreen = Screen.Discovery(null) },
                            onProductClick = { product -> currentScreen = Screen.ProductDetail(product) },
                            onAddToCart = {},
                            onIncreaseQuantity = {},
                            onDecreaseQuantity = {}
                        )
                    }
                }
            }
        }
    }
}
