---
name: hmyh-android-starter
description: >-
  Works inside the HmyhAndroidStarter Android project (Kotlin, Jetpack Compose,
  Room, Dagger Hilt, Retrofit, MVVM clean architecture). Use when adding a
  feature, screen, API call, Room table, or ViewModel to this project, when
  changing its build or dependencies, or when copying it as the template for a
  new Android app.
---

# HmyhAndroidStarter

Single-module Android app (`:app`). Layers are packages under `com.hmyh.androidstarter`. The `post` feature is the reference implementation: copy its shape for every new feature.

Read `Doc.md` at the project root for the full folder tree and the post refresh flow.

## Layer rules

| Package | May use | Must not use |
| --- | --- | --- |
| `feature/<name>/domain` | Kotlin, coroutines, `core/common` | Android, Room, Retrofit, OkHttp, Hilt modules, Compose |
| `feature/<name>/data` | domain, `core`, Room, Retrofit, Android `Context` | Compose, ViewModel |
| `feature/<name>/presentation` | domain, Compose, Hilt ViewModel, Lifecycle | DAO, entity, DTO, Retrofit API |
| `feature/<name>/di`, `di/` | Hilt and the types they provide | UI logic |
| `core/` | Room, shared result types | Any `feature` package |

Call chain for every screen action: composable → ViewModel → use case → repository interface → repository implementation → Room / Retrofit. A ViewModel takes use cases only.

## Conventions

- Room is the source of truth. Screens observe a DAO `Flow` mapped to domain models. Network calls only write into Room.
- Repositories return `AppResult<T>` (`core/common/AppResult.kt`). Map errors inside the repository: rethrow `CancellationException`, `IOException` → `AppError.Network`, `HttpException` → `AppError.Http`, else `AppError.Unknown`.
- Repository writes run on `@IoDispatcher`. Do not wrap Room `Flow` queries in a dispatcher.
- DTOs are `@Serializable` and live in `data/remote`. Entities live in `core/database`. Mapping functions live in `data/mapper`.
- UI state is an immutable `data class <Name>UiState` held in `MutableStateFlow` and exposed as `StateFlow`.
- Split each screen into `<Name>Route` (takes `hiltViewModel()`, collects with `collectAsStateWithLifecycle()`) and `<Name>Screen` (takes state and lambdas only), so `@Preview` needs no Hilt.
- User-facing error text is built in presentation (`toUserMessage()`), not in data.
- One `AppDatabase`, one `Retrofit`, one `OkHttpClient` for the whole app.
- Add libraries through `gradle/libs.versions.toml`, never with inline version strings.

## Adding a feature

Copy this checklist and work through it in order:

```
- [ ] core/database: <Name>Entity, <Name>Dao, add entity to @Database, bump version, provide DAO in di/DatabaseModule
- [ ] domain: model, repository interface, use cases (@Inject constructor)
- [ ] data: <Name>Api + <Name>Dto in remote/, mapper/, repository/<Name>RepositoryImpl (@Singleton)
- [ ] di: feature/<name>/di module — @Binds repository, @Provides API from shared Retrofit
- [ ] presentation: UiState, @HiltViewModel ViewModel, Route, Screen, one @Preview
- [ ] test: ViewModel test with a fake repository and MainDispatcherRule
- [ ] build and run unit tests
```

Use `feature/post` as the template for each file. For a second screen, add Navigation Compose through the version catalog and move routes out of `MainActivity` into a nav graph.

The database uses `fallbackToDestructiveMigration(dropAllTables = true)`. Bumping the version wipes local data. Write real `Migration` objects once the app stores data users care about.

## Build facts

- AGP 9 uses built-in Kotlin. Do not add `org.jetbrains.kotlin.android` or `kotlin-kapt`; applying them fails the build. Annotation processing uses KSP (`ksp(...)`) for Room and Hilt.
- The Kotlin version is pinned through the `kotlin-gradle-plugin` classpath in the root `build.gradle.kts`. Keep it equal to `kotlin` in the version catalog, which the Compose and serialization plugins use.
- `hiltViewModel()` comes from `androidx.hilt.lifecycle.viewmodel.compose` (artifact `hilt-lifecycle-viewmodel-compose`), not the older navigation-compose package.
- Room schemas are exported to `app/schemas/` through the `room { schemaDirectory(...) }` block. Commit them.
- `BASE_URL` is a `buildConfigField` in `app/build.gradle.kts` and must end with `/`.

Verify with:

```
gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

On the office network, HTTPS is intercepted and the command-line wrapper fails with "PKIX path building failed" while downloading Gradle. If that happens, report it and ask the user to build from Android Studio. Do not disable certificate validation.

The same network blocks the app's API calls on device. The app then loads `assets/posts.json` into an empty database and shows "Couldn't reach the server. Showing offline posts." That is expected behavior, not a bug.

## Reusing as a template for a new app

When the user wants a new project based on this one:

1. Copy the project folder, excluding `build/`, `app/build/`, `.gradle/`, `.idea/`, and `local.properties`.
2. Rename the package directories under `app/src/main/java` and `app/src/test/java`, and update every `package` and `import` line.
3. Update `rootProject.name` in `settings.gradle.kts`, `namespace` and `applicationId` in `app/build.gradle.kts`, `app_name` in `strings.xml`, the theme name in `themes.xml` and `AndroidManifest.xml`, `BASE_URL`, and the database name in `di/DatabaseModule.kt`.
4. Delete `app/schemas/`.
5. Replace `feature/post`, `PostEntity`, `PostDao`, and `assets/posts.json` with the new domain, or keep them as the wired example if the user asks.
6. Check `gradle/libs.versions.toml` against current stable releases before building.
