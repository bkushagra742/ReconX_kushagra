package com.kushagra.reconx.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kushagra.reconx.ReconXApplication
import com.kushagra.reconx.models.QueryEngine
import com.kushagra.reconx.ui.about.AboutAppScreen
import com.kushagra.reconx.ui.about.AboutDeveloperScreen
import com.kushagra.reconx.ui.dashboard.DashboardScreen
import com.kushagra.reconx.ui.login.LoginScreen
import com.kushagra.reconx.ui.notes.NoteEditorScreen
import com.kushagra.reconx.ui.notes.NotesScreen
import com.kushagra.reconx.ui.projects.ProjectDetailScreen
import com.kushagra.reconx.ui.projects.ProjectsScreen
import com.kushagra.reconx.ui.reports.ReportsScreen
import com.kushagra.reconx.ui.settings.SettingsScreen
import com.kushagra.reconx.ui.tools.ChecklistsScreen
import com.kushagra.reconx.ui.tools.CveLookupScreen
import com.kushagra.reconx.ui.tools.DomainIntelScreen
import com.kushagra.reconx.ui.tools.DorkBuilderScreen
import com.kushagra.reconx.ui.tools.EncodingToolsScreen
import com.kushagra.reconx.ui.tools.HashToolsScreen
import com.kushagra.reconx.ui.tools.IpIntelScreen
import com.kushagra.reconx.ui.tools.PasswordToolsScreen
import com.kushagra.reconx.ui.tools.RegexLabScreen
import com.kushagra.reconx.ui.tools.SearchHistoryScreen
import com.kushagra.reconx.ui.tools.ToolsHubScreen
import com.kushagra.reconx.ui.tools.WebsiteSecurityScreen
import com.kushagra.reconx.viewmodel.CveLookupViewModel
import com.kushagra.reconx.viewmodel.DashboardViewModel
import com.kushagra.reconx.viewmodel.DomainIntelViewModel
import com.kushagra.reconx.viewmodel.DorkBuilderViewModel
import com.kushagra.reconx.viewmodel.EncodingToolsViewModel
import com.kushagra.reconx.viewmodel.HashToolsViewModel
import com.kushagra.reconx.viewmodel.IpIntelViewModel
import com.kushagra.reconx.viewmodel.LoginViewModel
import com.kushagra.reconx.viewmodel.NotesViewModel
import com.kushagra.reconx.viewmodel.PasswordToolsViewModel
import com.kushagra.reconx.viewmodel.ProjectDetailViewModel
import com.kushagra.reconx.viewmodel.ProjectsViewModel
import com.kushagra.reconx.viewmodel.RegexLabViewModel
import com.kushagra.reconx.viewmodel.ReportsViewModel
import com.kushagra.reconx.viewmodel.SettingsViewModel
import com.kushagra.reconx.viewmodel.ViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel

private val BOTTOM_NAV_ROUTES = setOf(
    Destinations.DASHBOARD, Destinations.TOOLS, Destinations.PROJECTS,
    Destinations.NOTES, Destinations.SETTINGS,
)

private data class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(Destinations.DASHBOARD, "Home", Icons.Default.Home),
    BottomNavItem(Destinations.TOOLS, "Tools", Icons.Default.Build),
    BottomNavItem(Destinations.PROJECTS, "Projects", Icons.Default.Folder),
    BottomNavItem(Destinations.NOTES, "Notes", Icons.Default.Description),
    BottomNavItem(Destinations.SETTINGS, "Settings", Icons.Default.Settings),
)

/**
 * AppNavHost.kt
 * ================
 * The single navigation graph for the whole app. Wraps every top-level
 * destination in a Scaffold with a persistent bottom navigation bar
 * (Home / Tools / Projects / Notes / Settings, matching the reference
 * design), while sub-screens get a simple back-button top bar instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(app: ReconXApplication) {
    val navController = rememberNavController()
    val factory = ViewModelFactory(app)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in BOTTOM_NAV_ROUTES && currentRoute != null
    val showBackBar = currentRoute != null && currentRoute != Destinations.LOGIN && !showBottomBar

    Scaffold(
        topBar = {
            if (showBackBar) {
                TopAppBar(
                    title = { Text(routeTitle(currentRoute)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BOTTOM_NAV_ITEMS.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.LOGIN,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destinations.LOGIN) {
                val vm: LoginViewModel = viewModel(factory = factory)
                LoginScreen(vm) {
                    navController.navigate(Destinations.DASHBOARD) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                }
            }

            composable(Destinations.DASHBOARD) {
                val vm: DashboardViewModel = viewModel(factory = factory)
                DashboardScreen(
                    vm,
                    onOpenProject = { navController.navigate(Destinations.projectDetail(it)) },
                    onOpenTools = { navController.navigate(Destinations.TOOLS) },
                )
            }

            composable(Destinations.TOOLS) {
                ToolsHubScreen(onToolClick = { route -> navController.navigate(route) })
            }

            composable(Destinations.PROJECTS) {
                val vm: ProjectsViewModel = viewModel(factory = factory)
                ProjectsScreen(vm, onOpenProject = { navController.navigate(Destinations.projectDetail(it)) })
            }

            composable(Destinations.NOTES) {
                val vm: NotesViewModel = viewModel(factory = factory)
                NotesScreen(
                    vm,
                    onOpenNote = { navController.navigate(Destinations.noteEditor(noteId = it)) },
                    onNewNote = { navController.navigate(Destinations.noteEditor()) },
                )
            }

            composable(Destinations.SETTINGS) {
                val vm: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(
                    vm,
                    onOpenAboutApp = { navController.navigate(Destinations.ABOUT_APP) },
                    onOpenAboutDeveloper = { navController.navigate(Destinations.ABOUT_DEVELOPER) },
                )
            }

            composable(
                Destinations.PROJECT_DETAIL,
                arguments = listOf(navArgument("projectId") { type = androidx.navigation.NavType.LongType }),
            ) { backStack ->
                val projectId = backStack.arguments?.getLong("projectId") ?: -1L
                val vm: ProjectDetailViewModel = viewModel(factory = factory)
                ProjectDetailScreen(projectId, vm)
            }

            composable(
                Destinations.NOTE_EDITOR,
                arguments = listOf(
                    navArgument("noteId") { type = androidx.navigation.NavType.LongType; defaultValue = -1L },
                    navArgument("projectId") { type = androidx.navigation.NavType.LongType; defaultValue = -1L },
                ),
            ) { backStack ->
                val vm: NotesViewModel = viewModel(factory = factory)
                val notes by vm.notes.collectAsState()
                val noteId = backStack.arguments?.getLong("noteId") ?: -1L
                val projectIdArg = backStack.arguments?.getLong("projectId") ?: -1L
                val existingNote = notes.find { it.id == noteId }
                NoteEditorScreen(
                    vm,
                    existingNote = existingNote,
                    projectId = if (projectIdArg == -1L) null else projectIdArg,
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(Destinations.REPORTS) {
                val vm: ReportsViewModel = viewModel(factory = factory)
                ReportsScreen(vm)
            }

            composable(Destinations.ABOUT_APP) { AboutAppScreen() }
            composable(Destinations.ABOUT_DEVELOPER) { AboutDeveloperScreen() }

            composable(
                Destinations.DORK_BUILDER,
                arguments = listOf(navArgument("engine") { type = androidx.navigation.NavType.StringType }),
            ) { backStack ->
                val engineName = backStack.arguments?.getString("engine") ?: "GOOGLE"
                val vm: DorkBuilderViewModel = viewModel(factory = factory)
                DorkBuilderScreen(QueryEngine.valueOf(engineName), vm)
            }

            composable(Destinations.DOMAIN_INTEL) {
                val vm: DomainIntelViewModel = viewModel(factory = factory)
                DomainIntelScreen(vm)
            }
            composable(Destinations.WEBSITE_SECURITY) {
                val vm: WebsiteSecurityViewModel = viewModel(factory = factory)
                WebsiteSecurityScreen(vm)
            }
            composable(Destinations.IP_INTEL) {
                val vm: IpIntelViewModel = viewModel(factory = factory)
                IpIntelScreen(vm)
            }
            composable(Destinations.HASH_TOOLS) {
                val vm: HashToolsViewModel = viewModel(factory = factory)
                HashToolsScreen(vm)
            }
            composable(Destinations.PASSWORD_TOOLS) {
                val vm: PasswordToolsViewModel = viewModel(factory = factory)
                PasswordToolsScreen(vm)
            }
            composable(Destinations.ENCODING_TOOLS) {
                val vm: EncodingToolsViewModel = viewModel(factory = factory)
                EncodingToolsScreen(vm)
            }
            composable(Destinations.REGEX_LAB) {
                val vm: RegexLabViewModel = viewModel(factory = factory)
                RegexLabScreen(vm)
            }
            composable(Destinations.CVE_LOOKUP) {
                val vm: CveLookupViewModel = viewModel(factory = factory)
                CveLookupScreen(vm)
            }
            composable(Destinations.CHECKLISTS) { ChecklistsScreen() }
            composable(Destinations.SEARCH_HISTORY) { SearchHistoryScreen(app.activityRepository) }
            composable(Destinations.GLOBAL_SEARCH) {
                val vm: com.kushagra.reconx.viewmodel.GlobalSearchViewModel = viewModel(factory = factory)
                com.kushagra.reconx.ui.search.GlobalSearchScreen(vm)
            }
        }
    }
}

private fun routeTitle(route: String?): String = when {
    route == null -> ""
    route.startsWith("project_detail") -> "Project"
    route.startsWith("note_editor") -> "Note"
    route == Destinations.REPORTS -> "Reports"
    route == Destinations.ABOUT_APP -> "About Application"
    route == Destinations.ABOUT_DEVELOPER -> "About Developer"
    route.startsWith("dork_builder") -> "Dork Builder"
    route == Destinations.DOMAIN_INTEL -> "Domain Intelligence"
    route == Destinations.WEBSITE_SECURITY -> "Website Security"
    route == Destinations.IP_INTEL -> "IP Intelligence"
    route == Destinations.HASH_TOOLS -> "Hash Tools"
    route == Destinations.PASSWORD_TOOLS -> "Password Tools"
    route == Destinations.ENCODING_TOOLS -> "Encoding Tools"
    route == Destinations.REGEX_LAB -> "Regex Lab"
    route == Destinations.CVE_LOOKUP -> "Offline CVE Lookup"
    route == Destinations.CHECKLISTS -> "Checklists"
    route == Destinations.SEARCH_HISTORY -> "Search History"
    route == Destinations.GLOBAL_SEARCH -> "Global Search"
    else -> "Kushagra ReconX"
}
