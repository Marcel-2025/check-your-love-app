package dev.flondy.checkyourlove.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.CompositionLocalProvider
import dev.flondy.checkyourlove.ui.theme.LocalWindowLayout
import dev.flondy.checkyourlove.content.Content
import dev.flondy.checkyourlove.content.QuizPack
import dev.flondy.checkyourlove.ui.group.GroupScreen
import dev.flondy.checkyourlove.ui.group.GroupViewModel
import dev.flondy.checkyourlove.ui.ideas.IdeasScreen
import dev.flondy.checkyourlove.ui.ideas.IdeasViewModel
import dev.flondy.checkyourlove.ui.quiz.QuizPhase
import dev.flondy.checkyourlove.ui.quiz.QuizScreen
import dev.flondy.checkyourlove.ui.quiz.QuizViewModel
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Quiz
import dev.flondy.checkyourlove.ui.theme.WindowLayout
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import dev.flondy.checkyourlove.platform.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import dev.flondy.checkyourlove.data.AppSettings
import dev.flondy.checkyourlove.data.MotionPreference
import dev.flondy.checkyourlove.data.ThemeMode
import dev.flondy.checkyourlove.monetization.CatalogItem
import dev.flondy.checkyourlove.monetization.ResultEffect
import dev.flondy.checkyourlove.monetization.ShareTemplate
import dev.flondy.checkyourlove.monetization.ads.RewardedStatus
import dev.flondy.checkyourlove.monetization.billing.PurchaseEvent
import dev.flondy.checkyourlove.monetization.billing.SimulatedBillingGateway
import dev.flondy.checkyourlove.ui.components.AppBackground
import dev.flondy.checkyourlove.ui.history.HistoryScreen
import dev.flondy.checkyourlove.ui.history.HistoryViewModel
import dev.flondy.checkyourlove.ui.history.ResultDetailSheet
import dev.flondy.checkyourlove.ui.history.toResultUi
import dev.flondy.checkyourlove.ui.premium.PremiumScreen
import dev.flondy.checkyourlove.ui.premium.SimulationControls
import dev.flondy.checkyourlove.ui.settings.SettingsScreen
import dev.flondy.checkyourlove.ui.settings.SettingsViewModel
import dev.flondy.checkyourlove.ui.share.ShareSheet
import dev.flondy.checkyourlove.ui.test.LoveTestScreen
import dev.flondy.checkyourlove.ui.test.LoveTestViewModel
import dev.flondy.checkyourlove.ui.test.ResultUi
import dev.flondy.checkyourlove.ui.test.TestEvent
import dev.flondy.checkyourlove.ui.theme.AppPalette
import dev.flondy.checkyourlove.ui.theme.CheckYourLoveTheme
import dev.flondy.checkyourlove.ui.theme.LocalHaptics
import dev.flondy.checkyourlove.ui.theme.LocalMotion
import dev.flondy.checkyourlove.ui.theme.LocalPalette
import kotlinx.coroutines.launch
import dev.flondy.checkyourlove.platform.AppServices
import dev.flondy.checkyourlove.platform.formatShortDateTime
import dev.flondy.checkyourlove.platform.currentTimeMillis
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

private object Routes {
    const val TEST = "test"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val PREMIUM = "premium"
    const val GROUP = "group"
    const val QUIZ = "quiz"
    const val IDEAS = "ideas"
}

private data class Tab(val route: String, val label: StringResource, val icon: ImageVector, val selectedIcon: ImageVector)

private val tabs = listOf(
    Tab(Routes.TEST, Res.string.nav_test, Icons.Rounded.FavoriteBorder, Icons.Rounded.Favorite),
    Tab(Routes.GROUP, Res.string.nav_group, Icons.Outlined.Groups, Icons.Rounded.Groups),
    Tab(Routes.QUIZ, Res.string.nav_quiz, Icons.Outlined.Quiz, Icons.Rounded.Quiz),
    Tab(Routes.IDEAS, Res.string.nav_ideas, Icons.Outlined.Lightbulb, Icons.Rounded.Lightbulb),
    Tab(Routes.HISTORY, Res.string.nav_history, Icons.Rounded.History, Icons.Rounded.History),
)
private val tabRoutes = tabs.map { it.route }.toSet()

/**
 * Gemeinsamer Einstiegspunkt der Oberfläche für Android und iOS.
 * [onDarkThemeChanged] erlaubt der Plattform, Systemleisten passend einzufärben.
 */
@Composable
fun CheckYourLoveApp(container: AppServices, onDarkThemeChanged: (Boolean) -> Unit = {}) {
    val settingsVm: SettingsViewModel = viewModel { SettingsViewModel(container.settings) }
    val settings by settingsVm.settings.collectAsStateWithLifecycle()
    val entitlements by container.entitlements.entitlements.collectAsStateWithLifecycle()
    val current = settings ?: AppSettings()

    val dark = when (current.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val selectedPalette = AppPalette.byId(current.paletteId)
    val palette = if (entitlements.isUnlocked(selectedPalette)) selectedPalette else AppPalette.AUBERGINE
    val reduced = when (current.motion) {
        MotionPreference.SYSTEM -> null
        MotionPreference.REDUCED -> true
        MotionPreference.FULL -> false
    }

    LaunchedEffect(dark) { onDarkThemeChanged(dark) }

    // Einwilligung & Werbe-SDK nur für Nutzer ohne Premium, sobald der Status feststeht.
    LaunchedEffect(entitlements.showAds) {
        if (entitlements.showAds) container.ads.gatherConsent()
    }

    CheckYourLoveTheme(palette = palette, dark = dark, reducedMotionPreference = reduced, haptics = current.haptics) {
        AppBackground {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val layout = WindowLayout(maxWidth, maxHeight)
                CompositionLocalProvider(LocalWindowLayout provides layout) {
                    if (settings != null) AppContent(container, settingsVm, current, palette, dark)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppContent(
    container: AppServices,
    settingsVm: SettingsViewModel,
    settings: AppSettings,
    palette: AppPalette,
    dark: Boolean,
) {
    val ads = container.ads
    val gateway = container.premiumGateway
    val entitlements by container.entitlements.entitlements.collectAsStateWithLifecycle()
    val premiumState by gateway.state.collectAsStateWithLifecycle()
    val canRequestAds by ads.canRequestAds.collectAsStateWithLifecycle()
    val sdkReady by ads.sdkReady.collectAsStateWithLifecycle()
    val privacyRequired by ads.privacyOptionsRequired.collectAsStateWithLifecycle()
    val rewardedStatus by ads.rewardedStatus.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val motion = LocalMotion.current
    val haptics = LocalHaptics.current
    val colors = LocalPalette.current

    var lockedItem by remember { mutableStateOf<CatalogItem?>(null) }
    var rewardItem by remember { mutableStateOf<CatalogItem?>(null) }
    var shareResult by remember { mutableStateOf<ResultUi?>(null) }
    var detailId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pairDetail by remember { mutableStateOf<ResultUi?>(null) }
    val openSettings: () -> Unit = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } }
    val shareChooser = stringResource(Res.string.share_chooser)
    fun shareText(text: String) = runCatching { container.share.shareText(text, shareChooser) }
    var bannerFailed by remember { mutableStateOf(false) }

    val effect = ResultEffect.byId(settings.effectId).let { if (entitlements.isUnlocked(it)) it else ResultEffect.CONFETTI }
    val template = ShareTemplate.byId(settings.shareTemplateId)
    val rewardedAvailable = ads.rewardedEnabled && entitlements.showAds && canRequestAds

    fun message(id: StringResource, vararg args: Any) {
        scope.launch { snackbar.showSnackbar(getString(id, *args)) }
    }

    LaunchedEffect(gateway) {
        gateway.events.collect { event ->
            haptics.confirm()
            message(
                when (event) {
                    PurchaseEvent.SUCCESS -> Res.string.purchase_success
                    PurchaseEvent.PENDING -> Res.string.purchase_pending
                    PurchaseEvent.CANCELED -> Res.string.purchase_canceled
                    PurchaseEvent.FAILED -> Res.string.purchase_failed
                    PurchaseEvent.ALREADY_OWNED -> Res.string.purchase_already_owned
                    PurchaseEvent.RESTORED -> Res.string.purchase_restored
                    PurchaseEvent.NOTHING_TO_RESTORE -> Res.string.purchase_nothing_to_restore
                    PurchaseEvent.REVOKED -> Res.string.purchase_revoked
                    PurchaseEvent.SERVICE_UNAVAILABLE -> Res.string.purchase_service_unavailable
                }
            )
        }
    }

    val showBottomBar = route == null || route in tabRoutes
    val showBanner = entitlements.showAds && canRequestAds && sdkReady &&
        (route == Routes.TEST || route == Routes.HISTORY) && !isImeVisible()

    val layout = LocalWindowLayout.current
    // Tablets und Handys im Querformat: Navigation seitlich (Rail), sonst unten.
    val useRail = layout.useNavigationRail
    fun selectTab(target: String) {
        if (route != target) navController.navigate(target) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val bannerSlot: @Composable () -> Unit = {
        val banner = container.banner
        if (showBanner && banner != null && !bannerFailed) {
            banner(ads.bannerUnitId, { bannerFailed = true }, Modifier)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (showBottomBar && !useRail) {
                Column {
                    bannerSlot()
                    NavigationBar(containerColor = colors.surface.copy(alpha = 0.92f), tonalElevation = 0.dp) {
                        tabs.forEach { tab ->
                            val selected = route == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { selectTab(tab.route) },
                                icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                                label = { Text(stringResource(tab.label), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = colors.onPrimaryContainer,
                                    indicatorColor = colors.primaryContainer,
                                    selectedTextColor = colors.primary,
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { inner ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .consumeWindowInsets(inner)
        ) {
            if (showBottomBar && useRail) {
                NavigationRail(
                    containerColor = colors.surface.copy(alpha = 0.92f),
                    windowInsets = WindowInsets(0),
                    modifier = Modifier.fillMaxHeight(),
                ) {
                    Spacer(Modifier.weight(1f))
                    tabs.forEach { tab ->
                        val selected = route == tab.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = { selectTab(tab.route) },
                            icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = colors.onPrimaryContainer,
                                indicatorColor = colors.primaryContainer,
                                selectedTextColor = colors.primary,
                            ),
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    NavigationRailItem(
                        selected = false,
                        onClick = openSettings,
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        label = { Text(stringResource(Res.string.nav_settings), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        colors = NavigationRailItemDefaults.colors(indicatorColor = colors.primaryContainer),
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight()) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding()
        ) {
            val duration = if (motion.reduced) 120 else 320
            NavHost(
                navController = navController,
                startDestination = Routes.TEST,
                enterTransition = { fadeIn(tween(duration)) + slideInHorizontally(tween(duration)) { it / 12 } },
                exitTransition = { fadeOut(tween(duration / 2)) },
                popEnterTransition = { fadeIn(tween(duration)) + slideInHorizontally(tween(duration)) { -it / 12 } },
                popExitTransition = { fadeOut(tween(duration / 2)) + slideOutHorizontally(tween(duration)) { it / 12 } },
            ) {
                composable(Routes.TEST) {
                    val vm: LoveTestViewModel = viewModel { LoveTestViewModel(container.history, createSavedStateHandle()) }
                    val state by vm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(vm) {
                        vm.events.collect { event ->
                            when (event) {
                                is TestEvent.Completed -> {
                                    haptics.confirm()
                                    ads.onTestCompleted(entitlements.showAds)
                                }
                                TestEvent.SaveFailed -> message(Res.string.result_saved_failed)
                            }
                        }
                    }
                    LoveTestScreen(
                        state = state,
                        effect = effect,
                        showPremiumEntry = !entitlements.premium && premiumState.availability != dev.flondy.checkyourlove.monetization.billing.StoreAvailability.NOT_CONFIGURED,
                        contentPadding = PaddingValues(),
                        onNameA = vm::onNameAChange,
                        onNameB = vm::onNameBChange,
                        onSwap = vm::swap,
                        onReset = vm::reset,
                        onEvaluate = { vm.evaluate(motion.evaluationMillis) },
                        onNewTest = {
                            ads.maybeShowInterstitial(entitlements.showAds) { vm.newTest() }
                        },
                        onToggleFavorite = vm::toggleFavorite,
                        onShare = { shareResult = it },
                        onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                        onMode = vm::setMode,
                        onSettings = if (useRail) null else openSettings,
                    )
                }
                composable(Routes.GROUP) {
                    val vm: GroupViewModel = viewModel { GroupViewModel() }
                    val state by vm.state.collectAsStateWithLifecycle()
                    GroupScreen(
                        state = state,
                        onSettings = if (useRail) null else openSettings,
                        onName = vm::setName,
                        onAdd = vm::add,
                        onRemove = vm::remove,
                        onMode = vm::setMode,
                        onEvaluate = {
                            if (vm.evaluate()) { haptics.confirm(); ads.onTestCompleted(entitlements.showAds) }
                        },
                        onEdit = { ads.maybeShowInterstitial(entitlements.showAds) { vm.edit() } },
                        onOpenPair = { pairDetail = it },
                        onShare = ::shareText,
                    )
                }
                composable(Routes.QUIZ) {
                    val vm: QuizViewModel = viewModel { QuizViewModel() }
                    val state by vm.state.collectAsStateWithLifecycle()
                    val pool = remember(state.pack) { Content.quiz(state.pack) }
                    QuizScreen(
                        state = state,
                        entitlements = entitlements,
                        effect = effect,
                        onSettings = if (useRail) null else openSettings,
                        onNameA = vm::setNameA,
                        onNameB = vm::setNameB,
                        onPack = vm::selectPack,
                        onLocked = { lockedItem = it },
                        onStart = { vm.start(if (entitlements.isUnlocked(state.pack)) pool else Content.quiz(QuizPack.BASIC)) },
                        onAnswer = vm::answer,
                        onReady = vm::ready,
                        onGuess = vm::guess,
                        onNext = {
                            vm.next()
                            if (vm.state.value.phase == QuizPhase.RESULT) ads.onTestCompleted(entitlements.showAds)
                        },
                        onPlayAgain = { ads.maybeShowInterstitial(entitlements.showAds) { vm.playAgain(pool) } },
                        onChangeNames = vm::backToSetup,
                        onShare = ::shareText,
                    )
                }
                composable(Routes.IDEAS) {
                    val vm: IdeasViewModel = viewModel { IdeasViewModel(container.settings) }
                    val state by vm.state.collectAsStateWithLifecycle()
                    IdeasScreen(
                        state = state,
                        entitlements = entitlements,
                        onSettings = if (useRail) null else openSettings,
                        onCategory = vm::selectCategory,
                        onFavorites = vm::showFavorites,
                        onLocked = { lockedItem = it },
                        onNext = vm::next,
                        onPrevious = vm::previous,
                        onToggleFavorite = vm::toggleFavorite,
                        onShare = ::shareText,
                    )
                }
                composable(Routes.HISTORY) {
                    val vm: HistoryViewModel = viewModel { HistoryViewModel(container.history) }
                    val state by vm.state.collectAsStateWithLifecycle()
                    val deletedText = stringResource(Res.string.history_deleted)
                    val undoText = stringResource(Res.string.history_undo)
                    val clearedText = stringResource(Res.string.history_cleared)
                    HistoryScreen(
                        state = state,
                        contentPadding = PaddingValues(),
                        onFilter = vm::setFilter,
                        onOpen = { detailId = it.id },
                        onToggleFavorite = vm::toggleFavorite,
                        onDelete = { item ->
                            vm.delete(item)
                            scope.launch {
                                val result = snackbar.showSnackbar(deletedText, actionLabel = undoText, duration = SnackbarDuration.Short)
                                if (result == SnackbarResult.ActionPerformed) vm.undoDelete(item)
                            }
                        },
                        onClear = { keep ->
                            vm.clear(keep)
                            scope.launch { snackbar.showSnackbar(clearedText) }
                        },
                        onStartTest = {
                            navController.navigate(Routes.TEST) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onSettings = if (useRail) null else openSettings,
                    )
                    val detail = state.items.firstOrNull { it.id == detailId }
                    if (detail != null) {
                        ResultDetailSheet(
                            result = detail.toResultUi(),
                            onToggleFavorite = { vm.toggleFavorite(detail) },
                            onShare = { shareResult = detail.toResultUi() },
                            onDismiss = { detailId = null },
                        )
                    }
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        config = container.config,
                        settings = settings,
                        entitlements = entitlements,
                        storeAvailability = premiumState.availability,
                        darkPreview = dark,
                        privacyOptionsRequired = privacyRequired,
                        contentPadding = PaddingValues(),
                        onThemeMode = settingsVm::setThemeMode,
                        onPalette = settingsVm::setPalette,
                        onEffect = settingsVm::setEffect,
                        onHaptics = settingsVm::setHaptics,
                        onMotion = settingsVm::setMotion,
                        onLocked = { lockedItem = it },
                        onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                        onRestore = gateway::restore,
                        onPrivacyOptions = ads::showPrivacyOptions,
                        onResetConsent = {
                            ads.resetConsentForDebug()
                            message(Res.string.settings_consent_reset_done)
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.PREMIUM) {
                    LaunchedEffect(rewardedAvailable) { if (rewardedAvailable) ads.loadRewarded() }
                    val sim = gateway as? SimulatedBillingGateway
                    val simOutcome = sim?.nextOutcome?.collectAsStateWithLifecycle()
                    PremiumScreen(
                        state = premiumState,
                        entitlements = entitlements,
                        darkPreview = dark,
                        simulation = if (sim != null && simOutcome != null) SimulationControls(
                            outcome = simOutcome.value,
                            onOutcome = { sim.nextOutcome.value = it },
                            onCompletePending = sim::completePending,
                            onRefund = sim::simulateRefund,
                        ) else null,
                        rewardedEnabled = rewardedAvailable,
                        rewardedStatus = rewardedStatus,
                        contentPadding = PaddingValues(),
                        onBack = { navController.popBackStack() },
                        onBuy = gateway::launchPurchase,
                        onRestore = gateway::restore,
                        onTry = { rewardItem = it },
                    )
                }
            }
        }
                if (showBottomBar && useRail) bannerSlot()
            }
        }
    }

    // ------------------------------------------------------------------ Dialoge & Sheets

    pairDetail?.let { result ->
        ResultDetailSheet(
            result = result,
            onToggleFavorite = {},
            onShare = { shareResult = result },
            onDismiss = { pairDetail = null },
        )
    }

    shareResult?.let { result ->
        ShareSheet(
            result = result,
            selected = template,
            sharePalette = palette.colors(dark = true),
            isUnlocked = entitlements::isUnlocked,
            onSelect = settingsVm::setShareTemplate,
            onLocked = { lockedItem = it },
            shareService = container.share,
            onDismiss = { shareResult = null },
        )
    }

    lockedItem?.let { item ->
        val name = stringResource(item.labelRes)
        AlertDialog(
            onDismissRequest = { lockedItem = null },
            title = { Text(stringResource(Res.string.locked_title, name)) },
            text = { Text(stringResource(Res.string.locked_text)) },
            confirmButton = {
                TextButton(onClick = {
                    lockedItem = null
                    shareResult = null
                    navController.navigate(Routes.PREMIUM)
                }) { Text(stringResource(Res.string.locked_go_premium)) }
            },
            dismissButton = {
                if (rewardedAvailable) {
                    TextButton(onClick = { lockedItem = null; rewardItem = item }) { Text(stringResource(Res.string.locked_watch_video)) }
                } else {
                    TextButton(onClick = { lockedItem = null }) { Text(stringResource(Res.string.action_cancel)) }
                }
            },
        )
    }

    rewardItem?.let { item ->
        LaunchedEffect(item) { ads.loadRewarded() }
        val name = stringResource(item.labelRes)
        AlertDialog(
            onDismissRequest = { rewardItem = null },
            title = { Text(stringResource(Res.string.reward_dialog_title)) },
            text = {
                Column {
                    Text(stringResource(Res.string.reward_dialog_text, name))
                    when (rewardedStatus) {
                        RewardedStatus.LOADING, RewardedStatus.IDLE -> Text(
                            stringResource(Res.string.reward_loading),
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        RewardedStatus.UNAVAILABLE -> Text(
                            stringResource(Res.string.reward_unavailable),
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        RewardedStatus.READY -> Unit
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = rewardedStatus == RewardedStatus.READY,
                    onClick = {
                        rewardItem = null
                        ads.showRewarded(
                            onReward = {
                                scope.launch {
                                    container.entitlements.grantReward(item)
                                    applySelection(item, settingsVm)
                                }
                            },
                            onClosed = { earned ->
                                if (earned) {
                                    val until = formatShortDateTime(currentTimeMillis() + dev.flondy.checkyourlove.monetization.Entitlements.REWARD_DURATION_MS)
                                    message(Res.string.reward_granted, name, until)
                                } else {
                                    message(Res.string.reward_not_granted)
                                }
                            },
                        )
                    },
                ) { Text(stringResource(Res.string.reward_dialog_watch)) }
            },
            dismissButton = { TextButton(onClick = { rewardItem = null }) { Text(stringResource(Res.string.action_cancel)) } },
        )
    }
}

/** Nach erfolgreicher Belohnung das freigeschaltete Element direkt auswählen. */
private fun applySelection(item: CatalogItem, vm: SettingsViewModel) {
    when (item) {
        is AppPalette -> vm.setPalette(item)
        is ResultEffect -> vm.setEffect(item)
        is ShareTemplate -> vm.setShareTemplate(item)
    }
}

