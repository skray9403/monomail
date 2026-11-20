package com.shrivatsav.monomail.ui.navigation
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.currentBackStackEntryAsState
import com.shrivatsav.monomail.ui.components.FloatingDockController
import com.shrivatsav.monomail.ui.components.LocalFloatingDockController
import com.shrivatsav.monomail.ui.components.DockMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shrivatsav.monomail.core.data.auth.AuthManager
import com.shrivatsav.monomail.core.data.repository.EmailRepository
import com.shrivatsav.monomail.core.data.settings.SettingsDataStore
import com.shrivatsav.monomail.feature.auth.ImapSetupScreen
import com.shrivatsav.monomail.feature.auth.ImapSetupViewModel
import com.shrivatsav.monomail.feature.auth.SignInScreen
import com.shrivatsav.monomail.feature.auth.SignInViewModel
import com.shrivatsav.monomail.feature.compose.ComposeMode
import com.shrivatsav.monomail.feature.compose.ComposeScreen
import com.shrivatsav.monomail.feature.compose.ComposeViewModel
import com.shrivatsav.monomail.data.model.EmailAttachment
import com.shrivatsav.monomail.model.InboxTab
import com.shrivatsav.monomail.feature.detail.EmailDetailScreen
import com.shrivatsav.monomail.feature.detail.EmailDetailViewModel
import com.shrivatsav.monomail.feature.inbox.InboxScreen
import com.shrivatsav.monomail.feature.inbox.InboxNavActions
import com.shrivatsav.monomail.feature.inbox.InboxViewModel
import com.shrivatsav.monomail.feature.inbox.scheduled.ScheduledMessagesScreen
import com.shrivatsav.monomail.feature.inbox.scheduled.ScheduledMessagesViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import com.shrivatsav.monomail.feature.settings.pgp.PgpKeyManagementScreen
import com.shrivatsav.monomail.feature.settings.SettingsScreen
import com.shrivatsav.monomail.feature.settings.SettingsViewModel
import com.shrivatsav.monomail.feature.attachment.AttachmentViewerScreen

sealed class Screen(val route: String) {
    object Onboarding   : Screen("onboarding")
    object SignIn       : Screen("sign_in")
    object ImapSetup    : Screen("imap_setup?prefillEmail={prefillEmail}&provider={provider}") {
        fun createRoute(prefillEmail: String? = null, provider: String? = null): String {
            val emailPart = if (prefillEmail != null) "imap_setup?prefillEmail=$prefillEmail" else "imap_setup"
            return if (provider != null) {
                val prefix = if (prefillEmail != null) "&" else "?"
                "$emailPart${prefix}provider=$provider"
            } else emailPart
        }
    }
    object Inbox        : Screen("inbox")
    object ThreadDetail : Screen("thread/{accountId}/{threadId}?focusedId={focusedId}") {
        fun createRoute(accountId: String, threadId: String, focusedId: String? = null): String {
            val base = "thread/${Uri.encode(accountId)}/${Uri.encode(threadId)}"
            return if (focusedId != null) "$base?focusedId=${Uri.encode(focusedId)}" else base
        }
    }
    object Compose      : Screen("compose?mode={mode}&to={to}&subject={subject}&threadId={threadId}&messageId={messageId}&scheduledId={scheduledId}&unified={unified}&accountId={accountId}") {
        fun createRoute(
            mode: ComposeMode = ComposeMode.NEW,
            to: String = "",
            subject: String = "",
            threadId: String = "",
            messageId: String = "",
            scheduledId: String = "",
            unified: Boolean = false,
            accountId: String = ""
        ): String {
            val enc = { s: String -> Uri.encode(s) }
            return "compose?mode=${mode.name}&to=${enc(to)}&subject=${enc(subject)}&threadId=${enc(threadId)}&messageId=${enc(messageId)}&scheduledId=${enc(scheduledId)}&unified=$unified&accountId=${enc(accountId)}"
        }
    }
    object Scheduled : Screen("scheduled")
    object Settings : Screen("settings")
    object SettingsSection : Screen("settings/{section}") {
        fun createRoute(section: com.shrivatsav.monomail.feature.settings.SettingsSection) =
            "settings/${section.name.lowercase()}"
    }
    object Legal : Screen("legal/{type}") {
        fun createRoute(type: String) = "legal/$type"
    }
    object PgpKeys : Screen("pgp_keys")
    object AttachmentViewer : Screen("attachment-viewer/{accountId}/{messageId}/{attachmentId}?mimeType={mimeType}&name={name}") {
        fun createRoute(accountId: String, messageId: String, attachmentId: String, mimeType: String, name: String): String {
            val enc = { s: String -> Uri.encode(s) }
            return "attachment-viewer/${enc(accountId)}/${enc(messageId)}/${enc(attachmentId)}?mimeType=${enc(mimeType)}&name=${enc(name)}"
        }
    }
}

private fun openLegalUrl(context: android.content.Context, type: String) {
    val url = if (type == "privacy") "https://monomail.millosaurs.me/pp" else "https://monomail.millosaurs.me/tos"
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
        .apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
    try { context.startActivity(intent) } catch (e: Exception) { android.util.Log.w("NavGraph", "Failed to open legal URL", e) }
}

@Composable
private fun ReauthDialog(
    authManager: AuthManager,
    navController: androidx.navigation.NavHostController,
    scope: CoroutineScope
) {
    val reauthInfo by authManager.reauthNeeded.collectAsState()
    val reauth = reauthInfo
    if (reauth == null) return

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            scope.launch {
                authManager.dismissReauth()
                authManager.forceRefreshToken(reauth.email)
            }
        }
    }

    AlertDialog(
        onDismissRequest = { authManager.dismissReauth() },
        title = { Text("Session Expired") },
        text = {
            Text(
                "Your ${reauth.provider.replaceFirstChar { it.uppercase() }} account " +
                "(${reauth.email}) needs to be re-authenticated.\n\n" +
                "You can sign out this account and continue with your other accounts."
            )
        },
        confirmButton = {
            Column {
                TextButton(onClick = {
                    val intent = reauth.intent
                    if (intent != null) {
                        launcher.launch(intent)
                    } else {
                        scope.launch {
                            authManager.dismissReauth()
                            authManager.forceRefreshToken(reauth.email)
                        }
                    }
                }) {
                    Text(if (reauth.intent != null) "Grant Permission" else "Refresh Token")
                }
                TextButton(onClick = {
                    scope.launch {
                        authManager.dismissReauth()
                        val accounts = authManager.getAccounts()
                        val targetId = accounts.find { it.email.equals(reauth.email, ignoreCase = true) }?.id ?: return@launch
                        authManager.removeAccount(targetId)
                        val remaining = authManager.getAccounts()
                        if (remaining.isEmpty()) {
                            authManager.signOutAll()
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }) {
                    Text("Sign out this account", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { authManager.dismissReauth() }) {
                Text("Later")
            }
        }
    )
}

private val NavSpatialSpec = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium,
)

private val NavFloatSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium,
)

private fun routeType(route: String?): String = when {
    route?.startsWith("compose") == true -> "compose"
    route?.startsWith("thread") == true ||
        route?.startsWith("settings") == true ||
        route?.startsWith("scheduled") == true ||
        route?.startsWith("pgp_keys") == true ||
        route?.startsWith("legal") == true ||
        route?.startsWith("attachment-viewer") == true -> "drill"
    else -> ""
}

private fun AnimatedContentTransitionScope<androidx.navigation.NavBackStackEntry>.slideInForRoute(
    motionScheme: MotionScheme
): EnterTransition = when (routeType(targetState.destination.route)) {
    "compose" ->
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = motionScheme.defaultSpatialSpec(),
        ) + fadeIn(animationSpec = motionScheme.fastEffectsSpec())
    "drill" ->
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = motionScheme.defaultSpatialSpec(),
        ) + fadeIn(animationSpec = motionScheme.fastEffectsSpec())
    else -> fadeIn(animationSpec = motionScheme.fastEffectsSpec())
}

private fun AnimatedContentTransitionScope<androidx.navigation.NavBackStackEntry>.slideOutForRoute(
    motionScheme: MotionScheme
): ExitTransition = when (routeType(targetState.destination.route)) {
    "compose", "drill" ->
        fadeOut(
            animationSpec = motionScheme.fastEffectsSpec(),
            targetAlpha = 0.92f,
        )
    else -> fadeOut(animationSpec = motionScheme.fastEffectsSpec())
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavGraph(
    authManager: AuthManager,
    emailRepository: EmailRepository,
    settingsDataStore: SettingsDataStore,
    onContentReady: () -> Unit = {}
) {
    val navController = rememberNavController()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val motionScheme = MaterialTheme.motionScheme
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    TrackBackSwipeEdge()

    // Navigation Compose owns the system predictive-back gesture, progress,
    // cancellation and completion. Avoid a second gesture observer here.
    var isLoading by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var hasSeenWelcomePrompt by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isAuthenticated = authManager.restoreSessionQuick()
        hasSeenWelcomePrompt = settingsDataStore.settingsFlow.first().hasSeenWelcomePrompt
        isLoading = false
        onContentReady()
        if (isAuthenticated) {
            scope.launch { authManager.restoreSession() }
        }
    }
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        return
    }
    val startDestination = when {
        isAuthenticated -> Screen.Inbox.route
        !hasSeenWelcomePrompt -> Screen.Onboarding.route
        else -> Screen.SignIn.route
    }

    val dockController = remember { FloatingDockController() }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val activeDockMode = when {
        currentRoute == Screen.Inbox.route -> DockMode.INBOX
        currentRoute?.startsWith("thread") == true -> DockMode.THREAD
        currentRoute?.startsWith("compose") == true -> DockMode.COMPOSE
        currentRoute?.startsWith("settings") == true -> {
            val sectionName = currentRoute.substringAfter("settings/", "").takeIf { it.isNotEmpty() }
            dockController.settingsTitle = sectionName?.replaceFirstChar { it.uppercase() } ?: "Settings"
            DockMode.SETTINGS
        }
        else -> DockMode.HIDDEN
    }
    dockController.mode = activeDockMode
    dockController.onBackClick = { navController.popBackStack() }
    if (activeDockMode != DockMode.INBOX && activeDockMode != DockMode.SETTINGS) {
        dockController.isScrolling = false
        dockController.isSearchActive = false
    }

    CompositionLocalProvider(LocalFloatingDockController provides dockController) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ReauthDialog(authManager, navController, scope)

        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { slideInForRoute(motionScheme) },
            exitTransition = { slideOutForRoute(motionScheme) },
            // Keep drill-down back navigation spatial. Navigation Compose makes these
            // transitions seekable, so Android's predictive-back gesture drives the
            // same motion continuously and can reverse it when the gesture is cancelled.
            // initialState is the screen being popped (the outgoing destination).
            popEnterTransition = {
                when (routeType(initialState.destination.route)) {
                    "drill", "compose" -> backRevealEnter()
                    else -> fadeIn(animationSpec = motionScheme.fastEffectsSpec())
                }
            },
            popExitTransition = {
                when (routeType(initialState.destination.route)) {
                    "compose" -> backSheetExit()
                    "drill" -> backCardExit(density)
                    else -> fadeOut(animationSpec = motionScheme.fastEffectsSpec())
                }
            },
        ) {
            composable(Screen.Onboarding.route) {
                val onboardingScope = androidx.compose.runtime.rememberCoroutineScope()
                com.shrivatsav.monomail.feature.auth.OnboardingScreen(
                    onFinishOnboarding = {
                        onboardingScope.launch {
                            settingsDataStore.setHasSeenWelcomePrompt(true)
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    }
                )
            }
            composable(Screen.SignIn.route) {
                val vm: SignInViewModel = hiltViewModel()
                val ctx = LocalContext.current
                SignInScreen(
                    viewModel      = vm,
                    onSignInSuccess = {
                        navController.navigate(Screen.Inbox.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onNavigateToLegal = { type -> openLegalUrl(ctx, type) },
                    onNavigateToImapSetup = { email, provider ->
                        val route = Screen.ImapSetup.createRoute(email, provider)
                        navController.navigate(route) { launchSingleTop = true }
                    },
                )
            }
            composable(Screen.ImapSetup.route) {
                val vm: ImapSetupViewModel = hiltViewModel()
                val prefillEmail = it.arguments?.getString("prefillEmail")
                val prefillProvider = it.arguments?.getString("provider")
                ImapSetupScreen(
                    viewModel = vm,
                    prefillEmail = prefillEmail,
                    prefillProvider = prefillProvider,
                    onSetupComplete = {
                        navController.navigate(Screen.Inbox.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Inbox.route) {
                BackCardSurface(navController) {
                    val vm: InboxViewModel = hiltViewModel()
                    val inboxScope = androidx.compose.runtime.rememberCoroutineScope()
                    val activeAccount by authManager.activeAccountFlow.collectAsState(initial = authManager.currentUser)
                    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                    val isTablet = configuration.screenWidthDp >= 600
                    var selectedThreadId by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
                    var selectedAccountId by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

                    if (isTablet) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(0.4f)) {
                                InboxScreen(
                                    viewModel = vm,
                                    userProfile = activeAccount,
                                    navActions = InboxNavActions(
                                        onEmailClick = { accountId, threadId, _ ->
                                            selectedAccountId = accountId
                                            selectedThreadId = threadId
                                        },
                                        onSignOut = {
                                            inboxScope.launch {
                                                authManager.signOutActiveAccount()
                                                if (authManager.getAccounts().isEmpty()) {
                                                    navController.navigate(Screen.SignIn.route) { popUpTo(0) { inclusive = true } }
                                                    emailRepository.clearLocalData()
                                                }
                                            }
                                        },
                                        onCompose = {
                                            val isUnified = vm.currentTab.value == InboxTab.UNIFIED
                                            navController.navigate(Screen.Compose.createRoute(unified = isUnified)) { launchSingleTop = true }
                                        },
                                        onSettings = { navController.navigate(Screen.Settings.route) { launchSingleTop = true } },
                                        onScheduledClick = { navController.navigate(Screen.Scheduled.route) { launchSingleTop = true } },
                                        onNavigateToImapSetup = { _, _ ->
                                            navController.navigate(Screen.ImapSetup.createRoute()) { launchSingleTop = true }
                                        },
                                    )
                                )
                            }
                            androidx.compose.material3.VerticalDivider(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                thickness = 1.dp
                            )
                            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(0.6f)) {
                                if (selectedThreadId != null) {
                                    val detailViewModel: EmailDetailViewModel = hiltViewModel(key = "${selectedAccountId}:${selectedThreadId}")
                                    androidx.compose.runtime.LaunchedEffect(selectedAccountId, selectedThreadId) {
                                        detailViewModel.setThread(selectedThreadId!!, selectedAccountId.orEmpty())
                                    }
                                    EmailDetailScreen(
                                        viewModel = detailViewModel,
                                        onBack = { selectedThreadId = null },
                                        onReply = { to, subject, body, threadId, messageId ->
                                            navController.navigate(
                                                Screen.Compose.createRoute(
                                                    mode = ComposeMode.REPLY,
                                                    to = to,
                                                    subject = subject,
                                                    threadId = threadId,
                                                    messageId = messageId,
                                                    accountId = detailViewModel.accountId
                                                )
                                            )
                                        },
                                        onForward = { subject, body, threadId, messageId ->
                                            navController.navigate(
                                                Screen.Compose.createRoute(
                                                    mode = ComposeMode.FORWARD,
                                                    subject = subject,
                                                    threadId = threadId,
                                                    messageId = messageId,
                                                    accountId = detailViewModel.accountId
                                                )
                                            )
                                        },
                                        onFetchAttachment = { msg, att -> detailViewModel.fetchAttachmentBytes(msg, att) },
                                        onNavigateToAttachmentViewer = { mId, aId, mime, name ->
                                            navController.navigate(Screen.AttachmentViewer.createRoute(detailViewModel.accountId, mId, aId, mime, name))
                                        }
                                    )
                                } else {
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            androidx.compose.material3.Icon(
                                                imageVector = Icons.Rounded.Email,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                                            androidx.compose.material3.Text(
                                                text = "Select an email to read",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        InboxScreen(
                            viewModel    = vm,
                            userProfile  = activeAccount,
                            navActions = InboxNavActions(
                                onEmailClick = { accountId, threadId, focusedId ->
                                    navController.navigate(Screen.ThreadDetail.createRoute(accountId, threadId, focusedId)) { launchSingleTop = true }
                                },
                                onSignOut = {
                                    inboxScope.launch {
                                        authManager.signOutActiveAccount()
                                        val accounts = authManager.getAccounts()
                                        if (accounts.isEmpty()) {
                                            navController.navigate(Screen.SignIn.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                            emailRepository.clearLocalData()
                                        }
                                    }
                                },
                                onCompose = {
                                    val isUnified = vm.currentTab.value == InboxTab.UNIFIED
                                    navController.navigate(Screen.Compose.createRoute(unified = isUnified)) { launchSingleTop = true }
                                },
                                onSettings = {
                                    navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                                },
                                onScheduledClick = {
                                    navController.navigate(Screen.Scheduled.route) { launchSingleTop = true }
                                },
                                onNavigateToImapSetup = { _, _ ->
                                    navController.navigate(Screen.ImapSetup.createRoute()) { launchSingleTop = true }
                                },
                            )
                        )
                    }
                }
            }
            composable(Screen.Settings.route) {
                dockController.settingsTitle = "Settings"
                dockController.onBackClick = { navController.popBackStack() }
                BackCardSurface(navController) {
                    val settingsViewModel: SettingsViewModel = hiltViewModel()
                    val ctx = LocalContext.current
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        authManager = authManager,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSection = { section ->
                            navController.navigate(Screen.SettingsSection.createRoute(section)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToLegal = { type -> openLegalUrl(ctx, type) },
                        onNavigateToPgpKeys = {
                            navController.navigate(Screen.PgpKeys.route) { launchSingleTop = true }
                        },
                    )
                }
            }

            composable(
                route = Screen.SettingsSection.route,
                arguments = listOf(navArgument("section") { type = NavType.StringType })
            ) { backStackEntry ->
                val sectionName = backStackEntry.arguments?.getString("section")
                val formattedTitle = sectionName?.replaceFirstChar { it.uppercase() } ?: "Settings"
                dockController.settingsTitle = formattedTitle
                dockController.onBackClick = { navController.popBackStack() }
                BackCardSurface(navController) {
                    val sectionName = backStackEntry.arguments?.getString("section")
                    val section = remember(sectionName) {
                        com.shrivatsav.monomail.feature.settings.SettingsSection.entries.find {
                            it.name.equals(sectionName, ignoreCase = true)
                        }
                    }
                    val parentEntry = remember(backStackEntry) {
                        runCatching { navController.getBackStackEntry(Screen.Settings.route) }.getOrNull()
                    }
                    val settingsViewModel: SettingsViewModel = if (parentEntry != null) {
                        hiltViewModel(parentEntry)
                    } else {
                        hiltViewModel()
                    }

                    if (section != null) {
                        com.shrivatsav.monomail.feature.settings.SettingsSectionScreen(
                            section = section,
                            viewModel = settingsViewModel,
                            authManager = authManager,
                            onNavigateToPgpKeys = {
                                navController.navigate(Screen.PgpKeys.route) { launchSingleTop = true }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }

            composable(Screen.PgpKeys.route) {
                BackCardSurface(navController) {
                    PgpKeyManagementScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
            composable(
                route = Screen.Legal.route,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                BackCardSurface(navController) {
                    val type = backStackEntry.arguments?.getString("type") ?: "privacy"
                    com.shrivatsav.monomail.feature.settings.LegalScreen(
                        type = type,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
            composable(
                route = Screen.ThreadDetail.route,
                arguments = listOf(
                    navArgument("accountId") { type = NavType.StringType },
                    navArgument("threadId") { type = NavType.StringType },
                    navArgument("focusedId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { _ ->
                BackCardSurface(navController) {
                    val vm: EmailDetailViewModel = hiltViewModel()
                    EmailDetailScreen(
                        viewModel = vm,
                        onBack    = { navController.popBackStack() },
                        onReply   = { to, subject, _, tid, messageId ->
                            navController.navigate(
                                Screen.Compose.createRoute(
                                    mode = ComposeMode.REPLY,
                                    to = to,
                                    subject = subject,
                                    threadId = tid,
                                    messageId = messageId,
                                    accountId = vm.accountId
                                )
                            )
                        },
                        onForward = { subject, _, tid, messageId ->
                            navController.navigate(
                                Screen.Compose.createRoute(
                                    mode = ComposeMode.FORWARD,
                                    to = "",
                                    subject = subject,
                                    threadId = tid,
                                    messageId = messageId,
                                    accountId = vm.accountId
                                )
                            )
                        },
                        onFetchAttachment = { messageId, attachmentId ->
                            vm.fetchAttachmentBytes(messageId, attachmentId)
                        },
                        onNavigateToAttachmentViewer = { messageId, attachmentId, mimeType, name ->
                            navController.navigate(
                                Screen.AttachmentViewer.createRoute(vm.accountId, messageId, attachmentId, mimeType, name)
                            )
                        }
                    )
                }
            }
            composable(
                route = Screen.Compose.route,
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType; defaultValue = "NEW" },
                    navArgument("to") { type = NavType.StringType; defaultValue = "" },
                    navArgument("subject") { type = NavType.StringType; defaultValue = "" },
                    navArgument("threadId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("messageId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("scheduledId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("unified") { type = NavType.BoolType; defaultValue = false },
                    navArgument("accountId") { type = NavType.StringType; defaultValue = "" }
                ),
                deepLinks = listOf(
                    androidx.navigation.navDeepLink { action = Intent.ACTION_SEND; mimeType = "*/*" },
                    androidx.navigation.navDeepLink { action = Intent.ACTION_SEND_MULTIPLE; mimeType = "*/*" },
                    androidx.navigation.navDeepLink { action = Intent.ACTION_SENDTO; uriPattern = "mailto:.*" },
                    androidx.navigation.navDeepLink { action = Intent.ACTION_VIEW; uriPattern = "mailto:.*" }
                )
            ) { backStackEntry ->
                val vm: ComposeViewModel = hiltViewModel()
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    val intent = backStackEntry.arguments?.getParcelable(androidx.navigation.NavController.KEY_DEEP_LINK_INTENT) as? Intent
                    if (intent != null) applyComposeIntent(intent, context, vm)
                }
                BackCardSurface(navController) {
                    ComposeScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                        onSent = { navController.popBackStack() }
                    )
                }
            }
            composable(Screen.Scheduled.route) {
                BackCardSurface(navController) {
                    val vm: ScheduledMessagesViewModel = hiltViewModel()

                    ScheduledMessagesScreen(
                        viewModel = vm,

                        onBack = { navController.popBackStack() },
                        onCompose = {
                            navController.navigate(Screen.Compose.createRoute())
                        },
                        onEdit = { scheduled ->
                            navController.navigate(
                                Screen.Compose.createRoute(
                                    to = scheduled.to,
                                    subject = scheduled.subject,
                                    scheduledId = scheduled.id,
                                    accountId = scheduled.accountId
                                )
                            )
                        }
                    )
                }
            }
            composable(
                route = Screen.AttachmentViewer.route,
                arguments = listOf(
                    navArgument("accountId") { type = NavType.StringType },
                    navArgument("messageId") { type = NavType.StringType },
                    navArgument("attachmentId") { type = NavType.StringType },
                    navArgument("mimeType") { type = NavType.StringType; defaultValue = "application/octet-stream" },
                    navArgument("name") { type = NavType.StringType; defaultValue = "attachment" }
                )
            ) {
                BackCardSurface(navController) {
                    AttachmentViewerScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        ExpressivePersistentDock(
            dockController = dockController,
            onComposeClick = {
                navController.navigate(Screen.Compose.createRoute()) { launchSingleTop = true }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 20.dp)
        )
    }
}
}

/**
 * Reads display name, size and mime type for a shared content URI.
 */
private fun readSharedAttachment(context: Context, uri: Uri): EmailAttachment {
    val resolver = context.contentResolver
    var name = "attachment"
    var size = 0L
    val mime = resolver.getType(uri) ?: "application/octet-stream"
    resolver.query(uri, null, null, null, null)?.use { c ->
        if (c.moveToFirst()) {
            val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
            if (nameIndex != -1) name = c.getString(nameIndex) ?: name
            if (sizeIndex != -1) size = c.getLong(sizeIndex)
        }
    }
    return EmailAttachment(uri, name, size, mime)
}

/**
 * Populates the compose screen from a share-sheet / mailto deep link intent.
 */
private fun applyComposeIntent(intent: Intent, context: Context, vm: ComposeViewModel) {
    val action = intent.action
    if (action == Intent.ACTION_SENDTO || action == Intent.ACTION_VIEW) {
        intent.data?.takeIf { it.scheme.equals("mailto", ignoreCase = true) }?.let { uri ->
            uri.schemeSpecificPart?.let { ssp ->
                val parts = ssp.split("?", limit = 2)
                vm.updateTo(parts[0])
                if (parts.size > 1) {
                    val query = parts[1]
                    val params = query.split("&").associate {
                        val kv = it.split("=", limit = 2)
                        kv[0] to kv.getOrElse(1) { "" }
                    }
                    params["subject"]?.let { vm.updateSubject(android.net.Uri.decode(it)) }
                    params["body"]?.let { vm.updateBody(android.net.Uri.decode(it)) }
                }
            }
        }
    }
    intent.getStringExtra(Intent.EXTRA_EMAIL)?.let { vm.updateTo(it) }
    intent.getStringArrayExtra(Intent.EXTRA_EMAIL)?.firstOrNull()?.let { vm.updateTo(it) }
    intent.getStringExtra(Intent.EXTRA_SUBJECT)?.let { vm.updateSubject(it) }
    intent.getStringExtra(Intent.EXTRA_TEXT)?.let { vm.updateBody(it) }

    val streamUris = if (action == Intent.ACTION_SEND_MULTIPLE) {
        intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM).orEmpty()
    } else {
        listOfNotNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
    }
    streamUris.forEach { vm.addAttachment(readSharedAttachment(context, it)) }
}
