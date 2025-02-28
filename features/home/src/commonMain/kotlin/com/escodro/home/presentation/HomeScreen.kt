package com.escodro.home.presentation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.escodro.appstate.AppState
import com.escodro.category.presentation.list.CategoryListSection
import com.escodro.preference.presentation.PreferenceSection
import com.escodro.search.presentation.SearchSection
import com.escodro.task.presentation.list.TaskListSection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import java.util.Timer
import java.util.TimerTask
/**
 * Alkaa Home screen with enhanced features.
 */
@Composable
fun Home(
    appState: AppState,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
) {
    val (currentSection, setCurrentSection) = rememberSaveable { mutableStateOf(HomeSection.Tasks) }
    val navItems = remember { HomeSection.entries.toImmutableList() }
    
    // Authentication state
    var isAuthenticated by rememberSaveable { mutableStateOf(true) } // Default: user is logged in
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var userRole by rememberSaveable { mutableStateOf(UserRole.STANDARD) }
    var notificationCount by rememberSaveable { mutableIntStateOf(3) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Session timer simulation
    LaunchedEffect(key1 = true) {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Your session will expire in 5 minutes",
                        duration = SnackbarDuration.Long,
                        actionLabel = "Extend"
                    )
                }
            }
        }, 60000) // Show warning after 1 minute
    }
    
    // Check permissions for restricted sections
    LaunchedEffect(key1 = currentSection) {
        if (currentSection == HomeSection.Categories && userRole == UserRole.LIMITED) {
            snackbarHostState.showSnackbar("Categories require enhanced permissions")
            setCurrentSection(HomeSection.Tasks)
        }
    }
    if (!isAuthenticated) {
        AuthPrompt(
            onLoginClick = {
                isLoading = true
                // Simulate login process
                scope.launch {
                    delay(1500)
                    isAuthenticated = true
                    isLoading = false
                }
            },
            onRegisterClick = { onNavigateToLogin() },
            isLoading = isLoading
        )
    } else {
        AlkaaHomeScaffold(
            appState = appState,
            homeSection = currentSection,
            navItems = navItems,
            setCurrentSection = setCurrentSection,
            snackbarHostState = snackbarHostState,
            notificationCount = notificationCount,
            onProfileClick = { onNavigateToProfile() },
            onNotificationClick = { 
                onNavigateToNotifications()
                notificationCount = 0
            },
            onLogoutClick = {
                scope.launch {
                    isLoading = true
                    delay(500)
                    isAuthenticated = false
                    isLoading = false
                }
            },
            userRole = userRole
        )
    }
}
@Composable
private fun AuthPrompt(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Authentication Required",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please log in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onLoginClick,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Log In")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRegisterClick,
                enabled = !isLoading
            ) {
                Text("Register")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AlkaaHomeScaffold(
    appState: AppState,
    homeSection: HomeSection,
    navItems: ImmutableList<HomeSection>,
    setCurrentSection: (HomeSection) -> Unit,
    snackbarHostState: SnackbarHostState,
    notificationCount: Int,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onLogoutClick: () -> Unit,
    userRole: UserRole
) {
    var showProfileMenu by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { 
            AlkaaTopBar(
                currentSection = homeSection,
                notificationCount = notificationCount,
                onNotificationClick = onNotificationClick,
                onProfileClick = { showProfileMenu = true }
            ) 
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (homeSection == HomeSection.Tasks) {
                FloatingActionButton(
                    onClick = { /* Add new task */ },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text("+")
                }
            }
        },
        content = { paddingValues ->
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                    ),
            ) {
                if (appState.shouldShowNavRail) {
                    AlkaaNavRail(
                        currentSection = homeSection,
                        onSectionSelect = setCurrentSection,
                        items = navItems.filter { section ->
                            hasPermissionForSection(section, userRole)
                        }.toImmutableList(),
                        modifier = Modifier.consumeWindowInsets(paddingValues),
                    )
                }
                Column(Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AlkaaContent(
                            homeSection = homeSection,
                            modifier = Modifier
                        )
                        
                        DropdownMenu(
                            expanded = showProfileMenu,
                            onDismissRequest = { showProfileMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = {
                                    onProfileClick()
                                    showProfileMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Change Role") },
                                onClick = {
                                    // Toggle user role for demo purposes
                                    userRole = if (userRole == UserRole.STANDARD) UserRole.LIMITED else UserRole.STANDARD
                                    showProfileMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    onLogoutClick()
                                    showProfileMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (appState.shouldShowBottomBar) {
                AlkaaBottomNav(
                    currentSection = homeSection,
                    onSectionSelect = setCurrentSection,
                    items = navItems.filter { section ->
                        hasPermissionForSection(section, userRole)
                    }.toImmutableList(),
                )
            }
        },
    )
}
private fun hasPermissionForSection(section: HomeSection, userRole: UserRole): Boolean {
    return when (section) {
        HomeSection.Categories -> userRole == UserRole.STANDARD
        HomeSection.Settings -> userRole == UserRole.STANDARD
        else -> true // Tasks and Search accessible to all roles
    }
}
@Composable
private fun AlkaaNavRail(
    currentSection: HomeSection,
    onSectionSelect: (HomeSection) -> Unit,
    items: ImmutableList<HomeSection>,
    modifier: Modifier = Modifier,
) {
    NavigationRail(modifier = modifier) {
        items.forEach { section ->
            val selected = section == currentSection
            NavigationRailItem(
                selected = selected,
                onClick = { onSectionSelect(section) },
                alwaysShowLabel = true,
                icon = { Icon(imageVector = section.icon, contentDescription = null) },
                label = { Text(stringResource(section.title)) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}
@Composable
private fun AlkaaContent(
    homeSection: HomeSection,
    modifier: Modifier = Modifier,
) {
    when (homeSection) {
        HomeSection.Tasks -> TaskListSection(modifier = modifier)
        HomeSection.Search -> SearchSection(modifier = modifier)
        HomeSection.Categories -> CategoryListSection(modifier = modifier)
        HomeSection.Settings -> PreferenceSection(modifier = modifier)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlkaaTopBar(
    currentSection: HomeSection,
    notificationCount: Int = 0,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
                text = stringResource(currentSection.title),
                color = MaterialTheme.colorScheme.tertiary,
            )
        },
        actions = {
            // Notifications icon with badge
            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge { Text(notificationCount.toString()) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }
            
            // Profile icon
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile"
                )
            }
        }
    )
}
@Composable
private fun AlkaaBottomNav(
    currentSection: HomeSection,
    onSectionSelect: (HomeSection) -> Unit,
    items: ImmutableList<HomeSection>,
) {
    BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
        items.forEach { section ->
            val selected = section == currentSection
            val title = section.title
            NavigationBarItem(
                selected = selected,
                onClick = { onSectionSelect(section) },
                icon = {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = stringResource(title),
                    )
                },
                label = { Text(stringResource(title)) },
            )
        }
        
        // Logout option in bottom bar
        NavigationBarItem(
            selected = false,
            onClick = { /* Handled in dropdown menu */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout"
                )
            },
            label = { Text("Logout") }
        )
    }
}
/**
 * User roles for permission management
 */
enum class UserRole {
    STANDARD, // Full access
    LIMITED   // Limited access
}
