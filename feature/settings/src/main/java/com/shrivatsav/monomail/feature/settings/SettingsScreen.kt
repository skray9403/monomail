package com.shrivatsav.monomail.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import com.shrivatsav.monomail.ui.theme.cornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.background
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.shrivatsav.monomail.feature.settings.BuildConfig

enum class SettingsSection(val icon: ImageVector, val title: String, val subtitle: String) {
    ACCOUNTS(Icons.Rounded.ManageAccounts, "Accounts", "Manage accounts, providers, connection status"),
    APPEARANCE(Icons.Rounded.Palette, "Appearance", "Theme and list presentation"),
    INBOX(Icons.Rounded.Inbox, "Inbox", "Threading, grouping, list density, swipe actions"),
    READING(Icons.Rounded.MenuBook, "Reading", "Inline attachments, markdown renderer"),
    PRIVACY(Icons.Rounded.Shield, "Privacy & Security", "PGP encryption keys, remote images"),
    COMPOSE(Icons.AutoMirrored.Rounded.Send, "Compose", "Reply defaults, confirm send, undo send"),
    NAVIGATION(Icons.Rounded.Explore, "Navigation", "Dock tabs and mail folders"),
    NOTIFICATIONS(Icons.Rounded.Notifications, "Notifications", "Alerts & sync"),
    DEVELOPER(Icons.Rounded.DeveloperMode, "Developer", "Developer mode & debug tools"),
    SUPPORT(Icons.Rounded.Favorite, "Support", "Donate & community")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    authManager: com.shrivatsav.monomail.core.data.auth.AuthManager,
    onNavigateBack: () -> Unit,
    onNavigateToLegal: (String) -> Unit,
    onNavigateToPgpKeys: () -> Unit = {},
) {
    var currentSection by remember { mutableStateOf<SettingsSection?>(null) }
    BackHandler(currentSection != null) { currentSection = null }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val motionScheme = MaterialTheme.motionScheme

    if (isTablet) {
        androidx.compose.foundation.layout.Row(
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
        ) {
            androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.weight(0.4f)) {
                SettingsHubScreen(
                    viewModel = viewModel,
                    onSectionClick = { currentSection = it },
                    onNavigateBack = onNavigateBack,
                    onNavigateToLegal = onNavigateToLegal,
                )
            }
            androidx.compose.material3.VerticalDivider(
                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                thickness = 1.dp
            )
            androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.weight(0.6f)) {
                AnimatedContent(
                    targetState = currentSection,
                    transitionSpec = {
                        (
                            fadeIn(animationSpec = motionScheme.fastEffectsSpec()) +
                                scaleIn(
                                    initialScale = 0.98f,
                                    animationSpec = motionScheme.fastSpatialSpec(),
                                )
                        ).togetherWith(
                            fadeOut(animationSpec = motionScheme.fastEffectsSpec())
                        )
                    },
                    label = "settingsTabletDetail"
                ) { section ->
                    if (section == null) {
                        androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Rounded.Settings,
                                    contentDescription = null,
                                    modifier = androidx.compose.ui.Modifier.size(64.dp),
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.size(16.dp))
                                androidx.compose.material3.Text(
                                    text = "Select a setting to view",
                                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    } else {
                        SettingsSectionScreen(section, viewModel, authManager, onNavigateToPgpKeys) { currentSection = null }
                    }
                }
            }
        }
    } else {
        AnimatedContent(
            targetState = currentSection,
            transitionSpec = {
                val isDrill = targetState != null
                if (isDrill) {
                    (
                        slideInHorizontally(
                            initialOffsetX = { width -> width },
                            animationSpec = motionScheme.fastSpatialSpec(),
                        ) + fadeIn(animationSpec = motionScheme.fastEffectsSpec())
                    ).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { width -> -width / 4 },
                            animationSpec = motionScheme.fastSpatialSpec(),
                        ) + fadeOut(animationSpec = motionScheme.fastEffectsSpec())
                    )
                } else {
                    (
                        slideInHorizontally(
                            initialOffsetX = { width -> -width / 4 },
                            animationSpec = motionScheme.fastSpatialSpec(),
                        ) + fadeIn(animationSpec = motionScheme.fastEffectsSpec())
                    ).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { width -> width },
                            animationSpec = motionScheme.fastSpatialSpec(),
                        ) + fadeOut(animationSpec = motionScheme.fastEffectsSpec())
                    )
                }
            },
            label = "settingsNav"
        ) { section ->
            if (section == null) {
                SettingsHubScreen(
                    viewModel = viewModel,
                    onSectionClick = { currentSection = it },
                    onNavigateBack = onNavigateBack,
                    onNavigateToLegal = onNavigateToLegal,
                )
            } else {
                SettingsSectionScreen(section, viewModel, authManager, onNavigateToPgpKeys) { currentSection = null }
            }
        }
    }
}

@Composable
private fun SettingsSectionScreen(
    section: SettingsSection,
    viewModel: SettingsViewModel,
    authManager: com.shrivatsav.monomail.core.data.auth.AuthManager,
    onNavigateToPgpKeys: () -> Unit,
    onBack: () -> Unit,
) {
    when (section) {
        SettingsSection.ACCOUNTS -> AccountsSettingsScreen(
            authManager = authManager,
            onBack = onBack,
        )
        SettingsSection.APPEARANCE -> AppearanceSettingsScreen(
            viewModel = viewModel,
            onBack = onBack,
        )
        SettingsSection.INBOX -> InboxSettingsScreen(
            viewModel = viewModel,
            onBack = onBack,
        )
        SettingsSection.READING -> ReadingSettingsScreen(
            viewModel = viewModel,
            onBack = onBack,
        )
        SettingsSection.PRIVACY -> PrivacySettingsScreen(
            viewModel = viewModel,
            onNavigateToPgpKeys = onNavigateToPgpKeys,
            onBack = onBack,
        )
        SettingsSection.COMPOSE -> ComposeSettingsScreen(
            viewModel = viewModel,
            onBack = onBack,
        )
        SettingsSection.NAVIGATION -> NavigationSettingsScreen(
            viewModel = viewModel,
            onBack = onBack,
        )
        SettingsSection.DEVELOPER -> DeveloperSettingsScreen(
            viewModel = viewModel,
            onBack = onBack,
        )
        SettingsSection.NOTIFICATIONS -> NotificationSettingsScreen(
            authManager = authManager,
            viewModel = viewModel,
            onBack = onBack,
        )
        SettingsSection.SUPPORT -> SupportSettingsScreen(
            onBack = onBack,
        )
    }
}

// ── HUB SCREEN ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsHubScreen(
    viewModel: SettingsViewModel,
    onSectionClick: (SettingsSection) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLegal: (String) -> Unit,
    onNavigateToLicensing: () -> Unit = {},
) {
    val context = LocalContext.current
    val buildFlavorName = if (BuildConfig.IS_GITHUB_BUILD) "GitHub" else "Play Store"
    val buildTypeName = if (BuildConfig.DEBUG) "Debug" else "Release"
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val settings by viewModel.settings.collectAsState()
    var devTapCount by remember { mutableIntStateOf(0) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            bottom = 0.dp,
        ),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsGroupLabel("Accounts & Cloud")
            SettingsGroupSurface {
                SettingsHubItem(SettingsSection.ACCOUNTS) { onSectionClick(it) }
            }

            SettingsGroupLabel("Mail & Display")
            SettingsGroupSurface {
                SettingsHubItem(SettingsSection.APPEARANCE) { onSectionClick(it) }
                SettingsGroupDivider()
                SettingsHubItem(SettingsSection.INBOX) { onSectionClick(it) }
                SettingsGroupDivider()
                SettingsHubItem(SettingsSection.READING) { onSectionClick(it) }
            }

            SettingsGroupLabel("Interactions & Compose")
            SettingsGroupSurface {
                SettingsHubItem(SettingsSection.NAVIGATION) { onSectionClick(it) }
                SettingsGroupDivider()
                SettingsHubItem(SettingsSection.COMPOSE) { onSectionClick(it) }
            }

            SettingsGroupLabel("Security & Alerts")
            SettingsGroupSurface {
                SettingsHubItem(SettingsSection.PRIVACY) { onSectionClick(it) }
                SettingsGroupDivider()
                SettingsHubItem(SettingsSection.NOTIFICATIONS) { onSectionClick(it) }
            }

            SettingsGroupLabel("App & System")
            SettingsGroupSurface {
                SettingsHubItem(SettingsSection.SUPPORT) { onSectionClick(it) }
                if (settings.isDeveloperMode) {
                    SettingsGroupDivider()
                    SettingsHubItem(SettingsSection.DEVELOPER) { onSectionClick(it) }
                }
                SettingsGroupDivider()
                InfoRow(
                    icon = Icons.Rounded.Info,
                    title = "Version",
                    value = "$versionName ($buildFlavorName $buildTypeName)",
                    onClick = {
                        if (!settings.isDeveloperMode) {
                            devTapCount++
                            if (devTapCount >= 3) {
                                viewModel.setDeveloperMode(true)
                                devTapCount = 0
                                android.widget.Toast.makeText(
                                    context,
                                    "Developer Mode unlocked!",
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    },
                )
                SettingsGroupDivider()
                InfoRow(
                    icon = Icons.Rounded.Language,
                    title = "Website",
                    value = "",
                    onClick = { uriHandler.openUri("https://monomail.millosaurs.me") },
                )
                SettingsGroupDivider()
                InfoRow(
                    icon = Icons.Rounded.PrivacyTip,
                    title = "Privacy Policy",
                    value = "",
                    onClick = { onNavigateToLegal("privacy") },
                )
                SettingsGroupDivider()
                InfoRow(
                    icon = Icons.Rounded.Gavel,
                    title = "Terms of Service",
                    value = "",
                    onClick = { onNavigateToLegal("tos") },
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Made with ❤️",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun SettingsGroupLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun SettingsGroupSurface(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        content = { Column(content = content) },
    )
}

@Composable
private fun SettingsGroupDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
    )
}

@Composable
private fun SettingsHubItem(
    section: SettingsSection,
    onClick: (SettingsSection) -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = androidx.compose.ui.semantics.Role.Button) { onClick(section) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
        },
        headlineContent = {
            Text(section.title, style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            Text(
                section.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeveloperSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    ScrollableSettingsScaffold(
        title = "Developer",
        onBack = onBack
    ) {
        SettingsCard {
            SettingsToggleRow(
                icon = Icons.Rounded.BugReport,
                title = "Developer Mode",
                subtitle = "Enable share options for raw HTML/MD/plain text email body",
                checked = settings.isDeveloperMode,
                onCheckedChange = { viewModel.setDeveloperMode(it) }
            )
        }
        Spacer(Modifier.height(8.dp))
        if (BuildConfig.DEBUG) {
            SettingsCard {
                // Preview Welcome button (dev only)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            viewModel.resetWelcomePrompt()
                            android.widget.Toast.makeText(
                                context,
                                "Welcome prompt will show on next app launch",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        })
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Preview,
                        contentDescription = "Preview Welcome",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Preview Welcome",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Reset welcome prompt and show modal on next app launch",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                CardDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            throw RuntimeException("Intentional crash for testing purposes")
                        })
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "Test Crash",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Test App Crash",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Intentionally crash the app to verify crash handler",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

