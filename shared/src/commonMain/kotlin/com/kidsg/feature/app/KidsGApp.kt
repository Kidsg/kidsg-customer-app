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
import com.kidsg.data.repository.RepositoryProvider
import com.kidsg.feature.auth.AuthScreen
import com.kidsg.feature.bag.SchoolBagScreen
import com.kidsg.feature.checkout.AddAddressScreen
import com.kidsg.feature.checkout.CheckoutScreen
import com.kidsg.feature.checkout.OrderSuccessScreen
import com.kidsg.feature.checkout.PaymentMethodScreen
import com.kidsg.feature.discovery.DiscoveryScreen
import com.kidsg.feature.home.KidsGDeskHomeScreen
import com.kidsg.feature.onboarding.OnboardingScreen
import com.kidsg.feature.onboarding.StudentSetupScreen
import com.kidsg.feature.orders.OrdersListScreen
import com.kidsg.feature.product.ProductDeskViewScreen
import com.kidsg.feature.profile.HelpSupportScreen
import com.kidsg.feature.profile.ProfileScreen
import com.kidsg.feature.splash.SplashScreen
import com.kidsg.feature.tracking.OrderTrackingScreen

/**
 * KidsG Main Application Entry Point
 * Hosts all 20 screens with clean state navigation and bottom bar.
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

    val isTopLevelScreen = currentScreen is Screen.Home ||
            currentScreen is Screen.Discovery ||
            currentScreen is Screen.Bag ||
            currentScreen is Screen.Orders ||
            currentScreen is Screen.Profile

    KidsGTheme {
        Scaffold(
            bottomBar = {
                if (isTopLevelScreen) {
                    KidsGBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            currentTab = tab
                            when (tab) {
                                KidsGNavTab.HOME -> currentScreen = Screen.Home
                                KidsGNavTab.CATEGORIES -> currentScreen = Screen.Discovery(null, null)
                                KidsGNavTab.BAG -> currentScreen = Screen.Bag
                                KidsGNavTab.ORDERS -> currentScreen = Screen.Orders
                                KidsGNavTab.PROFILE -> currentScreen = Screen.Profile
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
                    .then(
                        if (currentScreen is Screen.Splash) Modifier else Modifier.padding(innerPadding)
                    )
            ) {
                when (val screen = currentScreen) {
                    is Screen.Splash -> {
                        SplashScreen(
                            onSplashFinished = {
                                if (currentUser != null && currentUser?.studentName?.isNotBlank() == true) {
                                    currentScreen = Screen.Home
                                    currentTab = KidsGNavTab.HOME
                                } else {
                                    currentScreen = Screen.Onboarding
                                }
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
                            userProfile = currentUser,
                            onNavigateToDiscovery = { mode ->
                                currentTab = KidsGNavTab.CATEGORIES
                                navigateTo(Screen.Discovery(mode, null))
                            },
                            onNavigateToCategory = { catId ->
                                currentTab = KidsGNavTab.CATEGORIES
                                navigateTo(Screen.Discovery(null, catId))
                            },
                            onNavigateToSearch = {
                                currentTab = KidsGNavTab.CATEGORIES
                                navigateTo(Screen.Discovery(null, null))
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
                            initialCategoryId = screen.initialCategoryId,
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
                                navigateTo(Screen.Checkout)
                            },
                            onStartShopping = {
                                currentTab = KidsGNavTab.HOME
                                currentScreen = Screen.Home
                            }
                        )
                    }

                    is Screen.Checkout -> {
                        CheckoutScreen(
                            userProfile = currentUser,
                            subtotal = cart.subtotal.takeIf { it > 0 } ?: 260.0,
                            onBack = { navigateBack() },
                            onChangeAddress = { navigateTo(Screen.AddAddress) },
                            onContinueToPayment = { totalAmount, speed ->
                                navigateTo(Screen.PaymentMethod(totalAmount, speed))
                            }
                        )
                    }

                    is Screen.AddAddress -> {
                        AddAddressScreen(
                            onBack = { navigateBack() },
                            onAddressSaved = {
                                navigateBack()
                            }
                        )
                    }

                    is Screen.PaymentMethod -> {
                        PaymentMethodScreen(
                            totalAmount = screen.totalAmount,
                            deliverySpeed = screen.deliverySpeed,
                            onBack = { navigateBack() },
                            onPaymentSuccess = { newOrderId ->
                                kotlinx.coroutines.runBlocking {
                                    cartRepository.clearCart()
                                }
                                currentScreen = Screen.OrderSuccess(newOrderId, screen.totalAmount)
                            }
                        )
                    }

                    is Screen.OrderSuccess -> {
                        OrderSuccessScreen(
                            orderId = screen.orderId,
                            totalAmount = screen.totalAmount,
                            onViewOrder = {
                                currentScreen = Screen.OrderTracking(screen.orderId)
                            },
                            onContinueShopping = {
                                currentTab = KidsGNavTab.HOME
                                currentScreen = Screen.Home
                            }
                        )
                    }

                    is Screen.OrderTracking -> {
                        OrderTrackingScreen(
                            orderId = screen.orderId,
                            onBack = {
                                currentTab = KidsGNavTab.ORDERS
                                currentScreen = Screen.Orders
                            }
                        )
                    }

                    is Screen.Orders -> {
                        OrdersListScreen(
                            onBack = {
                                currentTab = KidsGNavTab.HOME
                                currentScreen = Screen.Home
                            },
                            onOrderClick = { orderId ->
                                navigateTo(Screen.OrderTracking(orderId))
                            }
                        )
                    }

                    is Screen.Profile -> {
                        ProfileScreen(
                            userProfile = currentUser,
                            onNavigateToAddresses = { navigateTo(Screen.AddAddress) },
                            onNavigateToOrders = {
                                currentTab = KidsGNavTab.ORDERS
                                currentScreen = Screen.Orders
                            },
                            onNavigateToHelp = { navigateTo(Screen.HelpSupport) },
                            onLogout = {
                                kotlinx.coroutines.runBlocking {
                                    authRepository.logout()
                                }
                                currentScreen = Screen.Onboarding
                            }
                        )
                    }

                    is Screen.HelpSupport -> {
                        HelpSupportScreen(
                            onBack = { navigateBack() },
                            onTrackOrder = {
                                navigateTo(Screen.OrderTracking("KG12345678"))
                            }
                        )
                    }

                    else -> {
                        KidsGDeskHomeScreen(
                            productRepository = productRepository,
                            cartRepository = cartRepository,
                            userProfile = currentUser,
                            onNavigateToDiscovery = { mode -> currentScreen = Screen.Discovery(mode, null) },
                            onNavigateToCategory = { catId -> currentScreen = Screen.Discovery(null, catId) },
                            onNavigateToSearch = { currentScreen = Screen.Discovery(null, null) },
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
