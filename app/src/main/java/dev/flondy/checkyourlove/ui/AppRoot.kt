package dev.flondy.checkyourlove.ui

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.flondy.checkyourlove.AppContainer
import dev.flondy.checkyourlove.R
import dev.flondy.checkyourlove.data.AppSettings
import dev.flondy.checkyourlove.data.MotionPreference
import dev.flondy.checkyourlove.data.ThemeMode
import dev.flondy.checkyourlove.monetization.CatalogItem
import dev.flondy.checkyourlove.monetization.ResultEffect
import dev.flondy.checkyourlove.monetization.ShareTemplate
import dev.flondy.checkyourlove.monetization.ads.BannerAd
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
import java.text.DateFormat
import java.util.Date

private object Routes {
    const val TEST = "test"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val PREMIUM = "premium"
}

private data class Tab(val route: String, val label: Int, val icon: ImageVector, val selectedIcon: ImageVector)

private val tabs = listOf(
    Tab(Routes.TEST, R.string.nav_test, Icons.Rounded.FavoriteBorder, Icons.Rounded.Favorite),
    Tab(Routes.HISTORY, R.string.nav_history, Icons.Rounded.History, Icons.Rounded.History),
    Tab(Routes.SETTINGS, R.string.nav_settings, Icons.Outlined.Settings, Icons.Rounded.Settings),
)

@Composable
fun CheckYourLoveRoot(container: AppContainer) {
    val settingsVm: SettingsViewModel = viewModel(factory = viewModelFactory { initializer { SettingsViewModel(container.settings) } })
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

    val activity = LocalActivity.current as? ComponentActivity
    LaunchedEffect(dark, activity) {
        activity?.enableEdgeToEdge(
            statusBarStyle = if (dark) SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            else SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = if (dark) SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            else SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
        )
    }

    // Einwilligung & Werbe-SDK nur für Nutzer ohne Premium, sobald der Status feststeht.
    LaunchedEffect(entitlements.showAds, activity) {
        if (entitlements.showAds && activity != null) container.ads.gatherConsent(activity)
    }

    CheckYourLoveTheme(palette = palette, dark = dark, reducedMotionPreference = reduced, haptics = current.haptics) {
        AppBackground {
            if (settings != null) AppContent(container, settingsVm, current, palette, dark, activity)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppContent(
    container: AppContainer,
    settingsVm: SettingsViewModel,
    settings: AppSettings,
    palette: AppPalette,
    dark: Boolean,
    activity: Activity?,
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
    val resources = LocalResources.current
    val colors = LocalPalette.current

    var lockedItem by remember { mutableStateOf<CatalogItem?>(null) }
    var rewardItem by remember { mutableStateOf<CatalogItem?>(null) }
    var shareResult by remember { mutableStateOf<ResultUi?>(null) }
    var detailId by rememberSaveable { mutableStateOf<Long?>(null) }

    val effect = ResultEffect.byId(settings.effectId).let { if (entitlements.isUnlocked(it)) it else ResultEffect.CONFETTI }
    val template = ShareTemplate.byId(settings.shareTemplateId)
    val rewardedAvailable = ads.rewardedEnabled && entitlements.showAds && canRequestAds

    fun message(id: Int, vararg args: Any) {
        val text = resources.getString(id, *args)
        scope.launch { snackbar.showSnackbar(text) }
    }

    LaunchedEffect(gateway) {
        gateway.events.collect { event ->
            haptics.confirm()
            message(
                when (event) {
                    PurchaseEvent.SUCCESS -> R.string.purchase_success
                    PurchaseEvent.PENDING -> R.string.purchase_pending
                    PurchaseEvent.CANCELED -> R.string.purchase_canceled
                    PurchaseEvent.FAILED -> R.string.purchase_failed
                    PurchaseEvent.ALREADY_OWNED -> R.string.purchase_already_owned
                    PurchaseEvent.RESTORED -> R.string.purchase_restored
                    PurchaseEvent.NOTHING_TO_RESTORE -> R.string.purchase_nothing_to_restore
                    PurchaseEvent.REVOKED -> R.string.purchase_revoked
                    PurchaseEvent.SERVICE_UNAVAILABLE -> R.string.purchase_service_unavailable
                }
            )
        }
    }

    val showBottomBar = route != Routes.PREMIUM
    val showBanner = entitlements.showAds && canRequestAds && sdkReady &&
        (route == Routes.TEST || route == Routes.HISTORY) && !WindowInsets.isImeVisible

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (showBottomBar) {
                Column {
                    if (showBanner) BannerAd(ads.bannerUnitId, ads)
                    NavigationBar(containerColor = colors.surface.copy(alpha = 0.92f), tonalElevation = 0.dp) {
                        tabs.forEach { tab ->
                            val selected = route == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
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
        Box(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .consumeWindowInsets(inner)
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
                    val vm: LoveTestViewModel = viewModel(factory = viewModelFactory {
                        initializer { LoveTestViewModel(container.history, createSavedStateHandle()) }
                    })
                    val state by vm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(vm) {
                        vm.events.collect { event ->
                            when (event) {
                                is TestEvent.Completed -> {
                                    haptics.confirm()
                                    ads.onTestCompleted(entitlements.showAds)
                                }
                                TestEvent.SaveFailed -> message(R.string.result_saved_failed)
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
                            if (activity != null) ads.maybeShowInterstitial(activity, entitlements.showAds) { vm.newTest() }
                            else vm.newTest()
                        },
                        onToggleFavorite = vm::toggleFavorite,
                        onShare = { shareResult = it },
                        onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                    )
                }
                composable(Routes.HISTORY) {
                    val vm: HistoryViewModel = viewModel(factory = viewModelFactory { initializer { HistoryViewModel(container.history) } })
                    val state by vm.state.collectAsStateWithLifecycle()
                    val deletedText = stringResource(R.string.history_deleted)
                    val undoText = stringResource(R.string.history_undo)
                    val clearedText = stringResource(R.string.history_cleared)
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
                        onPrivacyOptions = { activity?.let(ads::showPrivacyOptions) },
                        onResetConsent = {
                            ads.resetConsentForDebug()
                            message(R.string.settings_consent_reset_done)
                        },
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
                        onBuy = { activity?.let(gateway::launchPurchase) },
                        onRestore = gateway::restore,
                        onTry = { rewardItem = it },
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------ Dialoge & Sheets

    shareResult?.let { result ->
        ShareSheet(
            result = result,
            selected = template,
            sharePalette = palette.colors(dark = true),
            isUnlocked = entitlements::isUnlocked,
            onSelect = settingsVm::setShareTemplate,
            onLocked = { lockedItem = it },
            onDismiss = { shareResult = null },
        )
    }

    lockedItem?.let { item ->
        val name = stringResource(item.labelRes)
        AlertDialog(
            onDismissRequest = { lockedItem = null },
            title = { Text(stringResource(R.string.locked_title, name)) },
            text = { Text(stringResource(R.string.locked_text)) },
            confirmButton = {
                TextButton(onClick = {
                    lockedItem = null
                    shareResult = null
                    navController.navigate(Routes.PREMIUM)
                }) { Text(stringResource(R.string.locked_go_premium)) }
            },
            dismissButton = {
                if (rewardedAvailable) {
                    TextButton(onClick = { lockedItem = null; rewardItem = item }) { Text(stringResource(R.string.locked_watch_video)) }
                } else {
                    TextButton(onClick = { lockedItem = null }) { Text(stringResource(R.string.action_cancel)) }
                }
            },
        )
    }

    rewardItem?.let { item ->
        LaunchedEffect(item) { ads.loadRewarded() }
        val name = stringResource(item.labelRes)
        AlertDialog(
            onDismissRequest = { rewardItem = null },
            title = { Text(stringResource(R.string.reward_dialog_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.reward_dialog_text, name))
                    when (rewardedStatus) {
                        RewardedStatus.LOADING, RewardedStatus.IDLE -> Text(
                            stringResource(R.string.reward_loading),
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        RewardedStatus.UNAVAILABLE -> Text(
                            stringResource(R.string.reward_unavailable),
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        RewardedStatus.READY -> Unit
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = rewardedStatus == RewardedStatus.READY && activity != null,
                    onClick = {
                        val act = activity ?: return@TextButton
                        rewardItem = null
                        ads.showRewarded(
                            activity = act,
                            onReward = {
                                scope.launch {
                                    container.entitlements.grantReward(item)
                                    applySelection(item, settingsVm)
                                }
                            },
                            onClosed = { earned ->
                                if (earned) {
                                    val until = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                        .format(Date(System.currentTimeMillis() + dev.flondy.checkyourlove.monetization.Entitlements.REWARD_DURATION_MS))
                                    message(R.string.reward_granted, name, until)
                                } else {
                                    message(R.string.reward_not_granted)
                                }
                            },
                        )
                    },
                ) { Text(stringResource(R.string.reward_dialog_watch)) }
            },
            dismissButton = { TextButton(onClick = { rewardItem = null }) { Text(stringResource(R.string.action_cancel)) } },
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

