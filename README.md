# MindNote

An Android notes + AI‑chat app. Capture short notes locally, sync them to a backend, and chat with an assistant that has your notes as context.

## How to run

### Requirements

- Android Studio (Iguana or newer)
- JDK 17
- Android SDK 34
- An Android device or emulator on API 26+

### Build + install

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug   # requires a connected device/emulator
```

Or click ▶ in Android Studio.

A pre‑built debug APK is included in the submission: `mindnote.apk`. Install directly:

```bash
adb install -r mindnote.apk
```

The backend is already hosted at `https://api-production-6707b.up.railway.app/` — no local server setup required.


### Unit tests

```bash
./gradlew :app:testDebugUnitTest
```

Covers ViewModels + DTO/entity mapping with hand‑rolled fakes.

---

## Architecture

The app is a single Gradle module organized by layer + feature. The UI reads from Room through Paging 3 for the notes list, and from a network‑only `PagingSource` for chat history. A `RemoteMediator` keeps Room in sync with the server page by page.

```
┌────────────────────────────────────────────────────────────┐
│  UI   (Compose screens) — features/{home, notes, ...}      │
│    ViewModels extend MviViewModel<Intent, State, Effect>   │
└────────────────────────────────────────────────────────────┘
        │ collects Flow<PagingData>/Flow<…> from repositories
        ▼
┌────────────────────────────────────────────────────────────┐
│  Domain — domain/repository, domain/model                  │
│    NotesRepository.notesPager(filter, tag, query)          │
│                  .observeRecent / .observeDistinctTags     │
└────────────────────────────────────────────────────────────┘
        ▲                        ▲
        │ impl                   │ impl
┌───────┴──────────────┐  ┌──────┴─────────────────────────┐
│  data/repository     │  │ data/remote                    │
│  • RoomNotesRepo     │  │ • NotesApi (offset/limit)      │
│  • NotesRemoteMedi…  │  │ • ChatApi (cursor: before/lim) │
│  • RoomFavoritesRepo │  │ • ChatPagingSource             │
└───────┬──────────────┘  └────────────────────────────────┘
        │
        ▼
┌────────────────────────────────────────────────────────────┐
│  Room DB  (data/db) — notes, topics, favorites, users      │
│  • NoteDao.pagingFiltered: PagingSource<Int, …>            │
└────────────────────────────────────────────────────────────┘
```

### MVI in practice

Every feature follows the same shape:

- **State** — immutable data class, collected via `vm.state.collectAsStateWithLifecycle()`.
- **Intent** — sealed interface covering user actions. `vm.send(intent)` routes to `handle(intent)`.
- **Effect** — sealed interface for one‑shot UI side effects (navigation, snackbar). Collected once via `LaunchedEffect(Unit) { vm.effects.collect { … } }`.

### Dynamic background

`MindNoteTheme` holds a `mutableStateOf(color)` that a `LaunchedEffect` loop swaps every 1000 ms. The target color is piped through `animateColorAsState(tween(800))` so the transition is a smooth drift, not a hard flicker. Palette is 8 visibly distinct pastels that keep contrast against near‑black body text.

### Chat streaming

`ChatApi.stream(conversationId, text)` returns `Flow<StreamEvent>` backed by the Ktor `HttpClient.sse { }` DSL. The server streams token deltas; the client re‑emits as `Token` / `Done` / `Error`. `ChatViewModel` optimistically appends a user message + an assistant placeholder, then mutates the placeholder's text on every token until `Done`.

### Error handling

Every API call in a VM goes through `safeApiCall { }` → `Result<T>`. `userMessage()` maps `ApiError.NETWORK` / `SERVER` / response body to a user‑facing string. Each feature's `Effect.ShowError(message)` is collected in the screen and shown via `MindNoteTheme.snackbar.showSnackbar(...)`.

---

## Layered folders (`app/src/main/java/com/mindnote/`)

| Package | Purpose |
|---|---|
| `MindNoteApp.kt` / `MainActivity.kt` | Application (Koin start) + single activity host. |
| `core/navigation` | `Destinations` + `MindNoteNavHost` (Navigation Compose). |
| `core/mvi` | `MviViewModel<Intent, State, Effect>` base class. |
| `core/ext` | `Result<T>` + `safeApiCall { }` + error mapping. |
| `core/storage` | `UserPrefs` — DataStore wrapper (username, onboarded flag, device id). |
| `core/di` | `appModule` — Koin wiring for HttpClient, Room, DAOs, repos, VMs. |
| `design/` | Compose design system + `MindNoteTheme` with dynamic background. |
| `domain/model` | Plain Kotlin data classes (`Note`, `ChatMessage`, `NoteFilter`, …). |
| `domain/repository` | Repository interfaces (`NotesRepository.notesPager`, …). |
| `data/db` | Room entities, DAOs (`NoteDao.pagingFiltered` → `PagingSource`). |
| `data/remote` | Ktor DTOs, `NotesApi`, `ChatApi`, `ChatPagingSource`. |
| `data/repository` | `RoomNotesRepository`, `NotesRemoteMediator`, `RoomFavoritesRepository`, `LocalUserRepository`. |
| `features/*` | One subpackage per screen: `…Screen.kt`, `…ViewModel.kt`, `…Contract.kt`. |

---

## Tech stack

| Concern | Library |
|---|---|
| UI | Jetpack Compose (BOM) + Material 3 + Material3 Window‑Size Class |
| Paging | AndroidX Paging 3.3.2 (`paging-runtime`, `paging-compose`, `room-paging`) |
| Navigation | androidx.navigation.compose |
| Dependency injection | Koin 4 (`koin-android`, `koin-androidx-compose`) |
| Local DB | Room 2.6 (KSP) |
| Preferences | AndroidX DataStore Preferences |
| HTTP + SSE | Ktor Client 3 (`ktor-client-core` + `okhttp` + `content-negotiation` + `logging`) |
| JSON | kotlinx.serialization 1.7 |
| Coroutines | kotlinx.coroutines 1.9 |
| Testing | JUnit 4 + `kotlinx-coroutines-test` |
| Build | Android Gradle Plugin 8.5, Kotlin 2.0, KSP 2.0 |

---
