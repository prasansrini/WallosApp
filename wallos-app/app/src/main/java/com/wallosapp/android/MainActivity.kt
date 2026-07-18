package com.wallosapp.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// DataStore Extension
private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeConfig { LIGHT, DARK, SYSTEM }
enum class AppScreen { Dashboard, Subscriptions, Statistics, Profile, Settings }

class MainActivity : ComponentActivity() {
    private val serverUrlKey = stringPreferencesKey("server_url")
    private val themeKey = stringPreferencesKey("theme_config")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            // Observe states from DataStore
            val serverUrlState = context.dataStore.data.map { it[serverUrlKey] ?: "" }.collectAsState(initial = "")
            val themeState = context.dataStore.data.map { it[themeKey] ?: ThemeConfig.SYSTEM.name }.collectAsState(initial = ThemeConfig.SYSTEM.name)

            // Local navigation state
            var currentScreen by remember { mutableStateOf(AppScreen.Dashboard) }

            // Theme computation
            val darkTheme = when (themeState.value) {
                ThemeConfig.LIGHT.name -> false
                ThemeConfig.DARK.name -> true
                else -> isSystemInDarkTheme()
            }

            WallosTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (serverUrlState.value.isEmpty()) {
                        ServerConfigScreen(
                            onSaveUrl = { url ->
                                scope.launch {
                                    context.dataStore.edit { it[serverUrlKey] = url }
                                }
                            }
                        )
                    } else {
                        MainShell(
                            serverUrl = serverUrlState.value,
                            currentScreen = currentScreen,
                            themeConfig = ThemeConfig.valueOf(themeState.value),
                            onScreenChange = { currentScreen = it },
                            onSaveTheme = { theme ->
                                scope.launch {
                                    context.dataStore.edit { it[themeKey] = theme.name }
                                }
                            },
                            onResetServer = {
                                scope.launch {
                                    context.dataStore.edit { it[serverUrlKey] = "" }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WallosTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        MinimalistDarkColorScheme
    } else {
        MinimalistLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

// Dynamic Server URL Entry Screen
@Composable
fun ServerConfigScreen(onSaveUrl: (String) -> Unit) {
    var urlText by remember { mutableStateOf(TextFieldValue("http://")) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Connection Info",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Connect to Wallos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter your self-hosted Wallos server instance URL",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = urlText,
            onValueChange = { urlText = it },
            label = { Text("Server URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
        )

        errorMsg?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val url = urlText.text.trim()
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    onSaveUrl(url)
                } else {
                    errorMsg = "URL must start with http:// or https://"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

// Side Drawer Navigation Shell
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    serverUrl: String,
    currentScreen: AppScreen,
    themeConfig: ThemeConfig,
    onScreenChange: (AppScreen) -> Unit,
    onSaveTheme: (ThemeConfig) -> Unit,
    onResetServer: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "WALLOS",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))

                DrawerItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    selected = currentScreen == AppScreen.Dashboard,
                    onClick = {
                        onScreenChange(AppScreen.Dashboard)
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    label = "Subscriptions",
                    icon = Icons.Default.List,
                    selected = currentScreen == AppScreen.Subscriptions,
                    onClick = {
                        onScreenChange(AppScreen.Subscriptions)
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    label = "Statistics",
                    icon = Icons.Default.Info,
                    selected = currentScreen == AppScreen.Statistics,
                    onClick = {
                        onScreenChange(AppScreen.Statistics)
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    label = "Profile",
                    icon = Icons.Default.Person,
                    selected = currentScreen == AppScreen.Profile,
                    onClick = {
                        onScreenChange(AppScreen.Profile)
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    label = "Settings",
                    icon = Icons.Default.Settings,
                    selected = currentScreen == AppScreen.Settings,
                    onClick = {
                        onScreenChange(AppScreen.Settings)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.name, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    AppScreen.Dashboard -> DashboardScreen()
                    AppScreen.Subscriptions -> SubscriptionsScreen()
                    AppScreen.Statistics -> StatisticsScreen()
                    AppScreen.Profile -> ProfileScreen(serverUrl)
                    AppScreen.Settings -> SettingsScreen(themeConfig, onSaveTheme, onResetServer)
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        icon = { Icon(icon, contentDescription = label) },
        selected = selected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            selectedIconColor = MaterialTheme.colorScheme.secondary,
            selectedTextColor = MaterialTheme.colorScheme.secondary,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    )
}

// 1. Dashboard Screen
@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DashboardCard(title = "Monthly Spent", value = "$124.50", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            DashboardCard(title = "Active Subs", value = "8", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Upcoming Bills", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))
        UpcomingBillItem("Netflix", "$15.49", "In 2 days")
        UpcomingBillItem("Spotify", "$10.99", "In 5 days")
        UpcomingBillItem("Google One", "$1.99", "In 12 days")
    }
}

@Composable
fun DashboardCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun UpcomingBillItem(name: String, cost: String, days: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(days, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Text(cost, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
    }
}

// 2. Subscriptions Screen with SQLite Caching UI
@Composable
fun SubscriptionsScreen() {
    var subsList by remember { mutableStateOf(listOf("Netflix", "Spotify", "Amazon Prime", "Github Copilot", "Internet")) }
    var newSubName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newSubName,
                onValueChange = { newSubName = it },
                label = { Text("New Subscription") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    if (newSubName.isNotBlank()) {
                        subsList = subsList + newSubName.trim()
                        newSubName = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("+", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(subsList) { sub ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(sub, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = { subsList = subsList.filter { it != sub } }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

// 3. Statistics Screen (Placeholder Chart Layout)
@Composable
fun StatisticsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Share, contentDescription = "Stats Chart", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Statistics & Trends", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Charts of monthly expenditures will render here.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
    }
}

// 4. Profile Screen
@Composable
fun ProfileScreen(serverUrl: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountBox,
            contentDescription = "Profile",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Local Admin Account", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Server: $serverUrl", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
    }
}

// 5. Settings Screen with Theme Config
@Composable
fun SettingsScreen(
    themeConfig: ThemeConfig,
    onSaveTheme: (ThemeConfig) -> Unit,
    onResetServer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("App Theme", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ThemeButton("Light", selected = themeConfig == ThemeConfig.LIGHT) { onSaveTheme(ThemeConfig.LIGHT) }
            ThemeButton("Dark", selected = themeConfig == ThemeConfig.DARK) { onSaveTheme(ThemeConfig.DARK) }
            ThemeButton("System", selected = themeConfig == ThemeConfig.SYSTEM) { onSaveTheme(ThemeConfig.SYSTEM) }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Server URL Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onResetServer,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Disconnect Server", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ThemeButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
