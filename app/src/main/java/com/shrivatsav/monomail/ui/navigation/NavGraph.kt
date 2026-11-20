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
