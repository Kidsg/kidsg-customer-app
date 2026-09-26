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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kidsg.core.designsystem.KidsGBottomBar
import com.kidsg.core.designsystem.KidsGNavTab
import com.kidsg.core.designsystem.KidsGTheme
import com.kidsg.core.navigation.BackHandler
import com.kidsg.core.navigation.Screen
import com.kidsg.core.storage.SessionStorage
import com.kidsg.data.repository.RepositoryProvider
import com.kidsg.domain.model.Address
import com.kidsg.feature.auth.AuthScreen
import com.kidsg.feature.bag.SchoolBagScreen
import com.kidsg.feature.checkout.AddAddressScreen
import com.kidsg.feature.checkout.CheckoutScreen
import com.kidsg.feature.checkout.OrderSuccessScreen
import com.kidsg.feature.checkout.PaymentMethodScreen
import com.kidsg.feature.discovery.DiscoveryScreen
import com.kidsg.feature.home.KidsGDeskHomeScreen
import com.kidsg.feature.home.LocationSelectionDialog
import com.kidsg.feature.onboarding.OnboardingScreen
import com.kidsg.feature.onboarding.StudentSetupScreen
import com.kidsg.feature.orders.OrdersListScreen
import com.kidsg.feature.product.ProductDeskViewScreen
import com.kidsg.feature.profile.HelpSupportScreen
import com.kidsg.feature.profile.ProfileScreen
import com.kidsg.feature.splash.SplashScreen
import com.kidsg.feature.tracking.OrderTrackingScreen
import kotlinx.coroutines.launch

/**
 * KidsG Main Application Entry Point
 * Hosts all 20 screens with clean state navigation, gesture back handling, and bottom bar.
 */
@Composable
fun KidsGApp(
    modifier: Modifier = Modifier,
    initialLocation: String? = null
) {
    val productRepository = remember { RepositoryProvider.productRepository }
    val cartRepository = remember { RepositoryProvider.cartRepository }
    val configRepository = remember { RepositoryProvider.configRepository }
    val authRepository = remember { RepositoryProvider.authRepository }
    val orderRepository = remember { RepositoryProvider.orderRepository }
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var currentTab by remember { mutableStateOf(KidsGNavTab.HOME) }
    val screenStack = remember { mutableStateListOf<Screen>() }

    var currentLocation by remember {
        mutableStateOf(initialLocation ?: SessionStorage.getCurrentLocation() ?: "HSR Layout, Bengaluru")
    }

    androidx.compose.runtime.LaunchedEffect(initialLocation) {
        if (!initialLocation.isNullOrBlank()) {
            currentLocation = initialLocation
        }
    }
    var showLocationDialog by remember { mutableStateOf(false) }

    fun navigateTo(screen: Screen) {
        screenStack.add(currentScreen)
        currentScreen = screen
    }

    fun navigateBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            currentScreen = screenStack.removeAt(screenStack.lastIndex)
            return true
        } else if (currentScreen != Screen.Home && currentScreen != Screen.Splash && currentScreen != Screen.Onboarding) {
            currentTab = KidsGNavTab.HOME
            currentScreen = Screen.Home
            return true
        }
        return false
    }

    // Intercept Android System Back Gestures and 3-Button Navigation
    BackHandler(enabled = screenStack.isNotEmpty() || (currentScreen != Screen.Home && currentScreen != Screen.Splash && currentScreen != Screen.Onboarding)) {
        navigateBack()
    }

    val cart by cartRepository.cartState.collectAsState()
    val currentUser by authRepository.currentUser.collectAsState()
    val activeOrders by orderRepository.observeActiveOrders().collectAsState(initial = emptyList())

    var selectedDeliveryAddress by remember {
        mutableStateOf(SessionStorage.getSelectedAddress())
    }

    androidx.compose.runtime.LaunchedEffect(currentUser, currentLocation) {
        if (selectedDeliveryAddress == null) {
            val studentName = currentUser?.studentName?.takeIf { it.isNotBlank() }
                ?: currentUser?.name?.takeIf { it.isNotBlank() }
                ?: "Student Desk"
            val phone = currentUser?.phone?.takeIf { it.isNotBlank() } ?: "9876543210"
            val school = currentUser?.schoolName?.takeIf { it.isNotBlank() }
                ?: currentUser?.studentGrade?.takeIf { it.isNotBlank() }
                ?: currentLocation
            val defaultAddr = Address(
                id = "addr_default",
                label = "Home",
                recipientName = studentName,
                phoneNumber = phone,
                addressLine1 = school.ifBlank { "12, Green Park" },
                addressLine2 = currentUser?.studentGrade ?: "",
                city = "Bengaluru",
                pincode = "560001",
                isDefault = true
            )
            selectedDeliveryAddress = defaultAddr
            SessionStorage.saveSelectedAddress(defaultAddr)
        }
    }

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
                                } else if (SessionStorage.isOnboardingCompleted()) {
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
                                SessionStorage.setOnboardingCompleted(true)
                                if (currentUser != null) {
                                    currentScreen = Screen.Home
                                    currentTab = KidsGNavTab.HOME
                                } else {
                                    navigateTo(Screen.Auth)
                                }
                            },
                            onExploreGuest = {
                                SessionStorage.setOnboardingCompleted(true)
                                currentScreen = Screen.Home
                                currentTab = KidsGNavTab.HOME
                            }
                        )
                    }

                    is Screen.Auth -> {
                        AuthScreen(
                            authRepository = authRepository,
                            onAuthSuccess = { profile, isReturningUser ->
                                if (isReturningUser && profile.studentName.isNotBlank()) {
                                    currentScreen = Screen.Home
                                    currentTab = KidsGNavTab.HOME
                                } else {
                                    navigateTo(Screen.StudentSetup)
                                }
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
                            currentLocationName = currentLocation,
                            onLocationClick = { showLocationDialog = true },
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
                                coroutineScope.launch {
                                    cartRepository.addToCart(product, 1)
                                }
                            },
                            onIncreaseQuantity = { product ->
                                val item = cart.items.find { it.product.id == product.id }
                                val qty = (item?.quantity ?: 0) + 1
                                coroutineScope.launch {
                                    cartRepository.updateQuantity(product.id, qty)
                                }
                            },
                            onDecreaseQuantity = { product ->
                                val item = cart.items.find { it.product.id == product.id }
                                val qty = (item?.quantity ?: 0) - 1
                                coroutineScope.launch {
                                    cartRepository.updateQuantity(product.id, qty)
                                }
                            },
                            onNavigateToBag = {
                                currentTab = KidsGNavTab.BAG
                                currentScreen = Screen.Bag
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
                                coroutineScope.launch {
                                    cartRepository.addToCart(product, 1)
                                }
                            },
                            onIncreaseQuantity = { product ->
                                val item = cart.items.find { it.product.id == product.id }
                                val qty = (item?.quantity ?: 0) + 1
                                coroutineScope.launch {
                                    cartRepository.updateQuantity(product.id, qty)
                                }
                            },
                            onDecreaseQuantity = { product ->
                                val item = cart.items.find { it.product.id == product.id }
                                val qty = (item?.quantity ?: 0) - 1
                                coroutineScope.launch {
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
                            selectedAddress = selectedDeliveryAddress,
                            onBack = { navigateBack() },
                            onChangeAddress = { navigateTo(Screen.AddAddress) },
                            onContinueToPayment = { totalAmount, speed, address ->
                                selectedDeliveryAddress = address
                                SessionStorage.saveSelectedAddress(address)
                                navigateTo(Screen.PaymentMethod(totalAmount, speed, address))
                            }
                        )
                    }

                    is Screen.AddAddress -> {
                        AddAddressScreen(
                            onBack = { navigateBack() },
                            onAddressSaved = { savedAddr ->
                                selectedDeliveryAddress = savedAddr
                                SessionStorage.saveSelectedAddress(savedAddr)
                                navigateBack()
                            }
                        )
                    }

                    is Screen.PaymentMethod -> {
                        PaymentMethodScreen(
                            totalAmount = screen.totalAmount,
                            deliverySpeed = screen.deliverySpeed,
                            deliveryAddress = screen.deliveryAddress ?: selectedDeliveryAddress,
                            onBack = { navigateBack() },
                            onPaymentSuccess = { newOrderId ->
                                coroutineScope.launch {
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
                            orders = activeOrders,
                            orderRepository = orderRepository,
                            cartRepository = cartRepository,
                            onBack = {
                                currentTab = KidsGNavTab.HOME
                                currentScreen = Screen.Home
                            },
                            onOrderClick = { orderId ->
                                navigateTo(Screen.OrderTracking(orderId))
                            },
                            onExploreDesk = {
                                currentTab = KidsGNavTab.HOME
                                currentScreen = Screen.Home
                            },
                            onStartShopping = {
                                currentTab = KidsGNavTab.HOME
                                currentScreen = Screen.Home
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
                                coroutineScope.launch {
                                    authRepository.logout()
                                }
                                currentScreen = Screen.Onboarding
                            },
                            onUpdateProfile = { updated ->
                                coroutineScope.launch {
                                    authRepository.updateProfile(updated)
                                }
                            }
                        )
                    }

                    is Screen.HelpSupport -> {
                        HelpSupportScreen(
                            onBack = { navigateBack() },
                            onTrackOrder = {
                                val targetOrder = activeOrders.firstOrNull()?.displayOrderId ?: "KG-ORDER"
                                navigateTo(Screen.OrderTracking(targetOrder))
                            }
                        )
                    }

                    else -> {
                        KidsGDeskHomeScreen(
                            productRepository = productRepository,
                            cartRepository = cartRepository,
                            userProfile = currentUser,
                            currentLocationName = currentLocation,
                            onLocationClick = { showLocationDialog = true },
                            onNavigateToDiscovery = { mode -> currentScreen = Screen.Discovery(mode, null) },
                            onNavigateToCategory = { catId -> currentScreen = Screen.Discovery(null, catId) },
                            onNavigateToSearch = { currentScreen = Screen.Discovery(null, null) },
                            onProductClick = { product -> currentScreen = Screen.ProductDetail(product) },
                            onAddToCart = { product ->
                                coroutineScope.launch {
                                    cartRepository.addToCart(product, 1)
                                }
                            },
                            onIncreaseQuantity = { product ->
                                val item = cart.items.find { it.product.id == product.id }
                                val qty = (item?.quantity ?: 0) + 1
                                coroutineScope.launch {
                                    cartRepository.updateQuantity(product.id, qty)
                                }
                            },
                            onDecreaseQuantity = { product ->
                                val item = cart.items.find { it.product.id == product.id }
                                val qty = (item?.quantity ?: 0) - 1
                                coroutineScope.launch {
                                    cartRepository.updateQuantity(product.id, qty)
                                }
                            },
                            onNavigateToBag = {
                                currentTab = KidsGNavTab.BAG
                                currentScreen = Screen.Bag
                            }
                        )
                    }
                }

                // Global Live Location Picker Dialog
                if (showLocationDialog) {
                    LocationSelectionDialog(
                        currentLocation = currentLocation,
                        onDismiss = { showLocationDialog = false },
                        onLocationSelected = { loc, label ->
                            currentLocation = "$loc ($label)"
                        }
                    )
                }
            }
        }
    }
}
