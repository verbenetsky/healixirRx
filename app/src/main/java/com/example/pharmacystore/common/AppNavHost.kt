package com.example.pharmacystore.common

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.pharmacystore.data.remote.displayName
import com.example.pharmacystore.domain.model.CartItem
import com.example.pharmacystore.domain.model.UserSettings
import com.example.pharmacystore.repo.toOrderItem
import com.example.pharmacystore.repo.toStockDecrementItem
import com.example.pharmacystore.ui.auth.AuthSmsViewModel
import com.example.pharmacystore.ui.auth.OriginScreen
import com.example.pharmacystore.ui.auth.SmsAuthScreen
import com.example.pharmacystore.ui.auth.signIn.AuthGate
import com.example.pharmacystore.ui.auth.signIn.AuthGateViewModel
import com.example.pharmacystore.ui.auth.signIn.CheckIfUserLoggedIn
import com.example.pharmacystore.ui.auth.signIn.EmailPasswordSignIn
import com.example.pharmacystore.ui.auth.signIn.EmailPasswordSignInViewModel
import com.example.pharmacystore.ui.auth.signIn.EmailVerificationViewModel
import com.example.pharmacystore.ui.auth.signIn.SingInScreenOptions
import com.example.pharmacystore.ui.auth.signUp.EmailPasswordSignUp
import com.example.pharmacystore.ui.auth.signUp.EmailPasswordSignUpViewModel
import com.example.pharmacystore.ui.auth.signUp.RegistrationScreenOptions
import com.example.pharmacystore.ui.drug.DrugSearchScreen
import com.example.pharmacystore.ui.drug.DrugViewModel
import com.example.pharmacystore.ui.drug.PickPackageDrugScreen
import com.example.pharmacystore.ui.home.HomeScreen
import com.example.pharmacystore.ui.map.MapScreen
import com.example.pharmacystore.ui.map.MapViewModel
import com.example.pharmacystore.ui.orders.OrdersScreen
import com.example.pharmacystore.ui.orders.OrdersViewModel
import com.example.pharmacystore.ui.pharmacies.PharmaciesScreen
import com.example.pharmacystore.ui.pharmacies.PharmacyScreen
import com.example.pharmacystore.ui.pharmacies.PharmacyStockScreen
import com.example.pharmacystore.ui.pharmacies.PharmacyViewModel
import com.example.pharmacystore.ui.profileScreen.ProfileScreen
import com.example.pharmacystore.ui.profileScreen.ProfileScreenViewModel
import com.example.pharmacystore.ui.profileSetUp.ProfileSetUpScreen
import com.example.pharmacystore.ui.profileSetUp.ProfileSetUpViewModel
import com.example.pharmacystore.ui.settings.Settings
import com.example.pharmacystore.ui.settings.SettingsViewModel
import com.example.pharmacystore.ui.shoppingcart.ShoppingCartScreen
import com.example.pharmacystore.ui.shoppingcart.ShoppingCartViewModel
import com.example.pharmacystore.ui.summary.SummaryCheckoutScreen

@Composable
fun AppNavHost(nav: NavHostController, modifier: Modifier) {

//    LogNavHostBackStack(nav)
//    LogVisibleEntries(nav)

    NavHost(
        navController = nav,
        startDestination = NavGraphs.CHECK_IF_LOGGED_IN_GRAPH.toRegularString(),
        modifier = modifier
    ) {

        // sluzy do sprawdzenia czy user jest zalogowany
        navigation(
            startDestination = Screen.CheckIfUserLoggedInScreen.route,
            route = NavGraphs.CHECK_IF_LOGGED_IN_GRAPH.toRegularString(),
        ) {
            composable(Screen.CheckIfUserLoggedInScreen.route) {
                val viewModel: EmailPasswordSignInViewModel = hiltViewModel()
                CheckIfUserLoggedIn(
                    isLoggedIn = viewModel.isLoggedIn.collectAsStateWithLifecycle().value,
                    navigateToLogInScreen = {
                        nav.navigate(NavGraphs.AUTH_GRAPH.toRegularString()) {
                            popUpTo(NavGraphs.CHECK_IF_LOGGED_IN_GRAPH.toRegularString()) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }

                    },
                    navigateToAuthGateScreen = {
                        nav.navigate(Screen.AuthGate.route)
                    },
                )
            }
        }

        // authentication graph
        navigation(
            startDestination = Screen.SignInOptions.route,
            route = NavGraphs.AUTH_GRAPH.toRegularString(),
        ) {
            // ekran wyboru metody logowania
            composable(Screen.SignInOptions.route) {
                SingInScreenOptions(
                    navigateToSingInWithPhoneNumber = { screen ->
                        nav.navigate(
                            Screen.AuthSmsScreen.route(
                                screen
                            )
                        )
                    },
                    navigateToRegistrationOptions = { nav.navigate(Screen.RegisterOptions.route) },
                    navigateToSingInWithEmail = { nav.navigate(Screen.EmailPasswordSignInScreen.route) }
                )
            }

            // ekran wyboru metody rejestracji
            composable(Screen.RegisterOptions.route) {
                RegistrationScreenOptions(
                    navigateToSignInScreenOptions = { nav.navigate(Screen.SignInOptions.route) },
                    onPhoneRegister = { screen -> nav.navigate(Screen.AuthSmsScreen.route(screen)) },
                    onEmailRegister = { nav.navigate(Screen.EmailPasswordSignUpScreen.route(null)) }
                )
            }

            // ekran z text fieldami gdzie wpisujemy email i haslo
            composable(
                Screen.EmailPasswordSignUpScreen.route,
                arguments = listOf(
                    navArgument("originScreen") {
                        type = NavType.StringType
                        defaultValue = null
                        nullable = true
                    })
            ) { backStack ->
                val screen = backStack.arguments?.getString("originScreen")
                val viewModel: EmailPasswordSignUpViewModel = hiltViewModel()

                EmailPasswordSignUp(
                    screen = screen,
                    emailPasswordSignUpViewModel = viewModel,
                    navigateToAccountSetUpScreen = { email ->
                        nav.navigate("profileSetUp?email=${Uri.encode(email)}")
                    },
                    navigateToProfileScreen = { nav.popBackStack() },
                    navigateToSingUpMethodScreen = {
                        nav.navigate(Screen.SignInOptions.route) {
                            popUpTo(NavGraphs.AUTH_GRAPH.toRegularString()) { inclusive = true }
                        }
                    },
                )
            }

            // dwa ekrany gdzie wpisujemy swoj numer telefonu a potem wpisujemy kod potwierdzenia
            composable(
                Screen.AuthSmsScreen.route,
                arguments = listOf(
                    navArgument("screen") {
                        type = NavType.EnumType(OriginScreen::class.java)
                        navArgument("vid") { type = NavType.StringType; nullable = true }
                    })
            ) { backStackEntry ->
                val viewModel: AuthSmsViewModel = hiltViewModel()
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                val screen = backStackEntry.arguments?.getSerializable("screen") as OriginScreen
                val verificationId = backStackEntry.arguments?.getString("vid") // może być null

                SmsAuthScreen(
                    navigateToHomeScreen = { nav.popBackStack() },
                    completeEnrollment = { code ->
                        if (verificationId != null)
                            settingsViewModel.completeEnrollment(
                            verificationId,
                            code
                        )
                    },

                    navigateToSettingsScreen = { nav.popBackStack() },

                    navigateToProfileSetUp = { phoneNumber ->
                        nav.navigate("profileSetUp?phoneNumber=${Uri.encode(phoneNumber)}")
                    },
                    authSmsViewModel = viewModel,
                    navigateToSingUpMethodScreen = {
                        nav.navigate(Screen.SignInOptions.route) {
                            popUpTo(NavGraphs.AUTH_GRAPH.toRegularString()) { inclusive = true }
                        }
                    },
                    navigateToAuthGate = {
                        nav.navigate(Screen.AuthGate.route)
                    },
                    originScreen = screen,
                )
            }

            // ekran gdzie robimy profile setup
            composable(
                Screen.ProfileSetUpScreen.route,
                arguments = listOf(
                    navArgument("phoneNumber") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("email") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
            ) { backStackEntry ->
                val viewModel: ProfileSetUpViewModel = hiltViewModel()

                val phone = backStackEntry.arguments?.getString("phoneNumber")
                val email = backStackEntry.arguments?.getString("email")

                ProfileSetUpScreen(
                    profileSetUpViewModel = viewModel,
                    navigateToSignIn = {
                        nav.navigate(Screen.SignInOptions.route)
                    },
                    navigateToHomeScreen = {
                        nav.navigate(Screen.HomeScreen.route) {
                            popUpTo(NavGraphs.AUTH_GRAPH.toRegularString()) {
                                inclusive = true
                            } // usuwa caly stack
                            launchSingleTop = true
                            // ochrona przed wielokrotnym nakładaniem tej samej destynacji na szczycie BackStacku.
                        }
                    },
                    phoneNumber = phone,
                    email = email
                )
            }

            composable(Screen.EmailPasswordSignInScreen.route) {
                val viewModel: EmailPasswordSignInViewModel = hiltViewModel()

                EmailPasswordSignIn(
                    emailPasswordSignInViewModel = viewModel,
//                    navigateToHomeScreen = {
//                        nav.navigate(Screen.HomeScreen.route) {
//                            popUpTo("auth_graph") { inclusive = true }
//                            launchSingleTop = true
//                        }
//                    },
                    navigateToSingInMethodScreen = {
                        nav.navigate(Screen.SignInOptions.route) {
                            popUpTo(NavGraphs.AUTH_GRAPH.toRegularString()) { inclusive = true }
                        }
                    },
                    navigateToAuthGate = { nav.navigate(Screen.AuthGate.route) }
                )
            }

            composable(Screen.AuthGate.route) {
                val viewModel: AuthGateViewModel = hiltViewModel()
                AuthGate(
                    viewModel = viewModel,
                    navigateToProfileSetup = { (contact, data) ->
                        if (contact == "email") {
                            nav.navigate("profileSetUp?email=${Uri.encode(data)}")
                        } else {
                            nav.navigate("profileSetUp?phoneNumber=${Uri.encode(data)}")
                        }
                    },
                    navigateToHomeScreen = {
                        nav.navigate(NavGraphs.MAIN_GRAPH.toRegularString()) {
                            popUpTo(nav.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
        navigation(
            startDestination = Screen.HomeScreen.route,
            route = NavGraphs.MAIN_GRAPH.toRegularString(),
        ) {
            composable(Screen.HomeScreen.route) {
                HomeScreen(
                    navigateToProfile = { nav.navigate(Screen.ProfileScreen.route) },
                    navigateToDrugs = { nav.navigate(Screen.DrugSearchScreen.route()) },
                    navigateToPharmacies = { nav.navigate(Screen.ListOfPharmacies.route) },
                    navigateToShoppingCart = { nav.navigate(Screen.CartScreen.route) }
                )
            }

            composable(Screen.ProfileScreen.route) {
                //---------------------------------ViewModel(s)-------------------------------------
                val mainGraphEntry =
                    remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                val viewModel: ProfileScreenViewModel = hiltViewModel(mainGraphEntry)
                val viewModel2: EmailPasswordSignInViewModel = hiltViewModel()
                val emailVerificationViewModel: EmailVerificationViewModel = hiltViewModel()
                //----------------------------------------------------------------------------------

                val emailVerifiedState by emailVerificationViewModel.emailVerifiedState.collectAsStateWithLifecycle()
                val coolDownTime by emailVerificationViewModel.remainingSec.collectAsStateWithLifecycle()

                ProfileScreen(
                    emailVerifiedState = emailVerifiedState,
                    coolDownTime = coolDownTime,
                    loadCoolDown = { emailVerificationViewModel.loadCooldown() },
                    authEvents = viewModel2.events,
                    onVerifyClick = { email -> emailVerificationViewModel.sendVerificationEmail() },
                    profileScreenViewModel = viewModel,
                    navigateToSettings = { nav.navigate("settings") },
                    navigateToOrdersScreen = { nav.navigate(Screen.OrdersScreen.route) },
                    navigateToProvidePhoneNumberScreen = {
                        nav.navigate(
                            Screen.AuthSmsScreen.route(
                                OriginScreen.PROFILE
                            )
                        )
                    },
                    onLogoutClick = {
                        viewModel2.logOut(
                            onSuccess = {
                                nav.navigate(NavGraphs.AUTH_GRAPH.toRegularString()) {
                                    popUpTo(NavGraphs.MAIN_GRAPH.toRegularString()) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    },
                    navigateToSignUpScreen = { screen ->
                        nav.navigate(Screen.EmailPasswordSignUpScreen.route(screen))
                    },
                    navigateToSettingsCue2FA = {
                        nav.navigate("settings?cue=2FA") {
                            launchSingleTop = true
                        }
                    },
                    checkIfEmailIsVerified = { emailVerificationViewModel.refreshEmailVerified() },
                    openMap = { nav.navigate(Screen.Map.route) }
                )
            }

            composable(
                Screen.Settings.route,
                arguments = listOf(navArgument("cue") { nullable = true })
            ) { backStackEntry ->
                //---------------------------------ViewModel(s)-----------------------------------------
                val mainGraphEntry =
                    remember(backStackEntry) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                val viewModel: ProfileScreenViewModel = hiltViewModel(mainGraphEntry)
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                //--------------------------------------------------------------------------------------

                val isVerified by settingsViewModel.isVerified.collectAsStateWithLifecycle()
                val reAuthState by settingsViewModel.reAuthState.collectAsStateWithLifecycle()

                val twoFaState by settingsViewModel.twoFaState.collectAsStateWithLifecycle()

                val cue = backStackEntry.arguments?.getString("cue")

                val userData by viewModel.userData.collectAsState()
                val origin = userData?.settings ?: UserSettings()

                var draft by remember(origin) { mutableStateOf(origin) }
                val isDirty = draft != origin

                Settings(
                    reAuthState = reAuthState,
                    twoFaEvents = settingsViewModel.twoFAEvents,
                    twoFaState = twoFaState,
                    isVerified = isVerified,
                    twoFAState = draft.twoFactorEnabled,
                    enableLinkingState = draft.enableLinking,
                    on2FAChange = { draft = draft.copy(twoFactorEnabled = it) },
                    onEnableLinkingChange = { draft = draft.copy(enableLinking = it) },
                    isDirty = isDirty,
                    settingsViewModel = settingsViewModel,
                    userSettings = draft,
                    cue = cue,
                    refreshUser = { viewModel.refreshUser() },
                    reAuthUser = { passwd, act -> settingsViewModel.reAuth(passwd, act) },

                    navigateToCodeScreen = { vid ->
                        nav.navigate(
                            Screen.AuthSmsScreen.route(
                                OriginScreen.SETTINGS,
                                vid
                            )
                        )
                    }
                )
            }

            composable(Screen.Map.route) {
                //---------------------------------ViewModel(s)-----------------------------------------
                val mainGraphEntry =
                    remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                val viewModel: ProfileScreenViewModel = hiltViewModel(mainGraphEntry)
                val mapViewModel: MapViewModel = hiltViewModel()
                //--------------------------------------------------------------------------------------

                val userData by viewModel.userData.collectAsState()
                MapScreen(
                    mapViewModel = mapViewModel,
                    address = combineAddress(
                        city = userData?.city ?: "",
                        woj = convertWojNumberToWojString(userData?.woj ?: ""),
                        powiat = userData?.powiat ?: "",
                        street = userData?.street ?: "",
                        houseNumber = userData?.houseNumber ?: ""
                    )
                )
            }

            composable(Screen.ListOfPharmacies.route) {
                //---------------------------------ViewModel(s)-----------------------------------------
                val mainGraphEntry =
                    remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                val viewModel: ProfileScreenViewModel = hiltViewModel(mainGraphEntry)
                val pharmacyGraphEntry =
                    remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                val pharmacyViewModel: PharmacyViewModel = hiltViewModel(pharmacyGraphEntry)
                //--------------------------------------------------------------------------------------

                val userData by viewModel.userData.collectAsStateWithLifecycle()

                PharmaciesScreen(
                    viewModel = pharmacyViewModel,
                    address = combineAddress(
                        city = userData?.city ?: "",
                        woj = convertWojNumberToWojString(userData?.woj ?: ""),
                        powiat = userData?.powiat ?: "",
                        street = userData?.street ?: "",
                        houseNumber = userData?.houseNumber ?: ""
                    ),
                    navigateToPharmacyScreen = {
                        nav.navigate(Screen.PharmacyFullInfoScreen.route)
                    },
                )
            }

            composable(Screen.PharmacyFullInfoScreen.route) {
                val drugGraphEntry =
                    remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                val pharmacyGraphEntry =
                    remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                val drugViewModel: DrugViewModel = hiltViewModel(drugGraphEntry)
                val pharmacyViewModel: PharmacyViewModel = hiltViewModel(pharmacyGraphEntry)
                val mapViewModel: MapViewModel = hiltViewModel()
                val latLng = mapViewModel.latLng.collectAsStateWithLifecycle()

                PharmacyScreen(
                    pharmacyViewModel = pharmacyViewModel,
                    onBack = { nav.navigateUp() },
                    drugViewModel = drugViewModel,
                    navigateToPharmacyStock = { nav.navigate(Screen.PharmacyStockScreen.route) },
                    latLong = latLng.value
                )
            }

            navigation(
                startDestination = Screen.DrugSearchScreen.route(),
                route = NavGraphs.SHOPPING_CART_GRAPH.toRegularString(),
            ) {
                composable(
                    Screen.DrugSearchScreen.route,
                    arguments = listOf(navArgument("drugNdc") {
                        type = NavType.StringType
                        nullable = true          // albo defaultValue
                        defaultValue = null      // opcjonalnie
                    })
                ) { backStackEntry ->
                    //---------------------------------ViewModel(s)-----------------------------------------
                    val shoppingGraphEntry =
                        remember(backStackEntry) { nav.getBackStackEntry(NavGraphs.SHOPPING_CART_GRAPH.toRegularString()) }
                    val mainGraphEntry =
                        remember(backStackEntry) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }

                    val viewModel: ProfileScreenViewModel = hiltViewModel(mainGraphEntry)

                    val shoppingCartViewModel: ShoppingCartViewModel =
                        hiltViewModel(shoppingGraphEntry)

                    val drugViewModel: DrugViewModel = hiltViewModel(mainGraphEntry)

                    val pharmacyViewModel: PharmacyViewModel = hiltViewModel(mainGraphEntry)
                    //--------------------------------------------------------------------------------------

                    val drugNdc = backStackEntry.arguments?.getString("drugNdc")
                    val userData by viewModel.userData.collectAsStateWithLifecycle()

                    DrugSearchScreen(
                        drugNdcFromShoppingCartScreen = drugNdc,
                        drugViewModel = drugViewModel,
                        pharmacyViewModel = pharmacyViewModel,
                        address = combineAddress(
                            city = userData?.city ?: "",
                            woj = convertWojNumberToWojString(userData?.woj ?: ""),
                            powiat = userData?.powiat ?: "",
                            street = userData?.street ?: "",
                            houseNumber = userData?.houseNumber ?: ""
                        ),
                        shoppingCartViewModel = shoppingCartViewModel,
                        navigateToPickPackage = { nav.navigate(Screen.PickPackageScreen.route) },
                        navigateToShoppingCartScreen = { nav.navigate(Screen.CartScreen.route) }
                    )
                }

                composable(Screen.PickPackageScreen.route) {
                    //---------------------------------ViewModel(s)-----------------------------------------
                    val shoppingGraphEntry =
                        remember(it) { nav.getBackStackEntry(NavGraphs.SHOPPING_CART_GRAPH.toRegularString()) }
                    val drugGraphEntry =
                        remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                    val pharmacyGraphEntry =
                        remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                    val viewModel: ShoppingCartViewModel = hiltViewModel(shoppingGraphEntry)
                    val drugViewModel: DrugViewModel = hiltViewModel(drugGraphEntry)
                    val pharmacyViewModel: PharmacyViewModel = hiltViewModel(pharmacyGraphEntry)
                    //--------------------------------------------------------------------------------------

                    val pickPackageState by drugViewModel.pickPackageState.collectAsStateWithLifecycle()
                    val cartSizeState by viewModel.cartSize.collectAsStateWithLifecycle()
                    val canIncrease by viewModel.canIncreaseMap.collectAsStateWithLifecycle()

                    pickPackageState?.let { drug ->
                        PickPackageDrugScreen(
                            shoppingCartEvent = viewModel.events,
                            drug = drug,
                            cartSize = cartSizeState,
                            navigateToShoppingCartScreen = { nav.navigate(Screen.CartScreen.route) },
                            drugViewModel = drugViewModel,
                            navigateToPharmacyStock = { nav.navigate(Screen.PharmacyStockScreen.route) },
                            pharmacyViewModel = pharmacyViewModel,
                            popBackStack = { nav.popBackStack() },
                            onAddToCartClick = { item, stockSize ->
                                viewModel.checkIfCanAdd(
                                    packageNdc = item.packageNdc,
                                    pharmacyId = item.pharmacyId,
                                    usersQt = item.quantity,
                                    stockSize = stockSize,
                                    onSuccess = {
                                        viewModel.addItemToCart(item)
                                        // zanim dodac do koszyka sprawdzamy czy taka ilosc jaka wprowadzilismy + to co jest w koszyku
                                        // nie jest wieksze od tej jaka jest w magazynie
                                        println("added to cart: $item")
                                    },
                                )
                            },
                            canIncrease = canIncrease,
                            checkShoppingCartSize = { ndc, id, usersQt, stockSize ->
                                viewModel.checkIfCanIncreaseQt(ndc, id, usersQt, stockSize)
                            },
                            onDecreaseQuantity = { key -> viewModel.onDecreaseMap(key) },
                        )
                    }
                }

                composable(Screen.CartScreen.route) {
                    //---------------------------------ViewModel(s)-----------------------------------------
                    val shoppingGraphEntry =
                        remember(it) { nav.getBackStackEntry(NavGraphs.SHOPPING_CART_GRAPH.toRegularString()) }
                    val viewModel: ShoppingCartViewModel = hiltViewModel(shoppingGraphEntry)
                    val pharmacyGraphEntry =
                        remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                    val pharmacyViewModel: PharmacyViewModel = hiltViewModel(pharmacyGraphEntry)
                    //--------------------------------------------------------------------------------------

                    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
                    val totalCartPrice by viewModel.totalCartPrice.collectAsStateWithLifecycle()
                    val state by viewModel.state.collectAsStateWithLifecycle()

                    ShoppingCartScreen(
                        state = state,
                        items = cartItems,
                        totalCartPrice = totalCartPrice,
                        event = viewModel.events,
                        // zanim zrobimy checkOut to trzeba sprawdzic ze rzeczywiscie w bazie danych dostapne te rzeczy, robi sie to dla tego ze
                        // jesli kilka userow ma ten sam produkt w koszuku i jeden kupi ten konkrenty produkt i ze stanu bedzie wynikac ze go juz nie ma to
                        // trzeba zeby drugi user ktory mial to w koszuku nie mogl juz tego kupic
                        // ewentualnie przejsc przez liste wszystkich userow co maja ten sam produkt w koszuku i usunac to stad ale jest to trudniejsze do zrealizacji
                        // po tym jak sie sprawdzi czy user moze kupic napewno te rzeczy i jesli nie moze to poprostu usunac mu z koszuka je lub zmniejszyc ilosc i tyle
                        // rowniez tutaj trzeba dodac mozliwosc rezerwacji konkretnych lekow po tym jak user kliknie checkcout

                        onCheckoutClick = { info ->
                            viewModel.checkIfCanBuy(info)
                        },

                        onIncrease = { item ->
                            // tutaj wywolac metode get z api zeby serwer zwrocil obecna ilosc tego leku w konkretnej aptece i tak sprawdzic czy mozna zwiekszyc ilos czy nie
                            // innej sensownej i w miare czystej metody nie widze
                            viewModel.getMaxAvailableQuantity(
                                packageNdc = item.packageNdc,
                                pharmacyId = item.pharmacyId,
                                usersQt = item.quantity,
                                onSuccess = {
                                    viewModel.increaseQt(item)
                                },
                                // jesli zwrocone maxQt przez serwer jest mniejsze od ilosci w koszuku to wywoluje sie onSuccess
                                // i jest zwiekszona ilosc rzeczy w koszyku
                            )
                        },
                        onDecrease = { item ->
                            viewModel.decreaseQt(item)
                        },
                        onRemove = { packageNdc, pharmacyId ->
                            viewModel.removeItem(
                                packageNdc,
                                pharmacyId
                            )
                        },
                        navigateToPharmacyScreen = { id ->
                            pharmacyViewModel.getPharmacyDetails(
                                id = id,
                                onSuccess = { nav.navigate(Screen.PharmacyFullInfoScreen.route) }
                            )
                        },
                        navigateToDrugSearchScreen = { drugNdc ->
                            nav.navigate(Screen.DrugSearchScreen.route(drugNdc))
                        },
                        navigateToSummaryScreen = {
                            nav.navigate(Screen.SummaryCheckoutScreen.route)
                        },
                        changeStateToIdle = {
                            viewModel.changeState(ShoppingCartViewModel.ShoppingCartUiState.Idle)
                        }
                    )
                }
                composable(Screen.PharmacyStockScreen.route) {
                    //---------------------------------ViewModel(s)-----------------------------------------
                    val shoppingGraphEntry =
                        remember(it) { nav.getBackStackEntry(NavGraphs.SHOPPING_CART_GRAPH.toRegularString()) }
                    val viewModel: ShoppingCartViewModel = hiltViewModel(shoppingGraphEntry)
                    val drugGraphEntry =
                        remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                    val pharmacyGraphEntry =
                        remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }
                    val drugViewModel: DrugViewModel = hiltViewModel(drugGraphEntry)
                    val pharmacyViewModel: PharmacyViewModel = hiltViewModel(pharmacyGraphEntry)
                    //--------------------------------------------------------------------------------------

                    val cartSizeState by viewModel.cartSize.collectAsStateWithLifecycle()

                    PharmacyStockScreen(
                        event = viewModel.events,
                        drugViewModel = drugViewModel,
                        pharmacyViewModel = pharmacyViewModel,
                        navigateToPharmacyScreen = { nav.navigate(Screen.PharmacyFullInfoScreen.route) },
                        cartSize = cartSizeState,
                        navigateToShoppingCartScreen = { nav.navigate(Screen.CartScreen.route) },
                        onAddToCartClick = { medStockDrug, pharmacyInfo, usersQt ->
                            viewModel.checkIfCanAdd(
                                packageNdc = medStockDrug.packageNdc,
                                pharmacyId = pharmacyInfo.id,
                                usersQt = usersQt,
                                stockSize = medStockDrug.qt,
                                onSuccess = {
                                    viewModel.addItemToCart(
                                        CartItem(
                                            packageNdc = medStockDrug.packageNdc,
                                            drugNDC = medStockDrug.drugNdc,
                                            name = medStockDrug.displayName,
                                            labelerName = medStockDrug.labelerName ?: "",
                                            brandName = medStockDrug.brandName,
                                            quantity = usersQt,
                                            drugPackageDesc = "",
                                            pharmacyId = pharmacyInfo.id,
                                            pharmacyName = pharmacyInfo.name,
                                            address = pharmacyInfo.address,
                                            city = pharmacyInfo.city,
                                            distanceKms = pharmacyInfo.distanceKms,
                                            price = medStockDrug.price
                                        )
                                    )
                                    // zanim dodac do koszyka sprawdzamy czy taka ilosc jaka wprowadzilismy + to co jest w koszyku
                                    // nie jest wieksze od tej jaka jest w magazynie
                                },
                            )
                        }
                    )
                }

                composable(Screen.SummaryCheckoutScreen.route) {
                    //---------------------------------ViewModel(s)---------------------------------
                    val shoppingGraphEntry =
                        remember(it) { nav.getBackStackEntry(NavGraphs.SHOPPING_CART_GRAPH.toRegularString()) }
                    val viewModel: ShoppingCartViewModel = hiltViewModel(shoppingGraphEntry)
                    val mainGraphEntry =
                        remember(it) { nav.getBackStackEntry(NavGraphs.MAIN_GRAPH.toRegularString()) }

                    val profileViewModel: ProfileScreenViewModel = hiltViewModel(mainGraphEntry)
                    val orderViewModel: OrdersViewModel = hiltViewModel()
                    //------------------------------------------------------------------------------

                    val state by orderViewModel.state.collectAsStateWithLifecycle()
                    val totalCartPrice by viewModel.totalCartPrice.collectAsStateWithLifecycle()
                    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
                    val userData by profileViewModel.userData.collectAsState()

                    SummaryCheckoutScreen(
                        navigateToShoppingCart = {
                            nav.navigate(Screen.CartScreen.route) {
                                popUpTo(Screen.SummaryCheckoutScreen.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        userData = userData,
                        events = orderViewModel.events,
                        state = state,
                        totalPrice = totalCartPrice,
                        itemsCount = cartItems.sumOf { item -> item.quantity },

                        // po submit dodac wszystkie produktu usera do firestora do orders
                        // i zupdejtowac pharmacyStock przez KTOR zmniejszajac ilosc towartu zgodnie z zakupem usera
                        // narazie bedzie to zaimplementowano nie do konca poprawnie, ale potem zrobie to przez outbox todo

                        onSubmitOrder = { delInfo, payMeth ->
                            orderViewModel.placeOrder(
                                paymentMethod = payMeth,
                                orderItem = cartItems.toOrderItem(),
                                stockDecrementItems = cartItems.toStockDecrementItem(),
                                deliveryData = delInfo,
                                totalPrice = totalCartPrice
                            )
                        },
                        onSuccessDialogConfirmed = {
                            orderViewModel.onSuccessDialogConfirmed()
                        },
                    )
                }
            }

            composable(Screen.OrdersScreen.route) {
                val orderViewModel: OrdersViewModel = hiltViewModel()

                val orders by orderViewModel.orders.collectAsStateWithLifecycle()

                OrdersScreen(
                    orders = orders,
                    loadOrders = { orderViewModel.loadOrders() },
                )
            }
        }
    }
}

sealed class Screen(val route: String) {
    // Auth flow
    data object SignInOptions : Screen("loginOpt")
    data object RegisterOptions : Screen("registerOpt") // 1
    data object AuthSmsScreen : Screen("authSms/{screen}?vid={vid}") {
        fun route(screen: OriginScreen, verificationId: String? = null): String {
            return if (verificationId == null) {
                "authSms/$screen"                // stara ścieżka nadal działa
            } else {
                "authSms/$screen?vid=${Uri.encode(verificationId)}"
            }
        }
    }

    data object EmailPasswordSignUpScreen : Screen("email_sign_up?originScreen={originScreen}") {
        fun route(originScreen: String? = null): String =
            if (originScreen == null) "email_sign_up" else "email_sign_up?originScreen=$originScreen"
    } // 2

    data object EmailPasswordSignInScreen : Screen("email_sign_in")
    data object AuthGate : Screen("authGate")

    // email i phone # sa wartosiamy opconalnymi, moze byc a moze i nie byc
    data object ProfileSetUpScreen :
        Screen("profileSetUp?phoneNumber={phoneNumber}&email={email}") // 3

    // Main flow
    data object HomeScreen : Screen("home")
    data object ProfileScreen : Screen("profile")
    data object Settings : Screen("settings?cue={cue}")
    data object Map : Screen("map")
    data object ListOfPharmacies : Screen("pharmacies")
    data object PharmacyFullInfoScreen : Screen("pharmacy")
    data object DrugSearchScreen : Screen("drug?drugNdc={drugNdc}") {
        fun route(drugNdc: String? = null): String =
            if (drugNdc == null) "drug" else "drug?drugNdc=$drugNdc"
    }

    data object PickPackageScreen : Screen("pick_package")
    data object CartScreen : Screen("cart")
    data object SummaryCheckoutScreen : Screen("summary_checkout_screen")
    data object PharmacyStockScreen : Screen("pharmacy_stock")
    data object CheckIfUserLoggedInScreen : Screen("check_login_state")

    data object OrdersScreen : Screen("orders")
}

enum class NavGraphs {
    MAIN_GRAPH,
    AUTH_GRAPH,
    CHECK_IF_LOGGED_IN_GRAPH,
    SHOPPING_CART_GRAPH
}

fun NavGraphs.toRegularString(): String {
    return if (this == NavGraphs.MAIN_GRAPH) {
        "main_graph"
    } else if (this == NavGraphs.AUTH_GRAPH) {
        "auth_graph"
    } else if (this == NavGraphs.CHECK_IF_LOGGED_IN_GRAPH) {
        "check_if_logged_in_graph"
    } else {
        "shopping_cart_graph"
    }
}

fun String?.toNavGraphOrNull(): NavGraphs? =
    when (this) {
        "main_graph" -> NavGraphs.MAIN_GRAPH
        "auth_graph" -> NavGraphs.AUTH_GRAPH
        "check_if_logged_in_graph" -> NavGraphs.CHECK_IF_LOGGED_IN_GRAPH
        "shopping_cart_graph" -> NavGraphs.SHOPPING_CART_GRAPH
        else -> null
    }