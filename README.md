# Wallos Companion Android App & Deployment Guide

This repository contains the self-hosted deployment files for the **Wallos** server and the codebase for the native **Wallos Companion Android Application**.

Wallos is a powerful, self-hosted personal subscription and expense tracker. Since Wallos natively only provides a Progressive Web App (PWA), this project aims to deliver a high-performance, native Android experience utilizing the Wallos REST API.

---

## 📂 Project Structure

```text
WallosApp/
├── wallos-server/                     # Server deployment directory
│   └── docker-compose.yaml            # Docker compose configuration for Wallos
├── wallos-app/                        # Native Android codebase
│   ├── app/                           # Android application module
│   ├── gradle/                        # Gradle wrapper and version catalog
│   ├── build.gradle.kts               # Root build script
│   └── settings.gradle.kts            # Project settings
├── wallos_app_dev_plan.md             # Native Android developer roadmap
├── wallos_full_deployment_dev_plan.md # Comprehensive roadmap & app dev plan
└── README.md                          # This documentation file
```

---

## 📱 Part 1: Native Android Application (`wallos-app`)

The native Android application is built with Kotlin and Jetpack Compose.

### Theme & Styling
* **Primary Color**: White (`#FFFFFF`)
* **Accent Color**: Lime Green (`#84CC16`)

### Features Implemented
* **Server URL Setup**: Input and validate self-hosted instance URL, persisted via DataStore.
* **Side Drawer Menu**: Swipe from left navigation drawer containing Home, Subscriptions, Statistics, Profile, and Settings.
* **Theme Configuration**: Persistent app theme picker (Light, Dark, System) saved in DataStore.
* **Local Offline Storage**: Architecture prepared with Room / SQLite database dependencies.

### 💻 Code Highlights (`MainActivity.kt`)

Key layout structure and theme definitions are located in [MainActivity.kt](file:///C:/Users/Phoex/StudioProjects/WallosApp/wallos-app/app/src/main/java/com/wallosapp/android/MainActivity.kt).

#### Theme Definition (White & Lime Green)
```kotlin
@Composable
fun WallosTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val limeGreen = Color(0xFF84CC16)
    val white = Color(0xFFFFFFFF)
    val darkGrey = Color(0xFF1E1E1E)
    val black = Color(0xFF121212)
    val lightGrey = Color(0xFFF3F4F6)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = white,
            secondary = limeGreen,
            tertiary = limeGreen,
            background = black,
            surface = darkGrey,
            onPrimary = black,
            onSecondary = black,
            onBackground = white,
            onSurface = white
        )
    } else {
        lightColorScheme(
            primary = white,
            secondary = limeGreen,
            tertiary = limeGreen,
            background = lightGrey,
            surface = white,
            onPrimary = black,
            onSecondary = white,
            onBackground = black,
            onSurface = black
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
```

#### Side Navigation Drawer Layout
```kotlin
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
            ModalDrawerSheet {
                // Navigation items: Home, Subscriptions, Statistics, Profile, Settings
                DrawerItem(label = "Home", icon = Icons.Default.Home, selected = currentScreen == AppScreen.Dashboard) { ... }
                // ...
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.name) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            // Screen switching: DashboardScreen, SubscriptionsScreen, etc.
        }
    }
}
```

---

## 🛠️ Build Instructions (Gradle)

To build the Android application locally, navigate to the `wallos-app` directory and use the Gradle wrapper:

### Prerequisites
* Java JDK 21+ installed and `JAVA_HOME` environment variable configured.
* Android SDK command line tools or Android Studio installed.

### Build Commands
1. Navigate to the app directory:
   ```bash
   cd wallos-app
   ```
2. Build the debug APK:
   * **Windows (cmd/powershell)**:
     ```powershell
     .\gradlew assembleDebug
     ```
   * **Linux/macOS**:
     ```bash
     chmod +x gradlew
     ./gradlew assembleDebug
     ```
3. Run Unit Tests:
   * **Windows**:
     ```powershell
     .\gradlew testDebugUnitTest
     ```
   * **Linux/macOS**:
     ```bash
     ./gradlew testDebugUnitTest
     ```
4. Clean build outputs:
   * **Windows**:
     ```powershell
     .\gradlew clean
     ```
   * **Linux/macOS**:
     ```bash
     ./gradlew clean
     ```

The built APK will be located at:
`wallos-app/app/build/outputs/apk/debug/app-debug.apk`

---

## 🚀 Part 2: Wallos Server Deployment

The Wallos server runs in a Docker container using Docker Compose.

### Quick Start
1. Ensure you have [Docker Desktop](https://www.docker.com/products/docker-desktop/) or Docker Engine installed.
2. Open a terminal and navigate to the `wallos-server` directory:
   ```bash
   cd wallos-server
   ```
3. Start the container in detached mode:
   ```bash
   docker compose up -d
   ```
4. Access the web panel to create your admin account:
   * URL: [http://localhost:8282](http://localhost:8282)

### Persistent Volumes
Data is stored locally on the host machine to ensure persistence:
* `wallos-server/db/` - Contains the SQLite database (`wallos.db`)
* `wallos-server/logos/` - Stores uploaded subscription logo images