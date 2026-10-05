# HmyhAndroidStarter

A single-module Android starter in Kotlin. It uses Jetpack Compose for UI, Room for the local database, Dagger Hilt for dependency injection, Retrofit for network calls, and MVVM with clean architecture.

The sample feature shows a list of posts. Room is the source of truth: the screen always reads from the database, and a network refresh only updates what the database holds. If the network is unavailable and the database is empty, the app loads sample posts bundled in `assets/posts.json`.

- Application id: `com.hmyh.androidstarter`
- Min SDK 26, compile and target SDK 37, Java/Kotlin target 17
- API: `https://jsonplaceholder.typicode.com/posts`

## Tech stack

| Area | Library |
| --- | --- |
| Build | Android Gradle Plugin 9.4.1 (built-in Kotlin), Gradle 9.6.1, version catalog |
| Language | Kotlin 2.4.20, coroutines, Flow |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3 |
| DI | Dagger Hilt 2.60.1, `hilt-lifecycle-viewmodel-compose` 1.4.0 |
| Database | Room 2.8.5 with the Room Gradle plugin |
| Network | Retrofit 3.0.0, OkHttp 4.12.0, kotlinx.serialization 1.11.0 |
| Code generation | KSP 2.3.11 (for Room and Hilt) |
| Tests | JUnit 4, kotlinx-coroutines-test |

All versions live in `gradle/libs.versions.toml`.

## Folder structure

```
HmyhAndroidStarter/
├── build.gradle.kts            Root build file (declares plugins)
├── settings.gradle.kts         Repositories and module list (:app)
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml      All library and plugin versions
│   └── wrapper/                Gradle wrapper
└── app/
    ├── build.gradle.kts        App module: SDK levels, BuildConfig, dependencies
    ├── schemas/                Room schema JSON (generated, commit it)
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── assets/posts.json          Offline sample posts
        │   ├── res/                       Strings, theme, launcher icon
        │   └── java/com/hmyh/androidstarter/
        │       ├── App.kt                 @HiltAndroidApp application
        │       ├── MainActivity.kt        Single activity, hosts Compose
        │       ├── core/
        │       │   ├── common/            AppResult, AppError
        │       │   └── database/          AppDatabase, PostEntity, PostDao
        │       ├── di/                    App-wide Hilt modules
        │       │   ├── DispatcherModule.kt / DispatcherQualifiers.kt
        │       │   ├── NetworkModule.kt   Json, OkHttpClient, Retrofit
        │       │   └── DatabaseModule.kt  AppDatabase, DAOs
        │       ├── feature/post/
        │       │   ├── domain/            Pure Kotlin business layer
        │       │   │   ├── model/Post.kt
        │       │   │   ├── repository/PostRepository.kt   (interface)
        │       │   │   └── usecase/ObservePostsUseCase.kt, RefreshPostsUseCase.kt
        │       │   ├── data/              Implementation details
        │       │   │   ├── remote/        PostApi (Retrofit), PostDto
        │       │   │   ├── local/         PostAssetDataSource (bundled JSON)
        │       │   │   ├── mapper/        DTO -> Entity -> Domain
        │       │   │   └── repository/    PostRepositoryImpl
        │       │   ├── di/PostModule.kt   Binds repository, provides PostApi
        │       │   └── presentation/      PostsRoute, PostsScreen, PostsViewModel, PostsUiState
        │       └── ui/theme/Theme.kt
        └── test/                          PostsViewModelTest + fake repository
```

## Layers and rules

| Layer | Contains | May depend on | Must not depend on |
| --- | --- | --- | --- |
| `domain` | Models, repository interfaces, use cases | Kotlin, coroutines, `core/common` | Android, Room, Retrofit, Compose, Hilt modules |
| `data` | Retrofit API, DTOs, Room access, mappers, repository implementation | `domain`, `core` | Compose, ViewModel |
| `presentation` | Composables, ViewModel, UI state | `domain` | Room, Retrofit, DAO, DTO, entity |
| `di` | Hilt modules | Everything it wires | UI logic |
| `core` | Shared database, result types | — | Any `feature` package |

Every screen action follows one path:

```
Composable -> ViewModel -> Use case -> Repository interface -> Repository implementation -> Room / Retrofit
```

A ViewModel never receives a repository or DAO directly. It only uses use cases.

## How the post list works

1. `MainActivity` shows `PostsRoute()`. Hilt creates `PostsViewModel` through `hiltViewModel()`.
2. The ViewModel collects `ObservePostsUseCase`, which returns the Room `Flow` mapped to `Post` models. Any database change updates the screen automatically.
3. The ViewModel calls `RefreshPostsUseCase` on start and when the user taps Retry.
4. `PostRepositoryImpl.refresh()` runs on the IO dispatcher:
   - Success: downloads posts from Retrofit and upserts them into Room.
   - Network error (`IOException`) or server error (`HttpException`): if the database is empty, loads `assets/posts.json` into Room, then returns a failure.
   - Coroutine cancellation is rethrown, never swallowed.
5. The ViewModel turns the failure into `errorMessage`. The screen shows:
   - Empty list while refreshing: a progress indicator
   - Empty list with an error: the message and a Retry button
   - Otherwise: the list, with the error message above it when present

## Build and run

Open the folder in Android Studio and run the `app` configuration, or from a terminal:

```
gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

`local.properties` points to the Android SDK and is not committed. Android Studio creates it automatically.

On a network that intercepts HTTPS (a "PKIX path building failed" error), the command-line wrapper cannot download Gradle. Build from Android Studio instead, or import the company root certificate into the JDK used by Gradle. The same network can block the app's API call; the app then shows the bundled offline posts.

## Add a new feature

Example: a `user` feature.

1. Database (in `core/database/`)
   - Add `UserEntity` and `UserDao`.
   - Add `UserEntity::class` to `@Database(entities = [...])` and increase `version`.
   - Expose `abstract fun userDao(): UserDao` and provide it in `DatabaseModule`.
2. Domain (`feature/user/domain/`)
   - `model/User.kt`, `repository/UserRepository.kt` (interface), and use cases.
3. Data (`feature/user/data/`)
   - `remote/UserApi.kt` and `UserDto`, `mapper/`, `repository/UserRepositoryImpl.kt`.
4. DI (`feature/user/di/UserModule.kt`)
   - `@Binds` the repository implementation and `@Provides` `UserApi` from the shared `Retrofit`.
5. Presentation (`feature/user/presentation/`)
   - `UserUiState`, `@HiltViewModel UserViewModel` that takes use cases, `UserRoute` and `UserScreen`.
6. Test the ViewModel with a fake repository, like `PostsViewModelTest`.

Keep one `AppDatabase`, one `Retrofit`, and one `OkHttpClient` for the whole app. For more than one screen, add Navigation Compose and move `PostsRoute()` out of `MainActivity` into a nav graph.

The database currently uses destructive migration, which deletes local data when the version changes. Replace it with real `Migration` objects before the app stores data users care about.

## Reuse this project for a new app

Copy the folder, then rename the identity in these places:

| What | Where |
| --- | --- |
| Project name | `settings.gradle.kts` → `rootProject.name` |
| Package and application id | `app/build.gradle.kts` → `namespace` and `applicationId` |
| Source package folders | `app/src/main/java/com/hmyh/androidstarter/` and `app/src/test/...` |
| `package` and `import` lines | Every `.kt` file |
| App name | `res/values/strings.xml` → `app_name` |
| Theme name | `res/values/themes.xml` and `AndroidManifest.xml` → `Theme.HmyhAndroidStarter` |
| API base URL | `app/build.gradle.kts` → `BASE_URL` (must end with `/`) |
| Database file name | `di/DatabaseModule.kt` → `"starter.db"` |

Steps:

1. Copy the folder without `build/`, `app/build/`, `.gradle/`, `.idea/`, and `local.properties`.
2. Open it in Android Studio.
3. Rename the package: in the Project view, switch to **Project** mode, right-click `com.hmyh.androidstarter`, choose **Refactor → Rename**, and rename each segment. This updates `package` and `import` lines.
4. Update the remaining entries in the table above.
5. Delete `app/schemas/` so Room generates a fresh schema for the new app.
6. Replace or delete `feature/post`, `assets/posts.json`, and `PostEntity`/`PostDao`. Keep `core`, `di`, and `ui/theme`.
7. Sync Gradle and run the app.

Check `gradle/libs.versions.toml` for newer stable versions when starting a project long after this one.
